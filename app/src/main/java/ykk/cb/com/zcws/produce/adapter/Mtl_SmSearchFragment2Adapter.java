package ykk.cb.com.zcws.produce.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;

public class Mtl_SmSearchFragment2Adapter extends BaseArrayRecyclerAdapter<ICItem> {
    private DecimalFormat df = new DecimalFormat("#.######");
    private Activity context;
//    private MyCallBack callBack;

    public Mtl_SmSearchFragment2Adapter(Activity context, List<ICItem> datas) {
        super(datas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.mtl_sm_search_fragment2_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ICItem entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_mtlNumber = holder.obtainView(R.id.tv_mtlNumber);
        TextView tv_mtlName = holder.obtainView(R.id.tv_mtlName);
        TextView tv_fLowLimit = holder.obtainView(R.id.tv_fLowLimit);
        TextView tv_fsecinv = holder.obtainView(R.id.tv_fsecinv);
        TextView tv_fhighlimit = holder.obtainView(R.id.tv_fhighlimit);

        // 赋值
        tv_row.setText(String.valueOf(pos + 1));
        tv_mtlNumber.setText(entity.getFnumber());
        tv_mtlName.setText(entity.getFname());
        tv_fLowLimit.setText(df.format(entity.getFlowlimit()));
        tv_fsecinv.setText(df.format(entity.getFsecinv()));
        tv_fhighlimit.setText(df.format(entity.getFhighlimit()));

        View view = (View) tv_row.getParent();
        if(entity.isCheck()) {
            view.setBackgroundResource(R.drawable.back_style_check1_true);
        } else {
            view.setBackgroundResource(R.drawable.back_style_check1_false);
        }
    }

//    public void setCallBack(MyCallBack callBack) {
//        this.callBack = callBack;
//    }

//    public interface MyCallBack {
//        void onClick_num(View v, ICItem entity, int position);
//    }

}
