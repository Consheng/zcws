package ykk.cb.com.zcws.purchase.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.pur.POInStock
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter

class Pur_Sel_ReceiveOrderList_DialogAdapter(private val context: Activity, private val datas: List<POInStock>) : BaseArrayRecyclerAdapter<POInStock>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.pur_sel_receive_orderlist_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: POInStock, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_suppName = holder.obtainView<TextView>(R.id.tv_suppName)
        val tv_fdate = holder.obtainView<TextView>(R.id.tv_fdate)
        val tv_waitUnload = holder.obtainView<TextView>(R.id.tv_waitUnload)

        // 赋值
        tv_row!!.setText((pos + 1).toString())
        tv_fnumber!!.setText(entity.fbillno)
        tv_suppName.text = Html.fromHtml("供应商:&nbsp;<font color='#6a5acd'>" + entity.suppName + "</font>")
        tv_fdate.text = Html.fromHtml("日期:&nbsp;<font color='#000000'>" + entity.fdate.substring(0,10) + "</font>")
        if(entity.isWmsUploadStatus) {
            tv_waitUnload.visibility = View.VISIBLE
        } else {
            tv_waitUnload.visibility = View.INVISIBLE
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: POInStock, position: Int)
    }

}
