package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.LoadWorkKeyResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 1. The working key is encrypted by the master key, in which PIN is the encrypted password and MAC key is used to encrypt MAC.
 * TDK is used to encrypt track information. If you do not use track encryption. All you need to do when loading the working key is set to 0<br>
 * 2. Suppose you plain master key is 11111111111111111111111111111111
 * Cipher PIN key is F40379AB9E0EC533F40379AB9E0EC533 then KVC = 82E13665 TDK and MAC calculation process and PIN.<br>
 * The calculation is as follows<br>
 * * * * * * * * * * * * * * * * * * * * * * * * * * *<br>
 * Key: 11111111111111111111111111111111<br>
 * Algorithm: 3DES ECB<br>
 * Crypto operation: Decoding<br>
 * Data: F40379AB9E0EC533F40379AB9E0EC533<br>
 * Padding Method: ISO9797-1 (Padding method 1)<br>
 * ----------------------------------------<br>
 * Decoded data: 11111111111111111111111111111111<br>
 * * * * * * * * * * * * * * * * * * * * * * * * * * *<br>
 * Key: 11111111111111111111111111111111<br>
 * Algorithm: 3DES ECB<br>
 * Crypto operation: Encryption<br>
 * Data: 0000000000000000<br>
 * ----------------------------------------<br>
 * Encrypted data: 82E13665B4624DF5<br>
 */
public class UpdateWorkKeyActivity extends BaseActivity {
    @BindView(R.id.button)
    Button button;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_keyIndex)
    EditText etKeyIndex;

    @BindView(R.id.et_PinKey)
    EditText et_PinKey;

    @BindView(R.id.et_PinKvc)
    EditText et_PinKvc;

    @BindView(R.id.et_MacKey)
    EditText et_MacKey;

    @BindView(R.id.et_MacKvc)
    EditText et_MacKvc;

    @BindView(R.id.et_TdkKey)
    EditText et_TdkKey;

    @BindView(R.id.et_TdkKvc)
    EditText et_TdkKvc;

    private final String TAG = "UpdateWorkKey";
    private final String pinKey = "F40379AB9E0EC533F40379AB9E0EC533";
    private final String macKey = "F40379AB9E0EC533F40379AB9E0EC533";
    private final String tdkKey = "F40379AB9E0EC533F40379AB9E0EC533";
    private final String kvc = "82E13665";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workkey);
        ButterKnife.bind(this);
        textView.setText(Html.fromHtml(getString(R.string.workkey_tip)));
        et_PinKey.setText(pinKey);
        et_PinKvc.setText(kvc);
        et_MacKey.setText(macKey);
        et_MacKvc.setText(kvc);
        et_TdkKey.setText(tdkKey);
        et_TdkKvc.setText(kvc);
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    updateWorkingKey();
                }
            });
        } else {
            SweetDialogUtils.showError(UpdateWorkKeyActivity.this, getString(R.string.device_not_connect));
        }
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.download_work_key));
    }

    private void updateWorkingKey() {
        String pinKey = et_PinKey.getText().toString();
        String macKey = et_MacKey.getText().toString();
        String tdkKey = et_TdkKey.getText().toString();
        String pinKvc = et_PinKvc.getText().toString();
        String macKvc = et_MacKvc.getText().toString();
        String tdkKvc = et_TdkKvc.getText().toString();

        if (pinKey == null || pinKey.length() != 32) {
            return;
        }

        if (macKey == null || macKey.length() != 32) {
            return;
        }

        if (tdkKey == null || tdkKey.length() != 32) {
            return;
        }

        if (pinKvc == null || pinKvc.length() != 8) {
            return;
        }

        if (macKvc == null || macKvc.length() != 8) {
            return;
        }

        if (tdkKvc == null || tdkKvc.length() != 8) {
            return;
        }

        String key = pinKey + pinKvc + macKey + macKvc + tdkKey + tdkKvc;

        byte[] keyArrays = Misc.asc2hex(key);
        Log.d(TAG, "updateWorkingKey key:" + key);

        LoadWorkKeyResult result = Controler.LoadWorkKey(getKeyIndex(), CommEnum.WORKKEYTYPE.DOUBLEMAG, keyArrays, keyArrays.length);
        showResult(result.loadResult);
    }

    private void showResult(final boolean result) {
        if (result) {
            SweetDialogUtils.showSuccess(UpdateWorkKeyActivity.this, getString(R.string.download_work_key_success));
            return;
        }
        SweetDialogUtils.showError(UpdateWorkKeyActivity.this, getString(R.string.download_work_key_fail));
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