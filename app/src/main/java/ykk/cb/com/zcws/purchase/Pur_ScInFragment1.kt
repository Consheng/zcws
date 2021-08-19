package ykk.cb.com.zcws.purchase

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.pur_sc_in_fragment1.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.basics.StockPos_DialogActivity
import ykk.cb.com.zcws.basics.Stock_DialogActivity
import ykk.cb.com.zcws.basics.Supplier_DialogActivity
import ykk.cb.com.zcws.bean.*
import ykk.cb.com.zcws.bean.k3Bean.ICItem
import ykk.cb.com.zcws.bean.pur.POOrderEntry
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.purchase.adapter.Pur_ScInFragment1Adapter
import ykk.cb.com.zcws.util.BigdecimalUtil
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.LogUtil
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 生产入库
 */
class Pur_ScInFragment1 : BaseFragment() {

    companion object {
        private val SEL_STOCK = 10
        private val SEL_STOCKPOS = 11
        private val SEL_SUPP = 12
        private val SEL_WRITE = 13
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUCC3 = 202
        private val UNSUCC3 = 502
        private val PASS = 203
        private val UNPASS = 503
        private val SETFOCUS = 1
        private val RESULT_NUM = 2
        private val SAOMA = 3
        private val WRITE_CODE = 4
        private val DELAYED_CLICK = 5
    }
    private val context = this
    private var mAdapter: Pur_ScInFragment1Adapter? = null
    private val checkDatas = ArrayList<ScanningRecord>()
    private var stock: Stock? = null
    private var stockPos: StockPosition? = null
    private var supplier: Supplier? = null // 供应商
    private var curPos: Int = 0 // 当前行
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: Pur_ScInMainActivity? = null
    private var strK3Number: String? = null // 保存k3返回的单号
    private var mtlBarcode: String? = null // 对应的条码号
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var isAutoSubmitDate: Boolean = false // 是否自动提交数据
    private var isAllSM: Boolean = false // 是否全部扫完条码
    private val df = DecimalFormat("#.####")
    private var timesTamp: String? = null // 时间戳
    private var isClickButton: Boolean = false // 是否点击了按钮

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Pur_ScInFragment1) : Handler() {
        private val mActivity: WeakReference<Pur_ScInFragment1>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                var errMsg: String? = null
                var msgObj: String? = null
                if (msg.obj is String) {
                    msgObj = msg.obj as String
                }
                when (msg.what) {
                    SUCC1 -> {
                        m.strK3Number = JsonUtil.strToString(msgObj)

                        m.setEnables(m.et_mtlCode, R.drawable.back_style_gray3, false)
                        m.btn_scan!!.visibility = View.GONE
                        m.btn_save!!.visibility = View.GONE
                        m.btn_pass!!.visibility = View.VISIBLE

                        if (m.isAutoSubmitDate) {
                            m.run_passSC(true)
                        } else {
                            m.run_passSC(false)
                            //                            Comm.showWarnDialog(m.mContext, "保存成功，请点击“审核按钮”！");
                        }
                    }
                    UNSUCC1 -> {
                        errMsg = JsonUtil.strToString(msgObj)
                        if (Comm.isNULLS(errMsg).length == 0) errMsg = "服务器繁忙，请稍候再试！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    PASS // 审核成功 返回
                    -> {
                        m.reset(false)
                        // 如果没有全部扫完的，审核后继续查询
                        if (!m.isAllSM) {
                            m.run_findPurOrderInStock()
                        } else {
                            m.mAdapter!!.notifyDataSetChanged()
                        }

                        if (m.isAutoSubmitDate) {
                            m.toasts("自动提交数据成功✔")
                        } else {
                            Comm.showWarnDialog(m.mContext, "审核成功✔")
                        }
                    }
                    UNPASS // 审核失败 返回
                    -> {
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "审核失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 // 扫码成功后进入
                    -> {
                        val list = JsonUtil.strToList(msgObj, POOrderEntry::class.java)
                        m.parent!!.isChange = true
                        m.getScanAfterData_1(list!!)
                    }
                    UNSUCC2 -> {
                        m.mAdapter!!.notifyDataSetChanged()
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC3 // 扫码成功后进入
                    -> {
                        val bt = JsonUtil.strToObject(msg.obj as String, BarCodeTable::class.java)
                        m.getMtlAfter(bt!!)
                    }
                    UNSUCC3 -> {
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SETFOCUS // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                    -> {
                        m.setFocusable(m.et_getFocus)
                        m.setFocusable(m.et_mtlCode)
                    }
                    SAOMA // 扫码之后
                    -> {
                        if (m.checkDatas.size == 0) {
                            m.isTextChange = false
                            Comm.showWarnDialog(m.mContext, "请查询数据！")
                            return
                        }
                        val etName = m.getValues(m.et_mtlCode)
                        if (m.mtlBarcode != null && m.mtlBarcode!!.length > 0) {
                            if (m.mtlBarcode == etName) {
                                m.mtlBarcode = etName
                            } else
                                m.mtlBarcode = etName.replaceFirst(m.mtlBarcode.toString(), "")

                        } else
                            m.mtlBarcode = etName
                        m.setTexts(m.et_mtlCode, m.mtlBarcode)
                        // 执行查询方法
                        m.run_smGetDatas()
                    }
                    DELAYED_CLICK // 延时进入点击后的操作
                    -> {
                        val btnView = msg.obj as View
                        m.btnClickAfter(btnView)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.pur_sc_in_fragment1, container, false)
    }

    override fun initView() {
        mContext = activity
        parent = mContext as Pur_ScInMainActivity?

        recyclerView!!.addItemDecoration(DividerItemDecoration(mContext!!, DividerItemDecoration.VERTICAL))
        recyclerView!!.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Pur_ScInFragment1Adapter(mContext!!, checkDatas)
        recyclerView!!.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView!!.isFocusable = false
        mAdapter!!.setCallBack(object : Pur_ScInFragment1Adapter.MyCallBack {
            override fun onClick_num(v: View, entity: ScanningRecord, position: Int) {
                Log.e("num", "行：$position")
                curPos = position
                val showInfo = "<font color='#666666'>可用数：</font>" + checkDatas[curPos].useableQty
                showInputDialog("入库数", showInfo, "", "0.0", RESULT_NUM)
            }

            override fun onClick_selStock(v: View, entity: ScanningRecord, position: Int) {
                curPos = position
                showForResult(Stock_DialogActivity::class.java, SEL_STOCK, null)
            }

        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val sr = checkDatas[pos]
            sr.isCheck = if (sr.isCheck == 0) 1 else 0
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(30, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(30, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        hideSoftInputMode(mContext, et_mtlCode)
        getUserInfo()
        tv_dateSel!!.text = Comm.getSysDate(7)
        timesTamp = user!!.id.toString() + "-" + Comm.randomUUID()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            //            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
        }
    }

    override fun onResume() {
        super.onResume()
        isClickButton = true
    }

    @OnClick(R.id.btn_scan, R.id.tv_purNo, R.id.tv_suppSel, R.id.tv_dateSel, R.id.btn_save, R.id.btn_pass, R.id.btn_clone)
    fun onViewClicked(view: View) {
        if (isClickButton && view.id == R.id.btn_save) {
            isClickButton = false
            view.isEnabled = false
            view.isClickable = false
            showLoadDialog("稍等哈...", false)

            val msgView = mHandler.obtainMessage(DELAYED_CLICK, view)
            mHandler.sendMessageDelayed(msgView, 1000)
        } else {
            btnClickAfter(view)
        }
    }

    private fun btnClickAfter(view: View) {
        hideLoadDialog()
        isClickButton = true
        view.isEnabled = true
        view.isClickable = true

        val bundle: Bundle? = null
        when (view.id) {
            R.id.btn_scan // 调用摄像头扫描（快递单）
            -> showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            R.id.tv_purNo // 选择采购单号
            -> showInputDialog("采购单号", getValues(tv_purNo), "none", SEL_WRITE)
            R.id.tv_suppSel // 选择供应商
            -> showForResult(Supplier_DialogActivity::class.java, SEL_SUPP, null)
            R.id.tv_dateSel // 日期
            -> Comm.showDateDialog(mContext, view, 0)
            R.id.btn_save // 保存
            -> {
                //                hideKeyboard(mContext.getCurrentFocus());
                if (!saveBefore(false)) {
                    return
                }
                isAutoSubmitDate = false
                //                run_findInStockSum();
                run_save(false)
            }
            R.id.btn_pass // 审核
            -> {
                if (strK3Number == null) {
                    Comm.showWarnDialog(mContext, "请先保存数据！")
                    return
                }
                run_passSC(false)
            }
            R.id.btn_clone // 重置
            ->
                //                hideKeyboard(mContext.getCurrentFocus());
                if (checkDatas != null && checkDatas.size > 0) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset(true) }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()
                    return
                } else {
                    reset(true)
                }
        }
    }

    /**
     * 查询方法
     */
    fun findFun() {
        if (checkDatas != null && checkDatas.size > 0) {
            Comm.showWarnDialog(mContext, "请先保存当前行数据！")
            return
        }
        run_findPurOrderInStock()
    }

    /**
     * 选择保存之前的判断
     */
    private fun saveBefore(isAutoCheck: Boolean): Boolean {
        if (checkDatas == null || checkDatas.size == 0) {
            Comm.showWarnDialog(mContext, "请先查询数据！")
            return false
        }
        var count = 0
        // 检查数据
        val size = checkDatas.size
        for (i in 0 until size) {
            val sr = checkDatas[i]
            if (isNULLS(sr.stockName).length == 0) {
                Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，请选择（仓库）！")
                return false
            }
            if (sr.realQty > sr.useableQty) {
                Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，入库数不能大于可用数！")
                return false
            }

            if (isAutoCheck && sr.realQty == sr.useableQty) {
                count += 1
            }
        }
        // 自动检查数据，并且全部扫完了
        return if (isAutoCheck) {
            if (count == size)
                true
            else
                false

        } else {
            true
        }
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_mtlCode -> setFocusable(et_mtlCode)
            }
        }
        et_mtlCode!!.setOnClickListener(click)


        // 物料
        et_mtlCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })

        // 长按输入条码
        et_mtlCode!!.setOnLongClickListener {
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }

        et_mtlCode!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                lin_focus1!!.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focus1 != null) {
                    lin_focus1!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
    }

    private fun reset(isRefresh: Boolean) {
        timesTamp = user!!.id.toString() + "-" + Comm.randomUUID()
        setEnables(et_mtlCode, R.color.transparent, true)
        btn_scan!!.visibility = View.VISIBLE
        strK3Number = null
        btn_save!!.visibility = View.VISIBLE
        btn_pass!!.visibility = View.GONE
        checkDatas.clear()
        tv_needNum!!.text = "0"
        tv_okNum!!.text = "0"

        if (isRefresh) mAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SEL_STOCK //行事件选择仓库	返回
            -> if (resultCode == Activity.RESULT_OK) {
                stock = data!!.getSerializableExtra("obj") as Stock
                // 启用了库位管理
                if (stock!!.fisStockMgr == 1) {
                    val bundle = Bundle()
                    bundle.putInt("fspGroupId", stock!!.fspGroupId)
                    showForResult(StockPos_DialogActivity::class.java, SEL_STOCKPOS, bundle)
                } else {
                    stockAllFill(false)
                }
            }
            SEL_STOCKPOS //行事件选择库位	返回
            -> if (resultCode == Activity.RESULT_OK) {
                stockPos = data!!.getSerializableExtra("obj") as StockPosition
                LogUtil.e("onActivityResult --> SEL_STOCKP", stockPos!!.fname)
                stockAllFill(true)
            }
            RESULT_NUM // 数量
            -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                if (bundle != null) {
                    val value = bundle.getString("resultValue", "")
                    val num = parseDouble(value)
                    if (num == 0.0) {
                        toasts("入库数量必须大于0！")
                        val showInfo = "<font color='#666666'>可用数：</font>" + checkDatas[curPos].useableQty
                        showInputDialog("入库数", showInfo, "", "0.0", RESULT_NUM)
                        return
                    }
                    val useableQty = checkDatas[curPos].useableQty
                    if (num > useableQty) {
                        toasts("入库数不能大于可用数！")
                        val showInfo = "<font color='#666666'>可用数：</font>" + checkDatas[curPos].useableQty
                        showInputDialog("入库数", showInfo, "", "0.0", RESULT_NUM)
                        return
                    }
                    checkDatas[curPos].realQty = num
                    checkDatas[curPos].isCheck = 1
                    mAdapter!!.notifyDataSetChanged()
                    countNum()
                }
            }
            WRITE_CODE // 输入条码返回
            -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                if (bundle != null) {
                    val value = bundle.getString("resultValue", "")
                    et_mtlCode!!.setText(value.toUpperCase())
                }
            }
            SEL_SUPP //查询部门	返回
            -> if (resultCode == Activity.RESULT_OK) {
                supplier = data!!.getSerializableExtra("obj") as Supplier
                LogUtil.e("onActivityResult --> SEL_SUPP", supplier!!.fname)
                tv_suppSel!!.text = supplier!!.fname
            }
            BaseFragment.CAMERA_SCAN // 扫一扫成功  返回
            -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                if (bundle != null) {
                    val code = bundle.getString(BaseFragment.DECODED_CONTENT_KEY, "")
                    setTexts(et_mtlCode, code)
                }
            }
            SEL_WRITE // 输入采购单号返回
            -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                if (bundle != null) {
                    val value = bundle.getString("resultValue", "")
                    tv_purNo!!.text = value
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 300)
    }

    /**
     * 仓库数据全部填充
     */
    private fun stockAllFill(inStockPosData: Boolean) {
        val size = checkDatas.size
        var isBool = false
        for (i in 0 until size) {
            val sr = checkDatas[i]
            if (isNULLS(sr.stockNumber).length > 0) {
                isBool = true
                break
            }
        }
        //        if(isBool) {
        val sr2 = checkDatas[curPos]
        sr2.stockNumber = stock!!.fnumber
        sr2.stockName = stock!!.fname
        sr2.stock = stock
        if (inStockPosData) {
            sr2.stockPos = stockPos
            sr2.stockPositionNumber = stockPos!!.fnumber
            sr2.stockPositionName = stockPos!!.fname
        }
        //        } else { // 全部都为空的时候，选择任意全部填充
        //            for (int i = 0; i < size; i++) {
        //                ScanningRecord2 sr2 = checkDatas.get(i);
        //                sr2.setStockId(stock2.getfStockid());
        //                sr2.setStockFnumber(stock2.getfNumber());
        //                sr2.setStockName(stock2.getfName());
        //                sr2.setStock(stock2);
        //                if(inStockPosData) {
        //                    sr2.setStockPos(stockP2);
        //                    sr2.setStockPositionId(stockP2.getId());
        //                    sr2.setStockPName(stockP2.getFname());
        //                }
        //            }
        //        }
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * 得到生产条码号扫码的数据
     */
    private fun getScanAfterData_1(list: List<POOrderEntry>) {
        val size = list.size
        for (i in 0 until size) {
            val purEntry = list[i]
            val pur = purEntry.poOrder
            val icItem = purEntry.icItem
            val sr = ScanningRecord()

            sr.type = 15 // 1：电商销售出库，10：生产产品入库，11：发货通知单销售出库，12：电商销售退货，13：电商外购入库，14：生产产品入库(选单入库)，15：采购订单入库
            sr.sourceId = purEntry.finterid!!
            sr.sourceNumber = pur.fbillno
            sr.sourceEntryId = purEntry.fentryid!!
            //        sr.setExpressNo(barcode);
            sr.icItemId = icItem.fitemid
            sr.icItemNumber = icItem.fnumber
            sr.icItemName = icItem.fname
            val supplier = pur.supplier
            if (supplier != null) {
                sr.custNumber = supplier.fnumber
                sr.custName = supplier.fname
            }
            val department = pur.department
            if (department != null) {
                sr.deptNumber = department.departmentNumber
                sr.deptName = department.departmentName
            }
            val stock = icItem.stock
            if (stock != null) {
                sr.stock = stock
                sr.stockNumber = stock.fnumber
                sr.stockName = stock.fname
            }
            val stockPos = icItem.stockPos
            if (stockPos != null && stockPos.fspId > 0) {
                sr.stockPos = stockPos
                sr.stockPositionNumber = stockPos.fnumber
                sr.stockPositionName = stockPos.fname
            }
            sr.deliveryWay = ""
            sr.sourceQty = purEntry.fqty
            sr.useableQty = purEntry.useableQty
            sr.realQty = 0.0
            sr.price = purEntry.fprice
            sr.createUserId = user!!.id!!
            sr.empId = user!!.empId
            sr.createUserName = user!!.username
            sr.dataTypeFlag = "APP"
            sr.tempTimesTamp = timesTamp
            sr.sourceObj = JsonUtil.objectToString(purEntry)

            // 启用序列号，批次号；    990156：启用批次号，990156：启用序列号
            if (icItem.snManager == 990156 || icItem.batchManager == 990156) {
                sr.strBarcodes = ""
                sr.isUniqueness = 'Y'
                //                sr.setRealQty(sr.getUseableQty());
            } else { // 未启用序列号， 批次号
                //                sr.setRealQty(sr.getUseableQty());
                sr.isUniqueness = 'N'
                // 不存在条码，就加入
                sr.strBarcodes = ""
            }

            checkDatas.add(sr)
        }

        mAdapter!!.notifyDataSetChanged()
        countNum()
    }

    /**
     * 得到扫码物料 数据
     */
    private fun getMtlAfter(bt: BarCodeTable) {
        val tmpICItem = JsonUtil.stringToObject(bt.relationObj, ICItem::class.java)

        val size = checkDatas.size
        var isFlag = false // 是否存在该订单
        var isOkNum = false // 相同的物料不同的条码是否扫完数
        for (i in 0 until size) {
            val sr = checkDatas[i]
            val srBarcode = isNULLS(sr.strBarcodes)
            // 如果扫码相同
            if (bt.icItemNumber == sr.icItemNumber) {
                isFlag = true

                // 启用序列号，批次号；    990156：启用批次号，990156：启用序列号
                if (tmpICItem.snManager == 990156 || tmpICItem.batchManager == 990156) {
                    //                    if (srBarcode.indexOf(bt.getBarcode()) > -1) {
                    //                        Comm.showWarnDialog(mContext, "条码已经使用！");
                    //                        return;
                    //                    }
                    if (sr.realQty >= sr.useableQty) {
                        //                        Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，已拣完！");
                        //                        return;
                        isOkNum = true
                        continue
                    }
                    //                    if(srBarcode.length() == 0) {
                    //                        sr.setStrBarcodes(bt.getBarcode()+",");
                    //                    } else {
                    //                        sr.setStrBarcodes(srBarcode +","+ bt.getBarcode()+",");
                    //                    }
                    sr.strBarcodes = bt.barcode
                    sr.isUniqueness = 'Y'
                    //                    if(tmpICItem.getBatchManager() == 990156 && tmpICItem.getSnManager() == 0 ) {
                    //                        sr.setRealQty(sr.getRealQty() + bt.getBarcodeQty());
                    //                    } else {
                    //                        sr.setRealQty(sr.getRealQty() + 1);
                    //                    }
                    sr.realQty = sr.useableQty
                    isOkNum = false

                } else { // 未启用序列号， 批次号
                    if (sr.realQty >= sr.useableQty) {
                        continue
                    }
                    sr.realQty = sr.useableQty
                    sr.isUniqueness = 'N'
                    sr.strBarcodes = bt.barcode
                    // 不存在条码，就加入
                    //                    if (srBarcode.indexOf(bt.getBarcode()) == -1) {
                    //                        if (srBarcode.length() == 0) {
                    //                            sr.setStrBarcodes(bt.getBarcode() + ",");
                    //                        } else {
                    //                            sr.setStrBarcodes(srBarcode + "," + bt.getBarcode() + ",");
                    //                        }
                    //                        sr.setStrBarcodes(sr.getStrBarcodes().substring(0, sr.getStrBarcodes().length()-1));
                    //                    }
                }
                break
            }
        }
        if (!isFlag) {
            Comm.showWarnDialog(mContext, "该物料与订单不匹配！")
            return
        }
        if (isOkNum) {
            Comm.showWarnDialog(mContext, "该物料条码在订单中数量已扫完！")
            return
        }

        setFocusable(et_mtlCode)
        mAdapter!!.notifyDataSetChanged()
        countNum()
        // 自动检查数据是否可以保存
        if (saveBefore(true)) {
            isAutoSubmitDate = true
            run_save(true)
        }
    }

    /**
     * 统计数量
     */
    private fun countNum() {
        var needNum = 0.0
        var okNum = 0.0
        for (i in checkDatas.indices) {
            val sc = checkDatas[i]
            needNum = BigdecimalUtil.add(needNum, sc.useableQty)
            okNum = BigdecimalUtil.add(okNum, sc.realQty)
        }
        tv_needNum!!.text = df.format(needNum)
        tv_okNum!!.text = df.format(okNum)
    }

    /**
     * 保存方法
     */
    private fun run_save(isAutoSubmit: Boolean) {
        val listRecord = ArrayList<ScanningRecord>()
        var count = 0
        val size = checkDatas.size
        for (i in 0 until size) {
            val sr = checkDatas[i]

            if (sr.realQty > 0) {
                listRecord.add(sr)
            }
            // 判断没行是否扫完数量
            if (sr.realQty >= sr.useableQty) {
                count += 1
            }
        }
        if (listRecord.size == 0) {
            Comm.showWarnDialog(mContext, "请至少输入一行数量！")
            return
        }
        // 判断是否全部扫完
        if (size == count)
            isAllSM = true
        else
            isAllSM = false

        if (isAutoSubmit)
            showLoadDialog("自动保存中...", false)
        else
            showLoadDialog("保存中...", false)
        val mJson = JsonUtil.objectToString<List<ScanningRecord>>(listRecord)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .build()

        val mUrl = getURL("scanningRecord/addScanningRecord")
        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                //                .post(body)
                .build()
        okHttpClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_save --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_findPurOrderInStock() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("purOrder/findPurOrderInStock")
        val purNo = getValues(tv_purNo).trim { it <= ' ' }
        val date = getValues(tv_dateSel)
        val formBody = FormBody.Builder()
                .add("fbillNo", purNo) // 生产单号
                .add("suppNumber", if (supplier != null) supplier!!.fnumber else "")
                .add("prodFdateBeg", date) // 开始日期
                .add("prodFdateEnd", date) // 结束日期
                .add("purStatus", "1") // 0：未审核，1：审核，3：结案
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC2)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_smGetDatas --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC2, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC2, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smGetDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        val mUrl = getURL("barCodeTable/findBarcode_SC")
        val formBody = FormBody.Builder()
                .add("barcode", mtlBarcode!!)
                .add("strCaseId", "11")
                .add("sourceType", "15") // 1：电商销售出库，10：生产产品入库，11：发货通知单销售出库，12：电商销售退货，13：电商外购入库，14：生产产品入库(选单入库)，15：采购订单入库
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC3)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_smGetDatas --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC3, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC3, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 判断表中存在该物料
     */
    private fun run_findInStockSum() {
        showLoadDialog("加载中...", false)
        val strFbillno = StringBuilder()
        val strEntryId = StringBuilder()
        var i = 0
        val size = checkDatas.size
        while (i < size) {
            i++
            //            ScanningRecord2 sr2 = checkDatas.get(i);
            //            if((i+1) == size) {
            //                strFbillno.append(sr2.getPoFbillno());
            //                strEntryId.append(sr2.getEntryId());
            //            } else {
            //                strFbillno.append(sr2.getPoFbillno() + ",");
            //                strEntryId.append(sr2.getEntryId() + ",");
            //            }
        }
        val mUrl = getURL("scanningRecord/findInStockSum")
        val formBody = FormBody.Builder()
                .add("fbillType", "4") // fbillType  1：采购订单入库，2：收料任务单入库，3：生产订单入库，4：销售订单出库，5：发货通知单出库
                .add("strFbillno", strFbillno.toString())
                .add("strEntryId", strEntryId.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC3)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNSUCC3)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC3, result)
                Log.e("run_findInStockSum --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 生产账号审核
     */
    private fun run_passSC(isAutoSubmit: Boolean) {
        if (isAutoSubmit)
            showLoadDialog("自动审核中...", false)
        else
            showLoadDialog("正在审核...", false)

        val mUrl = getURL("stockBill/passSC")
        getUserInfo()
        val formBody = FormBody.Builder()
                .add("strFbillNo", strK3Number!!)
                .add("empId", if (user != null) user!!.empId.toString() else "0")
                //                .add("outInType", "1") // 出入库类型：（1、生产账号--采购订单入库，2、生产账号--生产任务单入库，3、生产账号--发货通知单出库）
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNPASS)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNPASS, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(PASS, result)
                Log.e("run_passSC --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

    override fun onDestroyView() {
        closeHandler(mHandler)
        mBinder.unbind()
        super.onDestroyView()
    }


}
