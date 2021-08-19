package ykk.cb.com.zcws.warehouse

import android.graphics.drawable.BitmapDrawable
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import butterknife.OnClick
import kotlinx.android.synthetic.main.ware_outin_stock_search_main.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.comm.BaseActivity
import ykk.cb.com.zcws.util.adapter.BaseFragmentAdapter
import java.text.DecimalFormat
import java.util.*

/**
 * 日期：2019-10-16 09:14
 * 描述：其它出入库查询
 * 作者：ykk
 */
class OutInStock_Search_MainActivity : BaseActivity() {

    val context = this
    private val TAG = "Other_OutInStockSearchActivity"
    private var curRadio: View? = null
    private var curRadioName: TextView? = null
    private val df = DecimalFormat("#.####")
    val listFragment = ArrayList<Fragment>()
    private var pageId = 0 // 上个页面传来的id
    private var billType = "QTRK" // 上个页面传来的
    var fragment1:OutInStock_Search_Fragment1_TransferPickingList? = null
    var fragment2:OutInStock_Search_Fragment2_PurReceiveInStock? = null

    override fun setLayoutResID(): Int {
        return R.layout.ware_outin_stock_search_main;
    }

    override fun initData() {
        bundle()
//        val listFragment = ArrayList<Fragment>()
//        Bundle bundle2 = new Bundle();
//        bundle2.putSerializable("customer", customer);
//        fragment1.setArguments(bundle2); // 传参数
//        fragment2.setArguments(bundle2); // 传参数

        fragment1 = OutInStock_Search_Fragment1_TransferPickingList()
        fragment2 = OutInStock_Search_Fragment2_PurReceiveInStock()

        listFragment.add(fragment1!!)
        listFragment.add(fragment2!!)

        viewPager.setScanScroll(false); // 禁止左右滑动
        //ViewPager设置适配器
        viewPager.setAdapter(BaseFragmentAdapter(supportFragmentManager, listFragment))
        //ViewPager显示第一个Fragment
        viewPager!!.setCurrentItem(pageId)
//        viewPager!!.offscreenPageLimit = 7

        //ViewPager页面切换监听
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
//                    0 -> tabChange(viewRadio1!!, tv_radioName1, "表头", 0)
//                    1 -> tabChange(viewRadio2!!, tv_radioName2, "添加分录", 1)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

    }

    private fun bundle() {
        val bundle = context.intent.extras
        if (bundle != null) {
            pageId = bundle.getInt("pageId")
            billType = bundle.getString("billType")
            when(pageId) {
                0 -> tv_billType.text = "调拨拣货单列表"
                1 -> tv_billType.text = "外购入库列表"
            }
        }
    }

    @OnClick(R.id.btn_close, R.id.tv_billType, R.id.btn_search, R.id.btn_batchUpload)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {// 关闭
                context.finish()
            }
            R.id.tv_billType -> { // 单据类型选择
                pop_billType(view)
                popWindow!!.showAsDropDown(view)
            }
            R.id.btn_search -> { // 查询
                when(pageId) {
                    0 ->  fragment1!!.findFun()
                    1 ->  fragment2!!.findFun()
                }
            }
            R.id.btn_batchUpload -> { // 批量上传
                when(pageId) {
                    0 -> fragment1!!.batchUpload()
                    1 -> fragment2!!.batchUpload()
                }
            }
        }
    }

    /**
     * 创建PopupWindow 【单据类型选择 】
     */
    private var popWindow: PopupWindow? = null
    private fun pop_billType(v: View) {
        if (null != popWindow) {//不为空就隐藏
            popWindow!!.dismiss()
            return
        }
        // 获取自定义布局文件popupwindow_left.xml的视图
        val popV = layoutInflater.inflate(R.layout.ware_outin_stock_search_popwindow, null)
        // 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
        popWindow = PopupWindow(popV, v.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        // 设置动画效果
        // popWindow.setAnimationStyle(R.style.AnimationFade);
        popWindow!!.setBackgroundDrawable(BitmapDrawable())
        popWindow!!.isOutsideTouchable = true
        popWindow!!.isFocusable = true

        popV.findViewById<View>(R.id.tv1).visibility = View.GONE
        popV.findViewById<View>(R.id.tv2).visibility = View.GONE

        if (billType.equals("ZH_DBD")) { // 仓库模块
            popV.findViewById<View>(R.id.tv1).visibility = View.VISIBLE

        } else if(billType.equals("CGSHRK")) { // 采购
            popV.findViewById<View>(R.id.tv2).visibility = View.VISIBLE

        } else if(billType.equals("SCRK")) { // 生产

        } else if(billType.equals("DS_XSCK_BTOR")) { // 销售

        }

        // 点击其他地方消失
        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv1 -> {
                    tv_billType.text = "调拨拣货单列表"
                    pageId = 0
                }
                R.id.tv2 -> {
                    tv_billType.text = "外购入库单列表"
                    pageId = 1
                }
            }
            viewPager!!.setCurrentItem(pageId)
            popWindow!!.dismiss()
        }
        popV.findViewById<View>(R.id.tv1).setOnClickListener(click)
        popV.findViewById<View>(R.id.tv2).setOnClickListener(click)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }
}