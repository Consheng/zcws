package ykk.cb.com.zcws.warehouse.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.k3Bean.ICStockBillEntry_K3;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;

public class Ds_PurInStockPassFragment1Adapter extends BaseArrayRecyclerAdapter<ICStockBillEntry_K3> {
    private DecimalFormat df = new DecimalFormat("#.######");
    private Activity context;
    private MyCallBack callBack;

    public Ds_PurInStockPassFragment1Adapter(Activity context, List<ICStockBillEntry_K3> datas) {
        super(datas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.ware_ds_instock_pass_fragment1_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ICStockBillEntry_K3 entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_orderNo = holder.obtainView(R.id.tv_orderNo);
        TextView tv_mtlName = holder.obtainView(R.id.tv_mtlName);
        TextView tv_nums = holder.obtainView(R.id.tv_nums);
//        TextView tv_stockAP = holder.obtainView(R.id.tv_stockAP);

        // 赋值

        tv_row.setText(String.valueOf(pos + 1));
        tv_orderNo.setText(entity.getFbillNo());
        tv_mtlName.setText(entity.getFname());
        tv_nums.setText(df.format(entity.getSumRealQty()));
        // 990156：启用批次号，990156：启用序列号
//        if(icItem.getSnManager() == 990156 || icItem.getBatchManager() == 990156) {
//            tv_nums.setEnabled(false);
//            tv_nums.setBackgroundResource(R.drawable.back_style_gray3b);
//        } else {
//            tv_nums.setEnabled(true);
//            tv_nums.setBackgroundResource(R.drawable.back_style_blue2);
//        }
//        tv_nums.setText(Html.fromHtml(df.format(entity.getUseableQty())+"<br><font color='#009900'>"+df.format(entity.getRealQty())+"</font>"));
//        if(entity.getStockPos() != null) {
//            tv_stockAP.setText(Html.fromHtml(entity.getStock().getFname()+"<br><font color='#6a5acd'>"+entity.getStockPos().getFname()+"</font>"));
//        } else if(entity.getStock() != null) {
//            tv_stockAP.setText(entity.getStock().getFname());
//        } else {
//            tv_stockAP.setText("");
//        }

//        View.OnClickListener click = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.tv_nums: // 数量
//                        if (callBack != null) {
//                            callBack.onClick_num(v, entity, pos);
//                        }
//
//                        break;
//                    case R.id.tv_stockAP: // 选择仓库
//                        if (callBack != null) {
//                            callBack.onClick_selStock(v, entity, pos);
//                        }
//
//                        break;
//                }
//            }
//        };
//        tv_nums.setOnClickListener(click);
//        tv_stockAP.setOnClickListener(click);
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }

    public interface MyCallBack {
        void onClick_num(View v, ICStockBillEntry_K3 entity, int position);

        void onClick_selStock(View v, ICStockBillEntry_K3 entity, int position);
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
