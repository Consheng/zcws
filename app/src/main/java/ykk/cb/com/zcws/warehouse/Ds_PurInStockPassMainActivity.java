package ykk.cb.com.zcws.warehouse;

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
import android.widget.Button;
import android.widget.TextView;

import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ykk.cb.com.zcws.R;
import ykk.cb.com.zcws.comm.BaseActivity;
import ykk.cb.com.zcws.comm.Comm;
import ykk.cb.com.zcws.entrance.page5.PrintMainActivity;
import ykk.cb.com.zcws.util.MyViewPager;
import ykk.cb.com.zcws.util.adapter.BaseFragmentAdapter;
import ykk.cb.com.zcws.util.blueTooth.BluetoothDeviceListDialog;
import ykk.cb.com.zcws.util.blueTooth.Constant;
import ykk.cb.com.zcws.util.blueTooth.DeviceConnFactoryManager;
import ykk.cb.com.zcws.util.blueTooth.ThreadPool;
import ykk.cb.com.zcws.util.blueTooth.Utils;
import ykk.cb.com.zcws.util.interfaces.IFragmentExec;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static ykk.cb.com.zcws.util.blueTooth.Constant.MESSAGE_UPDATE_PARAMETER;
import static ykk.cb.com.zcws.util.blueTooth.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class Ds_PurInStockPassMainActivity extends BaseActivity {

    @BindView(R.id.viewRadio1)
    View viewRadio1;
    @BindView(R.id.viewRadio2)
    View viewRadio2;
    @BindView(R.id.viewRadio3)
    View viewRadio3;
    @BindView(R.id.btn_close)
    Button btnClose;
    @BindView(R.id.viewPager)
    MyViewPager viewPager;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_connState)
    TextView tvConnState;

    private Ds_PurInStockPassMainActivity context = this;
    private static final String TAG = "Ds_PurInStockPassMainActivity";
    private View curRadio;
    public boolean isChange; // ??????????????????????????????????????????????????????
    public boolean isKeyboard; // ?????????????????????
    private IFragmentExec fragment2Exec;
    private List<Map<String, Object>> listMaps = new ArrayList<>();
    private boolean isConnected; // ????????????????????????
//    private boolean isPair; // ?????????????????????
    private int tabFlag;
    private int id = 0; // ??????id
    private ThreadPool threadPool;
    private DecimalFormat df = new DecimalFormat("#.####");
    private static final int CONN_STATE_DISCONN = 0x007; // ??????????????????
    private static final int PRINTER_COMMAND_ERROR = 0x008; // ???????????????????????????
    private static final int CONN_PRINTER = 0x12;
    private Ds_PurInStockPassFragment1 fragment1 = new Ds_PurInStockPassFragment1();
//    private Customer customer; // ??????

    @Override
    public int setLayoutResID() {
        return R.layout.ware_ds_instock_pass_main;
    }

    @Override
    public void initData() {
//        Bundle bundle = context.getIntent().getExtras();
//        if (bundle != null) {
//            customer = (Customer) bundle.getSerializable("customer");
//        }

        curRadio = viewRadio2;
        List<Fragment> listFragment = new ArrayList<Fragment>();
//        Bundle bundle2 = new Bundle();
//        bundle2.putSerializable("customer", customer);
//        fragment1.setArguments(bundle2); // ?????????
//        fragment2.setArguments(bundle2); // ?????????
//        Pur_ScInFragment1 fragment1 = new Pur_ScInFragment1();
//        Sal_OutFragment2 fragment2 = new Sal_OutFragment2();
//        Sal_OutFragment3 fragment3 = new Sal_OutFragment3();

        listFragment.add(fragment1);
//        listFragment.add(fragment2);
//        listFragment.add(fragment3);
//        viewPager.setScanScroll(false); // ??????????????????
        //ViewPager???????????????
        viewPager.setAdapter(new BaseFragmentAdapter(getSupportFragmentManager(), listFragment));
        //ViewPager???????????????Fragment
        viewPager.setCurrentItem(1);

        //ViewPager??????????????????
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        tabChange(viewRadio1, "????????????--????????????", 0);

                        break;
                    case 1:
                        tabChange(viewRadio2, "????????????--??????", 1);

                        break;
                    case 2:
                        tabChange(viewRadio3, "????????????--?????????", 2);

                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void bundle() {
        Bundle bundle = context.getIntent().getExtras();
        if (bundle != null) {
//            customer = bundle.getParcelable("customer");
        }
    }

    @OnClick({R.id.btn_close, R.id.btn_print, R.id.lin_tab1, R.id.lin_tab2, R.id.lin_tab3, R.id.lin_find})
    public void onViewClicked(View view) {
        // setCurrentItem???????????????????????????????????????
        //  true:??????/false:??????
        //  viewPager.setCurrentItem(0, false);

        switch (view.getId()) {
            case R.id.btn_close: // ??????
//                if(isChange) {
//                    AlertDialog.Builder build = new AlertDialog.Builder(context);
//                    build.setIcon(R.drawable.caution);
//                    build.setTitle("????????????");
//                    build.setMessage("?????????????????????????????????????????????");
//                    build.setPositiveButton("???", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            context.finish();
//                        }
//                    });
//                    build.setNegativeButton("???", null);
//                    build.setCancelable(false);
//                    build.show();
//                } else {
                    context.finish();
//                }

                break;
            case R.id.lin_find: // ???????????????

                break;
            case R.id.btn_print: // ??????
                show(PrintMainActivity.class,null);

                break;
            case R.id.lin_tab1:
                tabChange(viewRadio1, "????????????--????????????", 0);

                break;
            case R.id.lin_tab2:
                tabChange(viewRadio2, "????????????--??????", 1);

                break;
            case R.id.lin_tab3:
                tabChange(viewRadio3, "????????????--?????????", 2);

                break;
        }
    }

    @OnLongClick({R.id.btn_close})
    public boolean onViewLongClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close: // ???????????????
                if(isConnected) {
                    setBoxListPrint();
                } else {
                    // ????????????????????????
                    startActivityForResult(new Intent(context, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
                }

                break;
        }
        return true;
    }

    /**
     * ????????????????????????
     */
    private void tabSelected(View v) {
        curRadio.setBackgroundResource(R.drawable.check_off2);
        v.setBackgroundResource(R.drawable.check_on);
        curRadio = v;
    }

    private void tabChange(View view, String str, int page) {
        tabSelected(view);
        tvTitle.setText(str);
        viewPager.setCurrentItem(page, false);
    }

    public void setFragmentExec(IFragmentExec fragment2Exec) {
        this.fragment2Exec = fragment2Exec;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // ???????????????????????????
//        if(!isKeyboard && (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
        // 240 ???PDA?????????????????????241 ???PDA???????????????
        if(!(event.getKeyCode() == 240 || event.getKeyCode() == 241)) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * ??????????????????
     * @param flag
     */
    public void setFragment2Print(int flag, List<Map<String, Object>> listMaps) {
        tabFlag = flag;
        if(tabFlag != flag) {
//            isConnected = false;
        }
        // ??????list
        context.listMaps.clear();

        context.listMaps.addAll(listMaps);

        if(isConnected) {
            setBoxListPrint();
        } else {
            // ????????????????????????
            startActivityForResult(new Intent(this, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
        }
    }

    /**
     * ????????????????????????????????????
     */
    private void setBoxListPrint() {
//        for(int i=0, size=listMaps.size(); i<size; i++) {
//            Map<String, Object> maps = listMaps.get(i);
//
//            int caseId = parseInt(maps.get("caseId"));
//            String barcode = maps.get("barcode").toString();
//            List<MaterialBinningRecord> listMbr = (List<MaterialBinningRecord>) maps.get("list");
//            int sizeJ = listMbr.size();
//
////            setBoxListFormat1(caseId, barcode, listMbr);
//            // ??????????????????
//            for(int j=0; j<sizeJ; j++) {
////                setBoxListFormat2(j, caseId, barcode, listMbr);
//            }
//        }
//        setBoxListFormat3();
        fragment2Exec.onFragmenExec();
    }

//    /**
//     * ????????????1
//     */
//    private void setBoxListFormat1(int caseId, String barcode, List<MaterialBinningRecord> listMbr) {
//        LabelCommand tsc = new LabelCommand();
//        setTscBegin(tsc);
//        // --------------- ?????????-------------Begin
//
//        int beginXPos = 20; // ??????????????????
//        int beginYPos = 12; // ??????????????????
//        int rowHigthSum = 0; // ?????????????????????
//        int rowSpacing = 30; // ?????????????????????
//
//        MaterialBinningRecord mbr = listMbr.get(0);
//        String custName = "", deliveryCompanyName = "", fDate = "";
//        if(caseId == 34) {
//            ProdOrder prodOrder = JsonUtil.stringToObject(mbr.getRelationObj(), ProdOrder.class);
//            custName = isNULLS(prodOrder.getCombineSalCustName());
//            deliveryCompanyName = isNULLS(prodOrder.getDeliveryCompanyName());
//            fDate = isNULLS(prodOrder.getProdFdate());
//            if(fDate.length() > 6) {
//                fDate = fDate.substring(0,10);
//            }
//        } else if(caseId == 37) {
//            DeliOrder deliOrder = JsonUtil.stringToObject(mbr.getRelationObj(), DeliOrder.class);
//            custName = isNULLS(deliOrder.getCombineSalCustName());
//            deliveryCompanyName = isNULLS(deliOrder.getDeliveryCompanyName());
//            fDate = isNULLS(deliOrder.getDeliDate());
//            if(fDate.length() > 6) {
//                fDate = fDate.substring(0,10);
//            }
//        }
//
//        // ??????????????????
//        rowHigthSum = beginYPos + 18;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"????????? \n");
//        tsc.add1DBarcode(115, rowHigthSum-18, LabelCommand.BARCODETYPE.CODE39, 65, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, 2, 5, barcode);
//        rowHigthSum = beginYPos + 96;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+deliveryCompanyName+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+custName+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+isNULLS(mbr.getSalOrderNo())+" \n");
//        tsc.addText(280, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+fDate+" \n");
//
//        // --------------- ?????????-------------End
//        setTscEnd(tsc);
//    }
//
//    /**
//     * ??????????????????2
//     */
//    private void setBoxListFormat2(int pos, int caseId, String barcode, List<MaterialBinningRecord> listMbr) {
//        LabelCommand tsc = new LabelCommand();
//        setTscBegin(tsc);
//        // --------------- ?????????-------------Begin
//
//        int beginXPos = 20; // ??????????????????
//        int beginYPos = 0; // ??????????????????
//        int rowHigthSum = 0; // ?????????????????????
//        int rowSpacing = 35; // ?????????????????????
//
//        MaterialBinningRecord mbr = listMbr.get(pos);
//        String mtlFnumber = "", mtlFname = "", leaf = "", leaf2 = "", width = "", high = "";
//        if(caseId == 34) {
//            ProdOrder prodOrder = JsonUtil.stringToObject(mbr.getRelationObj(), ProdOrder.class);
//            mtlFnumber = isNULLS(prodOrder.getMtlFnumber());
//            mtlFname = isNULLS(prodOrder.getMtlFname());
//            leaf = isNULLS(prodOrder.getLeaf());
//            leaf2 = isNULLS(prodOrder.getLeaf1());
//            width = isNULLS(prodOrder.getWidth());
//            high = isNULLS(prodOrder.getHigh());
//
//        } else if(caseId == 37){
//            DeliOrder deliOrder = JsonUtil.stringToObject(mbr.getRelationObj(), DeliOrder.class);
//            mtlFnumber = isNULLS(deliOrder.getMtlFnumber());
//            mtlFname = isNULLS(deliOrder.getMtlFname());
//            leaf = isNULLS(deliOrder.getLeaf());
//            leaf2 = isNULLS(deliOrder.getLeaf1());
//            width = isNULLS(deliOrder.getWidth());
//            high = isNULLS(deliOrder.getHigh());
//        }
//
//        tsc.addText(beginXPos, beginYPos, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"------------------------------------------------- \n");
//        rowHigthSum = beginYPos + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+mtlFnumber+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+mtlFname+" \n");
//
//        String strTmp = "";
//        if (leaf.length() > 0 && leaf2.length() > 0) strTmp = leaf + " , " + leaf2;
//        else if (leaf.length() > 0) strTmp = leaf;
//        else if (leaf2.length() > 0) strTmp = leaf2;
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"?????????"+strTmp+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"?????????"+df.format(mbr.getNumber())+" \n");
//        tsc.addText(200, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"??????"+width+" \n");
//        tsc.addText(360, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"??????"+high+" \n");
////        rowHigthSum = rowHigthSum + rowSpacing;
////        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"------------------------------------------------- \n");
//
//        // --------------- ?????????-------------End
//        setTscEnd(tsc);
//    }

    /**
     * ????????????
     */
    private void setBoxListFormat3() {
        LabelCommand tsc = new LabelCommand();
        setTscBegin(tsc);
        // --------------- ?????????-------------Begin

        int beginXPos = 20; // ??????????????????
        int beginYPos = 0; // ??????????????????
        int rowHigthSum = 0; // ?????????????????????
        int rowSpacing = 30; // ?????????????????????
        String date = Comm.getSysDate(7);

        tsc.addText(beginXPos, beginYPos, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"------------------------------------------------- \n");
        rowHigthSum = rowHigthSum + rowSpacing;
        tsc.addText(300, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"???????????????"+date+" \n");

        // --------------- ?????????-------------End
        setTscEnd(tsc);
    }

    /**
     * ??????????????????
     * @param tsc
     */
    private void setTscBegin(LabelCommand tsc) {
        // ?????????????????????????????????????????????
        tsc.addSize(78, 26);
        // ?????????????????????????????????????????????????????????????????????????????????0
//        tsc.addGap(10);
        tsc.addGap(0);
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
                                case 2: // ????????????????????????
                                    setBoxListPrint();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            /*????????????*/
            case Constant.BLUETOOTH_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
//                    isPair = true;
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
                }
//                if(!isPair) {
//                    // ????????????????????????
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startActivityForResult(new Intent(context, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
//                        }
//                    },500);
//
//                }
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish();
        }
        return false;
    }

}
