package ykk.cb.com.zcws.warehouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
import ykk.cb.com.zcws.bean.k3Bean.ICInvBackup;
import ykk.cb.com.zcws.comm.BaseDialogActivity;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter;
import ykk.cb.com.zcws.util.xrecyclerview.XRecyclerView;
import ykk.cb.com.zcws.warehouse.adapter.ICInvBackup_Material_DialogAdapter;

/**
 * 选择部门dialog
 */
public class ICInvBackup_Material_DialogActivity extends BaseDialogActivity implements XRecyclerView.LoadingListener {

    @BindView(R.id.btn_close)
    Button btnClose;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.btn_search)
    Button btnSearch;
    private ICInvBackup_Material_DialogActivity context = this;
    private static final int SUCC1 = 200, UNSUCC1 = 501;
    private List<ICInvBackup> listDatas = new ArrayList<>();
    private ICInvBackup_Material_DialogAdapter mAdapter;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private int limit = 1;
    private boolean isRefresh, isLoadMore, isNextPage;
    private int finterId; // 方案id

    // 消息处理
    private MyHandler mHandler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<ICInvBackup_Material_DialogActivity> mActivity;

        public MyHandler(ICInvBackup_Material_DialogActivity activity) {
            mActivity = new WeakReference<ICInvBackup_Material_DialogActivity>(activity);
        }

        public void handleMessage(Message msg) {
            ICInvBackup_Material_DialogActivity m = mActivity.get();
            if (m != null) {
                m.hideLoadDialog();
                switch (msg.what) {
                    case SUCC1: // 成功
                        List<ICInvBackup> list = JsonUtil.strToList2((String) msg.obj, ICInvBackup.class);
                        m.listDatas.addAll(list);
                        m.mAdapter.notifyDataSetChanged();

                        if (m.isRefresh) {
                            m.xRecyclerView.refreshComplete(true);
                        } else if (m.isLoadMore) {
                            m.xRecyclerView.loadMoreComplete(true);
                        }

                        m.xRecyclerView.setLoadingMoreEnabled(m.isNextPage);

                        break;
                    case UNSUCC1: // 数据加载失败！
                        m.mAdapter.notifyDataSetChanged();
                        m.toasts("抱歉，没有加载到数据！");

                        break;
                }
            }
        }

    }

    @Override
    public int setLayoutResID() {
        return R.layout.ware_icinvbackup_material_dialog;
    }

    @Override
    public void initView() {
        xRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        xRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new ICInvBackup_Material_DialogAdapter(context, listDatas);
        xRecyclerView.setAdapter(mAdapter);
        xRecyclerView.setLoadingListener(context);

        xRecyclerView.setPullRefreshEnabled(false); // 上啦刷新禁用
//        xRecyclerView.setLoadingMoreEnabled(false); // 不显示下拉刷新的view

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int pos) {
                ICInvBackup m = listDatas.get(pos-1);
                boolean isCheck = m.isCheck();
                if(isCheck) {
                    m.setCheck(false);
                } else {
                    m.setCheck(true);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void initData() {
        Bundle bundle = context.getIntent().getExtras();
        if(bundle != null) {
            finterId = bundle.getInt("finterId");
        }

        initLoadDatas();
    }


    // 监听事件
    @OnClick({R.id.btn_close, R.id.btn_search, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                closeHandler(mHandler);
                context.finish();

                break;
            case R.id.btn_search:
                initLoadDatas();

                break;
            case R.id.btn_confirm: // 确认
                int size = listDatas.size();
                if(size == 0) {
                    Comm.showWarnDialog(context,"请查询数据！");
                    return;
                }
                List<ICInvBackup> list = new ArrayList<>();
                for(int i=0; i<size; i++) {
                    ICInvBackup mtl = listDatas.get(i);
                    if(mtl.isCheck()) {
                        list.add(mtl);
                    }
                }
                if(list.size() == 0) {
                    Comm.showWarnDialog(context,"请至少选择一行数据！");
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("obj", (Serializable) list);
                context.setResult(RESULT_OK, intent);
                context.finish();

                break;
        }
    }

    private void initLoadDatas() {
        limit = 1;
        listDatas.clear();
        run_okhttpDatas();
    }

    /**
     * 通过okhttp加载数据
     */
    private void run_okhttpDatas() {
        showLoadDialog("加载中...",false);
        String mUrl = getURL("icInvBackup/findMtlList");
        FormBody formBody = new FormBody.Builder()
                .add("fNumberAndName", getValues(etSearch).trim())
                .add("finterId", finterId > 0 ? String.valueOf(finterId) : "") // 方案id
                .add("limit", String.valueOf(limit))
                .add("pageSize", "50")
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
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNSUCC1);
                    return;
                }
                isNextPage = JsonUtil.isNextPage(result);

                Message msg = mHandler.obtainMessage(SUCC1, result);
                Log.e("Dept_DialogActivity --> onResponse", result);
                mHandler.sendMessage(msg);
            }
        });
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        initLoadDatas();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        limit += 1;
        run_okhttpDatas();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeHandler(mHandler);
            context.finish();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        closeHandler(mHandler);
        super.onDestroy();
    }
}
