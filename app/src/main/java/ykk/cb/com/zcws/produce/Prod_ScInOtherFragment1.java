package ykk.cb.com.zcws.produce;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
import ykk.cb.com.zcws.basics.Dept_DialogActivity;
import ykk.cb.com.zcws.basics.StockPos_DialogActivity;
import ykk.cb.com.zcws.basics.Stock_DialogActivity;
import ykk.cb.com.zcws.bean.Department;
import ykk.cb.com.zcws.bean.Organization;
import ykk.cb.com.zcws.bean.ScanningRecord;
import ykk.cb.com.zcws.bean.Stock;
import ykk.cb.com.zcws.bean.StockPosition;
import ykk.cb.com.zcws.bean.User;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.bean.prod.ProdOrder;
import ykk.cb.com.zcws.comm.BaseFragment;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.produce.adapter.Prod_ScInOtherFragment1Adapter;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.LogUtil;
import ykk.cb.com.zcws.util.basehelper.BaseRecyclerAdapter;

/**
 * 生产任务单入库（选单入库）
 */
public class Prod_ScInOtherFragment1 extends BaseFragment {

    @BindView(R.id.tv_deptSel)
    TextView tvDeptSel;
    @BindView(R.id.tv_dateSel)
    TextView tvDateSel;
    @BindView(R.id.et_prodNo)
    EditText etProdNo;
    @BindView(R.id.tv_stockSel)
    TextView tvStockSel;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.cbAll)
    CheckBox cbAll;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.btn_pass)
    Button btnPass;

    private Prod_ScInOtherFragment1 context = this;
    private static final int SEL_STOCK1 = 10, SEL_STOCK = 11, SEL_STOCKPOS = 12, SEL_DEPT = 13;
    private static final int SUCC1 = 200, UNSUCC1 = 500, SUCC2 = 201, UNSUCC2 = 501, SUCC3 = 202, UNSUCC3 = 502, PASS = 203, UNPASS = 503;
    private static final int RESULT_NUM = 1, DELAYED_CLICK = 2;
    private Prod_ScInOtherFragment1Adapter mAdapter;
    private List<ScanningRecord> checkDatas = new ArrayList<>();
    private Stock stock1, stock;
    private StockPosition stockPos;
    private Department department; // 部门
    private int curPos; // 当前行
    private OkHttpClient okHttpClient = null;
    private User user;
    private Activity mContext;
    private Prod_ScInOtherMainActivity parent;
    private String strK3Number; // 保存k3返回的单号
    private boolean isAllSM; // 是否全部扫完条码
    private String timesTamp; // 时间戳
    private boolean isClickButton; // 是否点击了按钮

    // 消息处理
    private Prod_ScInOtherFragment1.MyHandler mHandler = new Prod_ScInOtherFragment1.MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Prod_ScInOtherFragment1> mActivity;

        public MyHandler(Prod_ScInOtherFragment1 activity) {
            mActivity = new WeakReference<Prod_ScInOtherFragment1>(activity);
        }

        public void handleMessage(Message msg) {
            Prod_ScInOtherFragment1 m = mActivity.get();
            if (m != null) {
                m.hideLoadDialog();

                String errMsg = null;
                String msgObj = null;
                if(msg.obj instanceof String) {
                    msgObj = (String) msg.obj;
                }
                switch (msg.what) {
                    case SUCC1:
                        m.strK3Number = JsonUtil.strToString(msgObj);

//                        m.btnSave.setVisibility(View.GONE);
//                        m.btnPass.setVisibility(View.VISIBLE);
//                        Comm.showWarnDialog(m.mContext,"保存成功，请点击“审核按钮”！");
                        m.reset(false);
                        if(!m.isAllSM) {
                            m.run_smGetDatas();
                        } else {
                            m.mAdapter.notifyDataSetChanged();
                        }
                        Comm.showWarnDialog(m.mContext,"保存成功✔");

                        break;
                    case UNSUCC1:
                        errMsg = JsonUtil.strToString(msgObj);
                        if(Comm.isNULLS(errMsg).length() == 0) errMsg = "服务器繁忙，请稍候再试！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case PASS: // 审核成功 返回
                        m.reset(false);
                        if(!m.isAllSM) m.run_smGetDatas();

                        Comm.showWarnDialog(m.mContext,"审核成功✔");

                        break;
                    case UNPASS: // 审核失败 返回
                        errMsg = JsonUtil.strToString(msgObj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "审核失败！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SUCC2: // 扫码成功后进入
                        List<ProdOrder> list = JsonUtil.strToList(msgObj, ProdOrder.class);
                        m.parent.isChange = true;
                        m.getScanAfterData_1(list);

                        break;
                    case UNSUCC2:
                        m.mAdapter.notifyDataSetChanged();
                        errMsg = JsonUtil.strToString(msgObj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "很抱歉，没能找到数据！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SUCC3: // 判断是否存在返回
                        m.run_save();

                        break;
                    case UNSUCC3: // 判断是否存在返回
                        m.run_save();

                        break;
                    case DELAYED_CLICK: // 延时进入点击后的操作
                        View btnView = (View) msg.obj;
                        m.btnClickAfter(btnView);

                        break;
                }
            }
        }
    }

    @Override
    public View setLayoutResID(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.prod_sc_in_other_fragment1, container, false);
    }

    @Override
    public void initView() {
        mContext = getActivity();
        parent = (Prod_ScInOtherMainActivity) mContext;

        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new Prod_ScInOtherFragment1Adapter(mContext, checkDatas);
        recyclerView.setAdapter(mAdapter);
        // 设值listview空间失去焦点
        recyclerView.setFocusable(false);
        mAdapter.setCallBack(new Prod_ScInOtherFragment1Adapter.MyCallBack() {
            @Override
            public void onClick_num(View v, ScanningRecord entity, int position) {
                Log.e("num", "行：" + position);
                curPos = position;
                showInputDialog("数量", String.valueOf(entity.getRealQty()), "0.0", RESULT_NUM);
            }

//            @Override
//            public void onClick_selStock(View v, ScanningRecord entity, int position) {
//                curPos = position;
//                showForResult(Stock_DialogActivity.class, SEL_STOCK, null);
//            }

        });

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int pos) {
                ScanningRecord sr = checkDatas.get(pos);
                sr.setIsCheck(sr.getIsCheck() == 0 ? 1 : 0);
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

        getUserInfo();
        tvDateSel.setText(Comm.getSysDate(7));
        timesTamp = user.getId()+"-"+Comm.randomUUID();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
//            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isClickButton = true;
    }

    @OnClick({R.id.tv_deptSel, R.id.tv_dateSel, R.id.tv_stockSel, R.id.btn_save, R.id.btn_pass })
    public void onViewClicked(View view) {
        if(isClickButton && view.getId() == R.id.btn_save) {
            isClickButton = false;
            view.setEnabled(false);
            view.setClickable(false);
            showLoadDialog("稍等哈...",false);

            Message msgView = mHandler.obtainMessage(DELAYED_CLICK, view);
            mHandler.sendMessageDelayed(msgView,1000);
        } else {
            btnClickAfter(view);
        }
    }

    /**
     * 查询的方法
     */
    public void findFun() {
        if(checkDatas != null && checkDatas.size() > 0) {
            Comm.showWarnDialog(mContext,"请先保存当前行数据！");
            return;
        }
        run_smGetDatas();
    }

    private void btnClickAfter(View view) {
        hideLoadDialog();
        isClickButton = true;
        view.setEnabled(true);
        view.setClickable(true);

        Bundle bundle = null;
        switch (view.getId()) {
            case R.id.tv_deptSel: // 选择生产车间
                bundle = new Bundle();
                bundle.putInt("departmentProperty", 1070); // 属性：1070-车间,1071-非车间
                showForResult(Dept_DialogActivity.class, SEL_DEPT, bundle);

                break;
            case R.id.tv_dateSel: // 日期
                Comm.showDateDialog(mContext, view, 0);

                break;
            case R.id.tv_stockSel: // 选择仓库
                showForResult(Stock_DialogActivity.class, SEL_STOCK1, null);

                break;
            case R.id.btn_save: // 保存
//                hideKeyboard(mContext.getCurrentFocus());
                if(!saveBefore()) {
                    return;
                }
//                run_findInStockSum();
                run_save();

                break;
            case R.id.btn_pass: // 审核
                if(strK3Number == null) {
                    Comm.showWarnDialog(mContext,"请先保存数据！");
                    return;
                }
                run_passSC();

                break;
        }
    }

    /**
     * 选择保存之前的判断
     */
    private boolean saveBefore() {
        if (checkDatas == null || checkDatas.size() == 0) {
            Comm.showWarnDialog(mContext,"请先查询数据！");
            return false;
        }

        // 检查数据
        for (int i = 0, size = checkDatas.size(); i < size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            if (sr.getIsCheck() == 1 && isNULLS(sr.getStockName()).length() == 0) {
                Comm.showWarnDialog(mContext,"第" + (i + 1) + "行，请选择（仓库）！");
                return false;
            }
            if (sr.getIsCheck() == 1 && sr.getRealQty() > sr.getUseableQty()) {
                Comm.showWarnDialog(mContext,"第" + (i + 1) + "行（入库数）不能大于（生产数）！");
                return false;
            }
        }
        return true;
    }

    @Override
    public void setListener() {
        cbAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int dataSize = checkDatas.size();
                if (dataSize > 0) {
                    for (int i = 0; i < dataSize; i++) {
                        checkDatas.get(i).setIsCheck(isChecked ? 1 : 0);
                    }
                    cbAll.setText(isChecked ? "反选" : "全选");
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void reset(boolean isRefresh) {
        timesTamp = user.getId()+"-"+Comm.randomUUID();
        strK3Number = null;
        btnSave.setVisibility(View.VISIBLE);
        btnPass.setVisibility(View.GONE);
        checkDatas.clear();
        stock1 = null;
        tvStockSel.setText("");

        if(isRefresh) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SEL_STOCK1: //选择仓库	返回
                if (resultCode == Activity.RESULT_OK) {
                    stock1 = (Stock) data.getSerializableExtra("obj");
                    if(checkDatas != null && checkDatas.size() > 0) {
                        for(int i=0; i<checkDatas.size(); i++) {
                            ScanningRecord sr2 = checkDatas.get(i);
                            sr2.setStock(stock1);
                            sr2.setStockNumber(stock1.getFnumber());
                            sr2.setStockName(stock1.getFname());
                            sr2.setStockPositionNumber("");
                            sr2.setStockPositionName("");
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    tvStockSel.setText(stock1.getFname());
                }

                break;
            case SEL_STOCK: //行事件选择仓库	返回
                if (resultCode == Activity.RESULT_OK) {
                    stock = (Stock) data.getSerializableExtra("obj");
                    // 启用了库位管理
                    if (stock.getFisStockMgr() == 1) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("fspGroupId", stock.getFspGroupId());
                        showForResult(StockPos_DialogActivity.class, SEL_STOCKPOS, bundle);
                    } else {
                        stockAllFill(false);
                    }
                }

                break;
            case SEL_STOCKPOS: //行事件选择库位	返回
                if (resultCode == Activity.RESULT_OK) {
                    stockPos = (StockPosition) data.getSerializableExtra("obj");
                    LogUtil.e("onActivityResult --> SEL_STOCKP", stockPos.getFname());
                    stockAllFill(true);
                }

                break;
            case RESULT_NUM: // 数量
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        double num = parseDouble(value);
                        checkDatas.get(curPos).setRealQty(num);
                        checkDatas.get(curPos).setIsCheck(1);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                break;
            case SEL_DEPT: //查询部门	返回
                if (resultCode == Activity.RESULT_OK) {
                    department = (Department) data.getSerializableExtra("obj");
                    LogUtil.e("onActivityResult --> SEL_DEPT", department.getDepartmentName());
                    tvDeptSel.setText(department.getDepartmentName());
                }

                break;
        }
    }

    /**
     * 仓库数据全部填充
     */
    private void stockAllFill(boolean inStockPosData) {
        int size = checkDatas.size();
        boolean isBool = false;
        for(int i=0; i<size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            if(isNULLS(sr.getStockNumber()).length() > 0) {
                isBool = true;
                break;
            }
        }
//        if(isBool) {
        ScanningRecord sr2 = checkDatas.get(curPos);
        sr2.setStockNumber(stock.getFnumber());
        sr2.setStockName(stock.getFname());
        sr2.setStock(stock);
        if(inStockPosData) {
            sr2.setStockPos(stockPos);
            sr2.setStockPositionNumber(stockPos.getFnumber());
            sr2.setStockPositionName(stockPos.getFname());
        }
//        } else { // 全部都为空的时候，选择任意全部填充
//            for (int i = 0; i < size; i++) {
//                ScanningRecord2 sr2 = checkDatas.get(i);
//                sr2.setStockId(stock2.getfStockid());
//                sr2.setStockFnumber(stock2.getfNumber());
//                sr2.setStockName(stock2.getfName());
//                sr2.setStock(stock2);
//                if(inStockPosData) {
//                    sr2.setStockPos(stockP2);
//                    sr2.setStockPositionId(stockP2.getId());
//                    sr2.setStockPName(stockP2.getFname());
//                }
//            }
//        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 得到生产条码号扫码的数据
     */
    private void getScanAfterData_1(List<ProdOrder> list) {
        int size = list.size();
        for(int i=0; i<size; i++) {
            ProdOrder prodOrder = list.get(i);
            ICItem icItem = prodOrder.getIcItem();
            ScanningRecord sr = new ScanningRecord();

            sr.setType(14); // 1：电商销售出库，10：生产产品入库，11：发货通知单销售出库，12：电商销售退货，13：电商外购入库，14：生产产品入库(选单入库)
            sr.setSourceId(prodOrder.getProdId());
            sr.setSourceNumber(prodOrder.getProdNo());
            sr.setSourceEntryId(prodOrder.getProdId());
//        sr.setExpressNo(barcode);
            sr.setIcItemId(icItem.getFitemid());
            sr.setIcItemNumber(icItem.getFnumber());
            sr.setIcItemName(icItem.getFname());
            Organization cust = prodOrder.getCust();
            if(cust != null) {
                sr.setCustNumber(cust.getfNumber());
                sr.setCustName(cust.getfName());
            }
            Department department = prodOrder.getDepartment();
            if(department != null) {
                sr.setDeptNumber(department.getDepartmentNumber());
                sr.setDeptName(department.getDepartmentName());
            }
            // 用物料默认的仓库
            Stock stock = icItem.getStock();
            if(stock != null) {
                sr.setStock(stock);
                sr.setStockNumber(stock.getFnumber());
                sr.setStockName(stock.getFname());
            } else {
                Stock stock2 = new Stock();
                stock2.setFitemId(254);
                stock2.setFnumber("CC.01.01");
                stock2.setFname("忠诚卫士成品仓");

                sr.setStock(stock2);
                sr.setStockNumber(stock2.getFnumber());
                sr.setStockName(stock2.getFname());
            }
            // 默认的仓位
            StockPosition stockPos = icItem.getStockPos();
            if(stockPos != null && stockPos.getFspId() > 0) {
                sr.setStockPos(stockPos);
                sr.setStockPositionNumber(stockPos.getFnumber());
                sr.setStockPositionName(stockPos.getFname());
            }

            // 用选择的仓库
//            if(stock1 != null) {
//                sr.setStock(stock1);
//                sr.setStockNumber(stock1.getFnumber());
//                sr.setStockName(stock1.getFname());
//
//            } else if(prodOrder.getGoodsType() == 990169) { // 990168代表非定制，990169代表定制
//                Stock stock = new Stock();
//                stock.setFitemId(38263);
//                stock.setFnumber("CC.01.05");
//                stock.setFname("定制产品仓");
//
//                sr.setStock(stock);
//                sr.setStockNumber(stock.getFnumber());
//                sr.setStockName(stock.getFname());
//
//            } else { // 990168代表非定制
//                Stock stock = new Stock();
//                stock.setFitemId(254);
//                stock.setFnumber("CC.01.01");
//                stock.setFname("忠诚卫士成品仓");
//
//                sr.setStock(stock);
//                sr.setStockNumber(stock.getFnumber());
//                sr.setStockName(stock.getFname());
//            }

            sr.setDeliveryWay("");
            sr.setSourceQty(prodOrder.getFqty());
            sr.setUseableQty(prodOrder.getUseableQty());
//        sr.setRealQty(1);
            sr.setPrice(0);
            sr.setCreateUserId(user.getId());
            sr.setEmpId(user.getEmpId());
            sr.setCreateUserName(user.getUsername());
            sr.setDataTypeFlag("APP");
            sr.setTempTimesTamp(timesTamp);
            sr.setSourceObj(JsonUtil.objectToString(prodOrder));

            // 启用序列号，批次号；    990156：启用批次号，990156：启用序列号
            if(icItem.getSnManager() == 990156 || icItem.getBatchManager() == 990156) {
                sr.setStrBarcodes("");
                sr.setIsUniqueness('Y');
                sr.setRealQty(sr.getUseableQty());
            } else { // 未启用序列号， 批次号
                sr.setRealQty(sr.getUseableQty());
                sr.setIsUniqueness('N');
                // 不存在条码，就加入
                sr.setStrBarcodes("");
            }

            checkDatas.add(sr);
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * 保存方法
     */
    private void run_save() {
        List<ScanningRecord> listRecord = new ArrayList<>();
        boolean isUnChecked = true; // 是否全部未选中
        int count = 0;
        int size = checkDatas.size();
        for(int i=0; i<size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            if(sr.getIsCheck() == 1) isUnChecked = false;

            if(sr.getIsCheck() == 1 && sr.getRealQty() > 0) {
                listRecord.add(sr);
            }
            // 判断没行是否扫完数量
            if(sr.getRealQty() >= sr.getUseableQty()) {
                count += 1;
            }
        }
        if(isUnChecked) {
            Comm.showWarnDialog(mContext,"请选中要入库的行！");
            return;
        }
        if(listRecord.size() == 0) {
            Comm.showWarnDialog(mContext,"请至少输入一行数量！");
            return;
        }
        // 判断是否全部扫完
        if(size == count) isAllSM = true;
        else isAllSM = false;

        showLoadDialog("保存中...",false);
        String mJson = JsonUtil.objectToString(listRecord);
        FormBody formBody = new FormBody.Builder()
                .add("strJson", mJson)
                .build();

        String mUrl = getURL("scanningRecord/addScanningRecord");
        final Request request = new Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
//                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(UNSUCC1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String result = body.string();
                LogUtil.e("run_save --> onResponse", result);
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

    /**
     * 扫码查询对应的方法
     */
    private void run_smGetDatas() {
        showLoadDialog("加载中...",false);
        String mUrl = getURL("prodOrder/findProdOrderInStock");
        String prodNo = getValues(etProdNo).trim();
        String date = getValues(tvDateSel);
        FormBody formBody = new FormBody.Builder()
                .add("fbillNo", prodNo) // 生产单号
                .add("deptNumber", department != null ? department.getDepartmentNumber() : "")
                .add("prodFdateBeg", date) // 开始日期
                .add("prodFdateEnd", date) // 结束日期
                .add("prodStatus", "1") // 0：计划，1：下达，3：结案
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
                mHandler.sendEmptyMessage(UNSUCC2);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String result = body.string();
                LogUtil.e("run_smGetDatas --> onResponse", result);
                if (!JsonUtil.isSuccess(result)) {
                    Message msg = mHandler.obtainMessage(UNSUCC2, result);
                    mHandler.sendMessage(msg);
                    return;
                }
                Message msg = mHandler.obtainMessage(SUCC2, result);
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     * 判断表中存在该物料
     */
    private void run_findInStockSum() {
        showLoadDialog("加载中...",false);
        StringBuilder strFbillno = new StringBuilder();
        StringBuilder strEntryId = new StringBuilder();
        for (int i = 0, size = checkDatas.size(); i < size; i++) {
//            ScanningRecord2 sr2 = checkDatas.get(i);
//            if((i+1) == size) {
//                strFbillno.append(sr2.getPoFbillno());
//                strEntryId.append(sr2.getEntryId());
//            } else {
//                strFbillno.append(sr2.getPoFbillno() + ",");
//                strEntryId.append(sr2.getEntryId() + ",");
//            }
        }
        String mUrl = getURL("scanningRecord/findInStockSum");
        FormBody formBody = new FormBody.Builder()
                .add("fbillType", "4") // fbillType  1：采购订单入库，2：收料任务单入库，3：生产订单入库，4：销售订单出库，5：发货通知单出库
                .add("strFbillno", strFbillno.toString())
                .add("strEntryId", strEntryId.toString())
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
                mHandler.sendEmptyMessage(UNSUCC3);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String result = body.string();
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNSUCC3);
                    return;
                }
                Message msg = mHandler.obtainMessage(SUCC3, result);
                Log.e("run_findInStockSum --> onResponse", result);
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     * 生产账号审核
     */
    private void run_passSC() {
        showLoadDialog("正在审核...",false);
        String mUrl = getURL("stockBill/passSC");
        getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("strFbillNo", strK3Number)
                .add("empId", user != null ? String.valueOf(user.getEmpId()) : "0")
//                .add("outInType", "2") // 出入库类型：（1、生产账号--采购订单入库，2、生产账号--生产任务单入库，3、生产账号--发货通知单出库）
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
                mHandler.sendEmptyMessage(UNPASS);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String result = body.string();
                if (!JsonUtil.isSuccess(result)) {
                    Message msg = mHandler.obtainMessage(UNPASS, result);
                    mHandler.sendMessage(msg);
                    return;
                }
                Message msg = mHandler.obtainMessage(PASS, result);
                Log.e("run_passSC --> onResponse", result);
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     *  得到用户对象
     */
    private void getUserInfo() {
        if(user == null) user = showUserByXml();
    }

    @Override
    public void onDestroyView() {
        closeHandler(mHandler);
        mBinder.unbind();
        super.onDestroyView();
    }

}
