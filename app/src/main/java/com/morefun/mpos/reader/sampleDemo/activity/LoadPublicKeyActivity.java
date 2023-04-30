package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.LoadPublicKeyResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;
import com.morefun.mpos.reader.sampleDemo.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoadPublicKeyActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_keyValue)
    TextView et_keyValue;

    @BindView(R.id.button)
    Button button;

    private final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyZAOI+yWHfV2Yv8sNMOGJDIjVXlgM3s63nP92+bSlB0CBAr/ipBGDCcRlXLRUQnjBNwn1kodufif0q8RtzIzkaZdItyVDCn2ePxAUdF8dU6bPEA7QGv+QvGp4a2Egdx3IgZ+3Gbe3mMJQKmlJnymJgngw5Akvjrc2msLm7VnyMxNsOURhFR9v4FGuZwY5dTT9j5OBtSDB9Y1IRe3F3uFFBqFMPfkwGNin8TIkrFJJc2+8k0wxCAcjCIQ1yKFMQsfcr1JQ3EYc+GzmQBnMNGuIAs6tA46xFng1xot87pNYyAu+IL9u2I5lH6QefrdMgK3BzsAgwmIKv/3SNjVL9OKPwIDAQAB";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_publickey);
        ButterKnife.bind(this);
        setButtonName();
        et_keyValue.setText(PUBLIC_KEY);
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            try {
                loadPublicKey();
            } catch (Exception e) {
                e.printStackTrace();
                SweetDialogUtils.showError(LoadPublicKeyActivity.this, getString(R.string.operation_fail));
            }
        } else {
            ToastUtils.show(LoadPublicKeyActivity.this, getString(R.string.connect_device_first));
        }
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.load_public_key));
    }

    private void loadPublicKey() throws Exception {
        String keyValue = et_keyValue.getText().toString();
        byte[] base64Decode = Base64.decode(keyValue, Base64.DEFAULT);
        LoadPublicKeyResult result = Controler.loadPublicKey(base64Decode);
        showResult(result.isLoadResult());
        Log.d("LoadPublicKeyActivity", "publicKey:" + BytesUtil.bytes2Hex(result.getPublicKey()));
        textView.setText(BytesUtil.bytes2Hex(result.getPublicKey()));
    }

    private void showResult(final boolean result) {
        if (result) {
            SweetDialogUtils.showSuccess(LoadPublicKeyActivity.this, getString(R.string.operation_success));
        } else {
            SweetDialogUtils.showError(LoadPublicKeyActivity.this, getString(R.string.operation_fail));
        }
    }
}