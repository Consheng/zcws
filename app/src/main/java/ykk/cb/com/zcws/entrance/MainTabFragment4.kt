package ykk.cb.com.zcws.entrance


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.warehouse.*


/**
 * 仓库
 */
class MainTabFragment4 : BaseFragment() {

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item4, container, false)
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5, R.id.relative6, R.id.relative7, R.id.relative8, R.id.relative9)
    fun onViewClicked(view: View) {
        var bundle: Bundle? = null
        when (view.id) {
            R.id.relative1 -> { // 生产入库审核
                show(Sc_ProdInStockPassMainActivity::class.java, null)
            }
            R.id.relative2 -> { // 电商入库审核
                show(Ds_PurInStockPassMainActivity::class.java, null)
            }
            R.id.relative3 -> { // 盘点
                //                show(ICInvBackupMainActivity.class, null);
                show(ICInvBackup_MainActivity::class.java, null)
            }
            R.id.relative4 -> { // 调拨
                show(StockTransferMainActivity::class.java, null)
            }
            R.id.relative5 -> { // 其他入库
            }
            R.id.relative6 -> { // 其他出库
            }
            R.id.relative7 -> { // 调拨任务
                show(MissionBillListActivity::class.java, null)
            }
            R.id.relative8 -> { // 待上传
                bundle = Bundle()
                bundle.putInt("pageId", 0)
                bundle.putString("billType", "ZH_DBD")
                show(OutInStock_Search_MainActivity::class.java, bundle)
            }
            R.id.relative9 -> { // 物料位置移动
                show(MaterialPositionMoveActivity::class.java, bundle)
            }
        }
    }
}
