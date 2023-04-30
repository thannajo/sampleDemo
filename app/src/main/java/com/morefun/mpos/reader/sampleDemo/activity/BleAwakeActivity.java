package com.morefun.mpos.reader.sampleDemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.PhoneUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BleAwakeActivity extends BaseActivity {

    @BindView(R.id.tv_bindDevice)
    TextView tv_bindDevice;

    @BindView(R.id.tv_amount)
    TextView tv_amount;

    @BindView(R.id.tv_cardNo)
    TextView tv_cardNo;

    @BindView(R.id.tv_voucher)
    TextView tv_voucher;

    @BindView(R.id.ll_transResult)
    LinearLayout ll_transResult;

    private final String TAG = "BleAwakeActivity";
    private String intentAmount;
    private String intentPan;
    private String intentTrace;

    private boolean currentActivityExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleawake);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.buttonOk, R.id.tv_bindDevice, R.id.iv_backHome})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonOk:
                Log.d(TAG, "buttonOk>>>>");
                finish();
                break;
            case R.id.tv_bindDevice:
                Log.d(TAG, "tv_bindDevice>>>>");
                if (Controler.posConnected()) {
                    String uuid = PhoneUtil.getDeviceId(BleAwakeActivity.this);
                    Log.d(TAG, "UUID:" + uuid);
                    boolean result = Controler.bindDevice(BytesUtil.hexString2ByteArray(uuid));
                    if (result) {
                        SweetDialogUtils.showSuccess(BleAwakeActivity.this, "设备绑定成功");
                    } else {
                        SweetDialogUtils.showError(BleAwakeActivity.this, "设备绑定失败");
                    }
                } else {
                    SweetDialogUtils.showError(BleAwakeActivity.this, "设备未连接");
                }
                break;
            case R.id.iv_backHome:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume>>>>");
        initView();
    }

    @Override
    protected void setButtonName() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //需要断开蓝牙否则收不到唤醒广播
        Controler.disconnectPos();
    }

    //点击通知时该页面已经存在，需要从这里获取最新的数据
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent>>>>");
        currentActivityExist = true;
        intentAmount = intent.getStringExtra("AMOUNT");
        intentTrace = intent.getStringExtra("TRACE_NO");
        intentPan = intent.getStringExtra("PAN");
    }

    private void initView() {
        String amount = getIntent().getStringExtra("AMOUNT");
        String traceNo = getIntent().getStringExtra("TRACE_NO");
        String pan = getIntent().getStringExtra("PAN");

        Log.d(TAG, "amount>>>>" + amount);
        Log.d(TAG, "traceNo>>>>" + traceNo);
        Log.d(TAG, "pan>>>>" + pan);
        if (currentActivityExist) {
            updateActivity(intentAmount, intentTrace, intentPan);
        } else {
            updateActivity(amount, traceNo, pan);
        }
        currentActivityExist = false;

    }

    private void updateActivity(String amount, String voucher, String cardNo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_amount.setText(amount != null ? amount : "");
                tv_voucher.setText(voucher != null ? voucher : "");
                tv_cardNo.setText(cardNo != null ? cardNo : "");
            }
        });
    }

}