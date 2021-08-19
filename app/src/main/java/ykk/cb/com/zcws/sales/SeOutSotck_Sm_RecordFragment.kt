package ykk.cb.com.zcws.sales

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.sal_seoutstock_sm_record_fragment.*
import kotlinx.android.synthetic.main.sal_seoutstock_sm_record_main.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.GoodsScanRecord
import ykk.cb.com.zcws.bean.OrderProcess
import ykk.cb.com.zcws.bean.User
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.sales.adapter.SeOutStock_Sm_RecordFragment_Adapter
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.LogUtil
import ykk.cb.com.zcws.util.xrecyclerview.XRecyclerView
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 扫码汇包和查询
 */
class SeOutSotck_Sm_RecordFragment : BaseFragment(), XRecyclerView.LoadingListener {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 202
        private val UNSUCC2 = 502
        private val SUCC3 = 203
        private val UNSUCC3 = 503

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
    }

    private val context = this
    private var mAdapter: SeOutStock_Sm_RecordFragment_Adapter? = null
    private val listDatas = ArrayList<GoodsScanRecord>()
    private var okHttpClient: OkHttpClient? = null
    private var mContext: Activity? = null
    private var parent: SeOutStock_Sm_RecordMainActivity? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private val df = DecimalFormat("#.####")
    private var limit = 1
    private var isRefresh: Boolean = false
    private var isLoadMore: Boolean = false
    private var isNextPage: Boolean = false
    private var statusFlag = '1' // 1：查询，2：录入
    private var user: User? = null
    private var orderProcess :OrderProcess? = null

    // 消息处理
    private val mHandler = SeOutSotck_Sm_RecordFragment.MyHandler(this)

    private class MyHandler(activity: SeOutSotck_Sm_RecordFragment) : Handler() {
        private val mActivity: WeakReference<SeOutSotck_Sm_RecordFragment>

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
                    SUCC1 -> {// 扫码成功后进入
                        if(msgObj!!.indexOf("finish") > -1) {
                            Comm.showWarnDialog(m.mContext,"（"+m.getValues(m.et_code)+"）对应的节点已全部汇报！")
                            return
                        }

                        m.orderProcess = JsonUtil.strToObject(msgObj, OrderProcess::class.java)
                        if (m.orderProcess!!.flagNode.equals("A")) { // 要判断节点
                            val build = AlertDialog.Builder(m.mContext)
                            build.setIcon(R.drawable.caution)
                            build.setTitle("系统提示")
                            build.setMessage(m.orderProcess!!.tips+"？")
                            build.setPositiveButton("是", object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface, which: Int) {
                                    m.run_add(m.orderProcess!!.flagValue.toString())
                                }
                            })
                            build.setNegativeButton("否", null)
                            build.setCancelable(false)
                            build.show()
                        } else {
                            m.run_add(m.orderProcess!!.nodeId.toString())
                        }
                    }
                    UNSUCC1 -> {
                        errMsg = JsonUtil.strToString(msgObj)
                        if (Comm.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> {// 保存 succ
                        m.initLoadDatas()
                    }
                    UNSUCC2 -> { // 保存 fail
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("抱歉，保存记录失败，请重新扫码！")
                    }
                    SUCC3 -> {// 查询列表   succ
                        val list = JsonUtil.strToList2(msgObj, GoodsScanRecord::class.java)
                        m.listDatas.addAll(list!!)
                        m.mAdapter!!.notifyDataSetChanged()

                        if (m.isRefresh) {
                            m.xRecyclerView!!.refreshComplete(true)
                        } else if (m.isLoadMore) {
                            m.xRecyclerView!!.loadMoreComplete(true)
                        }

                        m.xRecyclerView!!.isLoadingMoreEnabled = m.isNextPage
                    }
                    UNSUCC3 -> { // 查询列表   fail
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("抱歉，没有加载到数据！")
                    }
                    SETFOCUS -> {// 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        m.setFocusable(m.et_code)
                    }
                    SAOMA -> {// 扫码之后
                        if(m.statusFlag == '1') {
                            // 执行查询方法
                            m.initLoadDatas()
                        } else {
                            // 执行查询方法
                            m.run_findBarcode()
                        }
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.sal_seoutstock_sm_record_fragment, container, false)
    }

    override fun initView() {
        mContext = activity
        parent = mContext as SeOutStock_Sm_RecordMainActivity?

        xRecyclerView!!.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        xRecyclerView!!.layoutManager = LinearLayoutManager(mContext)
        mAdapter = SeOutStock_Sm_RecordFragment_Adapter(mContext!!, listDatas)
        xRecyclerView!!.adapter = mAdapter
        xRecyclerView!!.setLoadingListener(context)

        xRecyclerView!!.isPullRefreshEnabled = false // 上啦刷新禁用
        xRecyclerView.isLoadingMoreEnabled = false // 不显示下拉刷新的view
    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(30, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(30, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }
        tv_begDate.text = Comm.getSysDate(7)
        tv_endDate.text = Comm.getSysDate(7)
        hideSoftInputMode(mContext, et_code)

        getUserInfo()
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
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    @OnClick(R.id.btn_scan, R.id.tv_begDate, R.id.tv_endDate, R.id.btn_status)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_scan -> {// 调用摄像头扫描（物料）
                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.tv_begDate -> {
                Comm.showDateDialog(mContext,view,0)
            }
            R.id.tv_endDate -> {
                Comm.showDateDialog(mContext,view,0)
            }
            R.id.btn_status -> { // 切换状态
                if(statusFlag == '1') {
                    statusFlag = '2'
                    btn_status.text = "录入"
                    parent!!.tv_title.text = "发货单跟踪（录入）"
                    parent!!.lin_title.setBackgroundResource(R.color.purple)
                    parent!!.btn_close.setBackgroundResource(R.drawable.btn_purple2_selector)
                    btn_status.setBackgroundResource(R.drawable.shape_purple1a)

                } else {
                    statusFlag = '1'
                    btn_status.text = "查询"
                    parent!!.tv_title.text = "发货单跟踪（查询）"
                    parent!!.lin_title.setBackgroundResource(R.color.public_blue)
                    parent!!.btn_close.setBackgroundResource(R.drawable.btn_blue2_selector)
                    btn_status.setBackgroundResource(R.drawable.shape_blue1a)
                }
            }
        }
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

    fun searchFun() {
        initLoadDatas()
    }

    private fun initLoadDatas() {
        limit = 1
        listDatas.clear()
        run_findListParamByPage()
    }

    override fun onRefresh() {
        isRefresh = true
        isLoadMore = false
        run_findListParamByPage()
    }

    override fun onLoadMore() {
        isRefresh = false
        isLoadMore = true
        limit += 1
        run_findListParamByPage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            WRITE_CODE // 输入条码返回
            -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                if (bundle != null) {
                    val value = bundle.getString("resultValue", "")
                    et_code!!.setText(value.toUpperCase())
                }
            }
            BaseFragment.CAMERA_SCAN // 扫一扫成功  返回
            -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                if (bundle != null) {
                    val code = bundle.getString(BaseFragment.DECODED_CONTENT_KEY, "")
                    setTexts(et_code, code)
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 300)
    }

    /**
     * 扫码查询
     */
    private fun run_findBarcode() {
        isTextChange = false
        if(getValues(et_code).length == 0) {
            Comm.showWarnDialog(mContext, "请先扫描条码！")
            return
        }
        showLoadDialog("加载中...", false)
        val mUrl = getURL("goodsScanRecord/findBarcode")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
                .add("userId", user!!.id.toString())
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
                LogUtil.e("run_findBarcode --> onResponse", result)
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
     * 汇报记录
     */
    private fun run_add(nodeId :String) {
        isTextChange = false
        showLoadDialog("加载中...", false)
        val mUrl = getURL("goodsScanRecord/add")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
                .add("nodeId", nodeId)
                .add("userId", user!!.id.toString())
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
                LogUtil.e("run_add --> onResponse", result)
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
    private fun run_findListParamByPage() {
        isTextChange = false
        if(getValues(et_code).length == 0) {
            Comm.showWarnDialog(mContext, "请先扫描条码！")
            return
        }
        showLoadDialog("加载中...", false)
        val mUrl = getURL("goodsScanRecord/findListParamByPage")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
//                .add("begDate", getValues(tv_begDate))
//                .add("endDate", getValues(tv_endDate))
                .add("limit", limit.toString())
                .add("pageSize", "50")
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
                LogUtil.e("run_findListParamByPage --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC3, result)
                    mHandler.sendMessage(msg)
                    return
                }
                isNextPage = JsonUtil.isNextPage(result)

                val msg = mHandler.obtainMessage(SUCC3, result)
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
