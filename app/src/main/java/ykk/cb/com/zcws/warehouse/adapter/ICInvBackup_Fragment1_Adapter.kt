package ykk.cb.com.zcws.warehouse.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.k3Bean.ICInvBackup
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class ICInvBackup_Fragment1_Adapter(private val context: Activity, datas: List<ICInvBackup>) : BaseArrayRecyclerAdapter<ICInvBackup>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ware_icinvbackup2_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICInvBackup, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_batchNo = holder.obtainView<TextView>(R.id.tv_batchNo)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_stockName = holder.obtainView<TextView>(R.id.tv_stockName)
        val tv_stockPosName = holder.obtainView<TextView>(R.id.tv_stockPosName)
        val tv_num = holder.obtainView<TextView>(R.id.tv_num)
        val tv_unitName = holder.obtainView<TextView>(R.id.tv_unitName)
        val view_del = holder.obtainView<View>(R.id.view_del)
        val tv_num_hint = holder.obtainView<TextView>(R.id.tv_num_hint)
        val rela_parent = holder.obtainView<RelativeLayout>(R.id.lin_parent)

        val icitem = entity.icItem
        // 赋值
        tv_row.text = (pos + 1).toString()
        tv_mtlName.text = icitem.fname
        tv_mtlNumber.text = Html.fromHtml("物料代码:&nbsp;<font color='#6a5acd'>"+icitem.fnumber+"</font>")
        tv_batchNo.text = Html.fromHtml("批次:&nbsp;<font color='#6a5acd'>"+Comm.isNULLS(entity.fbatchNo)+"</font>")
        tv_fmodel.text = Html.fromHtml("规格型号:&nbsp;<font color='#6a5acd'>"+Comm.isNULLS(entity.icItem.fmodel)+"</font>")
        tv_unitName.text = entity.icItem.unitName
        tv_num.text = df.format(entity.realQty)

        if(icitem.batchManager.equals("Y")) {
            tv_num_hint.visibility = View.VISIBLE
            tv_batchNo.visibility = View.VISIBLE
        } else {
            tv_num_hint.visibility = View.INVISIBLE
            tv_batchNo.visibility = View.INVISIBLE
        }

        if(entity.stock != null ) {
            tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#6a5acd'>"+entity.stock!!.fname+"</font>")
        }
        if(entity.stockPos != null ) {
            tv_stockPosName.visibility = View.VISIBLE
            tv_stockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#6a5acd'>"+entity.stockPos!!.fname+"</font>")
        } else {
            tv_stockPosName.visibility = View.INVISIBLE
        }

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv_num -> {// 盘点数
                    if (callBack != null) {
                        callBack!!.onClick_num(entity, pos)
                    }
                }
                R.id.view_del -> {// 删除行
                     if (callBack != null) {
                         callBack!!.onDelete(entity, pos)
                     }
                }
            }
        }
        tv_num.setOnClickListener(click)
        view_del.setOnClickListener(click)

        // 数量长按输入批次
        tv_num.setOnLongClickListener {
            if(icitem.batchManager.equals("Y")) {
                if (callBack != null) {
                    callBack!!.onClick_batch(entity, pos)
                }
                true
            } else {
                if (callBack != null) {
                    callBack!!.onClick_num(entity, pos)
                }
                false
            }
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick_num(entity: ICInvBackup, position: Int)
        fun onClick_batch(entity: ICInvBackup, position: Int)
        fun onDelete(entity: ICInvBackup, position: Int)
    }

}
