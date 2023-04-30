package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.createQRcode;
import com.mf.mpos.pub.result.OpenScanResult;
import com.morefun.mpos.reader.sampleDemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QrCodeActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.btnQrCodeShow, R.id.btnQrCodeScan})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnQrCodeShow:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = "http://www.morefun-et.com/";
                        byte timeOut = 30;
                        createQRcode.QrData ret = createQRcode.createQR(data);
                        Controler.ShowBitMap(ret.width, timeOut, ret.buffer);
                    }
                });
                break;
            case R.id.btnQrCodeScan:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        OpenScanResult openScanResult = Controler.openScan(30);
                        if (openScanResult.getResult() == 0x0) {
                            showResult(openScanResult.getContent());
                        } else if (openScanResult.getResult() == 0x1) {
                            showResult("Cancel");
                        } else if (openScanResult.getResult() == 0x2) {
                            showResult("Time Out");
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    protected void setButtonName() {
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