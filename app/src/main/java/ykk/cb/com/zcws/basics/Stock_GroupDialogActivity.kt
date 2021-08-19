package ykk.cb.com.zcws.basics

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ab_stock_group_dialog.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.Stock
import ykk.cb.com.zcws.bean.StockPosition
import ykk.cb.com.zcws.comm.BaseDialogActivity
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.LogUtil
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * 选择仓库组dialog
 */
class Stock_GroupDialogActivity : BaseDialogActivity() {

    companion object {
        private val SEL_STOCK = 10000
        private val SEL_STOCKPOS = 20000

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3

        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SEL_STOCKAUTOBACK = "SEL_STOCKAUTOBACK"
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var stock:Stock? = null
    private var stockPos:StockPosition? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var smqFlag = '1' // 使用扫码枪扫码（1：仓库扫码，2：库区扫码，3：货架扫码，4：库位扫码）

    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Stock_GroupDialogActivity) : Handler() {
        private val mActivity: WeakReference<Stock_GroupDialogActivity>

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
                            '1' -> { // 仓库扫码
                                val stock = JsonUtil.strToObject(msgObj, Stock::class.java)
                                m.getStock(stock,false)
                            }
                            '2' -> { // 库位扫码
                                val stockPos = JsonUtil.strToObject(msgObj, StockPosition::class.java)
                                var stock:Stock? = null
                                if(stockPos.stock != null) {
                                    stock = stockPos.stock
                                }
                                m.getStockGroup(stock, stockPos)
                            }
                        }
                    }
                    UNSUCC1 -> { // 扫码失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1' -> m.setFocusable(m.et_stockCode)
                            '2' -> m.setFocusable(m.et_stockPosCode)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        // 执行查询方法
                        m.run_smDatas()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.ab_stock_group_dialog
    }

    override fun initView() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }
        hideSoftInputMode(et_stockCode)
        hideSoftInputMode(et_stockPosCode)

        val spf = spf(getResStr(R.string.saveOther))
        if(spf.contains(SEL_STOCKAUTOBACK)) {
            cb_stockAutoConfirm.isChecked = true
        }
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            if(bundle.containsKey("stock") && bundle.getSerializable("stock") != null) {
                stock = bundle.getSerializable("stock") as Stock
                getStock(stock!!,false)
            }
            if(bundle.containsKey("stockPos") && bundle.getSerializable("stockPos") != null) {
                stockPos = bundle.getSerializable("stockPos") as StockPosition
                getStockPos(stockPos!!,false)
            }

        }
        if(stock == null) {
            val bundle = Bundle()
            bundle.putString("accountType", "ZH")
            showForResult(Stock_DialogActivity::class.java, SEL_STOCK, bundle)
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    // 监听事件
    @OnClick( R.id.btn_close, R.id.btn_stockSel, R.id.btn_stockPosSel, R.id.tv_stockName, R.id.tv_stockPosName,
              R.id.btn_stockScan, R.id.btn_stockPosScan,R.id.btn_confirm )
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                context.finish()
            }
            R.id.btn_stockSel -> { // 仓库
                val bundle = Bundle()
                bundle.putString("accountType", "ZH")
                showForResult(Stock_DialogActivity::class.java, SEL_STOCK, bundle)
            }
            R.id.btn_stockPosSel -> { // 库位
                val bundle = Bundle()
                bundle.putInt("fspGroupId", if(stock != null)stock!!.fspGroupId else 0)
                bundle.putString("accountType", "ZH")
                showForResult(StockPos_DialogActivity::class.java, SEL_STOCKPOS, bundle)
            }
            R.id.btn_stockScan -> { // 调用摄像头扫描（仓库）
                smqFlag = '1'
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.btn_stockPosScan -> { // 调用摄像头扫描（库位）
                smqFlag = '2'
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.tv_stockName -> { // 仓库点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
            }
            R.id.tv_stockPosName -> { // 库位点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
            }
            R.id.btn_confirm -> { // 确认
                if(stock == null) { // 是否启用了库区
                    Comm.showWarnDialog(context, "请选择仓库！")
                    return
                }
                if(stock!!.fisStockMgr == 1 && stockPos == null) { // 是否启用了库位
                    Comm.showWarnDialog(context, "请选择库位！")
                    return
                }
                // 传送对象
                sendObj()
            }
        }
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_stockCode -> setFocusable(et_stockCode)
                R.id.et_stockPosCode -> setFocusable(et_stockPosCode)
            }
        }
        et_stockCode!!.setOnClickListener(click)
        et_stockPosCode!!.setOnClickListener(click)

        // 仓库---数据变化
        et_stockCode!!.addTextChangedListener(object : TextWatcher {
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
        et_stockCode!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 仓库---焦点改变
        et_stockCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusStock.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusStock != null) {
                    lin_focusStock!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

        // 库位---数据变化
        et_stockPosCode!!.addTextChangedListener(object : TextWatcher {
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
        // 库位---长按输入条码
        et_stockPosCode!!.setOnLongClickListener {
            smqFlag = '2'
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 库位---焦点改变
        et_stockPosCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusStockPos.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusStockPos != null) {
                    lin_focusStockPos!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

        // 自动返回
        cb_stockAutoConfirm.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                spf(getResStr(R.string.saveOther)).edit().putBoolean(SEL_STOCKAUTOBACK, true).commit()
            } else {
                spf(getResStr(R.string.saveOther)).edit().remove(SEL_STOCKAUTOBACK).commit()
            }
        }
    }

    private fun sendObj() {
        // 传送对象
        val intent = Intent()
        intent.putExtra("stock", stock as Serializable)
        if(stockPos != null) {
            intent.putExtra("stockPos", stockPos as Serializable)
        }
        context.setResult(Activity.RESULT_OK, intent)
        context.finish()
    }

    private fun stockStartParam() {
        // 是否启用了库位
        if(stock!!.fisStockMgr == 1) {
            setEnables(lin_focusStockPos, R.drawable.back_style_blue, true)
            btn_stockPosScan.isEnabled = true
            btn_stockPosSel.isEnabled = true
            et_stockPosCode.isEnabled = true
        } else {
            setEnables(lin_focusStockPos, R.drawable.back_style_gray3, false)
            btn_stockPosScan.isEnabled = false
            btn_stockPosSel.isEnabled = false
            et_stockPosCode.isEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_STOCK -> {// 仓库	返回
                    stock = data!!.getSerializableExtra("obj") as Stock
                    getStock(stock!!, true)
                }
                SEL_STOCKPOS -> { // 库位	返回
                    stockPos = data!!.getSerializableExtra("obj") as StockPosition
                    getStockPos(stockPos!!, true)
                }
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        if (bundle != null) {
                            val code = bundle.getString(BaseFragment.DECODED_CONTENT_KEY, "")
                            when (smqFlag) {
                                '1' -> setTexts(et_stockCode, code)
                                '2' -> setTexts(et_stockPosCode, code)
                            }
                        }
                    }
                }
                WRITE_CODE -> {
                    // 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        when (smqFlag) {
                            '1' -> setTexts(et_stockCode, value.toUpperCase())
                            '2' -> setTexts(et_stockPosCode, value.toUpperCase())
                        }
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 得到仓库
     */
    fun getStock(stock :Stock, autoNext :Boolean) {
        context.stock = stock
        tv_stockName.text = stock!!.fname

        stockStartParam()
        stockPos = null
        tv_stockPosName.text = ""

        var isBool = true
        // 只有选择的时候，自动打开下一级
        if(autoNext && btn_stockPosSel.isEnabled) {
            isBool = false
            val bundle = Bundle()
            bundle.putInt("fspGroupId", if(stock != null)stock!!.fspGroupId else 0)
            showForResult(StockPos_DialogActivity::class.java, SEL_STOCKPOS, bundle)
        }

        // 自动返回
        if(isBool && autoNext && cb_stockAutoConfirm.isChecked) {
            sendObj()
        }
    }

    /**
     * 得到库位
     */
    fun getStockPos(stockPos : StockPosition, autoNext :Boolean) {
        context.stockPos = stockPos
        tv_stockPosName.text = stockPos!!.fname

        // 自动返回
        if(autoNext && cb_stockAutoConfirm.isChecked) {
            sendObj()
        }
    }

    /**
     * 根据库位扫描得到（仓库，库区，货架，库位）
     */
    fun getStockGroup(stock: Stock?, stockPos : StockPosition?) {
        if(context.stock == null && stock != null) {
            getStock(stock,false)
        }
        if(context.stockPos == null && stockPos != null) {
            getStockPos(stockPos, false)
        }
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode:String? = null
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stock/findBarcode")
                barcode = getValues(et_stockCode)
            }
            '2' -> {
                mUrl = getURL("stockPosition/findBarcode")
                barcode = getValues(et_stockPosCode)
            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("fspGroupId", if(stock != null) stock!!.fspGroupId.toString() else "" )
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
                LogUtil.e("run_smDatas --> onResponse", result)
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
