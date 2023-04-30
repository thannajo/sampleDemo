package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.LoadMainKeyResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 1. The master key download can choose plaintext or KEK encryption mode.<br>
 * 2. If you choose plaintext, assuming the master key is 11111111111111111111111111111111, then KVC =82E13665<br>
 * The calculation is as follows:<br>
 * Key: 11111111111111111111111111111111<br>
 * Algorithm: 3DES ECB<br>
 * Crypto operation: Encryption<br>
 * Data: 0000000000000000<br>
 * Padding Method: ISO9797-1 (Padding method 1)<br>
 * ----------------------------------------<br>
 * Encrypted data: 82E13665B4624DF5<br>
 * 3. If you choose KEK encryption, assuming that KEK is 11111111111111111111111111111111 and the cipher master key is F40379AB9E0EC533F40379AB9E0EC533,
 * then KVC=82E13665, calculated as follows<br>
 * * * * * * * * * * * * * * * * * * * * * * * * * * * <br>
 * Key: 11111111111111111111111111111111<br>
 * Algorithm: 3DES ECB<br>
 * Crypto operation: Decoding<br>
 * Data: F40379AB9E0EC533F40379AB9E0EC533<br>
 * Padding Method: ISO9797-1 (Padding method 1)<br>
 * ----------------------------------------<br>
 * Decoded data: 11111111111111111111111111111111<br>
 * * * * * * * * * * *  * * * * * * * * * * * * * * *<br>
 * Key: 11111111111111111111111111111111<br>
 * Algorithm: 3DES ECB<br>
 * Crypto operation: Encryption<br>
 * Data: 0000000000000000<br>
 * Padding Method: ISO9797-1 (Padding method 1)<br>
 * ----------------------------------------<br>
 * Encrypted data: 82E13665B4624DF5<br>
 */
public class UpdateMasterKeyActivity extends BaseActivity {
    @BindView(R.id.button)
    Button button;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_keyValue)
    EditText etKeyValue;

    @BindView(R.id.et_checkValue)
    TextView etCheckValue;

    @BindView(R.id.rb_plainText)
    RadioButton rbPlainText;

    @BindView(R.id.rb_KEK)
    RadioButton rbKEK;

    @BindView(R.id.et_keyIndex)
    EditText etKeyIndex;

    private final String TAG = "UpdateMasterKey";
    private final String plainKey = "11111111111111111111111111111111";
    private final String cipherKey = "F40379AB9E0EC533F40379AB9E0EC533";
    private final String kvc = "82E13665";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masterkey);
        ButterKnife.bind(this);
        setButtonName();
        textView.setText(Html.fromHtml(getString(R.string.masterkey_tip)));
        if (rbPlainText.isChecked()) {
            etKeyValue.setText(plainKey);
        } else if (rbKEK.isChecked()) {
            etKeyValue.setText(cipherKey);
        }
        etCheckValue.setText(kvc);
        rbPlainText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etKeyValue.setText(plainKey);
            }
        });

        rbKEK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etKeyValue.setText(cipherKey);
            }
        });
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    loadMainKey();
                }
            });
        } else {
            SweetDialogUtils.showError(UpdateMasterKeyActivity.this, getString(R.string.device_not_connect));
        }
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.download_master_key));
    }

    private void loadMainKey() {
        String key = etKeyValue.getText().toString();
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

        Misc.traceHex(TAG, "updateMainKey kekD1", kekD1);
        Misc.traceHex(TAG, "updateMainKey kekD2", kekD2);
        Misc.traceHex(TAG, "updateMainKey kvc", kvcBuf);

        LoadMainKeyResult result = Controler.LoadMainKey(getMainKeyEncryptType(),
                getKeyIndex(),
                CommEnum.MAINKEYTYPE.DOUBLE,
                kekD1, kekD2, kvcBuf);

        showResult(result.loadResult);
    }

    private void showResult(final boolean result) {
        if (result) {
            SweetDialogUtils.showSuccess(UpdateMasterKeyActivity.this, getString(R.string.download_master_key_success));
        } else {
            SweetDialogUtils.showError(UpdateMasterKeyActivity.this, getString(R.string.download_master_key_fail));
        }
    }

    private CommEnum.MAINKEYENCRYPT getMainKeyEncryptType() {
        if (rbPlainText.isChecked()) {
            return CommEnum.MAINKEYENCRYPT.PLAINTEXT;
        } else if (rbKEK.isChecked()) {
            return CommEnum.MAINKEYENCRYPT.KEK;
        }
        return CommEnum.MAINKEYENCRYPT.PLAINTEXT;
    }

    private CommEnum.KEYINDEX getKeyIndex() {
        try {
            int index = Integer.parseInt(etKeyIndex.getText().toString());
            return CommEnum.KEYINDEX.values()[index];
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return CommEnum.KEYINDEX.INDEX0;
    }

}