package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoadRKIActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et)
    EditText etText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_rki);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    boolean ret = Controler.loadRKI(BytesUtil.hexString2ByteArray(etText.getText().toString()));
                    if (ret) {
                        SweetDialogUtils.showSuccess(LoadRKIActivity.this, getString(R.string.operation_success));
                    } else {
                        SweetDialogUtils.showError(LoadRKIActivity.this, getString(R.string.operation_fail));
                    }
                }
            });
        } else {
            SweetDialogUtils.showError(LoadRKIActivity.this, getString(R.string.device_not_connect));
        }
    }

    @Override
    protected void setButtonName() {

    }
}