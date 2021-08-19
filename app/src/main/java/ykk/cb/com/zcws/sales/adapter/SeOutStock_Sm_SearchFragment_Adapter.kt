package ykk.cb.com.zcws.sales.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.k3Bean.SeoutStockSearchStatus
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class SeOutStock_Sm_SearchFragment_Adapter(private val context: Activity, datas: List<SeoutStockSearchStatus>) : BaseArrayRecyclerAdapter<SeoutStockSearchStatus>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.sal_seoutstock_sm_search_fragment_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: SeoutStockSearchStatus, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_deliveryNo = holder.obtainView<TextView>(R.id.tv_deliveryNo)
        val tv_fqty = holder.obtainView<TextView>(R.id.tv_fqty)
        val tv_stockQty = holder.obtainView<TextView>(R.id.tv_stockQty)
        val tv_unStockQty = holder.obtainView<TextView>(R.id.tv_unStockQty)
        val tv_fdate = holder.obtainView<TextView>(R.id.tv_fdate)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_deliveryNo.text = entity.fbillNo
        tv_fqty.text = Html.fromHtml("数量:&nbsp;<font color='#6a5acd'>" + df.format(entity.fqty) + "</font>")
        tv_stockQty.text = Html.fromHtml("已发货:&nbsp;<font color='#009900'>"+ df.format(entity.stockQty) +"</font>")
        tv_unStockQty.text = Html.fromHtml("未发货:&nbsp;<font color='#FF4400'>"+ df.format(entity.unStockQty) +"</font>")
        tv_fdate.text = Html.fromHtml("日期:&nbsp;<font color='#000000'>"+entity.fdate.substring(0,10)+"</font>")
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
//        fun onDelete(entity: ICStockBillEntry, position: Int)
    }

}
