package ykk.cb.com.zcws.basics;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.comm.BaseDialogActivity;

public class PublicInputDialog extends BaseDialogActivity {

    @BindView(R.id.btn_close)
    Button btnClose;
    @BindView(R.id.tv_hintName)
    TextView tvHintName;
    @BindView(R.id.tv_showInfo)
    TextView tvShowInfo;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.tv_clear)
    TextView tvClear;
    @BindView(R.id.tv_tmp)
    TextView tv_tmp;
    @BindView(R.id.gridNums)
    GridView gridNums;

    private PublicInputDialog context = this;
//    private GridView gridNums;
//    private Button btn_confirm, btn_close;
//    private TextView tv_hintName, tvClear, tv_tmp;
//    private EditText etInput;
    private String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "-"};
    private static final int SHOW_INPUT = 100;
    private static final int HIDE_INPUT = 102;
    private String inputType = "0";
    private DecimalFormat df = new DecimalFormat("#.########");

    // 消息处理
    private PublicInputDialog.MyHandler mHandler = new PublicInputDialog.MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<PublicInputDialog> mActivity;

        public MyHandler(PublicInputDialog activity) {
            mActivity = new WeakReference<PublicInputDialog>(activity);
        }

        public void handleMessage(Message msg) {
            PublicInputDialog m = mActivity.get();
            if (m != null) {
                switch (msg.what) {
                    case SHOW_INPUT:
                        m.showKeyboard(m.etInput);

                        break;
                    case HIDE_INPUT:
                        m.hideKeyboard(m.etInput);

                        break;
                }
            }
        }
    }

    @Override
    public int setLayoutResID() {
            return R.layout.ab_public_input;
    }

    @Override
    public void initData() {
        df = new DecimalFormat("#.########");
        setListener();
        bundle();
    }

    @Override
    public void setListener() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                tvClear.setVisibility(s.toString().length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

    }

    /**
     * get send Data
     */
    private void bundle() {
        Bundle bundle = context.getIntent().getExtras();
        if (bundle != null) {
            String hintName = bundle.getString("hintName", "");
            String showInfo = bundle.getString("showInfo", "");
            String value = bundle.getString("value", "");

            tvHintName.setText(hintName);
            tvShowInfo.setVisibility(showInfo.length() > 0 ? View.VISIBLE : View.GONE);
            tvShowInfo.setText(Html.fromHtml(showInfo));
            setTexts(etInput, value);

            // 0:表示数字，0.0：表示有小数点，+0：表示全部为数字都是正整数，none:调用系统输入键盘
            inputType = bundle.getString("inputType", "0");
            if (inputType.equals("0") || inputType.equals("0.0") || inputType.equals("+0")) {
                // 如果传过来的值为0.0这种格式，就把.0去掉
                if(value.indexOf(".") > -1) {
                    double d = parseDouble(value);
                    setTexts(etInput, d > 0 ? df.format(d) : "");
                }
                nums[10] = inputType.equals("0") ? "" : "."; // 如果为数字就把小数点去掉
                nums[10] = inputType.equals("+0") ? "" : "."; // 如果为数字就把小数点去掉
                nums[11] = inputType.equals("+0") ? "" : "-"; // 如果为数字就把小数点去掉

                etInput.setEnabled(false);
                tv_tmp.setVisibility(View.GONE);

                gridNums.setVisibility(View.VISIBLE);
                MyAdapter adapter = new MyAdapter();
                gridNums.setAdapter(adapter);

                gridNums.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int pos, long arg3) {
                        String item = nums[pos];
//						if(item.equals("OK")) { // 返回回去
//							setResults(context, getValues(etInput));
//							context.finish();
//
                        if (item.equals("-")) { // 减号，每次点击都到第一个位置
                            String val = getValues(etInput);
                            setTexts(etInput, "-" + val.replace("-", ""));

                            String val2 = getValues(etInput);
                            if (val2.equals("-0")) {
                                setTexts(etInput, "");
                            }

                        } else {
                            setTexts(etInput, getValues(etInput) + item);
                        }
                    }
                });
            } else {
                // 显示输入框
                mHandler.sendEmptyMessageDelayed(SHOW_INPUT, 200);

            }
        }
    }

    @OnClick({R.id.btn_close, R.id.btn_confirm, R.id.tv_clear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                hideKeyboard(etInput);
                context.finish();

                break;
            case R.id.btn_confirm: // 确定按钮
                hideKeyboard(etInput);
                String inputName = getValues(etInput).trim();
                if (inputType.equals("0") || inputType.equals("0.0")) {
                    double num = parseDouble(inputName);
                    if(num == 0) {
                        toasts("请输入数量！");
                        return;
                    }
                    inputName = df.format(parseDouble(inputName));

                } else if (inputName.length() == 0) {
                    toasts("请输入内容！");
                    return;
                }
                setResults(context, inputName);
                context.finish();

                break;
            case R.id.tv_clear:
                etInput.setText("");
                
                break;
        }
    }

    /**
     * 适配器
     */
    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return nums.length;
        }

        @Override
        public Object getItem(int position) {
            return nums[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int pos, View v, ViewGroup parent) {
            ViewHolder holder = null;
            if (v == null) {
                v = context.getLayoutInflater().inflate(R.layout.ab_public_input_item, null);
                holder = new ViewHolder(v);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            holder.tv_item.setText(nums[pos]);

            return v;
        }

        class ViewHolder {
            TextView tv_item;

            public ViewHolder(View v) {
                tv_item = (TextView) v.findViewById(R.id.tv_item);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeHandler(mHandler);
            context.finish();
        }
        return false;
    }

}
