package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingSleepTimeActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.etSleepTime)
    EditText etSleepTime;

    @BindView(R.id.etShutDownTime)
    EditText etShutDownTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.doneBtn})
    public void onClick(View view) {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(this, getString(R.string.device_not_connect));
            return;
        }
        StringBuilder builder = new StringBuilder();
        int sleepTime = Integer.parseInt(etSleepTime.getText().toString());
        int shutDownTime = Integer.parseInt(etShutDownTime.getText().toString());

        switch (view.getId()) {
            case R.id.doneBtn:
                boolean result = Controler.setSleepTime(sleepTime, shutDownTime);
                builder.append("Setting result:" + result);
                break;

            default:
                break;
        }
        showResult(builder.toString());
    }

    @Override
    protected void setButtonName() {
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(result + "\r\n");
            }
        });
    }
}