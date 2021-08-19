package ykk.cb.com.zcws.warehouse.adapter

import android.app.Activity
import android.graphics.Color
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.TransferApplyEntry
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Ware_Transfer_Apply_SearchDetailAdapter(private val context: Activity, datas: List<TransferApplyEntry>) : BaseArrayRecyclerAdapter<TransferApplyEntry>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ware_transfer_apply_search_detail_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: TransferApplyEntry, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        val tv_fqty = holder.obtainView<TextView>(R.id.tv_fqty)
        val tv_commitQty = holder.obtainView<TextView>(R.id.tv_commitQty)
        val tv_stockName = holder.obtainView<TextView>(R.id.tv_stockName)
        val tv_stockPosName = holder.obtainView<TextView>(R.id.tv_stockPosName)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_fname.text = entity.icItem.fname
        tv_fnumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.icItem.fnumber+"</font>")
        tv_fqty.text = Html.fromHtml("申请数:&nbsp;<font color='#FF4400'>"+ df.format(entity.fqty) +"</font>")
        tv_commitQty.text = Html.fromHtml("已拣货数:&nbsp;<font color='#6a5acd'>"+ df.format(entity.commitQty) +"</font>")

        // 显示仓库组信息
        if(entity.stock != null ) {
            tv_stockName.visibility = View.VISIBLE
            tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#000000'>"+entity.stock.fname+"</font>")
        } else {
            tv_stockName.visibility = View.GONE
        }
        if(entity.stockPos != null ) {
            tv_stockPosName.visibility = View.VISIBLE
            tv_stockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#000000'>"+entity.stockPos.fname+"</font>")
        } else {
            tv_stockPosName.visibility = View.INVISIBLE
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: TransferApplyEntry, position: Int)
    }

}
