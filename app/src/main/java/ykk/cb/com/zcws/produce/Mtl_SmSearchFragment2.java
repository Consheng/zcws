package ykk.cb.com.zcws.produce;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.comm.BaseFragment;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.comm.OnItemTouchListener;
import ykk.cb.com.zcws.produce.adapter.Mtl_SmSearchFragment2Adapter;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.LogUtil;
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter;
import ykk.cb.com.zcws.util.xrecyclerview.XRecyclerView;
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity;
import ykk.cb.com.zcws.warehouse.Ware_Transfer_Apply_Activity;
import ykk.cb.com.zcws.warehouse.Ware_Transfer_Apply_Dialog;

/**
 * BOM子项查询
 */
public class Mtl_SmSearchFragment2 extends BaseFragment {

    @BindView(R.id.et_getFocus)
    EditText etGetFocus;
    @BindView(R.id.lin_focus1)
    LinearLayout linFocus1;
    @BindView(R.id.et_code)
    EditText etCode;
    @BindView(R.id.btn_submit)
    Button btn_submit;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;

    private Mtl_SmSearchFragment2 context = this;
    private static final int SUCC1 = 200, UNSUCC1 = 500;
    private static final int SETFOCUS = 1, RESULT_NUM = 2, SAOMA = 3, WRITE_CODE = 4, SEL_DB = 5;
    private Mtl_SmSearchFragment2Adapter mAdapter;
    private List<ICItem> listDatas = new ArrayList<>();
    private String barcode; // 对应的条码号
    private OkHttpClient okHttpClient = null;
    private Activity mContext;
    private Mtl_SmSearchMainActivity parent;
    private boolean isTextChange; // 是否进入TextChange事件
    private DecimalFormat df = new DecimalFormat("#.####");

    // 消息处理
    private Mtl_SmSearchFragment2.MyHandler mHandler = new Mtl_SmSearchFragment2.MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Mtl_SmSearchFragment2> mActivity;

        public MyHandler(Mtl_SmSearchFragment2 activity) {
            mActivity = new WeakReference<Mtl_SmSearchFragment2>(activity);
        }

        public void handleMessage(Message msg) {
            Mtl_SmSearchFragment2 m = mActivity.get();
            if (m != null) {
                m.hideLoadDialog();

                String errMsg = null;
                String msgObj = null;
                if(msg.obj instanceof String) {
                    msgObj = (String) msg.obj;
                }
                switch (msg.what) {
                    case SUCC1: // 扫码成功后进入
                        m.listDatas.clear();
                        List<ICItem> list = JsonUtil.strToList(msgObj, ICItem.class);
                        m.listDatas.addAll(list);
                        m.mAdapter.notifyDataSetChanged();

                        break;
                    case UNSUCC1:
                        m.listDatas.clear();
                        m.mAdapter.notifyDataSetChanged();
                        errMsg = JsonUtil.strToString((String)msg.obj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "很抱歉，没能找到数据！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SETFOCUS: // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.etGetFocus);
                        m.setFocusable(m.etCode);

                        break;
                    case SAOMA: // 扫码之后
                        String etName = m.getValues(m.etCode);
                        if (m.barcode != null && m.barcode.length() > 0) {
                            if (m.barcode.equals(etName)) {
                                m.barcode = etName;
                            } else m.barcode = etName.replaceFirst(m.barcode, "");

                        } else m.barcode = etName;
                        m.setTexts(m.etCode, m.barcode);
                        // 执行查询方法
                        m.run_smGetDatas(m.barcode);

                        break;
                }
            }
        }
    }

    @Override
    public View setLayoutResID(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.mtl_sm_search_fragment2, container, false);
    }

    @Override
    public void initView() {
        mContext = getActivity();
        parent = (Mtl_SmSearchMainActivity) mContext;

        xRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        xRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new Mtl_SmSearchFragment2Adapter(mContext, listDatas);
        xRecyclerView.setAdapter(mAdapter);
        // 设值listview空间失去焦点
        xRecyclerView.setFocusable(false);
//        xRecyclerView.setLoadingListener(this);

        xRecyclerView.setPullRefreshEnabled(false); // 上啦刷新禁用
        xRecyclerView.setLoadingMoreEnabled(false); // 不显示下拉刷新的view

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int pos) {
                ICItem icItem = listDatas.get(pos-1);
                if(icItem.isCheck()) {
                    icItem.setCheck(false);
                } else {
                    icItem.setCheck(true);
                }
                boolean isBool = false;
                for(ICItem m : listDatas) {
                    if(m.isCheck()) {
                        isBool = true;
                        break;
                    }
                }
                if(isBool) {
                    btn_submit.setVisibility(View.VISIBLE);
                } else {
                    btn_submit.setVisibility(View.GONE);
                }
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void initData() {
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(30, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(30, TimeUnit.SECONDS) //设置读取超时时间
                    .build();
        }

        hideSoftInputMode(mContext, etCode);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogUtil.e("setUserVisibleHint", "冒泡麻婆。。。。。");
        if(isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
    }

    @OnClick({R.id.btn_scan, R.id.btn_submit })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_scan: // 调用摄像头扫描（物料）
                showForResult(CaptureActivity.class, CAMERA_SCAN, null);

                break;
            case R.id.btn_submit: // 提交
                StringBuffer strMtlId = new StringBuffer();
                for(ICItem m : listDatas) {
                    if(m.isCheck()) {
                        strMtlId.append(m.getFitemid()+",");
                    }
                }
                strMtlId.delete(strMtlId.length()-1,strMtlId.length()); // 删除最后一个，号

                Bundle bundle = new Bundle();
                bundle.putString("strMtlId", strMtlId.toString());
                show(Ware_Transfer_Apply_Dialog.class, bundle);

                break;
        }

    }

    @Override
    public void setListener() {
        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocusable(etGetFocus);
                switch (v.getId()) {
                    case R.id.et_code:
                        setFocusable(etCode);
                        break;
                }
            }
        };
        etCode.setOnClickListener(click);

        // 生产条码
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) return;
                if(!isTextChange) {
                    isTextChange = true;
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300);
                }
            }
        });

        // 长按输入条码
        etCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showInputDialog("输入条码", "", "none", WRITE_CODE);
                return true;
            }
        });

        etCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    linFocus1.setBackgroundResource(R.drawable.back_style_red_focus);
                } else {
                    if(linFocus1 != null) {
                        linFocus1.setBackgroundResource(R.drawable.back_style_gray4);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case WRITE_CODE: // 输入条码返回
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        etCode.setText(value.toUpperCase());
                    }
                }

                break;
            case CAMERA_SCAN: // 扫一扫成功  返回
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String code = bundle.getString(DECODED_CONTENT_KEY, "");
                        setTexts(etCode, code);
                    }
                }

                break;
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 300);
    }

    /**
     * 扫码查询对应的方法
     */
    private void run_smGetDatas(String val) {
        isTextChange = false;
        if(val.length() == 0) {
            Comm.showWarnDialog(mContext,"请对准条码！");
            return;
        }
        showLoadDialog("加载中...",false);
        String mUrl = getURL("bomInventory/getProdRawMaterialFromSC_app");
        String barcode = this.barcode;
        FormBody formBody = new FormBody.Builder()
                .add("barcode", barcode)
                .build();

        Request request = new Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(UNSUCC1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String result = body.string();
                LogUtil.e("run_smGetDatas --> onResponse", result);
                if (!JsonUtil.isSuccess(result)) {
                    Message msg = mHandler.obtainMessage(UNSUCC1, result);
                    mHandler.sendMessage(msg);
                    return;
                }
                Message msg = mHandler.obtainMessage(SUCC1, result);
                mHandler.sendMessage(msg);
            }
        });
    }

    @Override
    public void onDestroyView() {
        closeHandler(mHandler);
        mBinder.unbind();
        super.onDestroyView();
    }

}
