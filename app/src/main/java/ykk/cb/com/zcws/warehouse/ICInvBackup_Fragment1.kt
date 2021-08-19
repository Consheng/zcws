package ykk.cb.com.zcws.warehouse

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import butterknife.OnClick
import kotlinx.android.synthetic.main.ware_icinvbackup2_fragment1.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.basics.Mtl_MoreDialogActivity
import ykk.cb.com.zcws.basics.Stock_GroupDialogActivity
import ykk.cb.com.zcws.bean.*
import ykk.cb.com.zcws.bean.k3Bean.ICInvBackup
import ykk.cb.com.zcws.bean.k3Bean.ICItem
import ykk.cb.com.zcws.bean.k3Bean.ICStockCheckProcess
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.BigdecimalUtil
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.LogUtil
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity
import ykk.cb.com.zcws.warehouse.adapter.ICInvBackup_Fragment1_Adapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：WMS 盘点（无盘点方案）
 * 作者：ykk
 */
class ICInvBackup_Fragment1 : BaseFragment() {

    companion object {
        private val SEL_STOCK = 60
        private val SEL_MTL = 61

        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUBMIT = 202
        private val UNSUBMIT = 502
        private val SAVE = 203
        private val UNSAVE = 503
        private val FIND_AVGQTY = 204
        private val UNFIND_AVGQTY = 504
        private val SETFOCUS = 1
        private val RESULT_NUM = 2
        private val RESULT_BATCH = 3
        private val SAOMA = 4
        private val WRITE_CODE = 5
    }

    private val context = this
    private var mAdapter: ICInvBackup_Fragment1_Adapter? = null
    val checkDatas = ArrayList<ICInvBackup>()
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: ICInvBackup_MainActivity? = null
    private var stock:Stock? = null
    private var stockPos: StockPosition? = null

    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var icStockCheckProcess: ICStockCheckProcess? = null
    private var curPos:Int = 0 // 当前行
    private var smqFlag = '1' // 使用扫码枪扫码（1：仓库位置扫码，2：容器扫码，3：物料扫码）
    var mapMtlQty = HashMap<Int, Double>()
    var accountType = "ZH" // 综合账号

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: ICInvBackup_Fragment1) : Handler() {
        private val mActivity: WeakReference<ICInvBackup_Fragment1>

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
                    SUCC1 -> { // 扫码成功 进入
                        when(m.smqFlag) {
                            '1' -> { // 仓库位置扫描
                                m.resetStockGroup()
                                m.getStockGroup(msgObj)
                            }
                            '2' -> { // 物料扫描
                                val icItem = JsonUtil.strToObject(msgObj, ICItem::class.java)
                                var listIcitem = ArrayList<ICItem>()
                                listIcitem.add(icItem)
                                m.tv_mtlName.text = icItem!!.fnumber

                                m.getMtlAfter(listIcitem, 0)
                            }
                        }
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.mAdapter!!.notifyDataSetChanged()
                        when(m.smqFlag) {
                            '1' -> { // 仓库位置扫描
//                                m.tv_positionName.text = ""
                            }
                            '2' -> { // 物料扫描
//                                m.tv_mtlName.text = ""
                            }
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 历史查询 进入
                        m.checkDatas.clear()
//                        val icInvBackup = JsonUtil.strToList(msgObj, ICInvBackup::class.java)
//                        m.checkDatas.addAll(icInvBackup)
//                        m.mAdapter!!.notifyDataSetChanged()
                    }
                    UNSUCC2 -> { // 历史查询  失败
                        m.mAdapter!!.notifyDataSetChanged()
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SAVE -> { // 保存成功 进入
                        m.reset()
                        m.toasts("保存成功✔")
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_AVGQTY -> { // 查询库存    成功
                        /*val listAvbQty = JsonUtil.strToList(msgObj, InventoryNow::class.java)
                        m.mapMtlQty.clear()
                        listAvbQty.forEach {
                            m.mapMtlQty.put(it.icItemId, it.avbQty)
                        }*/
                    }
                    UNFIND_AVGQTY -> { // 查询库存  失败
                        m.mapMtlQty.clear()
                    }
                    SUBMIT -> { // 提交成功 返回
                        m.reset()
                        m.toasts("提交成功✔")
                    }
                    UNSUBMIT // 提交失败 返回
                    -> {
                        errMsg = JsonUtil.strToString(msgObj!!)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "服务器忙，请稍候再试！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1' -> m.setFocusable(m.et_positionCode)
                            '2' -> m.setFocusable(m.et_code)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        if(!m.checkSaoMa()) {
                            m.isTextChange = false
                            return
                        }
                        // 执行查询方法
                        m.run_okhttpDatas()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.ware_icinvbackup2_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as ICInvBackup_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = ICInvBackup_Fragment1_Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : ICInvBackup_Fragment1_Adapter.MyCallBack {
            override fun onClick_num(entity: ICInvBackup, position: Int) {
                curPos = position
                showInputDialog("数量", entity.realQty.toString(), "0.0", RESULT_NUM)
            }
            override fun onClick_batch(entity: ICInvBackup, position: Int) {
                curPos = position
                showInputDialog("批次号", Comm.isNULLS(entity.fbatchNo), "none", RESULT_BATCH)
            }
            override fun onDelete(entity: ICInvBackup, position: Int) {
                checkDatas.removeAt(position)
                mAdapter!!.notifyDataSetChanged()
            }
        })

    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        getUserInfo()
        hideSoftInputMode(mContext, et_positionCode)
        hideSoftInputMode(mContext, et_code)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.tv_accountType, R.id.btn_positionSel, R.id.btn_positionScan, R.id.btn_mtlSel, R.id.btn_scan,
            R.id.tv_positionName, R.id.tv_mtlName,
            R.id.btn_save, R.id.btn_clone, R.id.btn_submit)
    fun onViewClicked(view: View) {
        var bundle: Bundle? = null
        when (view.id) {
            R.id.tv_accountType -> { // 选择盘点账号
                pop_accountType(view)
                popWindow!!.showAsDropDown(view)
            }
            R.id.btn_positionSel -> { // 选择仓库位置
                smqFlag = '1'
                bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockPos", stockPos)
                showForResult(Stock_GroupDialogActivity::class.java, SEL_STOCK, bundle)
            }
            R.id.btn_mtlSel -> { // 选择物料
                smqFlag = '2'
                if(!checkSaoMa()) return
                bundle = Bundle()
                bundle.putString("accountType", "ZH")
                showForResult(Mtl_MoreDialogActivity::class.java, SEL_MTL, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（仓库位置）
//                if(!checkProject()) return
                smqFlag = '1'
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '2'
                if(!checkSaoMa()) return
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.tv_positionName -> { // 位置点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_mtlName -> { // 物料点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.btn_save -> { // 保存
                val list = checkSave();
                if(list == null) return
                if(checkDatas.size == 0) {
                    Comm.showWarnDialog(mContext,"请选择物料或者扫码条码！")
                    return
                }
                run_save()
            }
            R.id.btn_submit -> { // 提交到k3
//                if(!checkProject()) return
                run_submitTok3();
            }
            R.id.btn_clone -> { // 重置
                if (checkDatas.size > 0) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset() }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    reset()
                }
            }
        }
    }

    fun checkSaoMa() : Boolean{
        when(smqFlag) {
            '2' -> { // 物料扫描
                if(stock == null) {
                    Comm.showWarnDialog(mContext,"请扫描或选择位置！")
                    return false
                }
            }
        }
        return true
    }

    /**
     * 检查方案是否有值
     */
    private fun checkSave() : List<ICInvBackup>? {
        val list = ArrayList<ICInvBackup>()
        checkDatas.forEachIndexed { index, m ->
            if(m.realQty == 0.0) {
                Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请输入盘点数！")
                return null
            }
            if(m.icItem.batchManager.equals("Y") && Comm.isNULLS(m.fbatchNo).length == 0) {
                Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请长按数字框输入批次！")
                return null
            }
        }
        return list
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_positionCode -> setFocusable(et_positionCode)
                R.id.et_code -> setFocusable(et_code)
            }
        }
        et_positionCode!!.setOnClickListener(click)
        et_code!!.setOnClickListener(click)

        // 位置---数据变化
        et_positionCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '1'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 位置---长按输入条码
        et_positionCode!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 仓库---焦点改变
        et_positionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusPosition != null) {
                    lin_focusPosition!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

        // 物料---数据变化
        et_code!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '2'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code!!.setOnLongClickListener {
            smqFlag = '2'
            showInputDialog("输入条码号", "", "none", WRITE_CODE)
            true
        }
        // 物料---焦点改变
        et_code.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusMtl.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusMtl != null) {
                    lin_focusMtl.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

    }

    /**
     * 创建PopupWindow 【单据类型选择 】
     */
    private var popWindow: PopupWindow? = null
    private fun pop_accountType(v: View) {
        if (null != popWindow) {//不为空就隐藏
            popWindow!!.dismiss()
            return
        }
        // 获取自定义布局文件popupwindow_left.xml的视图
        val popV = layoutInflater.inflate(R.layout.ware_icinvbackup_popwindow, null)
        // 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
        popWindow = PopupWindow(popV, v.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        // 设置动画效果
        // popWindow.setAnimationStyle(R.style.AnimationFade);
        popWindow!!.setBackgroundDrawable(BitmapDrawable())
        popWindow!!.isOutsideTouchable = true
        popWindow!!.isFocusable = true

        // 点击其他地方消失
        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv1 -> {
                    tv_accountType.text = "综合账号"
                    accountType = "ZH"
                }
                R.id.tv2 -> {
                    tv_accountType.text = "电商账号"
                    accountType = "DS"
                }
                R.id.tv3 -> {
                    tv_accountType.text = "线束账号"
                    accountType = "XS"
                }
            }
            if(checkDatas.size > 0) {
                checkDatas.forEach {
                    it.accountType = accountType
                }
            }
            popWindow!!.dismiss()
        }
        popV.findViewById<View>(R.id.tv1).setOnClickListener(click)
        popV.findViewById<View>(R.id.tv2).setOnClickListener(click)
        popV.findViewById<View>(R.id.tv3).setOnClickListener(click)
    }

    /**
     * 查询历史记录
     */
    fun findFun() {
        if(parent!!.isChange) {
            Comm.showWarnDialog(mContext,"请先保存当前数据！")
            return;
        }
        run_findListByParamWms()
    }

    fun resetStockGroup() {
        stock = null
        stockPos = null
    }

    /**
     * 得到仓库组
     */
    fun getStockGroup(msgObj : String?) {
        if(msgObj != null) {
            resetStockGroup()

            var caseId:Int = 0
            if(msgObj.indexOf("Stock_CaseId=1") > -1) {
                caseId = 1
            } else if(msgObj.indexOf("StockPosition_CaseId=2") > -1) {
                caseId = 2
            }

            when(caseId) {
                1 -> {
                    stock = JsonUtil.strToObject(msgObj, Stock::class.java)
                    tv_positionName.text = stock!!.fname
                }
                2 -> {
                    stockPos = JsonUtil.strToObject(msgObj, StockPosition::class.java)
                    tv_positionName.text = stockPos!!.fname
                    if(stockPos!!.stock != null) stock = stockPos!!.stock
                }
            }
        }

        if(stock != null ) {
            tv_positionName.text = stock!!.fname
        }
        if(stockPos != null ) {
            tv_positionName.text = stockPos!!.fname
        }

        // 人为替换仓库信息
        if(checkDatas.size > 0) {
            checkDatas[curPos].stock = null
            checkDatas[curPos].stockPos = null

            if(stock != null) {
                checkDatas[curPos].stock = stock
            }
            if(stockPos != null) {
                checkDatas[curPos].stockPos = stockPos
            }
            mAdapter!!.notifyDataSetChanged()
        }

        // 自动跳到物料焦点
        smqFlag = '2'
        mHandler.sendEmptyMessage(SETFOCUS)

        if(checkDatas.size > 0) {
            setStockInfo()
        }
        // 查询库存数
//        run_findAvgQtyList()
    }

    private fun setStockInfo() {
        checkDatas.forEach {
            it.stockId = if(stock != null) stock!!.fitemId else 0
            it.stockPosId = if(stockPos != null) stockPos!!.fspId else 0
            it.stock = stock
            it.stockPos = stockPos
        }
        mAdapter!!.notifyDataSetChanged()
    }

    private fun reset() {
        tv_accountType.text = "综合账号"
        accountType = "ZH"
        stock = null
        stockPos = null
        smqFlag = '1'
        et_positionCode.setText("")
        tv_positionName.text = ""
        et_code!!.setText("")
        tv_mtlName.text = ""
        checkDatas.clear()
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)

        mAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_STOCK -> {// 仓库	返回
                    resetStockGroup()
                    stock = data!!.getSerializableExtra("stock") as Stock
                    if (data!!.getSerializableExtra("stockPos") != null) {
                        stockPos = data!!.getSerializableExtra("stockPos") as StockPosition
                    }
                    getStockGroup(null)
                }
                SEL_MTL -> {//查询物料	返回
                    val list = data!!.getSerializableExtra("obj") as List<ICItem>
                    getMtlAfter(list, 1)
                }
                WRITE_CODE -> {// 输入条码返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        when (smqFlag) {
                            '1' -> setTexts(et_positionCode, value.toUpperCase())
                            '2' -> setTexts(et_code, value.toUpperCase())
                        }
                    }
                }
                RESULT_NUM -> { // 数量
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas[curPos].realQty = num
                        curPos = -1
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                RESULT_BATCH -> { // 批次号
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        checkDatas[curPos].fbatchNo = value
                        curPos = -1
                        mAdapter!!.notifyDataSetChanged()
                    }
                }

            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 得到扫码或选择数据
     */
    private fun getMtlAfter(list: List<ICItem>, flag: Int) {
        // 循环判断业务
        list.forEach {
            // 填充数据
            val size = checkDatas.size
            var addRow = true
            var curPosition = 0
            for (i in 0 until size) {
                val sr = checkDatas.get(i)
                // 有相同的，就不新增了
                if (sr.fitemId == it.fitemid) {
                    curPosition = i
                    addRow = false
                    break
                }
            }
            if (addRow) {
                var icInvBackup = ICInvBackup()
                icInvBackup.accountType = accountType
                icInvBackup.fitemId = it.fitemid
                icInvBackup.stockId = stock!!.fitemId
                icInvBackup.stockPosId = if(stockPos != null) stockPos!!.fspId else 0

                icInvBackup.icItem = it
                icInvBackup.stock = stock
                icInvBackup.stockPos = stockPos

                if(it.batchManager.equals("Y")) { // 启用了批次号，就给个默认值
                    icInvBackup.fbatchNo = Comm.getSysDate(3)
                }
                icInvBackup.realQty = 1.0
                icInvBackup.toK3 = 0
                icInvBackup.createUserId = user!!.id
                checkDatas.add(icInvBackup)

            } else {
                // 已有相同物料行，就叠加数量
                val fqty = checkDatas[curPosition].realQty
                val addVal = BigdecimalUtil.add(fqty, 1.0);
                checkDatas[curPosition].realQty = addVal
            }
        }

        mAdapter!!.notifyDataSetChanged()
        if(flag == 1) {
            recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(checkDatas.size - 1) })
        }

//        run_findAvgQtyList()
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_okhttpDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode:String? = null
        var strCaseId = ""
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_positionCode)
            }
            '2' -> {
                mUrl = getURL("material/findBarcode")
                barcode = getValues(et_code)
                strCaseId = "11,21"
            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("strCaseId",strCaseId)
                .add("accountType", "ZH")
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_okhttpDatas --> onResponse", result)
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
     * 保存
     */
    private fun run_save() {
        showLoadDialog("保存中...", false)

        val mUrl = getURL("icInvBackup/save")
        val mJson = JsonUtil.objectToString(checkDatas)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSAVE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSAVE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SAVE, result)
                LogUtil.e("run_save --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 提交
     */
    private fun run_submitTok3() {
        showLoadDialog("提交中...", false)

        getUserInfo()
        val mUrl = getURL("icInvBackup/submitTok3")
        val mJson = JsonUtil.objectToString(checkDatas)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .add("userId", user!!.id.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUBMIT)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUBMIT, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUBMIT, result)
                LogUtil.e("run_submitTok3 --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 历史查询
     */
    private fun run_findListByParamWms() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        val mUrl = getURL("icInvBackup/findListByParamWms")
        val formBody = FormBody.Builder()
                .add("finterId", if (icStockCheckProcess != null) icStockCheckProcess!!.fid.toString() else "") // 方案id
                .add("toK3", "1")
                .add("userId", user!!.id.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
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
                LogUtil.e("run_findListByParamWms --> onResponse", result)
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
     * 可用库存查询
     */
    private fun run_findAvgQtyList() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("inventoryNow/findAvbQty")
        /*val strStockGroup = stock!!.fitemId.toString() +","+ (if(stockArea != null) stockArea!!.id else 0) +","+
                (if(storageRack != null) storageRack!!.id else 0) +","+ (if(stockPos != null) stockPos!!.id else 0)
        var strMtlId = StringBuffer()
        checkDatas.forEach {
            strMtlId.append((if(strMtlId.length > 0) "," else "" )+it.fitemid)
        }*/

        val formBody = FormBody.Builder()
                /*.add("strMtlId", strMtlId.toString())
                .add("strStockGroup", strStockGroup)*/
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_AVGQTY)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findListByParamWms --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_AVGQTY, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_AVGQTY, result)
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
        mBinder!!.unbind()
        super.onDestroyView()
    }
}