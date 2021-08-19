package ykk.cb.com.zcws.basics;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.comm.BaseDialogActivity;

public class PublicInputDialog2 extends BaseDialogActivity {


    @BindView(R.id.btn_close)
    Button btnClose;
    @BindView(R.id.tv_hintName)
    TextView tvHintName;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.tv_itemName)
    TextView tvItemName;
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.tv_clear)
    TextView tvClear;
    @BindView(R.id.tv_itemName2)
    TextView tvItemName2;
    @BindView(R.id.et_input2)
    EditText etInput2;
    @BindView(R.id.tv_clear2)
    TextView tvClear2;
    @BindView(R.id.tv_tmp)
    TextView tvTmp;

    private PublicInputDialog2 context = this;

    // 消息处理
//    private MyHandler mHandler = new MyHandler(this);
//
//    private static class MyHandler extends Handler {
//        private final WeakReference<PublicInputDialog2> parentActivity;
//
//        public MyHandler(PublicInputDialog2 activity) {
//            parentActivity = new WeakReference<PublicInputDialog2>(activity);
//        }
//
//    }

    @Override
    public int setLayoutResID() {
        return R.layout.ab_public_input2;
    }

    @Override
    public void initData() {
        setListener();
        bundle();
    }

    @Override
    public void setListener() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void afterTextChanged(Editable s) {
                tvClear.setVisibility(s.toString().length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
        etInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void afterTextChanged(Editable s) {
                tvClear2.setVisibility(s.toString().length() > 0 ? View.VISIBLE : View.GONE);
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
            String itemName = bundle.getString("itemName", "");
            String itemName2 = bundle.getString("itemName2", "");
            String value = bundle.getString("value", "");
            String value2 = bundle.getString("value2", "");

            tvHintName.setText(hintName);
            tvItemName.setText(itemName);
            tvItemName2.setText(itemName2);
            setTexts(etInput, value);
            setTexts(etInput2, value2);
        }
    }

    @OnClick({R.id.btn_close, R.id.btn_confirm, R.id.tv_clear, R.id.tv_clear2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                context.finish();

                break;
            case R.id.btn_confirm:
                String inputName = getValues(etInput).trim();
                if(inputName.length() == 0) {
                    toasts("请输入"+getValues(tvItemName)+"！");
                    return;
                }
                String inputName2 = getValues(etInput2).trim();
                if(inputName2.length() == 0) {
                    toasts("请输入"+getValues(tvItemName2)+"！");
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("resultVal1", inputName);
                bundle.putString("resultVal2", inputName2);
                setResults(context, bundle);
                context.finish();

                break;
            case R.id.tv_clear:
                etInput.setText("");

                break;
            case R.id.tv_clear2:
                etInput2.setText("");

                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            closeHandler(mHandler);
            context.finish();
        }
        return false;
    }

}
