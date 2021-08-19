package ykk.cb.com.zcws.basics.adapter;

import android.app.Activity;
import android.widget.TextView;

import java.util.List;

import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.Supplier;
import ykk.cb.com.zcws.util.basehelper.BaseArrayRecyclerAdapter;
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter;

public class Supplier_DialogAdapter extends BaseArrayRecyclerAdapter<Supplier> {

    private Activity context;
    private MyCallBack callBack;
    private List<Supplier> datas;

    public Supplier_DialogAdapter(Activity context, List<Supplier> datas) {
        super(datas);
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.ab_supplier_dialog_item;
    }

    @Override
    public void onBindHoder(BaseRecyclerAdapter.RecyclerHolder holder, Supplier entity, final int pos) {
        // 初始化id
        TextView tv_row = holder.obtainView(R.id.tv_row);
        TextView tv_fnumber = holder.obtainView(R.id.tv_fnumber);
        TextView tv_fname = holder.obtainView(R.id.tv_fname);
        // 赋值
        tv_row.setText(String.valueOf(pos + 1));
        tv_fnumber.setText(entity.getFnumber());
        tv_fname.setText(entity.getFname());
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }

    public interface MyCallBack {
        void onClick(Supplier entity, int position);
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
