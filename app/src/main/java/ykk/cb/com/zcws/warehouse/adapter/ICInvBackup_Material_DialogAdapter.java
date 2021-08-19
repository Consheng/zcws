package ykk.cb.com.zcws.warehouse.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.k3Bean.ICInvBackup;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;

public class ICInvBackup_Material_DialogAdapter extends BaseArrayRecyclerAdapter<ICInvBackup> {

    private Activity context;
    private MyCallBack callBack;
    private List<ICInvBackup> datas;

    public ICInvBackup_Material_DialogAdapter(Activity context, List<ICInvBackup> datas) {
        super(datas);
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.ware_icinvbackup_material_dialog_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, ICInvBackup entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_fnumber = holder.obtainView(R.id.tv_fnumber);
        TextView tv_fname = holder.obtainView(R.id.tv_fname);
        // 赋值
        tv_row.setText(String.valueOf(pos + 1));
        tv_fnumber.setText(entity.getIcItem().getFnumber());
        tv_fname.setText(entity.getIcItem().getFname());

        View view = (View) tv_row.getParent();
        if(entity.isCheck()) {
            view.setBackgroundResource(R.drawable.back_style_check1_true);
        } else {
            view.setBackgroundResource(R.drawable.back_style_check1_false);
        }
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }

    public interface MyCallBack {
        void onClick(ICInvBackup entity, int position);
    }

}
