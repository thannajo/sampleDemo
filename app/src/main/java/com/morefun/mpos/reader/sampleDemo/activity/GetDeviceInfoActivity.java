package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ReadPosInfoResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GetDeviceInfoActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.button)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    getDeviceInfo();
                }
            });
        } else {
            SweetDialogUtils.showError(this, getString(R.string.device_not_connect));
        }
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.get_deviceinof));
    }

    public void getDeviceInfo() {
        ReadPosInfoResult readPosInfoResult = Controler.ReadPosInfo2();

        StringBuilder builder = new StringBuilder();
        builder.append("\nPosVer:" + readPosInfoResult.posVer);
        builder.append("\nDataVer:" + readPosInfoResult.dataVer);
        builder.append("\nModel:" + readPosInfoResult.model);
        builder.append("\nSn:" + readPosInfoResult.sn);
        builder.append("\nStatus:" + readPosInfoResult.initStatus);
        /**
         * Battery status
         * 0    No power off
         * 1    Low battery (tension is low)
         * 2    On battery (normal)
         * 3    High battery (full battery)
         * 4    Full
         * 5    Charging
         */
        builder.append("\nBattery:" + readPosInfoResult.btype);
        showResult(builder.toString());
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(result);
            }
        });
    }
}