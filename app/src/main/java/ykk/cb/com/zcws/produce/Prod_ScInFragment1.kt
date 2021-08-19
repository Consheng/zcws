package ykk.cb.com.zcws.produce

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
import kotlinx.android.synthetic.main.prod_sc_in_fragment1.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.basics.Stock_GroupDialogActivity
import ykk.cb.com.zcws.bean.*
import ykk.cb.com.zcws.bean.prod.ProdOrder
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.produce.adapter.Prod_ScInFragment1Adapter
import ykk.cb.com.zcws.util.BigdecimalUtil
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.LogUtil
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 生产任务单入库
 */
class Prod_ScInFragment1 : BaseFragment() {

    companion object {
        private val SEL_STOCKPOS = 60
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
    private var mAdapter: Prod_ScInFragment1Adapter? = null
    private val checkDatas = ArrayList<ScanningRecord>()
    private var curViewFlag = '1' // 1：仓库，2：库位， 3：车间， 4：物料 ，箱码
    private var curPos: Int = 0 // 当前行
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: Prod_ScInMainActivity? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var strK3Number: String? = null // 保存k3返回的单号
    private val df = DecimalFormat("#.####")
    private var timesTamp: String? = null // 时间戳
    private var isClickButton: Boolean = false // 是否点击了按钮

    // 消息处理
    private val mHandler = Prod_ScInFragment1.MyHandler(this)

    private class MyHandler(activity: Prod_ScInFragment1) : Handler() {
        private val mActivity: WeakReference<Prod_ScInFragment1>

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

                        //                        m.setEnables(m.et_code, R.drawable.back_style_gray3, false);
                        //                        m.btn_scan.setVisibility(View.GONE);
                        //                        m.btn_save.setVisibility(View.GONE);
                        //                        m.btn_pass.setVisibility(View.VISIBLE);
                        //                        Comm.showWarnDialog(m.mContext,"保存成功，请点击“审核按钮”！");
                        m.reset()
                        Comm.showWarnDialog(m.mContext, "保存成功✔")
                    }
                    UNSUCC1 -> {
                        errMsg = JsonUtil.strToString(msgObj)
                        if (Comm.isNULLS(errMsg).length == 0) errMsg = "服务器繁忙，请稍候再试！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    PASS -> { // 审核成功 返回
                        m.reset()
                        Comm.showWarnDialog(m.mContext, "审核成功✔")
                    }
                    UNPASS // 审核失败 返回
                    -> {
                        errMsg = JsonUtil.strToString(msg.obj as String)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "审核失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 扫码成功后进入
                        var bt: BarCodeTable? = null
                        when (m.curViewFlag) {
                            '1' // 生产条码
                            -> {
                                bt = JsonUtil.strToObject(msg.obj as String, BarCodeTable::class.java)
                                val prodOrder = JsonUtil.stringToObject(bt!!.relationObj, ProdOrder::class.java)

                                // 填充数据
                                val size = m.checkDatas.size
                                var addRow = true
                                for (i in 0 until size) {
                                    val sr = m.checkDatas[i]
                                    // 有相同的，就不新增了
                                    if (sr.sourceId == prodOrder.prodId) {
                                        addRow = false
                                        break
                                    }
                                }
                                m.parent!!.isChange = true
                                if (addRow) {
                                    m.getScanAfterData_1(bt, prodOrder)
                                } else {
                                    m.getMtlAfter(bt, prodOrder)
                                }
                            }
                        }
                    }
                    UNSUCC2 -> {
                        errMsg = JsonUtil.strToString(msg.obj as String)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC3 -> { // 判断是否存在返回
                        m.run_save()
                    }
                    UNSUCC3 -> { // 判断是否存在返回
                        m.run_save()
                    }
                    SETFOCUS -> {
                        // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        m.setFocusable(m.et_code)
                    }
                    SAOMA -> { // 扫码之后
                        m.run_smGetDatas()
                    }
                    DELAYED_CLICK -> { // 延时进入点击后的操作
                        val btnView = msg.obj as View
                        m.btnClickAfter(btnView)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_sc_in_fragment1, container, false)
    }

    override fun initView() {
        mContext = activity
        parent = mContext as Prod_ScInMainActivity?

        recyclerView!!.addItemDecoration(DividerItemDecoration(mContext!!, DividerItemDecoration.VERTICAL))
        recyclerView!!.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Prod_ScInFragment1Adapter(mContext!!, checkDatas)
        recyclerView!!.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView!!.isFocusable = false
        /*mAdapter!!.setCallBack(object : Prod_ScInFragment1Adapter.MyCallBack {
            override fun onClick_num(v: View, entity: ScanningRecord, position: Int) {
                Log.e("num", "行：$position")
                curPos = position
                val useableQty = checkDatas[curPos].useableQty
                val showInfo = "<font color='#666666'>可用数：</font>$useableQty"
                showInputDialog("入库数", showInfo, useableQty.toString(), "0.0", RESULT_NUM)
            }

            override fun onClick_selStock(v: View, entity: ScanningRecord, position: Int) {
                curPos = position
                showForResult(Stock_GroupDialogActivity::class.java, SEL_STOCKPOS, null)
            }
        })*/
        // 单击选数量
        mAdapter!!.setOnItemClickListener { adapter, holder, view, position ->
            curPos = position
            val useableQty = checkDatas[curPos].useableQty
            val showInfo = "<font color='#666666'>可用数：</font>$useableQty"
            showInputDialog("入库数", showInfo, useableQty.toString(), "0.0", RESULT_NUM)
        }
        // 长按选择仓库
        mAdapter!!.setOnItemLongClickListener { adapter, holder, view, position ->
            curPos = position
            showForResult(Stock_GroupDialogActivity::class.java, SEL_STOCKPOS, null)
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

        hideSoftInputMode(mContext, et_code)
        getUserInfo()
        timesTamp = user!!.id.toString() + "-" + Comm.randomUUID()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        LogUtil.e("setUserVisibleHint", "冒泡麻婆。。。。。")
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    override fun onResume() {
        super.onResume()
        isClickButton = true
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    @OnClick(R.id.btn_scan, R.id.btn_save, R.id.btn_pass, R.id.btn_clone)
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
            R.id.btn_scan // 调用摄像头扫描（物料）
            -> {
                curViewFlag = '1'
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.btn_save // 保存
            -> {
                //                hideKeyboard(mContext.getCurrentFocus());
                if (!saveBefore()) {
                    return
                }
                //                run_findInStockSum();
                run_save()
            }
            R.id.btn_pass // 审核
            -> {
                if (strK3Number == null) {
                    Comm.showWarnDialog(mContext, "请先保存数据！")
                    return
                }
                run_passSC()
            }
            R.id.btn_clone // 重置
            ->
                //                hideKeyboard(mContext.getCurrentFocus());
                if (checkDatas != null && checkDatas.size > 0) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset() }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()
                    return
                } else {
                    reset()
                }
        }
    }

    /**
     * 选择保存之前的判断
     */
    private fun saveBefore(): Boolean {
        if (checkDatas == null || checkDatas.size == 0) {
            Comm.showWarnDialog(mContext, "请先扫描条码！")
            return false
        }

        // 检查数据
        var i = 0
        val size = checkDatas.size
        while (i < size) {
            val sr = checkDatas[i]
            if (isNULLS(sr.stockName).length == 0) {
                Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，请选择（仓库）！")
                return false
            }
            if (sr.stock.fisStockMgr == 1 && sr.stockPos == null) {
                Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，仓库启用了库位，请重新选择仓库！")
                return false
            }
            if (sr.realQty > sr.useableQty) {
                Comm.showWarnDialog(mContext, "第" + (i + 1) + "行（入库数）不能大于（可用数）！")
                return false
            }
            i++
        }
        return true
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_code -> setFocusable(et_code)
            }
        }
        et_code!!.setOnClickListener(click)

        // 生产条码
        et_code!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                curViewFlag = '1'
                if (!isTextChange) {
                    isTextChange = true
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })

        // 长按输入条码
        et_code!!.setOnLongClickListener {
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }

        et_code!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                lin_focus1!!.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focus1 != null) {
                    lin_focus1!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
    }

    private fun reset() {
        isClickButton = true
        timesTamp = user!!.id.toString() + "-" + Comm.randomUUID()
        setEnables(et_code, R.color.transparent, true)
        btn_scan!!.visibility = View.VISIBLE
        strK3Number = null
        et_code!!.setText("") // 生产条码号
        btn_save!!.visibility = View.VISIBLE
        btn_pass!!.visibility = View.GONE
        checkDatas.clear()
        curViewFlag = '1'
        tv_needNum!!.text = "0"
        tv_okNum!!.text = "0"

        mAdapter!!.notifyDataSetChanged()
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) return
            when (requestCode) {
                SEL_STOCKPOS -> {//行事件选择库位	返回
                    val stock = data!!.getSerializableExtra("stock") as Stock
                    var stockPos: StockPosition? = null
                    if (data!!.getSerializableExtra("stockPos") != null) {
                        stockPos = data!!.getSerializableExtra("stockPos") as StockPosition
                    }
                    stockAllFill(stock, stockPos)
                }
                RESULT_NUM -> {// 数量
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        if (num == 0.0) {
                            toasts("入库数量必须大于0！")
                            val useableQty = checkDatas[curPos].useableQty
                            val showInfo = "<font color='#666666'>可用数：</font>$useableQty"
                            showInputDialog("入库数", showInfo, useableQty.toString(), "0.0", RESULT_NUM)
                            return
                        }
                        val useableQty = checkDatas[curPos].useableQty
                        if (num > useableQty) {
                            toasts("入库数不能大于可用数！")
                            val useableQty2 = checkDatas[curPos].useableQty
                            val showInfo = "<font color='#666666'>可用数：</font>$useableQty2"
                            showInputDialog("入库数", showInfo, useableQty2.toString(), "0.0", RESULT_NUM)
                            return
                        }
                        checkDatas[curPos].realQty = num
                        checkDatas[curPos].isUniqueness = 'N'
                        mAdapter!!.notifyDataSetChanged()
                        countNum()
                    }
                }
                WRITE_CODE -> {// 输入条码返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        et_code!!.setText(value.toUpperCase())
                    }
                }
                BaseFragment.CAMERA_SCAN -> { // 扫一扫成功  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val code = bundle.getString(BaseFragment.DECODED_CONTENT_KEY, "")
                        setTexts(et_code, code)
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 300)
    }

    /**
     * 仓库数据全部填充
     */
    private fun stockAllFill(stock: Stock, stockPos: StockPosition?) {
        val sr = checkDatas[curPos]
        sr.stockId = stock.fitemId
        sr.stockNumber = stock.fnumber
        sr.stockName = stock.fname
        sr.stock = stock
        sr.stockPos = null
        sr.stockPositionNumber = ""
        sr.stockPositionName = ""
        if (stockPos != null) {
            sr.stockPos = stockPos
            sr.stockPositionNumber = stockPos.fnumber
            sr.stockPositionName = stockPos.fname
        }
        // 当行仓库没有值，就填充
        for (m in checkDatas) {
            if (isNULLS(m.stockName).length == 0) {
                m.stockId = stock.fitemId
                m.stockNumber = stock.fnumber
                m.stockName = stock.fname
                m.stock = stock
                m.stockPos = null
                m.stockPositionNumber = ""
                m.stockPositionName = ""
                if (stockPos != null) {
                    m.stockPos = stockPos
                    m.stockPositionNumber = stockPos.fnumber
                    m.stockPositionName = stockPos.fname
                }
            }
        }
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * 得到生产条码号扫码的数据
     */
    private fun getScanAfterData_1(bt: BarCodeTable?, prodOrder: ProdOrder) {
        val icItem = prodOrder.icItem
        val sr = ScanningRecord()

        sr.type = 10 // 1：电商销售出库，10：生产产品入库，11：发货通知单销售出库，12：电商销售退货，13：电商外购入库
        sr.sourceId = prodOrder.prodId
        sr.sourceNumber = prodOrder.prodNo
        sr.sourceEntryId = prodOrder.prodId
        //        sr.setExpressNo(barcode);
        sr.icItemId = icItem.fitemid
        sr.icItemNumber = icItem.fnumber
        sr.icItemName = icItem.fname
        val cust = prodOrder.cust
        if (cust != null) {
            sr.custNumber = cust.getfNumber()
            sr.custName = cust.getfName()
        }
        val department = prodOrder.department
        if (department != null) {
            sr.deptNumber = department.departmentNumber
            sr.deptName = department.departmentName
        }
        // 用物料默认的仓库
        val stock = icItem.stock
        if (stock != null) {
            sr.stock = stock
            sr.stockNumber = stock.fnumber
            sr.stockName = stock.fname
        }
        // 默认的仓位
        val stockPos = icItem.stockPos
        if (stockPos != null && stockPos.fspId > 0) {
            sr.stockPos = stockPos
            sr.stockPositionNumber = stockPos.fnumber
            sr.stockPositionName = stockPos.fname
        }

        // 用选择的仓库
        //        if(stock1 != null) {
        //            sr.setStock(stock1);
        //            sr.setStockNumber(stock1.getFnumber());
        //            sr.setStockName(stock1.getFname());
        //
        //        } else if(prodOrder.getGoodsType() == 990169) { // 990168代表非定制，990169代表定制
        //            Stock stock = new Stock();
        //            stock.setFitemId(38263);
        //            stock.setFnumber("CC.01.05");
        //            stock.setFname("定制产品仓");
        //
        //            sr.setStock(stock);
        //            sr.setStockNumber(stock.getFnumber());
        //            sr.setStockName(stock.getFname());
        //
        //        } else { // 990168代表非定制
        //            Stock stock = new Stock();
        //            stock.setFitemId(254);
        //            stock.setFnumber("CC.01.01");
        //            stock.setFname("忠诚卫士成品仓");
        //
        //            sr.setStock(stock);
        //            sr.setStockNumber(stock.getFnumber());
        //            sr.setStockName(stock.getFname());
        //        }

        sr.deliveryWay = ""
        sr.sourceQty = prodOrder.fqty
        sr.useableQty = prodOrder.useableQty
        sr.price = 0.0
        //        sr.setRealQty(1);
        sr.createUserId = user!!.id!!
        sr.empId = user!!.empId
        sr.createUserName = user!!.username
        sr.dataTypeFlag = "APP"
        sr.tempTimesTamp = timesTamp
        sr.sourceObj = JsonUtil.objectToString(prodOrder)

        var isBool = false // 是否使用弹出框来确认数量

        // 启用序列号，批次号；    990156：启用批次号，990156：启用序列号
        if (icItem.snManager == 990156 || icItem.batchManager == 990156) {
            sr.strBarcodes = bt!!.barcode
            sr.isUniqueness = 'Y'
            if (icItem.batchManager == 990156 && icItem.snManager == 990155) {
                sr.realQty = sr.realQty + bt.barcodeQty
                isBool = true
            } else {
                sr.realQty = sr.realQty + 1
            }
        } else { // 未启用序列号， 批次号
            sr.realQty = sr.useableQty
            sr.isUniqueness = 'N'
            // 不存在条码，就加入
            sr.strBarcodes = bt!!.barcode
        }

        checkDatas.add(sr)
        mAdapter!!.notifyDataSetChanged()
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        countNum()

        if (isBool) {
            // 使用弹出框确认数量
            curPos = checkDatas.size - 1
            val useableQty = checkDatas[curPos].useableQty
            val showInfo = "<font color='#666666'>可用数：</font>$useableQty"
            showInputDialog("入库数", showInfo, useableQty.toString(), "0.0", RESULT_NUM)
        }
    }

    /**
     * 得到扫码物料 数据
     */
    private fun getMtlAfter(bt: BarCodeTable?, prodOrder: ProdOrder) {
        val tmpICItem = prodOrder.icItem

        val size = checkDatas.size
        var isFlag = false // 是否存在该订单
        var isBool = false // 是否使用弹出框来确认数量
        var isOkNum = false // 相同的物料不同的条码是否扫完数
        var pos = -1
        for (i in 0 until size) {
            val sr = checkDatas[i]
            val srBarcode = isNULLS(sr.strBarcodes)
            // 如果扫码相同
            if (bt!!.relationBillId == sr.sourceId) {
                isFlag = true
                pos = i

                // 启用序列号，批次号；    990156：启用批次号，990156：启用序列号
                if (tmpICItem.snManager == 990156 || tmpICItem.batchManager == 990156) {
                    if (srBarcode.indexOf(bt.barcode) > -1) {
                        Comm.showWarnDialog(mContext, "条码已经使用！")
                        return
                    }
                    if (sr.realQty >= sr.useableQty) {
                        //                        Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，已拣完！");
                        //                        return;
                        isOkNum = true
                        continue
                    }
                    if (srBarcode.length == 0) {
                        sr.strBarcodes = bt.barcode
                    } else {
                        sr.strBarcodes = srBarcode + "," + bt.barcode
                    }
                    //                    sr.setIsUniqueness('Y');
                    if (tmpICItem.batchManager == 990156 && tmpICItem.snManager == 990155) {
                        sr.strBarcodes = bt.barcode
                        sr.realQty = sr.realQty + bt.barcodeQty
                        isBool = true
                    } else {
                        sr.realQty = sr.realQty + 1
                    }
                    isOkNum = false
                } else { // 未启用序列号， 批次号
                    if (sr.realQty >= sr.useableQty) {
                        continue
                    }
                    sr.realQty = sr.useableQty
                    //                    sr.setIsUniqueness('N');
                    // 不存在条码，就加入
                    if (srBarcode.indexOf(bt.barcode) == -1) {
                        if (srBarcode.length == 0) {
                            sr.strBarcodes = bt.barcode
                        } else {
                            sr.strBarcodes = srBarcode + "," + bt.barcode
                        }
                    }
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

        mAdapter!!.notifyDataSetChanged()
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        countNum()

        if (isBool) {
            // 使用弹出框确认数量
            curPos = pos
            val useableQty = checkDatas[curPos].useableQty
            val showInfo = "<font color='#666666'>可用数：</font>$useableQty"
            showInputDialog("入库数", showInfo, useableQty.toString(), "0.0", RESULT_NUM)
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
    private fun run_save() {
        showLoadDialog("保存中...", false)

        val mJson = JsonUtil.objectToString<List<ScanningRecord>>(checkDatas)
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
    private fun run_smGetDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        var mUrl: String? = null
        when (curViewFlag) {
            '1' // 生产条码查询订单
            -> {
                mUrl = getURL("prodOrder/findBarcode")
            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code).trim())
                .add("strCaseId", "21")
                .add("sourceType", "10") // 1：电商销售出库，10：生产产品入库，11：发货通知单销售出库
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl!!)
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
    private fun run_passSC() {
        showLoadDialog("正在审核...", false)
        val mUrl = getURL("stockBill/passSC")
        getUserInfo()
        val formBody = FormBody.Builder()
                .add("strFbillNo", strK3Number!!)
                .add("empId", if (user != null) user!!.empId.toString() else "0")
                //                .add("outInType", "2") // 出入库类型：（1、生产账号--采购订单入库，2、生产账号--生产任务单入库，3、生产账号--发货通知单出库）
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
