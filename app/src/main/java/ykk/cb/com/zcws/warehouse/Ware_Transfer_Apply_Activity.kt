package ykk.cb.com.zcws.warehouse

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import com.gprinter.command.EscCommand
import com.gprinter.command.LabelCommand
import kotlinx.android.synthetic.main.ware_transfer_apply.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.basics.Mtl_MoreStockDialogActivity
import ykk.cb.com.zcws.basics.Stock_DialogActivity
import ykk.cb.com.zcws.bean.TransferApply
import ykk.cb.com.zcws.bean.TransferApplyEntry
import ykk.cb.com.zcws.bean.User
import ykk.cb.com.zcws.bean.k3Bean.ICItem
import ykk.cb.com.zcws.comm.BaseActivity
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import ykk.cb.com.zcws.util.blueTooth.*
import ykk.cb.com.zcws.util.blueTooth.Constant.MESSAGE_UPDATE_PARAMETER
import ykk.cb.com.zcws.util.blueTooth.DeviceConnFactoryManager.CONN_STATE_FAILED
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity
import ykk.cb.com.zcws.warehouse.adapter.Ware_Transfer_Apply_Adapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

/**
 * 调拨申请
 */
class Ware_Transfer_Apply_Activity : BaseActivity() {

    companion object {
        private val SEL_STOCK = 61
        private val SEL_MTL = 62
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SAVE = 202
        private val UNSAVE = 502

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
        private val RESULT_NUM = 4

        private val CONN_STATE_DISCONN = 0x007 // 连接状态断开
        private val PRINTER_COMMAND_ERROR = 0x008 // 使用打印机指令错误
        private val CONN_PRINTER = 0x12
    }
    private val context = this
    private var mAdapter: Ware_Transfer_Apply_Adapter? = null
    private var okHttpClient: OkHttpClient? = null
    private var accountType: String? = null // 账号类型（DS：电商，SC：生产）
    private var user: User? = null
    private var timesTamp:String? = null // 时间戳
    private val checkDatas = ArrayList<TransferApplyEntry>()
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var curPos = -1
    private var smqFlag = '1' // 扫描类型1：位置扫描，2：物料扫描

    private var isConnected: Boolean = false // 蓝牙是否连接标识
    private val id = 0 // 设备id
    private var threadPool: ThreadPool? = null
    private var listPrintData:List<TransferApplyEntry>? = null

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Ware_Transfer_Apply_Activity) : Handler() {
        private val mActivity: WeakReference<Ware_Transfer_Apply_Activity>

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
                    SUCC1 -> { // 扫描成功
                        /*when(m.smqFlag) {
                            '1'-> { // 仓库位置
                                val stock = JsonUtil.strToObject(msg.obj as String, Stock::class.java)
                                m.getStock(stock)
                            }
                            '2'-> { // 物料
                                val icitem = JsonUtil.strToObject(msg.obj as String, ICItem::class.java)
                                val list = ArrayList<ICItem>()
                                list.add(icitem)
                                m.tv_icItemName.text = icitem.fname
                                m.getMtlAfter(list)
                            }
                        }*/
                        val icitem = JsonUtil.strToObject(msg.obj as String, ICItem::class.java)
                        val list = ArrayList<ICItem>()
                        list.add(icitem)
                        m.tv_icItemName.text = icitem.fname
                        m.getMtlAfter(list)
                    }
                    UNSUCC1 -> { // 数据加载失败！
                        /*when(m.smqFlag) {
                            '1' -> m.tv_positionName.text = ""
                            '2' ->  m.tv_icItemName.text = ""
                        }*/
                        m.tv_icItemName.text = ""
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有加载到数据！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SAVE -> { // 保存成功 进入
                        m.listPrintData = JsonUtil.strToList(msgObj, TransferApplyEntry::class.java)
                        m.timesTamp = m.user!!.getId().toString() + "-" + Comm.randomUUID()
                        m.toasts("保存成功")
                        m.checkDatas.clear()
                        m.mAdapter!!.notifyDataSetChanged()

                        // 开始打印
                        if (m.isConnected) {
                            m.setPrintBegin()
                        } else {
                            // 打开蓝牙配对页面
                            m.showForResult(BluetoothDeviceListDialog::class.java, Constant.BLUETOOTH_REQUEST_CODE,null)
                        }
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        /*when(m.smqFlag) {
                            '1'-> m.setFocusable(m.et_positionCode)
                            '2'-> m.setFocusable(m.et_code)
                        }*/
                        m.setFocusable(m.et_code)
                    }
                    SAOMA -> { // 扫码之后
                        /*if(m.smqFlag == '2' && m.stock == null) {
                            Comm.showWarnDialog(m.context,"请选择仓库！")
                            return
                        }*/
                        // 执行查询方法
                        m.run_smDatas()
                    }
                    CONN_STATE_DISCONN -> {
                        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[m.id] != null) {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[m.id].closePort(m.id)
                        }
                    }
                    PRINTER_COMMAND_ERROR -> {
                        Utils.toast(m.context, m.getString(R.string.str_choice_printer_command))
                    }
                    CONN_PRINTER -> {
                        Utils.toast(m.context, m.getString(R.string.str_cann_printer))
                    }
                    MESSAGE_UPDATE_PARAMETER -> {
                        val strIp = msg.data.getString("Ip")
                        val strPort = msg.data.getString("Port")
                        //初始化端口信息
                        DeviceConnFactoryManager.Build()
                                //设置端口连接方式
                                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                                //设置端口IP地址
                                .setIp(strIp)
                                //设置端口ID（主要用于连接多设备）
                                .setId(m.id)
                                //设置连接的热点端口号
                                .setPort(Integer.parseInt(strPort))
                                .build()
                        m.threadPool = ThreadPool.getInstantiation()
                        m.threadPool!!.addTask(Runnable {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[m.id].openPort()
                        })
                    }
                }
            }
        }

    }

    override fun setLayoutResID(): Int {
        return R.layout.ware_transfer_apply
    }

    override fun initView() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = Ware_Transfer_Apply_Adapter(context, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件删除
        mAdapter!!.setCallBack(object : Ware_Transfer_Apply_Adapter.MyCallBack {
            override fun onDelete(entity: TransferApplyEntry, position: Int) {
                checkDatas.removeAt(position)
                mAdapter!!.notifyDataSetChanged()
            }
        })
        // 行事件输入数量
        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            curPos = pos
            showInputDialog("调拨数", checkDatas[pos].fqty.toString(), "0.0", RESULT_NUM)
        }
    }

    override fun initData() {
        getUserInfo()
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
//        hideSoftInputMode(et_positionCode)
        hideSoftInputMode(et_code)
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }


    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_search, R.id.btn_positionSel, R.id.btn_mtlSel, R.id.btn_positionScan, R.id.btn_scan, R.id.tv_positionName, R.id.tv_icItemName, R.id.btn_save)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
            R.id.btn_search -> {
                show(Ware_Transfer_Apply_SearchActivity::class.java,null)
            }
            R.id.btn_positionSel -> { // 选择仓库
                smqFlag = '1'
                showForResult(Stock_DialogActivity::class.java, SEL_STOCK, null)
            }
            R.id.btn_mtlSel -> { // 选择物料
                /*if(stock == null) {
                    Comm.showWarnDialog(context,"请选择仓库！")
                    return
                }*/
                smqFlag = '2'
                val bundle = Bundle()
                bundle.putString("mtlStockIdGt0", "1")
                bundle.putString("accountType", "ZH")
//                showForResult(Mtl_MoreDialogActivity::class.java, SEL_MTL, bundle)
                showForResult(Mtl_MoreStockDialogActivity::class.java, SEL_MTL, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smqFlag = '1'
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.btn_scan -> { // 调用摄像头
                smqFlag = '2'
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.tv_positionName -> { // 位置点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_icItemName -> { // 物料点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.btn_save -> { // 提交申请
                val size = checkDatas.size
                if (size == 0) {
                    Comm.showWarnDialog(context, "请扫描或选择物料信息！")
                    return
                }
                run_save(JsonUtil.objectToString(checkDatas))
            }
        }
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_positionCode -> setFocusable(et_positionCode)
                R.id.et_code -> setFocusable(et_code)
            }
        }
        et_code.setOnClickListener(click)
//        et_positionCode.setOnClickListener(click)
        /*
        // 仓库---数据变化
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
        // 仓库---长按输入条码
        et_positionCode!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码号", getValues(et_positionCode), "none", WRITE_CODE)
            true
        }
        // 仓库---焦点改变
        et_positionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusPosition != null) {
                    lin_focusPosition.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
        */
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
            showInputDialog("输入条码号", getValues(et_code), "none", WRITE_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            // 当选择蓝牙的时候按了返回键
            if (data == null) return
            when (requestCode) {
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val code = bundle.getString(BaseFragment.DECODED_CONTENT_KEY, "")
                        /*when (smqFlag) {
                            '1' -> setTexts(et_positionCode, code)
                            '2' -> setTexts(et_code, code)
                        }*/
                        setTexts(et_code, code)
                    }
                }
                SEL_MTL -> { //查询物料	返回
                    val listICItem = data!!.getSerializableExtra("obj") as List<ICItem>
                    getMtlAfter(listICItem)
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        /*when (smqFlag) {
                            '1' -> setTexts(et_positionCode, value.toUpperCase())
                            '2' -> setTexts(et_code, value.toUpperCase())
                        }*/
                        setTexts(et_code, value.toUpperCase())
                    }
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas[curPos].fqty = num
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                /*蓝牙连接*/
                Constant.BLUETOOTH_REQUEST_CODE -> {
                    /*获取蓝牙mac地址*/
                    val macAddress = data!!.getStringExtra(BluetoothDeviceListDialog.EXTRA_DEVICE_ADDRESS)
                    //初始化话DeviceConnFactoryManager
                    DeviceConnFactoryManager.Build()
                            .setId(id)
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(macAddress)
                            .build()
                    //打开端口
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort()
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 得到扫码或选择数据
     */
    private fun getMtlAfter(listICItem : List<ICItem>) {
        val mapICItem = HashMap<Int, Boolean>()
        checkDatas.forEach {
            if(!mapICItem.containsKey(it.icitemId)) {
                mapICItem.put(it.icitemId, true)
            }
        }

        listICItem.forEach {
            if(!mapICItem.containsKey(it.fitemid)) {
                val entry = TransferApplyEntry()
                entry.icitemId = it.fitemid
                entry.fqty = 1.0
                if(it.stock != null) {
                    entry.stockId = it.stock.fitemId
                    entry.stock = it.stock
                }
                if(it.stockPos != null && it.stockPos.fspId > 0) {
                    entry.stockPosId = it.stockPos.fspId
                    entry.stockPos = it.stockPos
                }

                entry.icItem = it
                checkDatas.add(entry)
            }
        }

        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * 扫描查询物料
     */
    private fun run_smDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        var mUrl = ""
        var barcode = ""
        var strCaseId = ""
        /*when(smqFlag) {
            '1' -> {
                mUrl = getURL("stock/findBarcode")
                barcode = getValues(et_positionCode).trim()
                strCaseId = "31"
            }
            '2' -> {
                mUrl = getURL("material/findBarcode")
                barcode = getValues(et_code).trim()
                strCaseId = "11,21"
            }
        }*/
        mUrl = getURL("material/findBarcode")
        barcode = getValues(et_code).trim()
        strCaseId = "11,21"
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("strCaseId", strCaseId) // 物料，生产订单
                .add("checkMtlStock", "1") // 检查物料默认仓库
                .add("accountType", isNULL2(accountType, "ZH"))
                .build()
        val request = Request.Builder()
                .addHeader("cookie", session)
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
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }

                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_smDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 保存
     */
    private fun run_save(strJson :String) {
        showLoadDialog("保存中...", false)
        val mUrl = getURL("transferApply/save")
        val formBody = FormBody.Builder()
                .add("strJson", strJson)
                .add("createUserId", user!!.id.toString()) // 物料，生产订单
                .add("accountType", isNULL2(accountType, "ZH"))
                .add("timesTamp", timesTamp)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
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
                    mHandler.sendEmptyMessage(UNSAVE)
                    return
                }

                val msg = mHandler.obtainMessage(SAVE, result)
                Log.e("run_smDatas --> onResponse", result)
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // 按了删除键，回退键
        //        if(!isKeyboard && (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
        // 240 为PDA两侧面扫码键，241 为PDA中间扫码键
        return if (!(event.keyCode == 240 || event.keyCode == 241)) {
            false
        } else super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeHandler(mHandler)
            context.finish()
        }
        return false
    }

    private fun setPrintBegin() {
        val mapCount = HashMap<Int, Int>()
        listPrintData!!.forEach {
            if(mapCount.containsKey(it.parentId)) {
                var num = mapCount.get(it.parentId)
                mapCount.put(it.parentId, (num!!+1))
            } else {
                mapCount.put(it.parentId, 1)
            }
        }
        var listParentId = ArrayList<Int>()
        var pos = 0
        var count = 0
        listPrintData!!.forEach {
            if(!listParentId.contains(it.parentId)) {
                listParentId.add(it.parentId)
                // 打印头部
                setPrintFormat1(it.transferApply)
                count = mapCount[it.parentId]!!
                pos = 0
            }
            pos += 1
            // 打印列表
            setPrintFormatList(count, pos, it)
        }
    }

    /**
     * 打印头部1
     */
    private fun setPrintFormat1(m :TransferApply) {
        val tsc = LabelCommand();
        setTscBegin(tsc);
        // --------------- 打印区-------------Begin

        var beginXPos = 20; // 开始横向位置
        var beginYPos = 10; // 开始纵向位置
        var rowHigthSum = 0; // 纵向高度的叠加
        var rowSpacing = 30; // 每行之间的距离

        rowHigthSum = beginYPos+10;
        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"+++++++++++++++ "+m.billNo+" ++++++++++++++开始");
        rowHigthSum = rowHigthSum + rowSpacing;
        tsc.addText(beginXPos, rowHigthSum+6, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"申请单： "+ m.billNo +"");
        rowHigthSum = rowHigthSum + rowSpacing;
        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"申请人： "+ m.createUser.username +"");
        rowHigthSum = rowHigthSum + rowSpacing;
        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"申请日期："+ m.createDate.substring(0,19) +"");

        // --------------- 打印区-------------End
        setTscEnd(tsc);
    }

    /**
     * 打印头部1
     */
    private fun setPrintFormatList(count :Int, pos :Int, m :TransferApplyEntry) {
        val tsc = LabelCommand();
        if (count == pos) {
            setTscBegin3(tsc)
        } else {
            setTscBegin2(tsc)
        }
        // --------------- 打印区-------------Begin

        var beginXPos = 20; // 开始横向位置
        var beginYPos = 12; // 开始纵向位置
        var rowHigthSum = 0; // 纵向高度的叠加
        var rowSpacing = 30; // 每行之间的距离

        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "" + pos + "、" + m.icItem.fname + "");
        rowHigthSum = rowHigthSum + rowSpacing
        tsc.addText(56, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "代码： " + m.icItem.fnumber + "");
        rowHigthSum = rowHigthSum + rowSpacing
        tsc.addText(56, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "申请数：" + m.fqty + "");
        rowHigthSum = rowHigthSum + rowSpacing
        if (m.stockPos != null) {
            tsc.addText(56, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "仓库：" + m.stock.fname + "   库位"+m.stockPos.fname+"");
        } else {
            tsc.addText(56, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "仓库：" + m.stock.fname + " ");
        }
        if(count == pos) {
            rowHigthSum = rowHigthSum + rowSpacing
            tsc.addText(beginXPos, rowHigthSum+6, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "--------------- " + m.transferApply.billNo + " --------------结束 ");
        }

        // --------------- 打印区-------------End
        setTscEnd(tsc);
    }

    /**
     * 打印前段配置
     * @param tsc
     */
    private fun setTscBegin(tsc: LabelCommand) {
        // 设置标签尺寸，按照实际尺寸设置
        tsc.addSize(78, 18)
        // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        //        tsc.addGap(10);
        tsc.addGap(0)
        // 设置打印方向
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        // 开启带Response的打印，用于连续打印
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        // 设置原点坐标
        tsc.addReference(0, 0)
        // 撕纸模式开启
        tsc.addTear(EscCommand.ENABLE.ON)
        // 清除打印缓冲区
        tsc.addCls()
    }

    /**
     * 打印前段配置
     * @param tsc
     */
    private fun setTscBegin2(tsc: LabelCommand) {
        // 设置标签尺寸，按照实际尺寸设置
        tsc.addSize(78, 16)
        // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        //        tsc.addGap(10);
        tsc.addGap(0)
        // 设置打印方向
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        // 开启带Response的打印，用于连续打印
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        // 设置原点坐标
        tsc.addReference(0, 0)
        // 撕纸模式开启
        tsc.addTear(EscCommand.ENABLE.ON)
        // 清除打印缓冲区
        tsc.addCls()
    }

    /**
     * 打印前段配置
     * @param tsc
     */
    private fun setTscBegin3(tsc: LabelCommand) {
        // 设置标签尺寸，按照实际尺寸设置
        tsc.addSize(78, 24)
        // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        //        tsc.addGap(10);
        tsc.addGap(0)
        // 设置打印方向
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        // 开启带Response的打印，用于连续打印
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        // 设置原点坐标
        tsc.addReference(0, 0)
        // 撕纸模式开启
        tsc.addTear(EscCommand.ENABLE.ON)
        // 清除打印缓冲区
        tsc.addCls()
    }

    /**
     * 打印后段配置
     * @param tsc
     */
    private fun setTscEnd(tsc: LabelCommand) {
        // 打印标签
        tsc.addPrint(1, 1)
        // 打印标签后 蜂鸣器响

        tsc.addSound(2, 100)
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255)
        val datas = tsc.command
        // 发送数据
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            return
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas)
    }

    /**
     * 蓝牙监听广播
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                // 蓝牙连接断开广播
                ACTION_USB_DEVICE_DETACHED, BluetoothDevice.ACTION_ACL_DISCONNECTED -> mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget()
                DeviceConnFactoryManager.ACTION_CONN_STATE -> {
                    val state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1)
                    val deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1)
                    when (state) {
                        DeviceConnFactoryManager.CONN_STATE_DISCONNECT -> if (id == deviceId) {
                            tv_connState.setText(getString(R.string.str_conn_state_disconnect))
                            tv_connState.setTextColor(Color.parseColor("#666666")) // 未连接-灰色
                            isConnected = false
                        }
                        DeviceConnFactoryManager.CONN_STATE_CONNECTING -> {
                            tv_connState.setText(getString(R.string.str_conn_state_connecting))
                            tv_connState.setTextColor(Color.parseColor("#6a5acd")) // 连接中-紫色
                            isConnected = false
                        }
                        DeviceConnFactoryManager.CONN_STATE_CONNECTED -> {
                            //                            tv_connState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            tv_connState.setText(getString(R.string.str_conn_state_connected))
                            tv_connState.setTextColor(Color.parseColor("#008800")) // 已连接-绿色
                            // 连接成功，开始打印
                            setPrintBegin()
                            isConnected = true
                        }
                        CONN_STATE_FAILED -> {
                            Utils.toast(context, getString(R.string.str_conn_fail))
                            tv_connState.setText(getString(R.string.str_conn_state_disconnect))
                            tv_connState.setTextColor(Color.parseColor("#666666")) // 未连接-灰色
                            isConnected = false
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_DEVICE_DETACHED)
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE)
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        DeviceConnFactoryManager.closeAllPort()
        if (threadPool != null) {
            threadPool!!.stopThreadPool()
        }
        super.onDestroy()
    }

}
