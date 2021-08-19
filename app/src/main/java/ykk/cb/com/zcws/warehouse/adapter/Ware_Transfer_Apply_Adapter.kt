package ykk.cb.com.zcws.warehouse.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.TransferApplyEntry
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Ware_Transfer_Apply_Adapter(private val context: Activity, datas: List<TransferApplyEntry>) : BaseArrayRecyclerAdapter<TransferApplyEntry>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ware_transfer_apply_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: TransferApplyEntry, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_invQty = holder.obtainView<TextView>(R.id.tv_invQty)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_num = holder.obtainView<TextView>(R.id.tv_num)
        val view_del = holder.obtainView<View>(R.id.view_del)
        val tv_stockName = holder.obtainView<TextView>(R.id.tv_stockName)
        val tv_stockPosName = holder.obtainView<TextView>(R.id.tv_stockPosName)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+ entity.icItem.fnumber +"</font>")
        if(entity.icItem.inventoryQty > 0) {
            tv_invQty.text = Html.fromHtml("库存:&nbsp;<font color='#008800'>" + df.format(entity.icItem.inventoryQty) + "</font>")
        } else {
            tv_invQty.text = Html.fromHtml("库存:&nbsp;<font color='#FF0000'>" + df.format(entity.icItem.inventoryQty) + "</font>")
        }
        tv_fmodel.text = Html.fromHtml("规格型号:&nbsp;<font color='#6a5acd'>"+ entity.icItem.fmodel +"</font>")

        tv_num.text = Html.fromHtml("申请数:&nbsp;<font color='#FF2200'>"+ df.format(entity.fqty) +"</font>&nbsp;<font color='#666666'>"+ entity.icItem.unitName +"</font>")

        // 显示仓库组信息
        if(entity.stock != null ) {
            tv_stockName.visibility = View.VISIBLE
            tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#6a5acd'>"+entity.stock!!.fname+"</font>")
        } else {
            tv_stockName.visibility = View.INVISIBLE
        }
        if(entity.stockPos != null ) {
            tv_stockPosName.visibility = View.VISIBLE
            tv_stockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#6a5acd'>"+entity.stockPos!!.fname+"</font>")
        } else {
            tv_stockPosName.visibility = View.INVISIBLE
        }

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.view_del -> {// 删除行
                    if (callBack != null) {
                        callBack!!.onDelete(entity, pos)
                    }
                }
            }
        }
        view_del!!.setOnClickListener(click)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: TransferApplyEntry, position: Int)
    }

}
