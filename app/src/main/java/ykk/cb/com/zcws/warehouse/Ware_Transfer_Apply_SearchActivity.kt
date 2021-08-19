package ykk.cb.com.zcws.warehouse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ware_transfer_apply_search.*
import okhttp3.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.TransferApply
import ykk.cb.com.zcws.bean.User
import ykk.cb.com.zcws.comm.BaseActivity
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import ykk.cb.com.zcws.util.xrecyclerview.XRecyclerView
import ykk.cb.com.zcws.warehouse.adapter.Ware_Transfer_Apply_SearchAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 调拨申请查询
 */
class Ware_Transfer_Apply_SearchActivity : BaseActivity(), XRecyclerView.LoadingListener {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
    }
    private val context = this
    private var mAdapter: Ware_Transfer_Apply_SearchAdapter? = null
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private val listDatas = ArrayList<TransferApply>()
    private var limit = 1
    private var isRefresh: Boolean = false
    private var isLoadMore:Boolean = false
    private var isNextPage:Boolean = false

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Ware_Transfer_Apply_SearchActivity) : Handler() {
        private val mActivity: WeakReference<Ware_Transfer_Apply_SearchActivity>

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
                        val list = JsonUtil.strToList2(msg.obj as String, TransferApply::class.java)
                        m.listDatas.addAll(list!!)
                        m.mAdapter!!.notifyDataSetChanged()

                        if (m.isRefresh) {
                            m.xRecyclerView.refreshComplete(true)
                        } else if (m.isLoadMore) {
                            m.xRecyclerView.loadMoreComplete(true)
                        }

                        m.xRecyclerView.isLoadingMoreEnabled = m.isNextPage
                    }
                    UNSUCC1 -> { // 数据加载失败！
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("抱歉，没有加载到数据！")
                    }
                }
            }
        }

    }

    override fun setLayoutResID(): Int {
        return R.layout.ware_transfer_apply_search
    }

    override fun initView() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        xRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        xRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = Ware_Transfer_Apply_SearchAdapter(context, listDatas)
        xRecyclerView.adapter = mAdapter
        xRecyclerView.setLoadingListener(context)

        xRecyclerView.isPullRefreshEnabled = false // 上啦刷新禁用
        xRecyclerView.isLoadingMoreEnabled = false // 不显示下拉刷新的view

        // 行事件查明细
        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val m = listDatas.get(pos-1)
            val bundle = Bundle()
            bundle.putInt("transferApplyId", m.id)
            show(Ware_Transfer_Apply_SearchDetailActivity::class.java, bundle)
        }
    }

    override fun initData() {
        getUserInfo()
        initLoadDatas()
    }


    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_this, R.id.btn_all, R.id.tv_begDate, R.id.tv_endDate)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
            R.id.btn_this -> { // 查个人
                initLoadDatas()
            }
            R.id.btn_all -> { // 查全部
                initLoadDatas()
            }
            R.id.tv_begDate -> { // 开始日期
                Comm.showDateDialog(context,view,0)
            }
            R.id.tv_endDate -> { // 结束日期
                Comm.showDateDialog(context,view,0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
            }
        }
    }

    private fun initLoadDatas() {
        limit = 1
        listDatas.clear()
        run_findDatas()
    }

    /**
     * 查询列表
     */
    private fun run_findDatas() {
        showLoadDialog("加载中...", false)
        var mUrl = getURL("transferApply/findListByPage")
        val formBody = FormBody.Builder()
                .add("createUserId", if(btn_this.isChecked) user!!.id.toString() else "")
                .add("begDate", getValues(tv_begDate))
                .add("endDate", if(getValues(tv_endDate).length > 0) getValues(tv_endDate)+" 23:59:59" else "")
                .add("limit", limit.toString())
                .add("pageSize", "30")
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
                isNextPage = JsonUtil.isNextPage(result)

                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_findDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    override fun onRefresh() {
        isRefresh = true
        isLoadMore = false
        initLoadDatas()
    }

    override fun onLoadMore() {
        isRefresh = false
        isLoadMore = true
        limit += 1
        run_findDatas()
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

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
