package ykk.cb.com.zcws.warehouse.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.k3Bean.Inventory_K3
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class StockTransferFragment1_DS_Adapter(private val context: Activity, datas: List<Inventory_K3>) : BaseArrayRecyclerAdapter<Inventory_K3>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ware_sc_stock_transfer_fragment1_ds_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: Inventory_K3, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView(R.id.tv_row) as TextView
        val tv_mtlNumber = holder.obtainView(R.id.tv_mtlNumber) as TextView
        val tv_mtlName = holder.obtainView(R.id.tv_mtlName) as TextView
        val tv_nums = holder.obtainView(R.id.tv_nums) as TextView

        // 赋值
        tv_row!!.setText((pos + 1).toString())
        tv_mtlNumber!!.setText(entity.mtlNumber)
        tv_mtlName!!.setText(entity.mtlName)
        tv_nums!!.setText(Html.fromHtml(df.format(entity.fqty) + entity.unitName + "<br><font color='#009900'>" + df.format(entity.realQty) + "</font>"))

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv_nums // 调拨数
                -> if (callBack != null) {
                    callBack!!.onClick_num(entity, pos)
                }
            }
        }
        tv_nums!!.setOnClickListener(click)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick_num(entity: Inventory_K3, position: Int)
    }

}
