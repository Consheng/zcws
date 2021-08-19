package ykk.cb.com.zcws.sales.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.ScanningRecord;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.bean.k3Bean.ICStockBill_K3;
import ykk.cb.com.zcws.bean.k3Bean.ICStockBillEntry_K3;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;

public class Sal_NxOutReturnFragment1Adapter extends BaseArrayRecyclerAdapter<ScanningRecord> {
    private DecimalFormat df = new DecimalFormat("#.######");
    private Activity context;
    private MyCallBack callBack;

    public Sal_NxOutReturnFragment1Adapter(Activity context, List<ScanningRecord> datas) {
        super(datas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.sal_nx_out_return_fragment1_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ScanningRecord entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_orderNo = holder.obtainView(R.id.tv_orderNo);
        TextView tv_mtlName = holder.obtainView(R.id.tv_mtlName);
        TextView tv_nums = holder.obtainView(R.id.tv_nums);
        TextView tv_price = holder.obtainView(R.id.tv_price);
        TextView tv_returnReason = holder.obtainView(R.id.tv_returnReason);
        TextView tv_delRow = holder.obtainView(R.id.tv_delRow);

        // 赋值
        ICStockBillEntry_K3 stockbillentry = JsonUtil.stringToObject(entity.getSourceObj(), ICStockBillEntry_K3.class);
        ICStockBill_K3 stockBill = stockbillentry.getStockBill();
        ICItem icItem = stockbillentry.getIcItem();

        tv_row.setText(String.valueOf(pos + 1));
        tv_orderNo.setText(stockBill.getFbillno());
        tv_mtlName.setText(icItem.getFname());
        // 990156：启用批次号，990156：启用序列号
//        if(icItem.getSnManager() == 990156 || icItem.getBatchManager() == 990156) {
        if(icItem.getSnManager() == 990156) {
            tv_nums.setEnabled(false);
            tv_nums.setBackgroundResource(R.drawable.back_style_gray3b);
        } else {
            tv_nums.setEnabled(true);
            tv_nums.setBackgroundResource(R.drawable.back_style_blue2);
        }
        tv_nums.setText(Html.fromHtml(df.format(entity.getUseableQty())+"<br><font color='#009900'>"+df.format(entity.getRealQty())+"</font>"));
        tv_price.setText(df.format(entity.getPrice()));
        tv_returnReason.setText(entity.getReturnReasonName());

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.tv_nums: // 数量
                        if(callBack != null) {
                            callBack.onClick_num(v, entity, pos);
                        }

                        break;
                    case R.id.tv_price: // 退货理由
                        if(callBack != null) {
                            callBack.onClick_price(v, entity, pos);
                        }

                        break;
                    case R.id.tv_returnReason: // 退货理由
                        if(callBack != null) {
                            callBack.sel_returnReason(v, entity, pos);
                        }

                        break;
                    case R.id.tv_delRow: // 删除行
                        if(callBack != null) {
                            callBack.onClick_del(v, entity, pos);
                        }

                        break;
                }
            }
        };
        tv_nums.setOnClickListener(click);
        tv_price.setOnClickListener(click);
        tv_delRow.setOnClickListener(click);
        tv_returnReason.setOnClickListener(click);
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }

    public interface MyCallBack {
        void onClick_num(View v, ScanningRecord entity, int position);
        void onClick_price(View v, ScanningRecord entity, int position);
        void sel_returnReason(View v, ScanningRecord entity, int position);
        void onClick_del(View v, ScanningRecord entity, int position);
    }

    /*之下的方法都是为了方便操作，并不是必须的*/

    //在指定位置插入，原位置的向后移动一格
//    public boolean addItem(int position, String msg) {
//        if (position < datas.size() && position >= 0) {
//            datas.add(position, msg);
//            notifyItemInserted(position);
//            return true;
//        }
//        return false;
//    }
//
//    //去除指定位置的子项
//    public boolean removeItem(int position) {
//        if (position < datas.size() && position >= 0) {
//            datas.remove(position);
//            notifyItemRemoved(position);
//            return true;
//        }
//        return false;
//    }

    //清空显示数据
//    public void clearAll() {
//        datas.clear();
//        notifyDataSetChanged();
//    }


}
