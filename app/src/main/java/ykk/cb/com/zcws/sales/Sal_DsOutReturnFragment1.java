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
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import ykk.cb.com.zcws.basics.ReturnReason_DialogActivity;
import ykk.cb.com.zcws.bean.Department;
import ykk.cb.com.zcws.bean.Organization;
import ykk.cb.com.zcws.bean.ScanningRecord;
import ykk.cb.com.zcws.bean.Stock;
import ykk.cb.com.zcws.bean.User;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.bean.k3Bean.ICStockBill_K3;
import ykk.cb.com.zcws.bean.k3Bean.ICStockBillEntry_K3;
import ykk.cb.com.zcws.bean.k3Bean.ReturnReason;
import ykk.cb.com.zcws.comm.BaseFragment;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.sales.adapter.Sal_DsOutReturnFragment1Adapter;
import ykk.cb.com.zcws.util.BigdecimalUtil;
import ykk.cb.com.zcws.util.JsonUtil;
import ykk.cb.com.zcws.util.LogUtil;
import ykk.cb.com.zcws.util.zxing.android.CaptureActivity;

/**
 * ็ตๅ้่ดง
 */
public class Sal_DsOutReturnFragment1 extends BaseFragment {

    @BindView(R.id.et_getFocus)
    EditText etGetFocus;
    @BindView(R.id.lin_focus1)
    LinearLayout linFocus1;
    @BindView(R.id.lin_focus2)
    LinearLayout linFocus2;
    @BindView(R.id.et_mtlCode)
    EditText etMtlCode;
    @BindView(R.id.et_expressCode)
    EditText etExpressCode;
    @BindView(R.id.btn_scan)
    Button btnScan;
    @BindView(R.id.btn_scan2)
    Button btnScan2;
    @BindView(R.id.tv_custInfo)
    TextView tvCustInfo;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.btn_pass)
    Button btnPass;
    @BindView(R.id.tv_okNum)
    TextView tvOkNum;
    @BindView(R.id.cb_saoMaNext)
    CheckBox cbSaoMaNext;


    private Sal_DsOutReturnFragment1 context = this;
    private static final int SUCC1 = 200, UNSUCC1 = 500, SUCC2 = 201, UNSUCC2 = 501, SUCC3 = 202, UNSUCC3 = 502, PASS = 203, UNPASS = 503;
    private static final int SETFOCUS = 1, RESULT_NUM = 2, SAOMA = 3, PRICE = 4, RETURN_REASON = 5, WRITE_CODE = 6, WRITE_CODE2 = 7, DELAYED_CLICK = 8;
    private Sal_DsOutReturnFragment1Adapter mAdapter;
    private List<ScanningRecord> checkDatas = new ArrayList<>();
    private String mtlBarcode; // ๅฏนๅบ็ๆก็?ๅท
    private char curViewFlag = '1'; // 1๏ผไปๅบ๏ผ2๏ผๅบไฝ๏ผ 3๏ผ่ฝฆ้ด๏ผ 4๏ผ็ฉๆ ๏ผ็ฎฑ็?
    private int curPos = -1; // ๅฝๅ่ก
    private OkHttpClient okHttpClient = null;
    private User user;
    private Organization cust; // ๅฎขๆท
    private Activity mContext;
    private Sal_DsOutReturnMainActivity parent;
    private boolean isTextChange; // ๆฏๅฆ่ฟๅฅTextChangeไบไปถ
    private String strK3Number; // ไฟๅญk3่ฟๅ็ๅๅท
    private DecimalFormat df = new DecimalFormat("#.####");
    private String timesTamp; // ๆถ้ดๆณ
    private boolean isClickButton; // ๆฏๅฆ็นๅปไบๆ้ฎ

    // ๆถๆฏๅค็
    private Sal_DsOutReturnFragment1.MyHandler mHandler = new Sal_DsOutReturnFragment1.MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Sal_DsOutReturnFragment1> mActivity;

        public MyHandler(Sal_DsOutReturnFragment1 activity) {
            mActivity = new WeakReference<Sal_DsOutReturnFragment1>(activity);
        }

        public void handleMessage(Message msg) {
            Sal_DsOutReturnFragment1 m = mActivity.get();
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

                        m.setEnables(m.etMtlCode, R.drawable.back_style_gray3, false);
                        m.setEnables(m.etExpressCode, R.drawable.back_style_gray3, false);
                        m.btnScan.setVisibility(View.GONE);
                        m.btnScan2.setVisibility(View.GONE);
                        m.btnSave.setVisibility(View.GONE);
                        m.btnPass.setVisibility(View.VISIBLE);
                        Comm.showWarnDialog(m.mContext,"ไฟๅญๆๅ๏ผ่ฏท็นๅปโๅฎกๆ?ธๆ้ฎโ๏ผ");

                        break;
                    case UNSUCC1:
                        errMsg = JsonUtil.strToString(msgObj);
                        if(Comm.isNULLS(errMsg).length() == 0) errMsg = "ๆๅกๅจ็นๅฟ๏ผ่ฏท็จๅๅ่ฏ๏ผ";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case PASS: // ๅฎกๆ?ธๆๅ ่ฟๅ
                        m.reset();
                        Comm.showWarnDialog(m.mContext,"ๅฎกๆ?ธๆๅโ");

                        break;
                    case UNPASS: // ๅฎกๆ?ธๅคฑ่ดฅ ่ฟๅ
                        errMsg = JsonUtil.strToString(msgObj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "ๅฎกๆ?ธๅคฑ่ดฅ๏ผ";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SUCC2: // ๆซ็?ๆๅๅ่ฟๅฅ
                        switch (m.curViewFlag) {
                            case '1': // ๅฟซ้ๅ
                                List<ICStockBillEntry_K3> list = JsonUtil.strToList(msgObj, ICStockBillEntry_K3.class);
                                ICStockBillEntry_K3 stockBillEntry = list.get(0);
                                ICStockBill_K3 stockOrder = stockBillEntry.getStockBill();
                                Organization tempCust = stockOrder.getCust();
                                // ๆพ็คบๅฎขๆท
                                if(m.cust != null && tempCust != null && !(m.cust.getfNumber().equals(tempCust.getfNumber()))) {
                                    Comm.showWarnDialog(m.mContext,"ๆซๆ็ๅฎขๆทไธไธ่ด๏ผ่ฏทๆฃๆฅ๏ผ");
                                    return;
                                }
                                m.cust = tempCust;
                                m.tvCustInfo.setText(Html.fromHtml("ๅฎขๆท๏ผ<font color='#000000'>"+tempCust.getfName()+"</font>"));
//                                m.getScanAfterData_1(list);

                                // ๅกซๅๆฐๆฎ
                                int size = m.checkDatas.size();
                                boolean addRow = true;
                                for (int i = 0; i < size; i++) {
                                    ScanningRecord sr = m.checkDatas.get(i);
                                    // ๆ็ธๅ็๏ผๅฐฑไธๆฐๅขไบ
                                    if (sr.getSourceId() == stockOrder.getFinterid() && sr.getSourceEntryId() == stockBillEntry.getFentryid()) {
                                        addRow = false;
                                        break;
                                    }
                                }
                                m.parent.isChange = true;
                                if (addRow) {
                                    m.getScanAfterData_1(stockBillEntry);
                                } else {
                                    m.getMtlAfter(stockBillEntry);
                                }
                                // ๅฆๆ็นๅปไบ่ชๅจ่ทณๅฐๅฟซ้ๅ๏ผๅฐฑๆ็ฆ็น่ทณๅฐๅฟซ้ๅ
                                if(m.cbSaoMaNext.isChecked()) {
                                    m.setFocusable(m.etGetFocus);
                                    m.setFocusable(m.etExpressCode);
                                }

                                break;
                        }

                        break;
                    case UNSUCC2:
                        errMsg = JsonUtil.strToString(msgObj);
                        if(m.isNULLS(errMsg).length() == 0) errMsg = "ๅพๆฑๆญ๏ผๆฒก่ฝๆพๅฐๆฐๆฎ๏ผ";
                        Comm.showWarnDialog(m.mContext, errMsg);

                        break;
                    case SUCC3: // ๅคๆญๆฏๅฆๅญๅจ่ฟๅ
                        m.run_save();

                        break;
                    case UNSUCC3: // ๅคๆญๆฏๅฆๅญๅจ่ฟๅ
                        m.run_save();

                        break;
                    case SETFOCUS: // ๅฝๅผนๅบๅถไป็ชๅฃไผๆขๅคบ็ฆ็น๏ผ้่ฆ่ทณ่ฝฌไธ๏ผๆ่ฝๆญฃๅธธๅพๅฐๅผ
                        m.setFocusable(m.etGetFocus);
                        switch (m.curViewFlag) {
                            case '1': // ็ฉๆ
                                m.setFocusable(m.etMtlCode);
                                break;
                        }

                        break;
                    case SAOMA: // ๆซ็?ไนๅ
                        String etName = null;
                        switch (m.curViewFlag) {
                            case '1': // ็ฉๆ
                                etName = m.getValues(m.etMtlCode);
                                if (m.mtlBarcode != null && m.mtlBarcode.length() > 0) {
                                    if (m.mtlBarcode.equals(etName)) {
                                        m.mtlBarcode = etName;
                                    } else m.mtlBarcode = etName.replaceFirst(m.mtlBarcode, "");

                                } else m.mtlBarcode = etName;
                                m.setTexts(m.etMtlCode, m.mtlBarcode);
                                // ๆง่กๆฅ่ฏขๆนๆณ
                                m.run_smGetDatas(m.mtlBarcode);

                                break;
                        }

                        break;
                    case DELAYED_CLICK: // ๅปถๆถ่ฟๅฅ็นๅปๅ็ๆไฝ
                        View btnView = (View) msg.obj;
                        m.btnClickAfter(btnView);

                        break;
                }
            }
        }
    }

    @Override
    public View setLayoutResID(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.sal_ds_out_return_fragment1, container, false);
    }

    @Override
    public void initView() {
        mContext = getActivity();
        parent = (Sal_DsOutReturnMainActivity) mContext;

        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new Sal_DsOutReturnFragment1Adapter(mContext, checkDatas);
        recyclerView.setAdapter(mAdapter);
        // ่ฎพๅผlistview็ฉบ้ดๅคฑๅป็ฆ็น
        recyclerView.setFocusable(false);
        mAdapter.setCallBack(new Sal_DsOutReturnFragment1Adapter.MyCallBack() {
            @Override
            public void onClick_num(View v, ScanningRecord entity, int position) {
                curPos = position;
                double useableQty = checkDatas.get(curPos).getUseableQty();
                String showInfo = "<font color='#666666'>ๅฏ้ๆฐ๏ผ</font>"+useableQty;
                showInputDialog("้่ดงๆฐ", showInfo, String.valueOf(useableQty), "0.0", RESULT_NUM);
            }

            @Override
            public void onClick_price(View v, ScanningRecord entity, int position) {
                curPos = position;
                showInputDialog("ๅไปท", String.valueOf(entity.getPrice()), "0.0", PRICE);
            }

            @Override
            public void sel_returnReason(View v, ScanningRecord entity, int position) {
                curPos = position;
                Bundle bundle = new Bundle();
                bundle.putString("flag", "DS"); // ๆฅ่ฏข็ตๅ่ดฆๅท็ๆฐๆฎ
                showForResult(ReturnReason_DialogActivity.class, RETURN_REASON, bundle);
            }

            @Override
            public void onClick_del(View v, ScanningRecord entity, int position) {
                checkDatas.remove(position);
                mAdapter.notifyDataSetChanged();
            }

        });
    }

    @Override
    public void initData() {
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(10, TimeUnit.SECONDS) // ่ฎพ็ฝฎ่ฟๆฅ่ถๆถๆถ้ด๏ผ้ป่ฎคไธบ10็ง๏ผ
                    .writeTimeout(30, TimeUnit.SECONDS) // ่ฎพ็ฝฎๅ็่ถๆถๆถ้ด
                    .readTimeout(30, TimeUnit.SECONDS) //่ฎพ็ฝฎ่ฏปๅ่ถๆถๆถ้ด
                    .build();
        }

        hideSoftInputMode(mContext, etMtlCode);
        hideSoftInputMode(mContext, etExpressCode);
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
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
    }

    @OnClick({R.id.btn_scan, R.id.btn_scan2, R.id.btn_save, R.id.btn_pass, R.id.btn_clone, R.id.btn_batchAdd })
    public void onViewClicked(View view) {
        if(isClickButton && view.getId() == R.id.btn_save) {
            isClickButton = false;
            view.setEnabled(false);
            view.setClickable(false);
            showLoadDialog("็จ็ญๅ...",false);

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
            case R.id.btn_scan: // ่ฐ็จๆๅๅคดๆซๆ๏ผ็ฉๆ๏ผ
                curViewFlag = '2';
                showForResult(CaptureActivity.class, CAMERA_SCAN, null);

                break;
            case R.id.btn_scan2: // ่ฐ็จๆๅๅคดๆซๆ๏ผๅฟซ้ๅ๏ผ
                curViewFlag = '1';
                showForResult(CaptureActivity.class, CAMERA_SCAN, null);

                break;
            case R.id.btn_batchAdd: // ๆน้ๅกซๅ
                if (checkDatas == null || checkDatas.size() == 0) {
                    Comm.showWarnDialog(mContext, "่ฏทๅๆซๆ่ฆ้่ดง็ๆก็?๏ผ");
                    return;
                }
                if(curPos == -1) {
                    Comm.showWarnDialog(mContext, "่ฏท้ๆฉไปปๆไธ่ก็้่ดง็็ฑ๏ผ");
                    return;
                }
                ScanningRecord srTemp = checkDatas.get(curPos);
                int id = srTemp.getReturnReasonId();
                String name = srTemp.getReturnReasonName();
                for(int i=curPos; i<checkDatas.size(); i++) {
                    ScanningRecord sr = checkDatas.get(i);
                    if (sr.getReturnReasonId() == 0) {
                        sr.setReturnReasonId(id);
                        sr.setReturnReasonName(name);
                    }
                }
                mAdapter.notifyDataSetChanged();

                break;
            case R.id.btn_save: // ไฟๅญ
//                hideKeyboard(mContext.getCurrentFocus());
                if(!saveBefore()) {
                    return;
                }
                String expressCode = getValues(etExpressCode).trim();
                if(expressCode.length() == 0) {
                    AlertDialog.Builder build = new AlertDialog.Builder(mContext);
                    build.setIcon(R.drawable.caution);
                    build.setTitle("็ณป็ปๆ็คบ");
                    build.setMessage("ๅฝๅๅฟซ้ๅไธบ็ฉบ๏ผๆฏๅฆ็ปง็ปญไฟๅญ๏ผ");
                    build.setPositiveButton("ๆฏ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            run_save();
                        }
                    });
                    build.setNegativeButton("ๅฆ", null);
                    build.setCancelable(false);
                    build.show();

                } else {
//                run_findInStockSum();
                    run_save();
                }

                break;
            case R.id.btn_pass: // ๅฎกๆ?ธ
                if(strK3Number == null) {
                    Comm.showWarnDialog(mContext,"่ฏทๅไฟๅญๆฐๆฎ๏ผ");
                    return;
                }
                run_passDS();

                break;
            case R.id.btn_clone: // ้็ฝฎ
//                hideKeyboard(mContext.getCurrentFocus());
                if (checkDatas != null && checkDatas.size() > 0) {
                    AlertDialog.Builder build = new AlertDialog.Builder(mContext);
                    build.setIcon(R.drawable.caution);
                    build.setTitle("็ณป็ปๆ็คบ");
                    build.setMessage("ๆจๆๆชไฟๅญ็ๆฐๆฎ๏ผ็ปง็ปญ้็ฝฎๅ๏ผ");
                    build.setPositiveButton("ๆฏ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    });
                    build.setNegativeButton("ๅฆ", null);
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
     * ้ๆฉไฟๅญไนๅ็ๅคๆญ
     */
    private boolean saveBefore() {
        if (checkDatas == null || checkDatas.size() == 0) {
            Comm.showWarnDialog(mContext,"่ฏทๆซๆๆๆๆก็?๏ผ");
            return false;
        }

        String expressCode = getValues(etExpressCode).trim();
        // ๆฃๆฅๆฐๆฎ
        for (int i = 0, size = checkDatas.size(); i < size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            sr.setExpressNo(expressCode); // ่ตๅผๅฟซ้ๅ
            if(sr.getReturnReasonId() == 0) {
                Comm.showWarnDialog(mContext,"็ฌฌ๏ผ"+(i+1)+"๏ผ่ก๏ผ่ฏท้ๆฉ้่ดง็็ฑ๏ผ");
                return false;
            }
           if(sr.getRealQty() > sr.getUseableQty()) {
                Comm.showWarnDialog(mContext,"็ฌฌ" + (i + 1) + "่ก๏ผ้่ดงๆฐไธ่ฝๅคงไบๅฏ้ๆฐ๏ผ");
                return false;
            }
        }
        return true;
    }

    @Override
    public void setListener() {
        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocusable(etGetFocus);
                switch (v.getId()) {
                    case R.id.et_mtlCode:
                        setFocusable(etMtlCode);
                        break;
                    case R.id.et_expressCode:
                        setFocusable(etExpressCode);
                        break;
                }
            }
        };
        etMtlCode.setOnClickListener(click);
        etExpressCode.setOnClickListener(click);

        // ็ฉๆ
        etMtlCode.addTextChangedListener(new TextWatcher() {
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

        // ้ฟๆ่พๅฅๆก็?
        etMtlCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showInputDialog("่พๅฅๆก็?", "", "none", WRITE_CODE2);
                return true;
            }
        });

        // ้ฟๆ่พๅฅๆก็?
        etExpressCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showInputDialog("่พๅฅๅฟซ้ๅท", "", "none", WRITE_CODE);
                return true;
            }
        });

        etMtlCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

        etExpressCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    linFocus2.setBackgroundResource(R.drawable.back_style_red_focus);
                    linFocus1.setBackgroundResource(R.drawable.back_style_gray4);
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
        cust = null;
        tvCustInfo.setText("ๅฎขๆท๏ผ");
        setEnables(etMtlCode, R.color.transparent, true);
        setEnables(etExpressCode, R.color.transparent, true);
        btnScan.setVisibility(View.VISIBLE);
        btnScan2.setVisibility(View.VISIBLE);
        strK3Number = null;
        etMtlCode.setText(""); // ็ฉๆ
        btnSave.setVisibility(View.VISIBLE);
        btnPass.setVisibility(View.GONE);
        checkDatas.clear();
        curViewFlag = '1';
        mtlBarcode = null;
        curPos = -1;
        tvOkNum.setText("0");

        mAdapter.notifyDataSetChanged();
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_NUM: // ๆฐ้
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        double num = parseDouble(value);
                        checkDatas.get(curPos).setRealQty(num);
                        checkDatas.get(curPos).setIsUniqueness('N');
                        mAdapter.notifyDataSetChanged();
                        countNum();
                    }
                }

                break;
            case WRITE_CODE: // ่พๅฅๆก็?่ฟๅ
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        etExpressCode.setText(value.toUpperCase());
                    }
                }

                break;
            case WRITE_CODE2: // ่พๅฅๆก็?่ฟๅ
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        etMtlCode.setText(value.toUpperCase());
                    }
                }

                break;
            case PRICE: // ๅไปท
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("resultValue", "");
                        double num = parseDouble(value);
                        if(num <= 0) {
                            Comm.showWarnDialog(mContext,"ๅไปทๅฟ้กปๅคงไบ0๏ผ");
                            return;
                        }
                        checkDatas.get(curPos).setPrice(num);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                break;
            case RETURN_REASON: // ้่ดง็็ฑ
                if (resultCode == Activity.RESULT_OK) {
                    ReturnReason returnReason = (ReturnReason) data.getSerializableExtra("obj");
                    checkDatas.get(curPos).setReturnReasonId(returnReason.getFitemId());
                    checkDatas.get(curPos).setReturnReasonName(returnReason.getFname());
                    mAdapter.notifyDataSetChanged();
                }

                break;
            case CAMERA_SCAN: // ๆซไธๆซๆๅ  ่ฟๅ
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String code = bundle.getString(DECODED_CONTENT_KEY, "");
                        switch (curViewFlag) {
                            case '1': // ๅฟซ้ๅ
                                setTexts(etExpressCode, code);
                                break;
                            case '2': // ็ฉๆ
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
     * ๅพๅฐๅฟซ้ๅๅทๆซ็?็ๆฐๆฎ
     */
    private void getScanAfterData_1(ICStockBillEntry_K3 stockBillEntry) {
//        int size = list.size();
//        for(int i=0; i<size; i++) {
//            Icstockbillentry stockBillEntry = list.get(i);
            ICStockBill_K3 stockOrder = stockBillEntry.getStockBill();
            ICItem icItem = stockBillEntry.getIcItem();
            ScanningRecord sr = new ScanningRecord();
            sr.setId(stockBillEntry.getScanningRecordId()); // ่ฟไธชๅผไธบไบๆๅฅๅฐ้่ดง่ฎฐๅฝ่กจไธญ
            sr.setType(12); // 1๏ผ็ตๅ้ๅฎๅบๅบ๏ผ10๏ผ็ไบงไบงๅๅฅๅบ๏ผ11๏ผๅ่ดง้็ฅๅ้ๅฎๅบๅบ๏ผ12๏ผ็ตๅ้ๅฎ้่ดง๏ผ13๏ผ็ตๅๅค่ดญๅฅๅบ
            sr.setSourceId(stockBillEntry.getFinterid());
            sr.setSourceNumber(stockBillEntry.getFbillNo());
            sr.setSourceEntryId(stockBillEntry.getFentryid());
//            sr.setExpressNo(expressBarcode);
            sr.setIcItemId(icItem.getFitemid());
            sr.setIcItemNumber(icItem.getFnumber());
            sr.setIcItemName(icItem.getFname());
            Organization cust = stockOrder.getCust();
            if(cust != null) {
                sr.setCustNumber(cust.getfNumber());
                sr.setCustName(cust.getfName());
            }
            Department department = stockOrder.getDepartment();
            if(department != null) {
                sr.setDeptNumber(department.getDepartmentNumber());
                sr.setDeptName(department.getDepartmentName());
            }
//            Stock stock = icItem.getStock();
            Stock stock = new Stock();
            stock.setFnumber("SC.02.01");
            stock.setFname("ไธ่ฏๅไป๏ผๅฎๅ๏ผ");
            if(stock != null) {
                sr.setStock(stock);
                sr.setStockNumber(stock.getFnumber());
                sr.setStockName(stock.getFname());
            }
//            StockPosition stockPos = icItem.getStockPos();
//            if(stockPos != null && stockPos.getFspId() > 0) {
//                sr.setStockPos(stockPos);
//                sr.setStockPositionNumber(stockPos.getFnumber());
//                sr.setStockPositionName(stockPos.getFname());
//            }
            sr.setDeliveryWay("");
            sr.setSourceQty(stockBillEntry.getFqtymust());
            sr.setUseableQty(stockBillEntry.getUseableQty());
            sr.setRealQty(stockBillEntry.getFqty());
            sr.setPrice(stockBillEntry.getFprice());
            sr.setCreateUserId(user.getId());
            sr.setEmpId(user.getEmpId());
            sr.setCreateUserName(user.getUsername());
            sr.setDataTypeFlag("APP");
            sr.setTempTimesTamp(timesTamp);
            sr.setSourceObj(JsonUtil.objectToString(stockBillEntry));
            sr.setStrBarcodes(mtlBarcode);
//            sr.setIsUniqueness('N');
            // ไธดๆถๅญๆฎต
            sr.setSalOrderNo(stockBillEntry.getSalOrderNo());
            sr.setReturnReasonId(0);
            sr.setReturnReasonName("");

            // ๅฏ็จๅบๅๅท๏ผๆนๆฌกๅท๏ผ    990156๏ผๅฏ็จๆนๆฌกๅท๏ผ990156๏ผๅฏ็จๅบๅๅท
            if(icItem.getSnManager() == 990156 || icItem.getBatchManager() == 990156) {
                sr.setStrBarcodes(mtlBarcode);
                sr.setIsUniqueness('Y');
                sr.setRealQty(1);

            } else { // ๆชๅฏ็จๅบๅๅท๏ผ ๆนๆฌกๅท
                sr.setRealQty(stockBillEntry.getFqty());
                sr.setIsUniqueness('N');
                // ไธๅญๅจๆก็?๏ผๅฐฑๅ?ๅฅ
                sr.setStrBarcodes(mtlBarcode);
            }

            checkDatas.add(sr);
//        }

        mAdapter.notifyDataSetChanged();
        setFocusable(etMtlCode);
        countNum();

        if(icItem.getBatchManager() == 990156) {
            // ไฝฟ็จๅผนๅบๆก็กฎ่ฎคๆฐ้
            curPos = checkDatas.size()-1;
            double useableQty = checkDatas.get(curPos).getUseableQty();
            String showInfo = "<font color='#666666'>ๅฏ้ๆฐ๏ผ</font>"+useableQty;
            showInputDialog("้่ดงๆฐ", showInfo, String.valueOf(useableQty), "0.0", RESULT_NUM);
        }
    }

    /**
     * ๅพๅฐๆซ็?็ฉๆ ๆฐๆฎ
     */
    private void getMtlAfter(ICStockBillEntry_K3 stockBillEntry) {
        ICItem tmpICItem = stockBillEntry.getIcItem();

        int size = checkDatas.size();
        boolean isFlag = false; // ๆฏๅฆๅญๅจ่ฏฅ่ฎขๅ
        for (int i = 0; i < size; i++) {
            ScanningRecord sr = checkDatas.get(i);
            String srBarcode = isNULLS(sr.getStrBarcodes());
            // ๅฆๆๆซ็?็ธๅ
            if (sr.getSourceId() == stockBillEntry.getFinterid() && sr.getSourceEntryId() == stockBillEntry.getFentryid()) {
                isFlag = true;
                if (sr.getRealQty() >= sr.getSourceQty()) {
                    Comm.showWarnDialog(mContext, "็ฌฌ" + (i + 1) + "่ก๏ผๅทฒๆซๅฎ๏ผ");
                    return;
                }

                // ๅฏ็จๅบๅๅท๏ผๆนๆฌกๅท๏ผ    990156๏ผๅฏ็จๆนๆฌกๅท๏ผ990156๏ผๅฏ็จๅบๅๅท
                if(tmpICItem.getSnManager() == 990156 || tmpICItem.getBatchManager() == 990156) {
                    if (srBarcode.indexOf(mtlBarcode) > -1) {
                        Comm.showWarnDialog(mContext, "ๆก็?ๅทฒไฝฟ็จ๏ผ");
                        return;
                    }
                    if(srBarcode.length() == 0) {
                        sr.setStrBarcodes(mtlBarcode);
                    } else {
                        sr.setStrBarcodes(srBarcode +","+ mtlBarcode);
                    }
                    sr.setIsUniqueness('Y');
                    sr.setRealQty(sr.getRealQty() + 1);

                } else { // ๆชๅฏ็จๅบๅๅท๏ผ ๆนๆฌกๅท
                    sr.setRealQty(sr.getSourceQty());
                    sr.setIsUniqueness('N');
                    // ไธๅญๅจๆก็?๏ผๅฐฑๅ?ๅฅ
                    if (srBarcode.indexOf(mtlBarcode) == -1) {
                        if (srBarcode.length() == 0) {
                            sr.setStrBarcodes(mtlBarcode);
                        } else {
                            sr.setStrBarcodes(srBarcode + "," + mtlBarcode);
                        }
                    }
                }
                break;
            }
        }
        if (!isFlag) {
            Comm.showWarnDialog(mContext, "่ฏฅๆก็?ไธ่กๆฐๆฎไธๅน้๏ผ");
            return;
        }
        mAdapter.notifyDataSetChanged();
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200);
        countNum();

        if(tmpICItem.getBatchManager() == 990156) {
            // ไฝฟ็จๅผนๅบๆก็กฎ่ฎคๆฐ้
            curPos = checkDatas.size()-1;
            double useableQty = checkDatas.get(curPos).getUseableQty();
            String showInfo = "<font color='#666666'>ๅฏ้ๆฐ๏ผ</font>"+useableQty;
            showInputDialog("้่ดงๆฐ", showInfo, String.valueOf(useableQty), "0.0", RESULT_NUM);
        }
    }

    /**
     * ็ป่ฎกๆฐ้
     */
    private void countNum() {
        double okNum = 0;
        for(int i=0; i<checkDatas.size(); i++) {
            ScanningRecord sc = checkDatas.get(i);
            okNum = BigdecimalUtil.add(okNum, sc.getRealQty());
        }
        tvOkNum.setText(df.format(okNum));
    }

    /**
     * ไฟๅญๆนๆณ
     */
    private void run_save() {
        showLoadDialog("ไฟๅญไธญ...",false);

        String mJson = JsonUtil.objectToString(checkDatas);
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
     * ๆซ็?ๆฅ่ฏขๅฏนๅบ็ๆนๆณ
     */
    private void run_smGetDatas(String val) {
        isTextChange = false;
        if(val.length() == 0) {
            Comm.showWarnDialog(mContext,"่ฏทๅฏนๅๆก็?๏ผ");
            return;
        }
        showLoadDialog("ๅ?่ฝฝไธญ...",false);
        String mUrl = null;
        String barcode = null;
        String strCaseId = null;
        switch (curViewFlag) {
            case '1': // ็ฉๆๆฅ่ฏข
                mUrl = getURL("scanningRecord/findBarcode");
                barcode = mtlBarcode;
                strCaseId = "11";
                break;
        }
        FormBody formBody = new FormBody.Builder()
                .add("barcode", barcode)
                .add("targetType", "1") // ็ฎๆ?ๆฐๆฎ็ฑปๅ
                .add("sourceType", "12") // 1๏ผ็ตๅ้ๅฎๅบๅบ๏ผ10๏ผ็ไบงไบงๅๅฅๅบ๏ผ11๏ผๅ่ดง้็ฅๅ้ๅฎๅบๅบ๏ผ12๏ผ็ตๅ้ๅฎ้่ดง
                .build();

        Request request = new Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { mHandler.sendEmptyMessage(UNSUCC2); }

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
     * ็ตๅ่ดฆๅทๅฎกๆ?ธ
     */
    private void run_passDS() {
        showLoadDialog("ๆญฃๅจๅฎกๆ?ธ...",false);
        String mUrl = getURL("stockBill/passDS");
        getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("strFbillNo", strK3Number)
                .add("empId", user != null ? String.valueOf(user.getEmpId()) : "0")
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
                Log.e("run_passDS --> onResponse", result);
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     *  ๅพๅฐ็จๆทๅฏน่ฑก
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
