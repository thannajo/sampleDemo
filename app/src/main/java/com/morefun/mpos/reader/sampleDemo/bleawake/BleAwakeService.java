package com.morefun.mpos.reader.sampleDemo.bleawake;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.EmvTagDef;
import com.mf.mpos.pub.createQRcode;
import com.mf.mpos.pub.param.ShowBitmapParam;
import com.mf.mpos.pub.result.ReadCardResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.activity.BleAwakeActivity;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.PhoneUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class BleAwakeService extends Service {
    public static final long ONE_MINUTE = 60;
    private static final String TAG = "BleAwakeService";
    public static final String SERVICE_ACTION = "com.morefun.mpos.reader.com.morefun.mpos.reader.sampleDemo.bleawake.BleAwakeService";
    private int manufacturerId = 0x004C;
    private PendingIntent callbackIntent;
    private NotificationManager notificationManager;
    private String bluetoothMac;
    //上次手机屏幕显示时间
    private long lastScreenActionTime;
    private byte[] standardManufacturerData;
    private long mIntervalTime = 0;
    private String lastTraceNo = "000000";

    private String mAmount;
    private String mTraceNo;
    private String mPan;
    private volatile boolean canDoTrans = true;

    public BleAwakeService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, ">>>>>>Start BleAwakeService");
        standardManufacturerData = BytesUtil.hexString2ByteArray("0215" + PhoneUtil.getDeviceId(getApplicationContext()));

        sendNotification("正在工作", "");
        callbackIntent = PendingIntent.getForegroundService(
                this,
                1,
                new Intent(SERVICE_ACTION).setPackage(getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT);
        startScanFilter();

        // 注册手机系统蓝牙状态监听广播
        IntentFilter statusFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBTStatusReceive, statusFilter);

        // 注册手机屏幕开关状态监听广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenStatusReceive, intentFilter);

        WakeAndLock wakeAndLock = new WakeAndLock(this);
        if (!wakeAndLock.isScreenOn()) {
            wakeAndLock.screenOn();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()-->");
        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "intent is null!");
            return START_STICKY;
        }

        //获取返回的错误码
        int errorCode = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, -1);
        Log.d(TAG, "errorCode:" + errorCode);

        //获取到的蓝牙设备的回调类型
        int callbackType = intent.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE, -1);
        Log.d(TAG, "callbackType:" + callbackType);

        if (errorCode == -1) {
            Log.d(TAG, "扫描操作成功！");
            List<ScanResult> scanResults = (List<ScanResult>) intent.getSerializableExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);

            //扫描到的时刻以及满足条件的设备列表
            if (scanResults != null && scanResults.size() > 0) {
                Log.d(TAG, "扫描结果到设备个数:" + scanResults.size());
                //遍历扫描到的设备
                for (ScanResult result : scanResults) {
                    ScanRecord scanRecord = result.getScanRecord();
                    if (scanRecord == null) {
                        Log.e(TAG, "scanRecord is null!");
                        return START_STICKY;
                    }
                    //76  0x004C  设备中标准的广播制造商数据 前一部分
                    //共25个字节，除去2字节id，2字节major,2字节minor,1字节xx
                    int tempLen = 18;
                    //依据manufacturerId获取实际的广播制造商数据
                    byte[] manufacturerData = scanRecord.getManufacturerSpecificData(manufacturerId);
                    byte[] tempData = new byte[tempLen];

                    Log.d(TAG, "getManufacturerSpecificData:" + BytesUtil.bytes2Hex(manufacturerData));
                    if (manufacturerData == null || manufacturerData.length < 23) {
                        Log.e(TAG, "manufacturerData不合法 !");
                        return START_STICKY;
                    } else {
                        System.arraycopy(manufacturerData, 0, tempData, 0, tempLen);
                    }

                    Log.d(TAG, ">>>>>>standardManufacturerData:" + BytesUtil.bytes2Hex(standardManufacturerData));
                    Log.d(TAG, ">>>>>>tempData:" + BytesUtil.bytes2Hex(tempData));

                    if (Arrays.equals(standardManufacturerData, tempData)) {
                        bluetoothMac = result.getDevice().getAddress();
                        Log.d(TAG, "目标设备!!!");
                        Log.d(TAG, "onScanResult--> name:" + result.getDevice().getName() +
                                ", address:" + result.getDevice().getAddress() +
                                ", result.getPrimaryPhy:" + result.getPrimaryPhy() +
                                ", rssi: " + result.getRssi() +
                                ", scanRecord: " + result.getScanRecord().toString());

                        Log.e(TAG, "canDoTrans:" + canDoTrans);
                        if (canDoTrans) {
                            ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
                            cachedThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    startTrans(bluetoothMac);
                                }
                            });
                        } else {
                            sendNotify("交易未完成请等待");
                        }
                    } else {
                        Log.e(TAG, "非目标设备!!!");
                    }
                }
            } else {
                Log.e(TAG, "scanResults = null || scaResult.size == 0");
            }
        } else {
            Log.e(TAG, "扫描操作失败！");
        }
        return START_STICKY;
    }

    //使用绑定服务回调的方法
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService()-->");
        return super.stopService(name);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBTStatusReceive);
        unregisterReceiver(mScreenStatusReceive);
        stopScanFilter();

        //查询用户是否已为此应用启用后台限制。
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.d(TAG, "activityManager.isBackgroundRestricted():" + activityManager.isBackgroundRestricted());
            //如果为true，则在后台运行时，该应用尝试执行的任何工作都会受到严格限制
        }

        //重新打开服务
        Intent intent = new Intent(this, BleAwakeService.class);
        intent.setAction(SERVICE_ACTION);
        startForegroundService(intent);
        //注意：系统说明调用 context.startForegroundService(intent);服务后5s内，需要调用startForeground(1, notification);
        sendNotification("正在工作", "");  //不能忘记！

        Log.d(TAG, "onDestroy()-->");
    }

    /**
     * 系统蓝牙开关状态监听
     */
    private BroadcastReceiver mBTStatusReceive = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(Objects.requireNonNull(intent.getAction()))) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_ON:  //蓝牙已打开
                        startScanFilter();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                }
            }
        }
    };
    /**
     * 手机屏幕开关监听
     */
    private BroadcastReceiver mScreenStatusReceive = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Log.e(TAG, "mScreenStatusReceive()--> action == null ! ");
                return;
            }
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    Log.d(TAG, "ACTION_SCREEN_ON屏幕打开 ");
                    awakeSystem();
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    Log.d(TAG, "ACTION_SCREEN_ON屏幕关闭 ");
                    awakeSystem();
                    break;
                case Intent.ACTION_USER_PRESENT:
                    break;
            }
        }
    };

    /**
     * 重新开启蓝牙扫描唤醒功能
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void awakeSystem() {
        long current = System.currentTimeMillis();
        long timeDuration = current - lastScreenActionTime;
        Log.d(TAG, "currentTimeMills:" + current + ",lastScreenActionTime:" + lastScreenActionTime + ", duration:" + timeDuration);
        // 屏幕开关状态时间距离上一次的时间间隔大于15min，就重启扫描
        if (timeDuration / 1000 >= ONE_MINUTE * 15) {
            stopScanFilter();
            startScanFilter();
        }
        lastScreenActionTime = current;
    }

    /**
     * 开启蓝牙过滤扫描
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startScanFilter() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        //指定需要识别到的蓝牙设备
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder builder2 = new ScanFilter.Builder();
        builder2.setManufacturerData(manufacturerId, standardManufacturerData);
        ScanFilter scanFilter2 = builder2.build();
        scanFilterList.add(scanFilter2);

        //指定蓝牙的方式，这里设置的ScanSettings.SCAN_MODE_LOW_LATENCY是比较高频率的扫描方式
        ScanSettings.Builder settingBuilder = new ScanSettings.Builder();
        settingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        settingBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        settingBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        settingBuilder.setLegacy(true);
        ScanSettings settings = settingBuilder.build();

        //启动蓝牙扫描
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // TODO: 2020/10/23  要加判断，否则蓝牙没有开启的时候会报bluetoothLeScanner为空的错误，导致APP打开即闪退！！
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        } else {
            bluetoothLeScanner.startScan(scanFilterList, settings, callbackIntent);
        }
    }

    /**
     * 停止蓝牙过滤扫描
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopScanFilter() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //停止扫描
        bluetoothAdapter.getBluetoothLeScanner().stopScan(callbackIntent);
    }

    /**
     * Send the sendNotification after Service start and keep it in background.
     * 服务启动后发送通知，并将其保留在后台
     *
     * @param contentTitle 通知标题
     * @param contentText  通知内容
     */
    private void sendNotification(String contentTitle, String contentText) {
        final int NOTIFY_ID = 1003;  //通知 ID
        String name = "IBC_SERVICE_CHANNEL";
        String id = "IBC_SERVICE_CHANNEL_1"; // The requestUser-visible name of the channel.
        String description = "IBC_SERVICE_CHANNEL_SHOW"; // The requestUser-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //Android 8.0以及以上
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(false);
                mChannel.enableLights(false);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);
            intent = new Intent(this, BleAwakeActivity.class);  //点击通知，进入首页面
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("BLUETOOTH_MAC", bluetoothMac);
            intent.putExtra("AMOUNT", mAmount);
            intent.putExtra("TRACE_NO", mTraceNo);
            intent.putExtra("PAN", mPan);

            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle(contentTitle)
                    .setSmallIcon(R.drawable.ic_ble_awake_icon)
                    .setContentText(contentText)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setChannelId(id)
                    .setTicker(contentTitle);
            builder.build().sound = null;
            builder.build().vibrate = null;

        } else {  //Android 8.0以下
            builder = new NotificationCompat.Builder(this, id);
            intent = new Intent(this, BleAwakeActivity.class);   //点击通知，进入首页面
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("BLUETOOTH_MAC", bluetoothMac);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle(contentTitle)
                    .setSmallIcon(R.drawable.ic_ble_awake_icon)
                    .setContentText(contentText)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(contentTitle)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVibrate(new long[]{0L});
        }

        Notification notification = builder.build();
        notification.sound = null;
        notification.vibrate = null;
        startForeground(NOTIFY_ID, notification);
    }

    /**
     * 使用当前运行进程判断 当前APP在前台还是后台
     *
     * @param context
     * @return
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        boolean isBackground = true;
        String processName = "empty";
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                processName = appProcess.processName;
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED) {
                    isBackground = true;
                } else if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    isBackground = false;
                } else {
                    isBackground = true;
                }
            }
        }
        if (isBackground) {
            Log.d(TAG, "后台:" + processName);
        } else {
            Log.d(TAG, "前台:" + processName);
        }
        return isBackground;
    }

    private void startBleAwakeActivity() {
        if (!ActivityCollector.isActivityExist(BleAwakeActivity.class)) {
            Log.i(TAG, ">>>startBleAwakeActivity");
            Intent intent = new Intent(getApplicationContext(), BleAwakeActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("BLUETOOTH_MAC", bluetoothMac);
            startActivity(intent);
        } else {
            Log.i(TAG, ">>>BleAwakeActivity Exist");
        }
    }

    private boolean canDoTrans() {
        if (mIntervalTime == 0) {
            return true;
        }
        if (System.currentTimeMillis() - mIntervalTime < 5 * 1000) {
            return false;
        } else {
            mIntervalTime = System.currentTimeMillis();
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized void startTrans(String bluetoothMac) {
        Log.d(TAG, ">>>>>>>startTrans<<<<<<<<<<");
        canDoTrans = false;
        Log.d(TAG, ">>>>>>>connectPos<<<<<<<<<<");
        Controler.connectPos(bluetoothMac);
        Log.d(TAG, "posConnected:" + Controler.posConnected());
        if (Controler.posConnected()) {
            try {
                String traceNo = "000001";
                String orderId = "100001";

                ReadCardResult result = Controler.getTransData(getTags(), traceNo, orderId);
                Log.d(TAG, "ReadCard Result:" + new Gson().toJson(result));
                if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                    String transName = "支付成功";
                    //被扫支付
                    if (result.cardType == 10) {
                        mPan = new String(BytesUtil.hexString2ByteArray(result.scanCode));
                        transName = "被扫支付";
                    } else if (result.cardType == 11) { //主扫支付
                        mPan = "";
                        if (result.transType.equals("00")) {
                            transName = "微信支付";
                        } else if (result.transType.equals("01")) {
                            transName = "支付宝支付";
                        } else if (result.transType.equals("02")) {
                            transName = "银联支付";
                        }
                        showBitmap();
                    } else {
                        mPan = result.pan;
                        if (result.transType.equals("00")) {
                            transName = "T0支付";
                        } else if (result.transType.equals("01")) {
                            transName = "T1支付";
                        }
                    }

                    mAmount = BytesUtil.getReadableAmount(new String(BytesUtil.hexString2ByteArray(result.posInputAmount)));
                    mTraceNo = new String(BytesUtil.hexString2ByteArray(result.traceNo));

                    String amountTip = transName + ":￥" + BytesUtil.getReadableAmount(new String(BytesUtil.hexString2ByteArray(result.posInputAmount)));

                    StringBuilder tip = new StringBuilder();
                    if (mPan != null && !mPan.isEmpty()) {
                        tip.append(BytesUtil.mask(mPan, "xxxxxx*xxxx")).append("\n");
                    }
                    tip.append("流水号:").append(mTraceNo).append("\n");
                    tip.append(amountTip);

                    Controler.screen_show(tip.toString(), 30);
                    sendNotify(amountTip);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        Controler.disconnectPos();
        canDoTrans = true;
    }

    private void showBitmap() {
        String data = "http://www.morefun-et.com/";
        createQRcode.QrData qrData = createQRcode.createQRcodejni(data);
        ShowBitmapParam param = new ShowBitmapParam();
        try {
            param.setTimeOut((byte) 30);
            param.setBmpWidth((byte) qrData.width);
            param.setBmpHeight((byte) qrData.width);
            param.setBmpTopOffset((byte) 5);
            param.setBmpLeftOffset((byte) 5);
            param.setBmpData(qrData.buffer);
            param.setText1LeftOffset((byte) 60);
            param.setText1TopOffset((byte) 10);
            param.setText1Content("  请使用".getBytes("GBK"));
            param.setText2LeftOffset((byte) 60);
            param.setText2TopOffset((byte) 40);
            param.setText2Content("支付宝支付".getBytes("GBK"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Controler.showBitMap(param);
    }

    private void sendNotify(String msg) {
        sendNotification(msg, "点击查看详情");
        WakeAndLock wakeAndLock = new WakeAndLock(this);
        if (!wakeAndLock.isScreenOn()) {
            wakeAndLock.screenOn();
        }
    }

    private List<byte[]> getTags() {
        List<byte[]> tags = new ArrayList<byte[]>();

        tags.add(EmvTagDef.EMV_TAG_9F02_TM_AUTHAMNTN);
        tags.add(EmvTagDef.EMV_TAG_9F26_IC_AC);
        tags.add(EmvTagDef.EMV_TAG_9F27_IC_CID);
        tags.add(EmvTagDef.EMV_TAG_9F10_IC_ISSAPPDATA);
        tags.add(EmvTagDef.EMV_TAG_9F37_TM_UNPNUM);
        tags.add(EmvTagDef.EMV_TAG_9F36_IC_ATC);
        tags.add(EmvTagDef.EMV_TAG_95_TM_TVR);
        tags.add(EmvTagDef.EMV_TAG_9A_TM_TRANSDATE);
        tags.add(EmvTagDef.EMV_TAG_9C_TM_TRANSTYPE);
        tags.add(EmvTagDef.EMV_TAG_5F2A_TM_CURCODE);
        tags.add(EmvTagDef.EMV_TAG_82_IC_AIP);
        tags.add(EmvTagDef.EMV_TAG_9F1A_TM_CNTRYCODE);
        tags.add(EmvTagDef.EMV_TAG_9F03_TM_OTHERAMNTN);
        tags.add(EmvTagDef.EMV_TAG_9F33_TM_CAP);
        tags.add(EmvTagDef.EMV_TAG_9F34_TM_CVMRESULT);
        tags.add(EmvTagDef.EMV_TAG_9F35_TM_TERMTYPE);
        tags.add(EmvTagDef.EMV_TAG_9F1E_TM_IFDSN);
        tags.add(EmvTagDef.EMV_TAG_84_IC_DFNAME);
        tags.add(EmvTagDef.EMV_TAG_9F09_TM_APPVERNO);
        tags.add(EmvTagDef.EMV_TAG_9F63_TM_BIN);
        tags.add(EmvTagDef.EMV_TAG_9F41_TM_TRSEQCNTR);
        return tags;
    }

}

