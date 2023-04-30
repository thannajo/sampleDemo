package com.morefun.mpos.reader.sampleDemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.FileUtil;
import com.morefun.mpos.reader.sampleDemo.utils.RSAUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VendorVerifyActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_plainData)
    EditText et_plainData;

    @BindView(R.id.et_signData)
    EditText et_signData;

    private String mSignFilePath;
    private byte[] mSignData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_verify);
        ButterKnife.bind(this);
        setButtonName();
        encryptByPrivateKey();
    }

    @OnClick({R.id.btn_chooseSignData, R.id.btn_vendorVerify})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_chooseSignData:
                encryptByPrivateKey();
                break;
            case R.id.btn_vendorVerify:
                if (Controler.posConnected()) {
                    runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean ret = Controler.vendorVerify(BytesUtil.hexString2ByteArray(et_plainData.getText().toString()),
                                    BytesUtil.hexString2ByteArray(et_signData.getText().toString()));
                            if (ret) {
                                SweetDialogUtils.showSuccess(VendorVerifyActivity.this, getString(R.string.operation_success));
                            } else {
                                SweetDialogUtils.showError(VendorVerifyActivity.this, getString(R.string.operation_fail));
                            }
                        }
                    });
                } else {
                    SweetDialogUtils.showError(VendorVerifyActivity.this, getString(R.string.device_not_connect));
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void setButtonName() {

    }

    private void encryptByPrivateKey() {
        try {
            PrivateKey privateKey = RSAUtil.loadPrivateKey(getAssets().open("privatekey.pem"));
            mSignData = RSAUtil.encrypt(BytesUtil.hexString2ByteArray(et_plainData.getText().toString()), privateKey);
            Log.d("VendorVerifyActivity", "mSignData:" + BytesUtil.bytes2Hex(mSignData));
            et_signData.setText(BytesUtil.bytes2Hex(mSignData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFileChooser(int requstCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requstCode);
    }

    public byte[] readSignData() {
        try {
            InputStream inputStream;
            File file = new File(mSignFilePath);
            if (!file.exists()) {
                return null;
            }
            inputStream  = new FileInputStream(file);
            return BytesUtil.input2byte(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            if (requestCode == 0) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    mSignFilePath = FileUtil.getPath(this, uri);
                    byte[] sign = readSignData();
                    et_signData.setText(BytesUtil.bytes2Hex(sign));
                }
            }
        }
    }
}