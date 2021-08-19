package ykk.cb.com.zcws.sales;

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

public class Sal_DsOutReturnMainActivity extends BaseActivity {

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

    private Sal_DsOutReturnMainActivity context = this;
    private static final String TAG = "Sal_OutMainActivity";
    private View curRadio;
    public boolean isChange; // 返回的时候是否需要判断数据是否保存了
    public boolean isKeyboard; // 是否使用软键盘
    private IFragmentExec fragment2Exec;
    private List<Map<String, Object>> listMaps = new ArrayList<>();
    private boolean isConnected; // 蓝牙是否连接标识
//    private boolean isPair; // 蓝牙是否打印了
    private int tabFlag;
    private int id = 0; // 设备id
    private ThreadPool threadPool;
    private DecimalFormat df = new DecimalFormat("#.####");
    private static final int CONN_STATE_DISCONN = 0x007; // 连接状态断开
    private static final int PRINTER_COMMAND_ERROR = 0x008; // 使用打印机指令错误
    private static final int CONN_PRINTER = 0x12;
//    private Customer customer; // 客户

    @Override
    public int setLayoutResID() {
        return R.layout.sal_ds_out_return_main;
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
//        fragment1.setArguments(bundle2); // 传参数
//        fragment2.setArguments(bundle2); // 传参数
        Sal_DsOutReturnFragment1 fragment1 = new Sal_DsOutReturnFragment1();
//        Sal_OutFragment2 fragment2 = new Sal_OutFragment2();
//        Sal_OutFragment3 fragment3 = new Sal_OutFragment3();

        listFragment.add(fragment1);
//        listFragment.add(fragment2);
//        listFragment.add(fragment3);
//        viewPager.setScanScroll(false); // 禁止左右滑动
        //ViewPager设置适配器
        viewPager.setAdapter(new BaseFragmentAdapter(getSupportFragmentManager(), listFragment));
        //ViewPager显示第一个Fragment
        viewPager.setCurrentItem(0);

        //ViewPager页面切换监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        tabChange(viewRadio1, "销售出库--销售订单", 0);

                        break;
                    case 1:
                        tabChange(viewRadio2, "销售出库--箱码", 1);

                        break;
                    case 2:
                        tabChange(viewRadio3, "销售出库--拣货单", 2);

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

    @OnClick({R.id.btn_close, R.id.btn_print, R.id.lin_tab1, R.id.lin_tab2, R.id.lin_tab3})
    public void onViewClicked(View view) {
        // setCurrentItem第二个参数控制页面切换动画
        //  true:打开/false:关闭
        //  viewPager.setCurrentItem(0, false);

        switch (view.getId()) {
            case R.id.btn_close: // 关闭
//                if(isChange) {
//                    AlertDialog.Builder build = new AlertDialog.Builder(context);
//                    build.setIcon(R.drawable.caution);
//                    build.setTitle("系统提示");
//                    build.setMessage("您有未保存的数据，继续关闭吗？");
//                    build.setPositiveButton("是", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            context.finish();
//                        }
//                    });
//                    build.setNegativeButton("否", null);
//                    build.setCancelable(false);
//                    build.show();
//                } else {
                    context.finish();
//                }

                break;
            case R.id.btn_print: // 打印
                show(PrintMainActivity.class,null);

                break;
            case R.id.lin_tab1:
                tabChange(viewRadio1, "销售出库--销售订单", 0);

                break;
            case R.id.lin_tab2:
                tabChange(viewRadio2, "销售出库--箱码", 1);

                break;
            case R.id.lin_tab3:
                tabChange(viewRadio3, "销售出库--拣货单", 2);

                break;
        }
    }

    @OnLongClick({R.id.btn_close})
    public boolean onViewLongClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close: // 测试打印的
                if(isConnected) {
                    setBoxListPrint();
                } else {
                    // 打开蓝牙配对页面
                    startActivityForResult(new Intent(context, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
                }

                break;
        }
        return true;
    }

    /**
     * 选中之后改变样式
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
        // 按了删除键，回退键
//        if(!isKeyboard && (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
        // 240 为PDA两侧面扫码键，241 为PDA中间扫码键
        if(!(event.getKeyCode() == 240 || event.getKeyCode() == 241)) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 生产装箱清单
     * @param flag
     */
    public void setFragment2Print(int flag, List<Map<String, Object>> listMaps) {
        tabFlag = flag;
        if(tabFlag != flag) {
//            isConnected = false;
        }
        // 清空list
        context.listMaps.clear();

        context.listMaps.addAll(listMaps);

        if(isConnected) {
            setBoxListPrint();
        } else {
            // 打开蓝牙配对页面
            startActivityForResult(new Intent(this, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
        }
    }

    /**
     * 设置生产装箱清单打印格式
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
//            // 绘制箱子条码
//            for(int j=0; j<sizeJ; j++) {
////                setBoxListFormat2(j, caseId, barcode, listMbr);
//            }
//        }
//        setBoxListFormat3();
        fragment2Exec.onFragmenExec();
    }

//    /**
//     * 打印头部1
//     */
//    private void setBoxListFormat1(int caseId, String barcode, List<MaterialBinningRecord> listMbr) {
//        LabelCommand tsc = new LabelCommand();
//        setTscBegin(tsc);
//        // --------------- 打印区-------------Begin
//
//        int beginXPos = 20; // 开始横向位置
//        int beginYPos = 12; // 开始纵向位置
//        int rowHigthSum = 0; // 纵向高度的叠加
//        int rowSpacing = 30; // 每行之间的距离
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
//        // 绘制箱子条码
//        rowHigthSum = beginYPos + 18;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"箱码： \n");
//        tsc.add1DBarcode(115, rowHigthSum-18, LabelCommand.BARCODETYPE.CODE39, 65, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, 2, 5, barcode);
//        rowHigthSum = beginYPos + 96;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"物流公司："+deliveryCompanyName+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"客户名称："+custName+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"订单编号："+isNULLS(mbr.getSalOrderNo())+" \n");
//        tsc.addText(280, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"订单日期："+fDate+" \n");
//
//        // --------------- 打印区-------------End
//        setTscEnd(tsc);
//    }
//
//    /**
//     * 打印物料信息2
//     */
//    private void setBoxListFormat2(int pos, int caseId, String barcode, List<MaterialBinningRecord> listMbr) {
//        LabelCommand tsc = new LabelCommand();
//        setTscBegin(tsc);
//        // --------------- 打印区-------------Begin
//
//        int beginXPos = 20; // 开始横向位置
//        int beginYPos = 0; // 开始纵向位置
//        int rowHigthSum = 0; // 纵向高度的叠加
//        int rowSpacing = 35; // 每行之间的距离
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
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"物料编码："+mtlFnumber+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"物料名称："+mtlFname+" \n");
//
//        String strTmp = "";
//        if (leaf.length() > 0 && leaf2.length() > 0) strTmp = leaf + " , " + leaf2;
//        else if (leaf.length() > 0) strTmp = leaf;
//        else if (leaf2.length() > 0) strTmp = leaf2;
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"面料："+strTmp+" \n");
//        rowHigthSum = rowHigthSum + rowSpacing;
//        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"数量："+df.format(mbr.getNumber())+" \n");
//        tsc.addText(200, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"宽："+width+" \n");
//        tsc.addText(360, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"高："+high+" \n");
////        rowHigthSum = rowHigthSum + rowSpacing;
////        tsc.addText(beginXPos, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"------------------------------------------------- \n");
//
//        // --------------- 打印区-------------End
//        setTscEnd(tsc);
//    }

    /**
     * 打印日期
     */
    private void setBoxListFormat3() {
        LabelCommand tsc = new LabelCommand();
        setTscBegin(tsc);
        // --------------- 打印区-------------Begin

        int beginXPos = 20; // 开始横向位置
        int beginYPos = 0; // 开始纵向位置
        int rowHigthSum = 0; // 纵向高度的叠加
        int rowSpacing = 30; // 每行之间的距离
        String date = Comm.getSysDate(7);

        tsc.addText(beginXPos, beginYPos, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"------------------------------------------------- \n");
        rowHigthSum = rowHigthSum + rowSpacing;
        tsc.addText(300, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,"打印日期："+date+" \n");

        // --------------- 打印区-------------End
        setTscEnd(tsc);
    }

    /**
     * 打印前段配置
     * @param tsc
     */
    private void setTscBegin(LabelCommand tsc) {
        // 设置标签尺寸，按照实际尺寸设置
        tsc.addSize(78, 26);
        // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
//        tsc.addGap(10);
        tsc.addGap(0);
        // 设置打印方向
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
        // 开启带Response的打印，用于连续打印
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON);
        // 设置原点坐标
        tsc.addReference(0, 0);
        // 撕纸模式开启
        tsc.addTear(EscCommand.ENABLE.ON);
        // 清除打印缓冲区
        tsc.addCls();
    }
    /**
     * 打印后段配置
     * @param tsc
     */
    private void setTscEnd(LabelCommand tsc) {
        // 打印标签
        tsc.addPrint(1, 1);
        // 打印标签后 蜂鸣器响

        tsc.addSound(2, 100);
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
        Vector<Byte> datas = tsc.getCommand();
        // 发送数据
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            return;
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
    }

    /**
     * 蓝牙监听广播
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                // 蓝牙连接断开广播
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
                                tvConnState.setTextColor(Color.parseColor("#666666")); // 未连接-灰色
                                isConnected = false;
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            tvConnState.setText(getString(R.string.str_conn_state_connecting));
                            tvConnState.setTextColor(Color.parseColor("#6a5acd")); // 连接中-紫色
                            isConnected = false;

                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
//                            tvConnState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            tvConnState.setText(getString(R.string.str_conn_state_connected));
                            tvConnState.setTextColor(Color.parseColor("#008800")); // 已连接-绿色

                            switch (tabFlag) {
                                case 2: // 装箱清单打印条码
                                    setBoxListPrint();

                                    break;
                            }

                            isConnected = true;

                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(context, getString(R.string.str_conn_fail));
                            tvConnState.setText(getString(R.string.str_conn_state_disconnect));
                            tvConnState.setTextColor(Color.parseColor("#666666")); // 未连接-灰色
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
                    //初始化端口信息
                    new DeviceConnFactoryManager.Build()
                            //设置端口连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //设置端口IP地址
                            .setIp(strIp)
                            //设置端口ID（主要用于连接多设备）
                            .setId(id)
                            //设置连接的热点端口号
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
            /*蓝牙连接*/
            case Constant.BLUETOOTH_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
//                    isPair = true;
                    /*获取蓝牙mac地址*/
                    String macAddress = data.getStringExtra(BluetoothDeviceListDialog.EXTRA_DEVICE_ADDRESS);
                    //初始化话DeviceConnFactoryManager
                    new DeviceConnFactoryManager.Build()
                            .setId(id)
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(macAddress)
                            .build();
                    //打开端口
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                }
//                if(!isPair) {
//                    // 打开蓝牙配对页面
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
