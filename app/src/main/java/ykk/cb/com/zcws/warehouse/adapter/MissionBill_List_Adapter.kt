package ykk.cb.com.zcws.warehouse.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.MissionBill
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class MissionBill_List_Adapter(private val context: Activity, private val datas: List<MissionBill>) : BaseArrayRecyclerAdapter<MissionBill>(datas) {
    private val df = DecimalFormat("#.####")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.missionbill_list_adapter
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MissionBill, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_date = holder.obtainView<TextView>(R.id.tv_date)
        val tv_billNo = holder.obtainView<TextView>(R.id.tv_billNo)
        val tv_missionType = holder.obtainView<TextView>(R.id.tv_missionType)
        val tv_missionContent = holder.obtainView<TextView>(R.id.tv_missionContent)

        // 赋值
        tv_row.text = (pos + 1).toString()
        tv_date.text = entity.createTime
        tv_billNo.text = entity.billNo
        //任务类型 	1：调拨拣货任务
        when (entity.missionType) {
            1 -> tv_missionType.text = "调拨拣货任务"
        }
        val missionContent = Comm.isNULLS(entity.missionContent)
        if(missionContent.length > 0) {
            tv_missionContent.visibility = View.VISIBLE
        } else {
            tv_missionContent.visibility = View.INVISIBLE
        }
        tv_missionContent.text = Html.fromHtml("任务内容:&nbsp;<font color='#000000'>"+ Comm.isNULLS(entity.missionContent)+"</font>")

        val view = tv_row.parent as View
        if(entity.isCheck) {
            view.setBackgroundResource(R.drawable.back_style_check1_true)
        } else {
            view.setBackgroundResource(R.drawable.back_style_check1_false)
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: MissionBill, position: Int)
    }


}
