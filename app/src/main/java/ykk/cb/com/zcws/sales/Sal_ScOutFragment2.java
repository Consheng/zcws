package ykk.cb.com.zcws.sales;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
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
import ykk.cb.com.zcws.bean.BarCodeTable;
import ykk.cb.com.zcws.bean.Customer;
import ykk.cb.com.zcws.bean.Department;
import ykk.cb.com.zcws.bean.ScanningRecord;
import ykk.cb.com.zcws.bean.Stock;
import ykk.cb.com.zcws.bean.User;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.bean.k3Bean.SeoutStock;
import ykk.cb.com.zcws.bean.k3Bean.SeoutStockEntry;
import ykk.cb.com.zcws.comm.BaseFragment;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.sales.adapter.Sal_ScOutFragment2Adapter;
import ykk.cb.com.zcws.util.BigdecimalUtil;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.LogUtil;
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity;

/**
 * 生产账号销售出库（根据快递单）
 */
public class Sal_ScOutFragment2 extends BaseFragment {

    @BindView(R.id.et_getFocus)
    EditText etGetFocus;
    @BindView(R.id.lin_focus1)
    LinearLayout linFocus1;
    @BindView(R.id.lin_focus2)
    LinearLayout linFocus2;
    @BindView(R.id.et_expressCode)
    EditText etExpressCode;
    @BindView(R.id.et_mtlCode)
    EditText etMtlCode;
    @BindView(R.id.btn_scan)
    Button btnScan;
    @BindView(R.id.btn_scan2)
    Button btnScan2;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.btn_pass)
    Button btnPass;
    @BindView(R.id.tv_needNum)
    TextView tvNeedNum;
    @BindView(R.id.tv_okNum)
    TextView tvOkNum;

    private Sal_ScOutFragment2 context = this;
    private static final int SUCC1 = 200, UNSUCC1 = 500, SUCC2 = 201, UNSUCC2 = 501, SUCC3 = 202, UNSUCC3 = 502, PASS = 203, UNPASS = 503;
    private static final int SETFOCUS = 1, RESULT_NUM = 2, SAOMA = 3, WRITE_CODE = 4, WRITE_CODE2 = 5, DELAYED_CLICK = 6;
    private Sal_ScOutFragment2Adapter mAdapter;
    private List<ScanningRecord> checkDatas = new ArrayList<>();
    private String deliBarcode, mtlBarcode; // 对应的条码号
    private char curViewFlag = '1'; // 1：仓库，2：库位， 3：车间， 4：物料 ，箱码
    private int curPos; // 当前行
    private OkHttpClient okHttpClient = null;
    private User user;
    private Activity mContext;
    private Sal_ScOutMainActivity parent;
    private boolean isTextChange; // 是否进入TextChange事件
    private String strK3Number; // 保存k3返回的单号
    private boolean isAutoSubmitDate; // 是否自动提交数据
    private DecimalFormat df = new DecimalFormat("#.####");
    private String timesTamp; // 时间戳
    private boolean isClickButton; // 是否点击了按钮

    // 消息处理
    private Sal_ScOutFragment2.MyHandler mHandler = new Sal_ScOutFragment2.MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Sal_ScOutFragment2> mActivity;

        public MyHandler(Sal_ScOutFragment2 activity) {
            mActivity = new WeakReference<Sal_ScOutFragment2>(activity);
        }

        public void handleMessage(Message msg) {
            Sal_ScOutFragment2 m = mActivity.get();
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

                        m.setEnables(m.etExpressCode, R.drawable.back_style_gray3, false);
                        m.setEnables(m.etMtlCode, R.drawable.back_style_gray3, false);
                        m.btnScan.setVisibility(View.GONE);
                        m.btnScan2.setVisibility(View.GONE);
                        m.btnSave.setVisibility(View.GONE);
                        m.btnPass.setVisibility(View.VISIBLE);

                        if(m.isAutoSubmitDate) {
                            m.run_passSC(true);
                        } else {
                            m.run_passSC(false);
//                            Comm.showWarnDialog(m.mContext, "保存成功，请点击“审核按钮”！");
                        }

                        break;
                    case UNSUCC1:
                        errMsg = JsonUtil.strToString(msgObj);
                        if(Comm.isNULLS(errMsg).length() == 0) errMsg = "服务器繁忙，请稍候再试！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case PASS: // 审核成功 返回
                        m.reset();
                        if(m.isAutoSubmitDate) {
                            m.toasts("自动提交数据成功✔");
                        } else {
                            Comm.showWarnDialog(m.mContext, "审核成功✔");
                        }

                        break;
                    case UNPASS: // 审核失败 返回
                        errMsg = JsonUtil.strToString(msgObj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "审核失败！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SUCC2: // 扫码成功后进入
                        BarCodeTable bt = null;
                        switch (m.curViewFlag) {
                            case '1': // 快递单
                                List<SeoutStockEntry> list = JsonUtil.strToList(msgObj, SeoutStockEntry.class);
                                m.getScanAfterData_1(list);

                                break;
                            case '2': // 物料
                                bt = JsonUtil.strToObject((String) msg.obj, BarCodeTable.class);
                                m.getMtlAfter(bt);

                                break;
                        }

                        break;
                    case UNSUCC2:
                        errMsg = JsonUtil.strToString(msgObj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "很抱歉，没能找到数据！";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SUCC3: // 判断是否存在返回
                        m.run_save(false);

                        break;
                    case UNSUCC3: // 判断是否存在返回
                        m.run_save(false);

                        break;
                    case SETFOCUS: // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.etGetFocus);
                        switch (m.curViewFlag) {
                            case '1': // 快递单
                                m.setFocusable(m.etExpressCode);
                                break;
                            case '2': // 物料
                                m.setFocusable(m.etMtlCode);
                                break;
                        }

                        break;
                    case SAOMA: // 扫码之后
                        String etName = null;
                        switch (m.curViewFlag) {
                            case '1': // 快递单
                                etName = m.getValues(m.etExpressCode);
                                if (m.deliBarcode != null && m.deliBarcode.length() > 0) {
                                    if (m.deliBarcode.equals(etName)) {
                                        m.deliBarcode = etName;
                                    } else m.deliBarcode = etName.replaceFirst(m.deliBarcode, "");

                                } else m.deliBarcode = etName;
                                m.setTexts(m.etExpressCode, m.deliBarcode);
                                // 执行查询方法
                                m.run_smGetDatas();

                                break;
                            case '2': // 物料
                                if(m.checkDatas.size() == 0) {
                                    m.isTextChange = false;
                                    Comm.showWarnDialog(m.mContext,"请扫描快递单号！");
                                    return;
                                }
                                etName = m.getValues(m.etMtlCode);
                                if (m.mtlBarcode != null && m.mtlBarcode.length() > 0) {
                                    if (m.mtlBarcode.equals(etName)) {
                                        m.mtlBarcode = etName;
                                    } else m.mtlBarcode = etName.replaceFirst(m.mtlBarcode, "");

                                } else m.mtlBarcode = etName;
                                m.setTexts(m.etMtlCode, m.mtlBarcode);
                                // 执行查询方法
                                m.run_smGetDatas();

                                break;
                        }

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
        return inflater.inflate(R.layout.sal_sc_out_fragment2, container, false);
    }

    @Override
    public void initView() {
        mContext = getActivity();
        parent = (Sal_ScOutMainActivity) mContext;

        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new Sal_ScOutFragment2Adapter(mContext, checkDatas);
        recyclerView.setAdapter(mAdapter);
        // 设值listview空间失去焦点
        recyclerView.setFocusable(false);
        mAdapter.setCallBack(new Sal_ScOutFragment2Adapter.MyCallBack() {
            @Override
            public void onClick_num(View v, ScanningRecord entity, int position) {
                Log.e("num", "行：" + position);
                curPos = position;
                double useableQty = checkDatas.get(curPos).getUseableQty();
                String showInfo = "<font color='#666666'>可用数：</font>"+useableQty;
                showInputDialog("拣货数", showInfo, String.valueOf(useableQty), "0.0", RESULT_NUM);
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

        hideSoftInputMode(mContext, etExpressCode);
        hideSoftInputMode(mContext, etMtlCode);
        getUserInfo();
        timesTamp = user.getId()+"-"+Comm.randomUUID();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isClickButton = true;
    }

    @OnClick({R.id.btn_scan, R.id.btn_scan2, R.id.btn_save, R.id.btn_pass, R.id.btn_clone })
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

    private void btnClickAfter(View view) {
        hideLoadDialog();
        isClickButton = true;
        view.setEnabled(true);
        view.setClickable(true);

        Bundle bundle = null;
        switch (view.getId()) {
            case R.id.btn_scan: // 调用摄像头扫描（快递单）
                curViewFlag = '1';
                showForResult(CaptureActivity.class, CAMERA_SCAN, null);

                break;
            case R.id.btn_scan2: // 调用摄像头扫描（物料）
                curViewFlag = '2';
                showForResult(CaptureActivity.class, CAMERA_SCAN, null);

                break;
            case R.id.btn_save: // 保存
//                hideKeyboard(mContext.getCurrentFocus());
                if(!saveBefore()) {
                    return;
                }
                isAutoSubmitDate = false;
//                run_findInStockSum();
                run_save(false);

                break;
            case R.id.btn_pass: // 审核
                if(strK3Number == null) {
                    Comm.showWarnDialog(mContext,"请先保存数据！");
                    return;
                }
                isAutoSubmitDate = false;
                run_passSC(false);

                break;
            case R.id.btn_clone: // 重置
//                hideKeyboard(mContext.getCurrentFocus());
                if (checkDatas != null && checkDatas.size() > 0) {
                    AlertDialog.Builder build = new AlertDialog.Builder(mContext);
                    build.setIcon(R.drawable.caution);
                    build.setTitle("系统提示");
                    build.setMessage("您有未保存的数据，继续重置吗？");
                    build.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    });
                    build.setNegativeButton("否", null);
                    build.setCancelable(false);
                    build.show();
                    return;
                } else {
                    reset();
                }

                break;
        }
    }

    /**
     * 选择保存之前的判断
     */
    private boolean saveBefore() {
        if (checkDatas == null || checkDatas.size() == 0) {
            Comm.showWarnDialog(mContext,"请先扫描快递单号！");
            return false;
        }
        // 检查数据
        int size = checkDatas.size();
        for (int i = 0; i < size; i++) {
            ScanningRecord sr = checkDatas.get(i);
//            if (sr.getSourceQty() > sr.getRealQty()) {
//                Comm.showWarnDialog(mContext,"第" + (i + 1) + "行货还没捡完货！");
//                return false;
//            }
        }

        return true;
    }

    /**
     * 判断是否扫完数
     */
    private boolean isFinish() {
        boolean isBool = true;
        for (int i = 0, size = checkDatas.size(); i < size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            if (sr.getUseableQty() > sr.getRealQty()) {
                isBool = false;
                break;
            }
        }
        return isBool;
    }

    @Override
    public void setListener() {
        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocusable(etGetFocus);
                switch (v.getId()) {
                    case R.id.et_expressCode:
                        setFocusable(etExpressCode);
                        break;
                    case R.id.et_mtlCode:
                        setFocusable(etMtlCode);
                        break;
                }
            }
        };
        etExpressCode.setOnClickListener(click);
        etMtlCode.setOnClickListener(click);

        // 快递单
        etExpressCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) return;
                curViewFlag = '1';
                if(!isTextChange) {
                    isTextChange = true;
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300);
                }
            }
        });

        // 物料
        etMtlCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) return;
                curViewFlag = '2';
                if(!isTextChange) {
                    isTextChange = true;
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300);
                }
            }
        });

        // 长按输入条码
        etExpressCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showInputDialog("输入快递号", "", "none", WRITE_CODE);
                return true;
            }
        });

        // 长按输入条码
        etMtlCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showInputDialog("输入条码", "", "none", WRITE_CODE2);
                return true;
            }
        });

        etExpressCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    linFocus1.setBackgroundResource(R.drawable.back_style_red_focus);
                    linFocus2.setBackgroundResource(R.drawable.back_style_gray4);
                } else {
                    if(linFocus1 != null) {
                        linFocus1.setBackgroundResource(R.drawable.back_style_gray4);
                    }
                }
            }
        });

        etMtlCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    linFocus1.setBackgroundResource(R.drawable.back_style_gray4);
                    linFocus2.setBackgroundResource(R.drawable.back_style_red_focus);
                } else {
                    if(linFocus2 != null) {
                        linFocus2.setBackgroundResource(R.drawable.back_style_gray4);
                    }
                }
            }
        });
    }

    private void reset() {
        isClickButton = true;
        timesTamp = user.getId()+"-"+Comm.randomUUID();
        setEnables(etExpressCode, R.color.transparent, true);
        setEnables(etMtlCode, R.color.transparent, true);
        btnScan.setVisibility(View.VISIBLE);
        btnScan2.setVisibility(View.VISIBLE);
        strK3Number = null;
        etExpressCode.setText(""); // 快递单号
        etMtlCode.setText(""); // 物料
        btnSave.setVisibility(View.VISIBLE);
        btnPass.setVisibility(View.GONE);
        checkDatas.clear();
        curViewFlag = '1';
        deliBarcode = null;
        mtlBarcode = null;
        tvNeedNum.setText("0");
        tvOkNum.setText("0");

        mAdapter.notifyDataSetChanged();
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_NUM: // 数量
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        double num = parseDouble(value);
                        checkDatas.get(curPos).setRealQty(num);
                        checkDatas.get(curPos).setIsUniqueness('N');
                        mAdapter.notifyDataSetChanged();
                        mHandler.sendEmptyMessageDelayed(SETFOCUS,200);
                        countNum();
                        // 自动检查数据是否可以保存
                        if (isFinish()) {
                            isAutoSubmitDate = true;
                            run_save(true);
                        }
                    }
                }

                break;
            case WRITE_CODE: // 输入条码返回
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        etExpressCode.setText(value.toUpperCase());
                    }
                }

                break;
            case WRITE_CODE2: // 输入条码返回
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        etMtlCode.setText(value.toUpperCase());
                    }
                }

                break;
            case CAMERA_SCAN: // 扫一扫成功  返回
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String code = bundle.getString(DECODED_CONTENT_KEY, "");
                        switch (curViewFlag) {
                            case '1': // 发货单
                                setTexts(etExpressCode, code);
                                break;
                            case '2': // 物料
                                setTexts(etMtlCode, code);
                                break;
                        }
                    }
                }
                break;
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 300);
    }

    /**
     * 得到快递单号扫码的数据
     */
    private void getScanAfterData_1(List<SeoutStockEntry> list) {
        if(checkDatas.size() > 0) {
            Comm.showWarnDialog(mContext,"请先保存当前行的数据！");
            return;
        }
        int size = list.size();
        for(int i=0; i<size; i++) {
            SeoutStockEntry seoutStockEntry = list.get(i);
            SeoutStock seoutStock = seoutStockEntry.getSeOutStock();
            seoutStock.setTempEmpId(user.getEmpId());
            seoutStock.setExpressNo(deliBarcode);
            ICItem icItem = seoutStockEntry.getIcItem();
            ScanningRecord sr = new ScanningRecord();

            sr.setType(11); // 1：电商销售出库，10：生产产品入库，11：生产销售出库
            sr.setSourceId(seoutStockEntry.getFinterid());
            sr.setSourceNumber(seoutStockEntry.getFbillno());
            sr.setSourceEntryId(seoutStockEntry.getFentryid());
            sr.setExpressNo(deliBarcode);
            sr.setIcItemId(icItem.getFitemid());
            sr.setIcItemNumber(icItem.getFnumber());
            sr.setIcItemName(icItem.getFname());
            Customer cust = seoutStock.getCustomer();
            if(cust != null) {
                sr.setCustNumber(cust.getFnumber());
                sr.setCustName(cust.getFname());
            }
            Department department = seoutStock.getDepartment();
            if(department != null) {
                sr.setDeptNumber(department.getDepartmentNumber());
                sr.setDeptName(department.getDepartmentName());
            }
            Stock stock = seoutStockEntry.getStock();
            if(stock != null) {
                sr.setStock(stock);
                sr.setStockNumber(stock.getFnumber());
                sr.setStockName(stock.getFname());
            }
            sr.setStockPositionNumber("");
            sr.setStockPositionName("");
            sr.setDeliveryWay("");
            sr.setSourceQty(seoutStockEntry.getFqty());
            sr.setUseableQty(seoutStockEntry.getUseableQty());
            sr.setRealQty(0);
            sr.setPrice(seoutStockEntry.getFprice());
            sr.setCreateUserId(user.getId());
            sr.setCreateUserName(user.getUsername());
            sr.setDataTypeFlag("APP");
            sr.setTempTimesTamp(timesTamp);
            sr.setSourceObj(JsonUtil.objectToString(seoutStockEntry));

            checkDatas.add(sr);
        }
        mAdapter.notifyDataSetChanged();
        setFocusable(etMtlCode);
        countNum();
    }

    /**
     * 得到扫码物料 数据
     */
    private void getMtlAfter(BarCodeTable bt) {
        ICItem tmpICItem = JsonUtil.stringToObject(bt.getRelationObj(), ICItem.class);

        int size = checkDatas.size();
        boolean isFlag = false; // 是否存在该订单
        boolean isBool = false; // 是否使用弹出框来确认数量
        boolean isOkNum = false; // 相同的物料不同的条码是否扫完数
        int pos = -1;
        for (int i = 0; i < size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            String srBarcode = isNULLS(sr.getStrBarcodes());
            // 如果扫码相同
            if (bt.getIcItemNumber().equals(sr.getIcItemNumber())) {
                isFlag = true;
                pos = i;

                // 启用序列号，批次号；    990156：启用批次号，990156：启用序列号
                if(tmpICItem.getSnManager() == 990156 || tmpICItem.getBatchManager() == 990156) {
                    if (srBarcode.indexOf(bt.getBarcode()) > -1) {
                        Comm.showWarnDialog(mContext, "条码已经使用！");
                        return;
                    }
                    if (sr.getRealQty() >= sr.getUseableQty()) {
//                        Comm.showWarnDialog(mContext, "第" + (i + 1) + "行，已拣完！");
//                        return;
                        isOkNum = true;
                        continue;
                    }
                    if(srBarcode.length() == 0) {
                        sr.setStrBarcodes(bt.getBarcode());
                    } else {
                        sr.setStrBarcodes(srBarcode +","+ bt.getBarcode());
                    }
                    sr.setIsUniqueness('Y');
                    if(tmpICItem.getBatchManager() == 990156 && tmpICItem.getSnManager() == 990155 ) {
                        sr.setStrBarcodes(bt.getBarcode());
                        sr.setRealQty(sr.getRealQty() + bt.getBarcodeQty());
                        isBool = true;
                    } else {
                        sr.setRealQty(sr.getRealQty() + 1);
                    }
                    isOkNum = false;
                } else { // 未启用序列号， 批次号
                    if (sr.getRealQty() >= sr.getUseableQty()) {
                        continue;
                    }
                    sr.setRealQty(sr.getUseableQty());
                    sr.setIsUniqueness('N');
                    // 不存在条码，就加入
                    if (srBarcode.indexOf(bt.getBarcode()) == -1) {
                        if (srBarcode.length() == 0) {
                            sr.setStrBarcodes(bt.getBarcode());
                        } else {
                            sr.setStrBarcodes(srBarcode + "," + bt.getBarcode());
                        }
                    }
                }
                break;
            }
        }
        if (!isFlag) {
            Comm.showWarnDialog(mContext, "该物料与订单不匹配！");
            return;
        }
        if(isOkNum) {
            Comm.showWarnDialog(mContext, "该物料条码在订单中数量已扫完！");
            return;
        }

        setFocusable(etMtlCode);
        mAdapter.notifyDataSetChanged();
        countNum();

        if(isBool) {
            // 使用弹出框确认数量
            curPos = pos;
            double useableQty = checkDatas.get(curPos).getUseableQty();
            String showInfo = "<font color='#666666'>可用数：</font>"+useableQty;
            showInputDialog("拣货数", showInfo, String.valueOf(useableQty), "0.0", RESULT_NUM);
        } else {
            // 自动检查数据是否可以保存
            if (isFinish()) {
                isAutoSubmitDate = true;
                run_save(true);
            }
        }
    }

    /**
     * 统计数量
     */
    private void countNum() {
        double needNum = 0;
        double okNum = 0;
        for(int i=0; i<checkDatas.size(); i++) {
            ScanningRecord sc = checkDatas.get(i);
            needNum = BigdecimalUtil.add(needNum, sc.getUseableQty());
            okNum = BigdecimalUtil.add(okNum, sc.getRealQty());
        }
        tvNeedNum.setText(df.format(needNum));
        tvOkNum.setText(df.format(okNum));
    }

    /**
     * 保存方法
     */
    private void run_save(boolean isAutoSubmit) {

        List<ScanningRecord> listRecord = new ArrayList<>();
        for(int i=0,size=checkDatas.size(); i<size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            if(sr.getRealQty() > 0) {
                listRecord.add(sr);
            }
        }
        if(listRecord.size() == 0) {
            Comm.showWarnDialog(mContext,"请扫码对应的条码！");
            return;
        }

        if(isAutoSubmit) showLoadDialog("自动保存中...",false);
        else showLoadDialog("保存中...",false);
        String mJson = JsonUtil.objectToString(listRecord);
        FormBody formBody = new FormBody.Builder()
                .add("strJson", mJson)
                .build();

        String mUrl = getURL("scanningRecord/addScanningRecord");
        Request request = new Request.Builder()
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
        isTextChange = false;
        showLoadDialog("加载中...", false);
        String mUrl = null;
        String barcode = null;
        String strCaseId = null;
        String expressNo = null;
        switch (curViewFlag) {
            case '1': // 快递单号查询
                mUrl = getURL("SEOutStock/findBarcode");
                expressNo = deliBarcode;
                barcode = "";
                strCaseId = "";
                break;
            case '2': // 物料查询
                mUrl = getURL("barCodeTable/findBarcode_SC");
                expressNo = "";
                barcode = mtlBarcode;
                strCaseId = "11,21";
                break;
        }
        FormBody formBody = new FormBody.Builder()
                .add("expressNo", expressNo)
                .add("logWay", "WL") // 发货方式：物流
                .add("barcode", barcode)
                .add("strCaseId", strCaseId)
                .add("sourceType", "11") // 1：电商销售出库，10：生产产品入库，11：发货通知单销售出库
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
    private void run_passSC(boolean isAutoSubmit) {
        if(isAutoSubmit) showLoadDialog("自动审核中...", false);
        else showLoadDialog("正在审核...", false);

        String mUrl = getURL("stockBill/passSC");
        getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("strFbillNo", strK3Number)
                .add("empId", user != null ? String.valueOf(user.getEmpId()) : "0")
//                .add("outInType", "3") // 出入库类型：（1、生产账号--采购订单入库，2、生产账号--生产任务单入库，3、生产账号--发货通知单出库）
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
