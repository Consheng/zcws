package ykk.cb.com.zcws.purchase.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.ScanningRecord
import ykk.cb.com.zcws.bean.pur.POOrderEntry
import ykk.cb.com.zcws.util.JsonUtil
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Pur_ScInFragment1Adapter(private val context: Activity, datas: List<ScanningRecord>) : BaseArrayRecyclerAdapter<ScanningRecord>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.pur_sc_in_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ScanningRecord, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_orderNo = holder.obtainView<TextView>(R.id.tv_orderNo)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_purNum = holder.obtainView<TextView>(R.id.tv_purNum)
        val tv_nums = holder.obtainView<TextView>(R.id.tv_nums)
        val tv_stockAP = holder.obtainView<TextView>(R.id.tv_stockAP)

        // 赋值
        val purEntry = JsonUtil.stringToObject(entity.sourceObj, POOrderEntry::class.java)
        val icItem = purEntry.icItem

        tv_row!!.setText((pos + 1).toString())
        tv_orderNo!!.setText(entity.sourceNumber)
        tv_mtlName!!.setText(entity.icItemName)
        tv_purNum!!.setText(df.format(entity.sourceQty))
        // 990156：启用批次号，990156：启用序列号
        //        if(icItem.getSnManager() == 990156 || icItem.getBatchManager() == 990156) {
        //            tv_nums.setEnabled(false);
        //            tv_nums.setBackgroundResource(R.drawable.back_style_gray3b);
        //        } else {
        //            tv_nums.setEnabled(true);
        //            tv_nums.setBackgroundResource(R.drawable.back_style_blue2);
        //        }
        tv_nums!!.setText(Html.fromHtml(df.format(entity.useableQty) + "<br><font color='#009900'>" + df.format(entity.realQty) + "</font>"))
        if (entity.stockPos != null) {
            tv_stockAP!!.setText(Html.fromHtml(entity.stock.fname + "<br><font color='#6a5acd'>" + entity.stockPos.fname + "</font>"))
        } else if (entity.stock != null) {
            tv_stockAP!!.setText(entity.stock.fname)
        } else {
            tv_stockAP!!.setText("")
        }

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv_nums // 数量
                -> if (callBack != null) {
                    callBack!!.onClick_num(v, entity, pos)
                }
                R.id.tv_stockAP // 选择仓库
                -> if (callBack != null) {
                    callBack!!.onClick_selStock(v, entity, pos)
                }
            }
        }
        tv_nums!!.setOnClickListener(click)
        tv_stockAP!!.setOnClickListener(click)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick_num(v: View, entity: ScanningRecord, position: Int)
        fun onClick_selStock(v: View, entity: ScanningRecord, position: Int)
    }


}
