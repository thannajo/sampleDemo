package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.LoadKekResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;
import com.morefun.mpos.reader.sampleDemo.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 1. KEK is used to encrypt the master key. If your master key is clear text, you do not need to call this interface.
 * 2. The key information is a 32-bit hexadecimal string
 * 3. KVC is through the Key 3DES encryption 16 0 calculation, and then take the first 8 bits
 * Such as:
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Key: 11111111111111111111111111111111
 * Algorithm: 3DES ECB
 * Crypto operation: Encryption
 * Data: 0000000000000000
 * ----------------------------------------
 * Encrypted data: 82E13665B4624DF5
 */
public class LoadKekActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_kekValue)
    EditText etKekValue;

    @BindView(R.id.et_checkValue)
    TextView etCheckValue;

    @BindView(R.id.button)
    Button button;

    private final String KEK = "11111111111111111111111111111111";
    private final String kvc = "82E13665";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kek);
        ButterKnife.bind(this);
        setButtonName();
        textView.setText(Html.fromHtml(getString(R.string.kek_tip)));
        etKekValue.setText(KEK);
        etCheckValue.setText(kvc);
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    loadKek();
                }
            });

        } else {
            ToastUtils.show(LoadKekActivity.this, getString(R.string.connect_device_first));
        }
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.load_kek));
    }

    private void loadKek() {
        String key = etKekValue.getText().toString();
        String kvc = etCheckValue.getText().toString();

        if (key == null || key.length() != 32) {
            return;
        }

        if (kvc == null || kvc.length() != 8) {
            return;
        }

        byte[] keyBuf = BytesUtil.hexString2ByteArray(key);

        byte[] kekD1 = BytesUtil.subBytes(keyBuf, 0, 8);
        byte[] kekD2 = BytesUtil.subBytes(keyBuf, 8, 8);
        byte[] kvcBuf = BytesUtil.hexString2ByteArray(kvc);

        LoadKekResult result = Controler.LoadKek(CommEnum.KEKTYPE.DOUBLE, kekD1, kekD2, kvcBuf);
        showResult(result.loadResult);
    }

    private void showResult(final boolean result) {
        if (result) {
            SweetDialogUtils.showSuccess(LoadKekActivity.this, getString(R.string.load_kek_success));
        } else {
            SweetDialogUtils.showError(LoadKekActivity.this, getString(R.string.load_kek_fail));
        }
    }
}