package ykk.cb.com.zcws.entrance


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import butterknife.OnClick
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.comm.BaseFragment
import ykk.cb.com.zcws.produce.Mtl_SmSearchMainActivity
import ykk.cb.com.zcws.produce.Prod_ScInMainActivity
import ykk.cb.com.zcws.produce.Prod_ScInOtherMainActivity
import ykk.cb.com.zcws.warehouse.Ware_Transfer_Apply_Activity
import ykk.cb.com.zcws.warehouse.Ware_Transfer_Apply_SearchActivity

/**
 * 生产
 */
class MainTabFragment2 : BaseFragment() {

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item2, container, false)
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.relative1 -> { // 生产入库
                show(Prod_ScInMainActivity::class.java, null)
            }
            R.id.relative2 -> { // 生产入库
                show(Prod_ScInOtherMainActivity::class.java, null)
            }
            R.id.relative3 -> { // 工艺查看
                show(Mtl_SmSearchMainActivity::class.java, null)
            }
            R.id.relative4 -> { // 调拨申请
                show(Ware_Transfer_Apply_Activity::class.java, null)
            }
            R.id.relative5 -> { // 申请列表
                show(Ware_Transfer_Apply_SearchActivity::class.java, null)
            }
        }
    }
}
