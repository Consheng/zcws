package ykk.cb.com.zcws.produce

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import butterknife.OnLongClick
import com.gprinter.command.EscCommand
import com.gprinter.command.LabelCommand
import kotlinx.android.synthetic.main.prod_sc_in_main.*
import ykk.cb.com.zcws.R
import ykk.cb.com.zcws.comm.BaseActivity
import ykk.cb.com.zcws.comm.Comm
import ykk.cb.com.zcws.entrance.page5.PrintMainActivity
import ykk.cb.com.zcws.util.adapter.BaseFragmentAdapter
import ykk.cb.com.zcws.util.blueTooth.*
import ykk.cb.com.zcws.util.blueTooth.Constant.MESSAGE_UPDATE_PARAMETER
import ykk.cb.com.zcws.util.blueTooth.DeviceConnFactoryManager.CONN_STATE_FAILED
import ykk.cb.com.zcws.util.interfaces.IFragmentExec
import java.text.DecimalFormat
import java.util.*

class Prod_ScInMainActivity : BaseActivity() {

    companion object {
        private val TAG = "Sal_OutMainActivity"
        private val CONN_STATE_DISCONN = 0x007 // 连接状态断开
        private val PRINTER_COMMAND_ERROR = 0x008 // 使用打印机指令错误
        private val CONN_PRINTER = 0x12
    }
    private val context = this
    private var curRadio: View? = null
    var isChange: Boolean = false // 返回的时候是否需要判断数据是否保存了
    var isKeyboard: Boolean = false // 是否使用软键盘
    private var fragment2Exec: IFragmentExec? = null
    private val listMaps = ArrayList<Map<String, Any>>()
    private var isConnected: Boolean = false // 蓝牙是否连接标识
    //    private boolean isPair; // 蓝牙是否打印了
    private var tabFlag: Int = 0
    private val id = 0 // 设备id
    private var threadPool: ThreadPool? = null
    private val df = DecimalFormat("#.####")

    /**
     * 蓝牙监听广播
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                // 蓝牙连接断开广播
                ACTION_USB_DEVICE_DETACHED, BluetoothDevice.ACTION_ACL_DISCONNECTED -> mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget()
                DeviceConnFactoryManager.ACTION_CONN_STATE -> {
                    val state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1)
                    val deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1)
                    when (state) {
                        DeviceConnFactoryManager.CONN_STATE_DISCONNECT -> if (id == deviceId) {
                            tv_connState!!.text = getString(R.string.str_conn_state_disconnect)
                            tv_connState!!.setTextColor(Color.parseColor("#666666")) // 未连接-灰色
                            isConnected = false
                        }
                        DeviceConnFactoryManager.CONN_STATE_CONNECTING -> {
                            tv_connState!!.text = getString(R.string.str_conn_state_connecting)
                            tv_connState!!.setTextColor(Color.parseColor("#6a5acd")) // 连接中-紫色
                            isConnected = false
                        }
                        DeviceConnFactoryManager.CONN_STATE_CONNECTED -> {
                            //                            tv_connState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            tv_connState!!.text = getString(R.string.str_conn_state_connected)
                            tv_connState!!.setTextColor(Color.parseColor("#008800")) // 已连接-绿色

                            when (tabFlag) {
                                2 // 装箱清单打印条码
                                -> setBoxListPrint()
                            }

                            isConnected = true
                        }
                        CONN_STATE_FAILED -> {
                            Utils.toast(context, getString(R.string.str_conn_fail))
                            tv_connState!!.text = getString(R.string.str_conn_state_disconnect)
                            tv_connState!!.setTextColor(Color.parseColor("#666666")) // 未连接-灰色
                            isConnected = false
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                CONN_STATE_DISCONN -> if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id)
                }
                PRINTER_COMMAND_ERROR -> Utils.toast(context, getString(R.string.str_choice_printer_command))
                CONN_PRINTER -> Utils.toast(context, getString(R.string.str_cann_printer))
                MESSAGE_UPDATE_PARAMETER -> {
                    val strIp = msg.data.getString("Ip")
                    val strPort = msg.data.getString("Port")
                    //初始化端口信息
                    DeviceConnFactoryManager.Build()
                            //设置端口连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //设置端口IP地址
                            .setIp(strIp)
                            //设置端口ID（主要用于连接多设备）
                            .setId(id)
                            //设置连接的热点端口号
                            .setPort(Integer.parseInt(strPort))
                            .build()
                    threadPool = ThreadPool.getInstantiation()
                    threadPool!!.addTask { DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort() }
                }
                else -> {
                }
            }
        }
    }
    //    private Customer customer; // 客户

    override fun setLayoutResID(): Int {
        return R.layout.prod_sc_in_main
    }

    override fun initData() {
        //        Bundle bundle = context.getIntent().getExtras();
        //        if (bundle != null) {
        //            customer = (Customer) bundle.getSerializable("customer");
        //        }

        curRadio = viewRadio2
        val listFragment = ArrayList<Fragment>()
        //        Bundle bundle2 = new Bundle();
        //        bundle2.putSerializable("customer", customer);
        //        fragment1.setArguments(bundle2); // 传参数
        //        fragment2.setArguments(bundle2); // 传参数
        val fragment1 = Prod_ScInFragment1()
        //        Sal_OutFragment2 fragment2 = new Sal_OutFragment2();
        //        Sal_OutFragment3 fragment3 = new Sal_OutFragment3();

        listFragment.add(fragment1)
        //        listFragment.add(fragment2);
        //        listFragment.add(fragment3);
        //        viewPager.setScanScroll(false); // 禁止左右滑动
        //ViewPager设置适配器
        viewPager!!.adapter = BaseFragmentAdapter(supportFragmentManager, listFragment)
        //ViewPager显示第一个Fragment
        viewPager!!.currentItem = 1

        //ViewPager页面切换监听
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> tabChange(viewRadio1, "销售出库--销售订单", 0)
                    1 -> tabChange(viewRadio2, "销售出库--箱码", 1)
                    2 -> tabChange(viewRadio3, "销售出库--拣货单", 2)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun bundle() {
        val bundle = context.intent.extras
        if (bundle != null) {
            //            customer = bundle.getParcelable("customer");
        }
    }

    @OnClick(R.id.btn_close, R.id.btn_print, R.id.lin_tab1, R.id.lin_tab2, R.id.lin_tab3)
    fun onViewClicked(view: View) {
        // setCurrentItem第二个参数控制页面切换动画
        //  true:打开/false:关闭
        //  viewPager.setCurrentItem(0, false);

        when (view.id) {
            R.id.btn_close // 关闭
            ->
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
                context.finish()
            R.id.btn_print // 打印
            -> show(PrintMainActivity::class.java, null)
            R.id.lin_tab1 -> tabChange(viewRadio1, "销售出库--销售订单", 0)
            R.id.lin_tab2 -> tabChange(viewRadio2, "销售出库--箱码", 1)
            R.id.lin_tab3 -> tabChange(viewRadio3, "销售出库--拣货单", 2)
        }//                }
    }

    @OnLongClick(R.id.btn_close)
    fun onViewLongClicked(view: View): Boolean {
        when (view.id) {
            R.id.btn_close // 测试打印的
            -> if (isConnected) {
                setBoxListPrint()
            } else {
                // 打开蓝牙配对页面
                startActivityForResult(Intent(context, BluetoothDeviceListDialog::class.java), Constant.BLUETOOTH_REQUEST_CODE)
            }
        }
        return true
    }

    /**
     * 选中之后改变样式
     */
    private fun tabSelected(v: View) {
        curRadio!!.setBackgroundResource(R.drawable.check_off2)
        v.setBackgroundResource(R.drawable.check_on)
        curRadio = v
    }

    private fun tabChange(view: View?, str: String, page: Int) {
        tabSelected(view!!)
        tv_title!!.text = str
        viewPager!!.setCurrentItem(page, false)
    }

    fun setFragmentExec(fragment2Exec: IFragmentExec) {
        this.fragment2Exec = fragment2Exec
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // 按了删除键，回退键
        //        if(!isKeyboard && (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
        // 240 为PDA两侧面扫码键，241 为PDA中间扫码键
        return if (!(event.keyCode == 240 || event.keyCode == 241)) {
            false
        } else super.dispatchKeyEvent(event)
    }

    /**
     * 生产装箱清单
     * @param flag
     */
    fun setFragment2Print(flag: Int, listMaps: List<Map<String, Any>>) {
        tabFlag = flag
        if (tabFlag != flag) {
            //            isConnected = false;
        }
        // 清空list
        context.listMaps.clear()

        context.listMaps.addAll(listMaps)

        if (isConnected) {
            setBoxListPrint()
        } else {
            // 打开蓝牙配对页面
            startActivityForResult(Intent(this, BluetoothDeviceListDialog::class.java), Constant.BLUETOOTH_REQUEST_CODE)
        }
    }

    /**
     * 设置生产装箱清单打印格式
     */
    private fun setBoxListPrint() {
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
        fragment2Exec!!.onFragmenExec()
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
    private fun setBoxListFormat3() {
        val tsc = LabelCommand()
        setTscBegin(tsc)
        // --------------- 打印区-------------Begin

        val beginXPos = 20 // 开始横向位置
        val beginYPos = 0 // 开始纵向位置
        var rowHigthSum = 0 // 纵向高度的叠加
        val rowSpacing = 30 // 每行之间的距离
        val date = Comm.getSysDate(7)

        tsc.addText(beginXPos, beginYPos, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "------------------------------------------------- \n")
        rowHigthSum = rowHigthSum + rowSpacing
        tsc.addText(300, rowHigthSum, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "打印日期：$date \n")

        // --------------- 打印区-------------End
        setTscEnd(tsc)
    }

    /**
     * 打印前段配置
     * @param tsc
     */
    private fun setTscBegin(tsc: LabelCommand) {
        // 设置标签尺寸，按照实际尺寸设置
        tsc.addSize(78, 26)
        // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        //        tsc.addGap(10);
        tsc.addGap(0)
        // 设置打印方向
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        // 开启带Response的打印，用于连续打印
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        // 设置原点坐标
        tsc.addReference(0, 0)
        // 撕纸模式开启
        tsc.addTear(EscCommand.ENABLE.ON)
        // 清除打印缓冲区
        tsc.addCls()
    }

    /**
     * 打印后段配置
     * @param tsc
     */
    private fun setTscEnd(tsc: LabelCommand) {
        // 打印标签
        tsc.addPrint(1, 1)
        // 打印标签后 蜂鸣器响

        tsc.addSound(2, 100)
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255)
        val datas = tsc.command
        // 发送数据
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            return
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas)
    }


    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_DEVICE_DETACHED)
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE)
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy()")
        DeviceConnFactoryManager.closeAllPort()
        if (threadPool != null) {
            threadPool!!.stopThreadPool()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (data == null) return
            when (requestCode) {
                /*蓝牙连接*/
                Constant.BLUETOOTH_REQUEST_CODE -> {
                    //                    isPair = true;
                    /*获取蓝牙mac地址*/
                    val macAddress = data!!.getStringExtra(BluetoothDeviceListDialog.EXTRA_DEVICE_ADDRESS)
                    //初始化话DeviceConnFactoryManager
                    DeviceConnFactoryManager.Build()
                            .setId(id)
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(macAddress)
                            .build()
                    //打开端口
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort()
                }//                if(!isPair) {
                //                    // 打开蓝牙配对页面
                //                    mHandler.postDelayed(new Runnable() {
                //                        @Override
                //                        public void run() {
                //                            startActivityForResult(new Intent(context, BluetoothDeviceListDialog.class), Constant.BLUETOOTH_REQUEST_CODE);
                //                        }
                //                    },500);
                //
                //                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }

    

}
