package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;

import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ConnectPosResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class UsbHidConnectActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_usbhid);

        ButterKnife.bind(this);
    }

    @OnClick({R.id.connect, R.id.isConnect, R.id.disconnect})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.connect:
                connectDevice();
                break;
            case R.id.isConnect:
                if (Controler.posConnected()) {
                    new SweetAlertDialog(UsbHidConnectActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setContentText(getString(R.string.device_already_connect))
                            .show();
                } else {
                    new SweetAlertDialog(UsbHidConnectActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setContentText(getString(R.string.device_not_connect))
                            .show();
                }
                break;
            case R.id.disconnect:
                Controler.disconnectPos();
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        finish();
    }

    @Override
    protected void setButtonName() {
    }

    private void connectDevice() {
        SweetDialogUtils.showProgress(UsbHidConnectActivity.this, getString(R.string.device_connect), false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Controler.posConnected()) {
                    SweetDialogUtils.changeAlertType(UsbHidConnectActivity.this, getString(R.string.device_already_connect), SweetAlertDialog.SUCCESS_TYPE);
                    return;
                }
                ConnectPosResult ret = Controler.connectPos("");

                if (ret.bConnected) {
                    SweetDialogUtils.changeAlertType(UsbHidConnectActivity.this, getString(R.string.device_already_connect), SweetAlertDialog.SUCCESS_TYPE);
                } else {
                    SweetDialogUtils.changeAlertType(UsbHidConnectActivity.this, getString(R.string.device_not_connect), SweetAlertDialog.ERROR_TYPE);
                }
            }
        }).start();
    }
}