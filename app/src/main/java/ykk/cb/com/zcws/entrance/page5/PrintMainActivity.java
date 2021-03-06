package ykk.cb.com.zcws.entrance.page5;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import butterknife.BindView;
import butterknife.OnClick;
import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.bean.prod.ProdOrder;
import ykk.cb.com.zcws.comm.BaseActivity;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.util.MyViewPager;
import ykk.cb.com.zcws.util.adapter.BaseFragmentAdapter;
import ykk.cb.com.zcws.util.blueTooth.BluetoothDeviceListDialog;
import ykk.cb.com.zcws.util.blueTooth.Constant;
import ykk.cb.com.zcws.util.blueTooth.DeviceConnFactoryManager;
import ykk.cb.com.zcws.util.blueTooth.ThreadPool;
import ykk.cb.com.zcws.util.blueTooth.Utils;
import ykk.cb.com.zcws.util.interfaces.IFragmentKeyeventListener;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static ykk.cb.com.zcws.util.blueTooth.Constant.MESSAGE_UPDATE_PARAMETER;
import static ykk.cb.com.zcws.util.blueTooth.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class PrintMainActivity extends BaseActivity {

    @BindView(R.id.viewPager)
    MyViewPager viewPager;
    @BindView(R.id.tv_connState)
    TextView tvConnState;
    @BindView(R.id.tv_1)
    TextView tv1;
    @BindView(R.id.tv_2)
    TextView tv2;
    @BindView(R.id.tv_3)
    TextView tv3;
    private PrintMainActivity context = this;
    private static final String TAG = "PrintMainActivity";
    private TextView curText;
    private IFragmentKeyeventListener fragment2Listener;
    private List<String> barcodeList = new ArrayList<>(); // ???????????????
    private String barcode; // ???????????????
    private boolean isConnected; // ????????????????????????
    private int tabFlag;
    private int id = 0; // ??????id
    private ThreadPool threadPool;
    private List<ProdOrder> prodOrderList = new ArrayList<>();
    private DecimalFormat df = new DecimalFormat("#.####");
    private static final int CONN_STATE_DISCONN = 0x007; // ??????????????????
    private static final int PRINTER_COMMAND_ERROR = 0x008; // ???????????????????????????
    private static final int CONN_PRINTER = 0x12;

    @Override
    public int setLayoutResID() {
        return R.layout.ab_print_main;
    }

    @Override
    public void initData() {

        curText = tv1;
        List<Fragment> listFragment = new ArrayList<Fragment>();
//        Bundle bundle2 = new Bundle();
//        bundle2.putSerializable("customer", customer);
//        fragment1.setArguments(bundle2); // ?????????
//        fragment2.setArguments(bundle2); // ?????????
        PrintFragment1 fragment1 = new PrintFragment1();
//        PrintFragment2 fragment2 = new PrintFragment2();
//        PrintFragment3 fragment3 = new PrintFragment3();

        listFragment.add(fragment1);
//        listFragment.add(fragment2);
//        listFragment.add(fragment3);
//        viewPager.setScanScroll(false); // ??????????????????
        //ViewPager???????????????
        viewPager.setAdapter(new BaseFragmentAdapter(getSupportFragmentManager(), listFragment));
        //ViewPager???????????????Fragment
        viewPager.setCurrentItem(0);

        //ViewPager??????????????????
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        tabChange(tv1,0);
                        break;
                    case 1:
                        tabChange(tv2,1);
                        break;
                    case 2:
                        tabChange(tv3,2);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // ?????????????????????2
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tabChange(tv2,1);
//            }
//        },200);
    }

    private void bundle() {
        Bundle bundle = context.getIntent().getExtras();
        if (bundle != null) {
        }
    }

    @OnClick({R.id.btn_close, R.id.tv_1, R.id.tv_2, R.id.tv_3})
    public void onViewClicked(View view) {
        // setCurrentItem???????????????????????????????????????
        //  true:??????/false:??????
        //  viewPager.setCurrentItem(0, false);

        switch (view.getId()) {
            case R.id.btn_close: // ??????
                context.finish();

                break;
            case R.id.tv_1: // ??????
                tabChange(tv1,0);

                break;
            case R.id.tv_2: // ????????????
                tabChange(tv2,1);

                break;
            case R.id.tv_3: // ????????????
                tabChange(tv3,2);

                break;
        }
    }

    /**
     * ????????????????????????
     */
    private void tabSelected(TextView tv) {
        if(curText.getId() == tv.getId()) {
            return;
        }
        curText.setText(getValues(curText).replace("???",""));
        curText.setTextColor(Color.parseColor("#666666" +""));
        tv.setText(getValues(tv)+"???");
        tv.setTextColor(Color.parseColor("#FF3300"));
        curText = tv;
    }

    private void tabChange(TextView tv, int page) {
        tabSelected(tv);
        viewPager.setCurrentItem(page, false);
    }

    /**
     * Fragment??????????????????
     */
    public void setFragment1Data(int flag, List<ProdOrder> list) {
        prodOrderList.clear();
        prodOrderList.addAll(list);
        tabFlag = flag;

        if(isConnected) {
            int size = prodOrderList.size();
            for(int i=0; i<size; i++) {
                setFragment1Print_A(i);
            }
            for(int i=0; i<size; i++) {
                setFragment1Print_B(i);
            }

        } else {
            // ????????????????????????
            startActivityForResult(new Intent(this, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
        }
    }

    /**
     * ????????????
     */
    private void setFragment1Print_A(int pos) {
        ProdOrder prodOrder = prodOrderList.get(pos);
        ICItem icItem = prodOrder.getIcItem();
        String barcode = isNULLS(prodOrder.getStrBarcode());
        String[] arrs = barcode.split(",");
        for(int i=0; i<arrs.length; i++) {
            LabelCommand tsc = new LabelCommand();
            setTscBegin(tsc, 10);
            // --------------- ?????????-------------Begin

            int beginXPos = 20; // ??????????????????
            int beginYPos = 12; // ??????????????????
            int rowHigthSum = 0; // ?????????????????????
            int rowSpacing = 30; // ?????????????????????

            // ??????????????????
            rowHigthSum = beginYPos;
            tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"????????? "+isNULLS(icItem.getSuitVehicleType())+"\n");
            rowHigthSum = rowHigthSum + rowSpacing+30;
            tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"?????????"+isNULLS(icItem.getFunctionDescription())+" \n");
            rowHigthSum = rowHigthSum + rowSpacing+30;
            tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"?????????"+isNULLS(Comm.dateLabel())+" \n");

            // --------------- ?????????-------------End
            setTscEnd(tsc);
        }
    }

    /**
     * ????????????
     */
    private void setFragment1Print_B(int pos) {
        ProdOrder prodOrder = prodOrderList.get(pos);
        ICItem icItem = prodOrder.getIcItem();
        String barcode = isNULLS(prodOrder.getStrBarcode());
        String[] arrs = barcode.split(",");
        // 2???????????????
        for(int i=0; i<arrs.length; i++) {
            LabelCommand tsc = new LabelCommand();
            setTscBegin(tsc, 10);
            // --------------- ?????????-------------Begin

            int beginXPos = 20; // ??????????????????
            int beginYPos = 12; // ??????????????????
            int rowHigthSum = 0; // ?????????????????????
            int rowSpacing = 30; // ?????????????????????

            // ??????????????????
            rowHigthSum = beginYPos + 10;
            // ??????????????????
            tsc.add1DBarcode(beginYPos, rowHigthSum, LabelCommand.BARCODETYPE.CODE39, 60, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, 2, 5, arrs[i]);
            rowHigthSum = rowHigthSum + rowSpacing + 60;

            String mtlName = prodOrder.getIcItemName();
            int tmpLen = mtlName.length();
            String mtlFname1 = null;
            String mtlFname2 = null;
            if(mtlName.length() <= 15) {
                tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+mtlName+" \n");
            } else {
                mtlFname1 = mtlName.substring(0, 15);
                mtlFname2 = mtlName.substring(15, tmpLen);
                tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+mtlFname1+" \n");
                rowHigthSum = rowHigthSum + rowSpacing;
                tsc.addText(80, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,""+mtlFname2+" \n");
            }
            rowHigthSum = rowHigthSum + rowSpacing;
            tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+isNULLS(prodOrder.getIcItemNumber())+" \n");

            // --------------- ?????????-------------End
            setTscEnd(tsc);
        }
    }

    public void setFragmentKeyeventListener(IFragmentKeyeventListener fragmentKeyeventListener) {
        this.fragment2Listener = fragmentKeyeventListener;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // ???????????????????????????????????????
            if(data == null) return;
            switch (requestCode) {
                /*????????????*/
                case Constant.BLUETOOTH_REQUEST_CODE: {
                    /*????????????mac??????*/
                    String macAddress = data.getStringExtra(BluetoothDeviceListDialog.EXTRA_DEVICE_ADDRESS);
                    //????????????DeviceConnFactoryManager
                    new DeviceConnFactoryManager.Build()
                            .setId(id)
                            //??????????????????
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //?????????????????????mac??????
                            .setMacAddress(macAddress)
                            .build();
                    //????????????
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                    break;
                }
            }
        }
    }

    /**
     * ??????????????????
     * @param tsc
     */
    private void setTscBegin(LabelCommand tsc, int gap) {
        // ?????????????????????????????????????????????
        tsc.addSize(50, 30);
        // ?????????????????????????????????????????????????????????????????????????????????0
        tsc.addGap(gap);
        // ??????????????????
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
        // ?????????Response??????????????????????????????
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON);
        // ??????????????????
        tsc.addReference(0, 0);
        // ??????????????????
        tsc.addTear(EscCommand.ENABLE.ON);
        // ?????????????????????
        tsc.addCls();
    }
    /**
     * ??????????????????
     * @param tsc
     */
    private void setTscEnd(LabelCommand tsc) {
        // ????????????
        tsc.addPrint(1, 1);
        // ??????????????? ????????????

        tsc.addSound(2, 100);
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
        Vector<Byte> datas = tsc.getCommand();
        // ????????????
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            return;
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
    }

    /**
     * ??????????????????
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                // ????????????????????????
                case ACTION_USB_DEVICE_DETACHED:
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            if (id == deviceId) {
                                tvConnState.setText(getString(R.string.str_conn_state_disconnect));
                                tvConnState.setTextColor(Color.parseColor("#666666")); // ?????????-??????
                                isConnected = false;
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            tvConnState.setText(getString(R.string.str_conn_state_connecting));
                            tvConnState.setTextColor(Color.parseColor("#6a5acd")); // ?????????-??????
                            isConnected = false;

                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
//                            tvConnState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            tvConnState.setText(getString(R.string.str_conn_state_connected));
                            tvConnState.setTextColor(Color.parseColor("#008800")); // ?????????-??????

                            switch (tabFlag) {
                                case 1: // ????????????
                                    int size = prodOrderList.size();
                                    for(int i=0; i<size; i++) {
                                        setFragment1Print_A(i);
                                    }
                                    for(int i=0; i<size; i++) {
                                        setFragment1Print_B(i);
                                    }

                                    break;
                            }

                            isConnected = true;

                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(context, getString(R.string.str_conn_fail));
                            tvConnState.setText(getString(R.string.str_conn_state_disconnect));
                            tvConnState.setTextColor(Color.parseColor("#666666")); // ?????????-??????
                            isConnected = false;

                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN:
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                    }
                    break;
                case PRINTER_COMMAND_ERROR:
                    Utils.toast(context, getString(R.string.str_choice_printer_command));
                    break;
                case CONN_PRINTER:
                    Utils.toast(context, getString(R.string.str_cann_printer));
                    break;
                case MESSAGE_UPDATE_PARAMETER:
                    String strIp = msg.getData().getString("Ip");
                    String strPort = msg.getData().getString("Port");
                    //?????????????????????
                    new DeviceConnFactoryManager.Build()
                            //????????????????????????
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //????????????IP??????
                            .setIp(strIp)
                            //????????????ID?????????????????????????????????
                            .setId(id)
                            //??????????????????????????????
                            .setPort(Integer.parseInt(strPort))
                            .build();
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy()");
        DeviceConnFactoryManager.closeAllPort();
        if (threadPool != null) {
            threadPool.stopThreadPool();
        }
    }

    private String getConnDeviceInfo() {
        String str = "";
        DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
        if (deviceConnFactoryManager != null
                && deviceConnFactoryManager.getConnState()) {
            if ("USB".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "USB\n";
                str += "USB Name: " + deviceConnFactoryManager.usbDevice().getDeviceName();
            } else if ("WIFI".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "WIFI\n";
                str += "IP: " + deviceConnFactoryManager.getIp() + "\t";
                str += "Port: " + deviceConnFactoryManager.getPort();
            } else if ("BLUETOOTH".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "BLUETOOTH\n";
                str += "MacAddress: " + deviceConnFactoryManager.getMacAddress();
//                deviceConnFactoryManager.get
            } else if ("SERIAL_PORT".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "SERIAL_PORT\n";
                str += "Path: " + deviceConnFactoryManager.getSerialPortPath() + "\t";
                str += "Baudrate: " + deviceConnFactoryManager.getBaudrate();
            }
        }
        return str;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // ?????????Fragment ??????????????????
        if (viewPager.getCurrentItem() == 1 && fragment2Listener!=null){
            boolean isBool = fragment2Listener.onFragmentKeyEvent(event);
            if(!isBool) {
                return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish();
        }
        return false;
    }

}
