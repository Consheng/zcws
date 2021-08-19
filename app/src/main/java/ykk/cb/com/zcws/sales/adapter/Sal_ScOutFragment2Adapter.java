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
import ykk.cb.com.zcws.bean.k3Bean.SeoutStock;
import ykk.cb.com.zcws.bean.k3Bean.SeoutStockEntry;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;

public class Sal_ScOutFragment2Adapter extends BaseArrayRecyclerAdapter<ScanningRecord> {
    private DecimalFormat df = new DecimalFormat("#.######");
    private Activity context;
    private MyCallBack callBack;

    public Sal_ScOutFragment2Adapter(Activity context, List<ScanningRecord> datas) {
        super(datas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.sal_sc_out_fragment2_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ScanningRecord entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_custName = holder.obtainView(R.id.tv_custName);
        TextView tv_mtlName = holder.obtainView(R.id.tv_mtlName);
        TextView tv_deliNum = holder.obtainView(R.id.tv_deliNum);
        TextView tv_nums = holder.obtainView(R.id.tv_nums);
        TextView tv_stockAP = holder.obtainView(R.id.tv_stockAP);

        // 赋值
        SeoutStockEntry seoutStockEntry = JsonUtil.stringToObject(entity.getSourceObj(), SeoutStockEntry.class);
        SeoutStock seoutStock = seoutStockEntry.getSeOutStock();
        ICItem icItem = seoutStockEntry.getIcItem();

        tv_row.setText(String.valueOf(pos + 1));
        tv_custName.setText(Comm.isNULLS(entity.getCustName()));
        tv_mtlName.setText(icItem.getFname());
        tv_deliNum.setText(df.format(entity.getSourceQty()));
        // 990156：启用批次号，990156：启用序列号
        //        if(icItem.getSnManager() == 990156 || icItem.getBatchManager() == 990156) {
        if(icItem.getSnManager() == 990156) {
            tv_nums.setEnabled(false);
            tv_nums.setBackgroundResource(R.drawable.back_style_gray3b);
        } else {
            tv_nums.setEnabled(true);
            tv_nums.setBackgroundResource(R.drawable.back_style_blue2);
        }
        tv_nums.setText(Html.fromHtml(df.format(entity.getUseableQty()) + "<br><font color='#009900'>" + df.format(entity.getRealQty()) + "</font>"));
        if (entity.getStock() != null) {
            tv_stockAP.setText(entity.getStock().getFname());
        } else {
            tv_stockAP.setText("");
        }

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_nums: // 数量
                        if (callBack != null) {
                            callBack.onClick_num(v, entity, pos);
                        }

                        break;
                }
            }
        };
        tv_nums.setOnClickListener(click);
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }

    public interface MyCallBack {
        void onClick_num(View v, ScanningRecord entity, int position);
    }

}

