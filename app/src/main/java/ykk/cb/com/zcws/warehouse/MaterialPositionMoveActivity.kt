package ykk.cb.com.zcws.warehouse

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.ware_material_position_move.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.basics.Stock_GroupDialogActivity
import ykk.cb.com.zcws.bean.*
import ykk.cb.com.zcws.bean.k3Bean.Inventory_K3
import ykk.cb.com.zcws.comm.BaseActivity
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.LogUtil
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat

/**
 * 物料位置移动
 */
class MaterialPositionMoveActivity : BaseActivity() {

    companion object {
        private val SEL_POSITION = 61
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUCC3 = 202
        private val UNSUCC3 = 502
        private val SAVE = 203
        private val UNSAVE = 503

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
        private val RESULT_QTY = 4
    }
    private val context = this
    private var user: User? = null
    private val okHttpClient = OkHttpClient()
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var curPos = -1
    private var smFlag = '1' // 扫描类型 1：物料，2：位置
    private var materialPositionMove = MaterialPositionMove()
    private val df = DecimalFormat("#.####")
    private var refreshStock :Stock? = null // 刷新的仓库
    private var refreshStockPos :StockPosition? = null // 刷新的库位

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: MaterialPositionMoveActivity) : Handler() {
        private val mActivity: WeakReference<MaterialPositionMoveActivity>

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
                    SUCC1 -> { // 扫码物料    成功
                        val bt = JsonUtil.strToObject(msg.obj as String, BarCodeTable::class.java)
                        m.setMoveData(bt)
                    }
                    UNSUCC1 -> { // 扫码物料    失败！
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SUCC2 -> { // 扫码位置    成功
                        m.getStockGroup(msgObj,null,null)
                    }
                    UNSUCC2 -> { // 扫码位置    失败！
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SUCC3 -> { // 查询库存 进入
                        val list = JsonUtil.strToList(msgObj, Inventory_K3::class.java)
                        m.materialPositionMove.newInventoryQty = list[0].fqty
                        m.tv_newInventoryQty.text = Html.fromHtml("即时库存:&nbsp;<font color='#FF4400'>" + m.df.format(m.materialPositionMove.newInventoryQty) + "</font>")
                    }
                    UNSUCC3 -> { // 查询库存  失败
                        m.tv_newInventoryQty.text = "即时库存：0"
                    }
                    SAVE -> { // 保存 成功
                        val barocode = m.materialPositionMove.barcode
                        m.reset()
                        m.setTexts(m.et_code, barocode)
                    }
                    UNSAVE -> { // 保存  失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        if(errMsg!!.indexOf("刷新") > -1) {
                            val build = AlertDialog.Builder(m.context)
                            build.setIcon(R.drawable.caution)
                            build.setTitle("系统提示")
                            build.setMessage(errMsg)
                            build.setNegativeButton("刷新") { dialog, which ->
                                m.refreshStock = m.materialPositionMove.newStock
                                m.refreshStockPos = m.materialPositionMove.newStockPosition
                                m.setTexts(m.et_code, m.materialPositionMove.barcode)
                            }
//                            build.setNegativeButton("否", null)
                            build.setCancelable(false)
                            build.show()

                        } else {
                            Comm.showWarnDialog(m.context, errMsg)
                        }

                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smFlag) {
                            '1' -> m.setFocusable(m.et_code)
                            '2' -> m.setFocusable(m.et_positionCode)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        when(m.smFlag) {
                            '1' -> m.run_smDatas()
                            '2' -> m.run_findBarcodeGroup()
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置移动类数据
     */
    private fun setMoveData(bt :BarCodeTable) {
        materialPositionMove.mtlId = bt.icItemId
        materialPositionMove.barcodeTableId = bt.id
        materialPositionMove.barcode = bt.barcode
        materialPositionMove.oldStockId = bt.icItem.stockId
        materialPositionMove.oldStockPositionId = bt.icItem.stockPosId
        materialPositionMove.oldInventoryQty = bt.icItem.inventoryQty
        materialPositionMove.newStockId = 0
        materialPositionMove.newStockPositionId = 0
        materialPositionMove.newInventoryQty = 0.0
        materialPositionMove.fqty = materialPositionMove.oldInventoryQty
        materialPositionMove.createUserId = user!!.id
        materialPositionMove.createUserName = user!!.username
        materialPositionMove.material = bt.icItem
        materialPositionMove.oldStock = bt.icItem.stock
        materialPositionMove.oldStockPosition = bt.icItem.stockPos

        tv_mtlName.text = Html.fromHtml("物料名称:&nbsp;<font color='#6a5acd'>" + bt.icItemName + "</font>")
        tv_mtlNumber.text = Html.fromHtml("物料代码:&nbsp;<font color='#6a5acd'>" + bt.icItemNumber + "</font>")
        tv_fmodel.text = Html.fromHtml("规格型号:&nbsp;<font color='#6a5acd'>" + isNULLS(bt.icItem.fmodel) + "</font>")
        tv_fhelpCode.text = Html.fromHtml("助记码:&nbsp;<font color='#6a5acd'>" + isNULLS(bt.icItem.fhelpCode) + "</font>")
        tv_barcode.text = Html.fromHtml("条码:&nbsp;<font color='#000000'>" + bt.barcode + "</font>")
        tv_barcodeQty.text = Html.fromHtml("数量:&nbsp;<font color='#6a5acd'>" + df.format(bt.barcodeQty) + "</font>")
        tv_inventoryQty.text = Html.fromHtml("即时库存:&nbsp;<font color='#FF4400'>" + df.format(materialPositionMove.oldInventoryQty) + "</font>")
        tv_fqty.text = df.format(materialPositionMove.oldInventoryQty)

        if(materialPositionMove.oldStock != null) {
            tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#6a5acd'>" + bt.icItem.stock.fname + "</font>")

        } else {
            tv_stockName.text = ""
        }
        if(materialPositionMove.oldStockPosition != null) {
            tv_stockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#6a5acd'>" + bt.icItem.stockPos.fname + "</font>")
        } else {
            tv_stockPosName.text = ""
        }

        // 跳到位置焦点
        smFlag = '2'
        mHandler.sendEmptyMessage(SETFOCUS)

        if(refreshStock != null) {
            getStockGroup(null, refreshStock, refreshStockPos)
        }
        // 用完就清空
        refreshStock = null
        refreshStockPos = null
    }

    override fun setLayoutResID(): Int {
        return R.layout.ware_material_position_move
    }

    override fun initView() {

    }

    override fun initData() {
        getUserInfo()
        hideSoftInputMode(et_code)
        hideSoftInputMode(et_positionCode)
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_scan,  R.id.btn_positionScan, R.id.btn_positionSel, R.id.tv_fqty, R.id.btn_clone, R.id.btn_save)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smFlag = '1'
                ScanUtil.startScan(context, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smFlag = '2'
                ScanUtil.startScan(context, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_positionSel -> { // 选择仓库
                smFlag = '2'
                val bundle = Bundle()
                bundle.putSerializable("stock", materialPositionMove.newStock)
                bundle.putSerializable("stockPos", materialPositionMove.newStockPosition)
                showForResult(Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.tv_fqty -> { // 移动数量
                showInputDialog("输入移动数量", materialPositionMove.oldInventoryQty.toString(), "0.0", RESULT_QTY)
            }
            R.id.btn_clone -> {
                if (materialPositionMove.newStockId == 0) {
                    val build = AlertDialog.Builder(context)
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
            R.id.btn_save -> {  // 确认移动
                if(materialPositionMove.mtlId == 0) {
                    Comm.showWarnDialog(context,"请扫描物料条码")
                    return
                }
                if(materialPositionMove.newStockId == 0) {
                    Comm.showWarnDialog(context,"请扫描要移动的位置条码！")
                    return
                }
                if(materialPositionMove.fqty > materialPositionMove.oldInventoryQty) {
                    Comm.showWarnDialog(context,"（移动数量）不能大于（即时库存）！")
                    return
                }
                val oldPosition = materialPositionMove.oldStockId.toString() +"-"+ materialPositionMove.oldStockPositionId.toString()
                val newPosition = materialPositionMove.newStockId.toString() +"-"+ materialPositionMove.newStockPositionId.toString()
                if(oldPosition.equals(newPosition)) {
                    Comm.showWarnDialog(context,"条码的位置和移动的位置不能相同！")
                    return
                }
                run_add()
            }
        }
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_code -> setFocusable(et_code)
                R.id.et_positionCode -> setFocusable(et_positionCode)
            }
        }
        et_code.setOnClickListener(click)
        et_positionCode.setOnClickListener(click)

        // 物料---数据变化
        et_code.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smFlag = '1'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code.setOnLongClickListener {
            smFlag = '1'
            showInputDialog("输入条码号", getValues(et_code), "none", WRITE_CODE)
            true
        }
        // 物料---焦点改变
        et_code.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusMtl.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusMtl != null) {
                    lin_focusMtl!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }


        // 位置---数据变化
        et_positionCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smFlag = '2'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 位置---长按输入条码
        et_positionCode!!.setOnLongClickListener {
            smFlag = '2'
            showInputDialog("输入条码号", getValues(et_positionCode), "none", WRITE_CODE)
            true
        }
        // 位置---焦点改变
        et_positionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusPosition != null) {
                    lin_focusPosition!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
    }

    /**
     * 重置
     */
    private fun reset() {
        et_code.setText("")
        et_positionCode.setText("")

        tv_mtlName.text = "物料名称："
        tv_mtlNumber.text = "物料代码："
        tv_fmodel.text = "规格型号："
        tv_fhelpCode.text = "助记码："
        tv_barcode.text = "条码："
        tv_barcodeQty.text = "数量：0"
        tv_stockName.text = "仓库："
        tv_stockPosName.text = "库位："
        tv_inventoryQty.text = "即时库存："
        tv_newStockName.text = "仓库："
        tv_newStockPosName.text = "库位："
        tv_newInventoryQty.text = "即时库存："
        tv_fqty.text = ""
        materialPositionMove.mtlId = 0
        materialPositionMove.barcodeTableId = 0
        materialPositionMove.barcode = null
        materialPositionMove.oldStockId = 0
        materialPositionMove.oldStockPositionId = 0
        materialPositionMove.oldInventoryQty = 0.0
        materialPositionMove.newStockId = 0
        materialPositionMove.newStockPositionId = 0
        materialPositionMove.newInventoryQty = 0.0
        materialPositionMove.fqty = 0.0
        materialPositionMove.material = null
        materialPositionMove.newStock = null
        materialPositionMove.newStockPosition = null

        smFlag = '1'
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 得到仓库组
     */
    fun getStockGroup(msgObj : String?, stock :Stock?, stockPos :StockPosition?) {
        // 重置数据
        materialPositionMove.newStockId = 0
        materialPositionMove.newStockPositionId = 0
        materialPositionMove.newStock = null
        materialPositionMove.newStockPosition = null
        tv_newStockName.text = ""
        tv_newStockPosName.text = ""

        var stock :Stock? = stock
        var stockPosition :StockPosition? = stockPos

        if(msgObj != null) {
            var caseId:Int = 0
            if(msgObj.indexOf("Stock_CaseId=1") > -1) {
                caseId = 1
            } else if(msgObj.indexOf("StockPosition_CaseId=2") > -1) {
                caseId = 2
            }

            when(caseId) {
                1 -> {
                    stock = JsonUtil.strToObject(msgObj, Stock::class.java)
                }
                2 -> {
                    stockPosition = JsonUtil.strToObject(msgObj, StockPosition::class.java)
                    if(stockPosition.stock != null) stock = stockPosition.stock
                }
            }
        }

        if(stock != null ) {
            tv_newStockName.text = Html.fromHtml("仓库:&nbsp;<font color='#6a5acd'>" + stock.fname + "</font>")
            materialPositionMove.newStockId = stock.fitemId
            materialPositionMove.newStock = stock
        }
        if(stockPosition != null ) {
            tv_newStockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#6a5acd'>" + stockPosition.fname + "</font>")
            materialPositionMove.newStockPositionId = stockPosition.fspId
            materialPositionMove.newStockPosition = stockPosition
        }
        run_findInventoryQty()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
//            if (data == null) return
            when (requestCode) {
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val hmsScan = data!!.getParcelableExtra(ScanUtil.RESULT) as HmsScan
                    if (hmsScan != null) {
                        when (smFlag) {
                            '1' -> setTexts(et_code, hmsScan.originalValue)
                            '2' -> setTexts(et_positionCode, hmsScan.originalValue)
                        }
                    }
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        when (smFlag) {
                            '1' -> setTexts(et_code, value.toUpperCase())
                            '2' -> setTexts(et_positionCode, value.toUpperCase())
                        }
                    }
                }
                RESULT_QTY -> {// 数量  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val result = parseDouble(value)
                        if(result <= 0) {
                            Comm.showWarnDialog(context,"移动数量必须大于0！")
                            return
                        }
                        if(result > materialPositionMove.oldInventoryQty) {
                            Comm.showWarnDialog(context,"（移动数量）不能大于（即时库存）！")
                            return
                        }
                        materialPositionMove.fqty = result
                        tv_fqty.text = df.format(result)
                    }
                }
                SEL_POSITION -> {// 选择位置  返回
                    var stock: Stock? = null
                    var stockPosition: StockPosition? = null

                    stock = data!!.getSerializableExtra("stock") as Stock
                    if (data!!.getSerializableExtra("stockPos") != null) {
                        stockPosition = data!!.getSerializableExtra("stockPos") as StockPosition
                    }
                    getStockGroup(null, stock, stockPosition)
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 通过okhttp加载数据
     */
    private fun run_smDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        val mUrl = getURL("barCodeTable/findBarcodeApp")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
                .add("strCaseId", "11,21")
                .add("searchMtlInfo", "1") // 查询物料信息
                .add("searchInventoryInfo","1")  // 查询即时库存
                .build()
        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                LogUtil.e("run_smDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 扫描查询位置
     */
    private fun run_findBarcodeGroup() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockPosition/findBarcodeGroup")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_positionCode))
                .build()
        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC2)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC2, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC2, result)
                LogUtil.e("run_findBarcodeGroup --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询库存
     */
    private fun run_findInventoryQty() {
//        showLoadDialog("加载中...", false)
        val mUrl = getURL("icInventory/findInventoryQty")
        val formBody = FormBody.Builder()
                .add("stockId", materialPositionMove.newStockId.toString())
                .add("stockPosId",  materialPositionMove.newStockPositionId.toString())
                .add("fitemId", materialPositionMove.mtlId.toString())
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
                mHandler.sendEmptyMessage(UNSUCC3)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findInventoryQty --> onResponse", result)
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
     * 生码
     */
    private fun run_add() {
        isTextChange = false
        showLoadDialog("加载中...", false)

        val formBody = FormBody.Builder()
                .add("strJson", JsonUtil.objectToString(materialPositionMove))
                .add("deptId", user!!.deptId.toString())
                .add("empId", user!!.empId.toString())
                .add("empName", user!!.empName)
                .add("erpUserId", user!!.erpUserId.toString())
                .build()
        val mUrl = getURL("materialPositionMove/add")

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient.newCall(request)
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
                LogUtil.e("run_add --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // 按了删除键，回退键
        //        if(!isKeyboard && (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
        // 240 为PDA两侧面扫码键，241 为PDA中间扫码键
        return if (!(event.keyCode == 240 || event.keyCode == 241)) {
            false
        } else super.dispatchKeyEvent(event)
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }


    override fun onDestroy() {
        super.onDestroy()
        closeHandler(mHandler)
    }

}
