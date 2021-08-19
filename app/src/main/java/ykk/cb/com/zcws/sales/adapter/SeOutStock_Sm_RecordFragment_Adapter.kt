package ykk.cb.com.zcws.sales.adapter

import android.app.Activity
import android.text.Html
import android.widget.TextView
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.bean.GoodsScanRecord
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class SeOutStock_Sm_RecordFragment_Adapter(private val context: Activity, datas: List<GoodsScanRecord>) : BaseArrayRecyclerAdapter<GoodsScanRecord>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.sal_seoutstock_sm_record_fragment_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: GoodsScanRecord, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_deliveryNo = holder.obtainView<TextView>(R.id.tv_deliveryNo)
        val tv_userName = holder.obtainView<TextView>(R.id.tv_userName)
        val tv_nodeName = holder.obtainView<TextView>(R.id.tv_nodeName)
        val tv_date = holder.obtainView<TextView>(R.id.tv_date)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_deliveryNo.text = entity.deliveryNo
        tv_userName.text = entity.userName
        tv_nodeName.text = entity.nodeName
        tv_date.text = entity.createDate
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
//        fun onDelete(entity: ICStockBillEntry, position: Int)
    }

}
