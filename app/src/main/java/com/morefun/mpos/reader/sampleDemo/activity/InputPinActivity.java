package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.param.InputPinParam;
import com.mf.mpos.pub.result.InputPinResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class InputPinActivity extends BaseActivity {
    @BindView(R.id.button)
    Button button;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_maxPinLen)
    EditText et_maxPinLen;

    @BindView(R.id.et_minPinLen)
    EditText et_minPinLen;

    @BindView(R.id.et_timeout)
    EditText et_timeout;

    @BindView(R.id.et_pan)
    EditText et_pan;

    @BindView(R.id.et_line1Tip)
    EditText et_line1Tip;

    @BindView(R.id.et_line2Tip)
    EditText et_line2Tip;

    @BindView(R.id.et_line3Tip)
    EditText et_line3Tip;

    @BindView(R.id.et_line4Tip)
    EditText et_line4Tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputpin);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    SweetDialogUtils.showProgress(InputPinActivity.this, getString(R.string.input_pin), false);
                    inputPin();
                }
            });
        } else {
            SweetDialogUtils.showError(InputPinActivity.this, getString(R.string.device_not_connect));
        }
    }

    @Override
    protected void setButtonName() {
    }

    private void inputPin() {
        try {
            byte maxPinLen = (byte) Integer.parseInt(et_maxPinLen.getText().toString());
            byte minPinLen = (byte) Integer.parseInt(et_minPinLen.getText().toString());
            byte timeout = (byte) Integer.parseInt(et_timeout.getText().toString());
            String pan = et_pan.getText().toString();
            String line1Tip = et_line1Tip.getText().toString();
            String line2Tip = et_line2Tip.getText().toString();
            String line3Tip = et_line3Tip.getText().toString();
            String line4Tip = et_line4Tip.getText().toString();

            InputPinParam param = new InputPinParam(maxPinLen, minPinLen, timeout, pan);
            param.setLine1Tip(line1Tip);
            param.setLine2Tip(line2Tip);
            param.setLine3Tip(line3Tip);
            param.setLine4Tip(line4Tip);

            InputPinResult result = Controler.InputPin(param);
            if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                StringBuilder builder = new StringBuilder();
                builder.append("pinBlock:").append(BytesUtil.bytes2Hex(result.pinBlock)).append("\r\n");
                builder.append("pinKsn:").append(BytesUtil.bytes2Hex(result.pinKsn)).append("\r\n");
                builder.append("keyType:").append(result.keyType).append("\r\n");

                showResult(builder.toString());
                SweetDialogUtils.changeAlertType(InputPinActivity.this, getString(R.string.operation_success), SweetAlertDialog.SUCCESS_TYPE);
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        SweetDialogUtils.changeAlertType(InputPinActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
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