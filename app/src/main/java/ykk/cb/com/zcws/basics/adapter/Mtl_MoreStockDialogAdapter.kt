package ykk.cb.com.zcws.basics.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.k3Bean.ICItem
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Mtl_MoreStockDialogAdapter(private val context: Activity, private val datas: List<ICItem>) : BaseArrayRecyclerAdapter<ICItem>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ab_mtl_list_more_stock_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICItem, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_invQty = holder.obtainView<TextView>(R.id.tv_invQty)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_stock = holder.obtainView<TextView>(R.id.tv_stock)
        val tv_stockPos = holder.obtainView<TextView>(R.id.tv_stockPos)
        val view_check = holder.obtainView<View>(R.id.view_check)
        // 赋值
        tv_row!!.setText((pos + 1).toString())
        tv_fname!!.setText(entity.fname)
        tv_fnumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.fnumber+"</font>")
        if(entity.inventoryQty > 0) {
            tv_invQty.text = Html.fromHtml("库存:&nbsp;<font color='#008800'>" + df.format(entity.inventoryQty) + "</font>")
        } else {
            tv_invQty.text = Html.fromHtml("库存:&nbsp;<font color='#FF0000'>" + df.format(entity.inventoryQty) + "</font>")
        }
        tv_fmodel.text = Html.fromHtml("规格:&nbsp;<font color='#6a5acd'>"+entity.fmodel+"</font>")
        if(entity.stock != null) {
            tv_stock.text = Html.fromHtml("仓库:&nbsp;<font color='#6a5acd'>"+ entity.stock.fname +"</font>")
        }
        if(entity.stockPos != null) {
            tv_stockPos.visibility = View.VISIBLE
            tv_stockPos.text = Html.fromHtml("库位:&nbsp;<font color='#6a5acd'>"+ entity.stockPos.fname +"</font>")
        } else {
            tv_stockPos.visibility = View.GONE
        }

        if (entity.isCheck) {
            view_check.setBackgroundResource(R.drawable.check_true)
        } else {
            view_check.setBackgroundResource(R.drawable.check_false)
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: ICItem, position: Int)
    }

}
