package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.CalMacResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CalcMacActivity extends BaseActivity {
    @BindView(R.id.button)
    Button button;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.rb_x99)
    RadioButton rb_x99;

    @BindView(R.id.rb_x919)
    RadioButton rb_x919;

    @BindView(R.id.rb_xor)
    RadioButton rb_xor;

    @BindView(R.id.et_macData)
    EditText et_macData;

    private final String TAG = "CalcMac";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcmac);
        ButterKnife.bind(this);

        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(CalcMacActivity.this, getString(R.string.device_not_connect));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                calcMac();
            }
        }).start();

    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.calc_mac));
    }

    private void calcMac() {
        byte[] mac = et_macData.getText().toString().getBytes();
        CalMacResult result = Controler.CalMac(getMacAlg(), mac, mac.length);

        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            showResult(BytesUtil.bytes2Hex(result.macvalue));
        }
    }

    private void showResult(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    private CommEnum.MACALG getMacAlg() {
        if (rb_x99.isChecked()) {
            return CommEnum.MACALG.ENCRYPTION_MAC_X99;
        } else if (rb_x919.isChecked()) {
            return CommEnum.MACALG.ENCRYPTION_MAC_X919;
        } else if (rb_xor.isChecked()) {
            return CommEnum.MACALG.ENCRYPTION_MAC_XOR;
        }
        return CommEnum.MACALG.ENCRYPTION_MAC_X99;
    }
}