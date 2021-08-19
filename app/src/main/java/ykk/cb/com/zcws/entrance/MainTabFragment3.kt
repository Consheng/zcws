package ykk.cb.com.zcws.entrance


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.sales.*

/**
 * 销售
 */
class MainTabFragment3 : BaseFragment() {

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item3, container, false)
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5, R.id.relative6, R.id.relative7)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.relative1 -> { // （电商）电商出库
                show(Sal_DsOutMainActivity::class.java, null)
            }
            R.id.relative2 -> { // （生产）销售出库
                show(Sal_ScOutMainActivity::class.java, null)
            }
            R.id.relative3 -> { // （电商）销售退货
                show(Sal_DsOutReturnMainActivity::class.java, null)
            }
            R.id.relative4 -> { // （内销）销售退货
                show(Sal_NxOutReturnMainActivity::class.java, null)
            }
            R.id.relative5 -> { // 电商退生产
                show(Sal_DsBToRFromPurchaseInStockMainActivity::class.java, null)
            }
            R.id.relative6 -> { // 物料发货查询
                show(SeOutStock_Sm_SearchMainActivity::class.java, null)
            }
            R.id.relative7 -> { // 发货单记录
                show(SeOutStock_Sm_RecordMainActivity::class.java, null)
            }
        }
    }
}
