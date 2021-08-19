package ykk.cb.com.zcws.warehouse.adapter

import android.app.Activity
import android.graphics.Color
import android.text.Html
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.TransferApply
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Ware_Transfer_Apply_SearchAdapter(private val context: Activity, datas: List<TransferApply>) : BaseArrayRecyclerAdapter<TransferApply>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ware_transfer_apply_search_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: TransferApply, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_billNo = holder.obtainView<TextView>(R.id.tv_billNo)
        val tv_execStatus = holder.obtainView<TextView>(R.id.tv_execStatus)
        val tv_count = holder.obtainView<TextView>(R.id.tv_count)
        val tv_createUserName = holder.obtainView<TextView>(R.id.tv_createUserName)
        val tv_date = holder.obtainView<TextView>(R.id.tv_date)

        // 赋值
        tv_row.text = (pos + 1).toString()
        tv_billNo.text = entity.billNo
        tv_count.text = Html.fromHtml("物料个数:&nbsp;<font color='#000000'>"+ entity.mtlCount +"</font>")
        tv_createUserName.text = entity.createUser.username
        tv_date.text = entity.createDate
        // 执行状态：未调拨拣货，部分拣货，完成拣货
        if(entity.status == 3) {
            tv_execStatus.text = "完成拣货"
            tv_execStatus.setTextColor(Color.parseColor("#009900"))
        } else if(entity.status == 2) {
            tv_execStatus.text = "部分拣货"
            tv_execStatus.setTextColor(Color.parseColor("#6a5acd"))
        } else {
            tv_execStatus.text = "未拣货"
            tv_execStatus.setTextColor(Color.parseColor("#666666"))
        }

    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: TransferApply, position: Int)
    }

}
