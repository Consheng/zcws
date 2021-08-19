package ykk.cb.com.zcws.produce.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.ScanningRecord
import ykk.cb.com.zcws.bean.prod.ProdOrder
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Prod_ScInFragment1Adapter(private val context: Activity, datas: List<ScanningRecord>) : BaseArrayRecyclerAdapter<ScanningRecord>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.prod_sc_in_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ScanningRecord, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_prodNo = holder.obtainView<TextView>(R.id.tv_prodNo)
        val tv_num = holder.obtainView<TextView>(R.id.tv_num)
        val tv_usableQty = holder.obtainView<TextView>(R.id.tv_usableQty)
        val tv_prodQty = holder.obtainView<TextView>(R.id.tv_prodQty)
        val tv_stockName = holder.obtainView<TextView>(R.id.tv_stockName)
        val tv_stockPosName = holder.obtainView<TextView>(R.id.tv_stockPosName)

        // 赋值
        val prodOrder = JsonUtil.stringToObject(entity.sourceObj, ProdOrder::class.java)
        val icItem = prodOrder.icItem

        tv_row.setText((pos + 1).toString())
        tv_fname.text = icItem.fname
        tv_fnumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+ icItem.fnumber+"</font>")
        tv_prodNo.text = Html.fromHtml("生产单号:&nbsp;<font color='#6a5acd'>"+ prodOrder.prodNo +"</font>")
        tv_num.text = Html.fromHtml("入库数:&nbsp;<font color='#FF0000'>"+ df.format(entity.realQty) +"</font>")
        tv_usableQty.text = Html.fromHtml("可用数:&nbsp;<font color='#6a5acd'>"+ df.format(entity.useableQty) +"</font>")
        tv_prodQty.text = Html.fromHtml("生产数:&nbsp;<font color='#6a5acd'>"+ df.format(entity.sourceQty) +"</font>&nbsp;<font color='#666666'>"+ prodOrder.unitName +"</font>")

        // 显示仓库组信息
        if(entity.stock != null ) {
            tv_stockName.visibility = View.VISIBLE
            tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#000000'>"+entity.stock!!.fname+"</font>")
        } else {
            tv_stockName.visibility = View.INVISIBLE
        }
        if(entity.stockPos != null ) {
            tv_stockPosName.visibility = View.VISIBLE
            tv_stockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#000000'>"+entity.stockPos!!.fname+"</font>")
        } else {
            tv_stockPosName.visibility = View.INVISIBLE
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
    }

}
