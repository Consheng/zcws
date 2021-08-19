package ykk.cb.com.zcws.produce.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.ScanningRecord;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.bean.prod.ProdOrder;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;

public class Prod_ScInFragment1Adapter_20200929 extends BaseArrayRecyclerAdapter<ScanningRecord> {
    private DecimalFormat df = new DecimalFormat("#.######");
    private Activity context;
    private MyCallBack callBack;

    public Prod_ScInFragment1Adapter_20200929(Activity context, List<ScanningRecord> datas) {
        super(datas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.prod_sc_in_fragment1_item_20200929;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ScanningRecord entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_orderNo = holder.obtainView(R.id.tv_orderNo);
        TextView tv_mtlName = holder.obtainView(R.id.tv_mtlName);
        TextView tv_prodNum = holder.obtainView(R.id.tv_prodNum);
        TextView tv_nums = holder.obtainView(R.id.tv_nums);
        TextView tv_stockAP = holder.obtainView(R.id.tv_stockAP);

        // 赋值
        ProdOrder prodOrder = JsonUtil.stringToObject(entity.getSourceObj(), ProdOrder.class);
        ICItem icItem = prodOrder.getIcItem();

        tv_row.setText(String.valueOf(pos + 1));
        tv_orderNo.setText(prodOrder.getProdNo());
        tv_mtlName.setText(icItem.getFname());
        tv_prodNum.setText(df.format(entity.getSourceQty()));
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
        if(entity.getStockPos() != null) {
            tv_stockAP.setText(Html.fromHtml(entity.getStock().getFname()+"<br><font color='#6a5acd'>"+entity.getStockPos().getFname()+"</font>"));
        } else if(entity.getStock() != null) {
            tv_stockAP.setText(entity.getStock().getFname());
        } else {
            tv_stockAP.setText("");
        }

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.tv_nums: // 数量
                        if(callBack != null) {
                            callBack.onClick_num(v, entity, pos);
                        }

                        break;
                    case R.id.tv_stockAP: // 选择仓库
                        if(callBack != null) {
                            callBack.onClick_selStock(v, entity, pos);
                        }

                        break;
                }
            }
        };
        tv_nums.setOnClickListener(click);
        tv_stockAP.setOnClickListener(click);
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }

    public interface MyCallBack {
        void onClick_num(View v, ScanningRecord entity, int position);
        void onClick_selStock(View v, ScanningRecord entity, int position);
    }

}
