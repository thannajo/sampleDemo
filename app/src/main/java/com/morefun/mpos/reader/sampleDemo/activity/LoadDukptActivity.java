package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.LoadDukptResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoadDukptActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.rb_IPEKPlain)
    RadioButton rb_IPEKPlain;

    @BindView(R.id.rb_BDKPlain)
    RadioButton rb_BDKPlain;

    @BindView(R.id.rb_IPEKEncryptWithKek)
    RadioButton rb_IPEKEncryptWithKek;

    @BindView(R.id.rb_BDKEncryptWithKek)
    RadioButton rb_BDKEncryptWithKek;

    @BindView(R.id.rb_IPEKEncryptWithMaster)
    RadioButton rb_IPEKEncryptWithMaster;

    @BindView(R.id.rb_BDKEncryptWithMaster)
    RadioButton rb_BDKEncryptWithMaster;

    @BindView(R.id.et_keyIndex)
    EditText et_keyIndex;

    @BindView(R.id.et_key)
    EditText et_key;

    @BindView(R.id.et_ksn)
    EditText et_ksn;

    private final String key = "88C77153E7D8E86C34D4C056DF58E22A";
    private final String ksn = "27000000010000000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dukpt);
        ButterKnife.bind(this);
        setButtonName();
        et_key.setText(key);
        et_ksn.setText(ksn);
    }

    @OnClick({R.id.btn_dukptInit, R.id.btn_getKsn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_dukptInit:
                if (Controler.posConnected()) {
                    runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            loadDukpt();
                        }
                    });
                } else {
                    ToastUtils.show(LoadDukptActivity.this, getString(R.string.connect_device_first));
                }
                break;
            case R.id.btn_getKsn:
                if (Controler.posConnected()) {
                    runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentKSN();
                        }
                    });
                } else {
                    ToastUtils.show(LoadDukptActivity.this, getString(R.string.connect_device_first));
                }
                break;
        }

    }

    @Override
    protected void setButtonName() {
    }

    private void loadDukpt() {
        LoadDukptResult bret = Controler.LoadDukpt(getKeyType(), getIndex(), getKey(), getKsn());

        StringBuilder sb = new StringBuilder();
        sb.append("Load Dukpt Result:").append(bret.loadResult).append("\n");
        sb.append("Load Dukpt Check Value:").append(Misc.hex2asc(bret.checkvalue)).append("\n");

        showResult(sb.toString());
    }

    private void getCurrentKSN() {
        String KSN = Controler.getCurrentKSN(getIndex().toByte());
        showResult(KSN);
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(result);
            }
        });
    }

    private byte[] getKey() {
        return BytesUtil.hexString2ByteArray(et_key.getText().toString());
    }

    private byte[] getKsn() {
        return BytesUtil.hexString2ByteArray(et_ksn.getText().toString());
    }

    private byte getKeyType() {
        if (rb_IPEKPlain.isChecked()) {
            return 0;
        } else if (rb_BDKPlain.isChecked()) {
            return 1;
        } else if (rb_IPEKEncryptWithKek.isChecked()) {
            return 2;
        } else if (rb_BDKEncryptWithKek.isChecked()) {
            return 3;
        } else if (rb_IPEKEncryptWithMaster.isChecked()) {
            return 4;
        } else if (rb_BDKEncryptWithMaster.isChecked()) {
            return 5;
        }
        return 0;
    }

    private CommEnum.KEYINDEX getIndex() {
        try {
            int index = Integer.parseInt(et_keyIndex.getText().toString());
            return CommEnum.KEYINDEX.values()[index];
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return CommEnum.KEYINDEX.values()[0];
    }
}