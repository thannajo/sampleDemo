package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.createQRcode;
import com.mf.mpos.pub.param.ShowBitmapParam;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShowBitmapActivity extends BaseActivity {
    @BindView(R.id.et_timeout)
    EditText et_timeout;

    @BindView(R.id.et_bmpTopOffset)
    EditText et_bmpTopOffset;

    @BindView(R.id.et_bmpLeftOffset)
    EditText et_bmpLeftOffset;

    @BindView(R.id.et_text1TopOffset)
    EditText et_text1TopOffset;

    @BindView(R.id.et_text1LeftOffset)
    EditText et_text1LeftOffset;

    @BindView(R.id.et_text1Content)
    EditText et_text1Content;

    @BindView(R.id.et_text2TopOffset)
    EditText et_text2TopOffset;

    @BindView(R.id.et_text2LeftOffset)
    EditText et_text2LeftOffset;

    @BindView(R.id.et_text2Content)
    EditText et_text2Content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bitmap);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.btn_showBitmap, R.id.btn_showText})
    public void onClick(View view) {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(ShowBitmapActivity.this, getString(R.string.device_not_connect));
            return;
        }
        switch (view.getId()) {
            case R.id.btn_showBitmap:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        showBitmap();
                    }
                });
                break;
            case R.id.btn_showText:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        showText();
                    }
                });
                break;
        }
    }

    @Override
    protected void setButtonName() {
    }

    private synchronized void showText() {
        Controler.CancelComm();
        Controler.ResetPos();
        Controler.screen_show(et_text1Content.getText().toString(), 30);
    }

    private synchronized void showBitmap() {
        ShowBitmapParam param = new ShowBitmapParam();
        try {
            String data = "http://www.morefun-et.com/";
            createQRcode.QrData qrData = createQRcode.createQR(data);
            Log.d("ShowBitmapActivity", "qrData width:" + qrData.width);
            byte timeOut = (byte) Integer.parseInt(et_timeout.getText().toString());
            byte bmpWidth = (byte) qrData.width;
            byte bmpHeight = (byte) qrData.width;
            byte bmpTopOffset = Byte.parseByte(et_bmpTopOffset.getText().toString());
            byte bmpLeftOffset = Byte.parseByte(et_bmpLeftOffset.getText().toString());
            byte text1TopOffset = Byte.parseByte(et_text1TopOffset.getText().toString());
            byte text1LeftOffset = Byte.parseByte(et_text1LeftOffset.getText().toString());
            byte text2TopOffset = Byte.parseByte(et_text2TopOffset.getText().toString());
            byte text2LeftOffset = Byte.parseByte(et_text2LeftOffset.getText().toString());
            String text1 = et_text1Content.getText().toString();
            String text2 = et_text2Content.getText().toString();

            param.setTimeOut(timeOut);
            param.setBmpWidth(bmpWidth);
            param.setBmpHeight(bmpHeight);
            param.setBmpTopOffset(bmpTopOffset);
            param.setBmpLeftOffset(bmpLeftOffset);
            param.setBmpData(qrData.buffer);
            param.setText1LeftOffset(text1LeftOffset);
            param.setText1TopOffset(text1TopOffset);
            param.setText1Content(text1.getBytes("GBK"));
            param.setText2LeftOffset(text2LeftOffset);
            param.setText2TopOffset(text2TopOffset);
            param.setText2Content(text2.getBytes("GBK"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Controler.showBitMap(param);
        Log.d("ShowBitmapActivity", ">>>>>>>>showBitmap");
    }
}