package com.morefun.mpos.reader.sampleDemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.IUpdatePosProc;
import com.mf.mpos.pub.result.UpdatePosResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class UpdateActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    private final String TAG = "UpdateActivity";
    private String mUpdateFilePath;
    private String mSignFilePath;
    private final int REQUEST_CODE_SIGN_FILE = 0;
    private final int REQUEST_CODE_UPDATE_FIEL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.btn_update, R.id.btn_verifySign, R.id.btn_chooseSignFile, R.id.btn_chooseFile})
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_update:
                if (!Controler.posConnected()) {
                    SweetDialogUtils.showError(UpdateActivity.this, getString(R.string.connect_device_first));
                    return;
                }

                SweetDialogUtils.showProgress(UpdateActivity.this, getString(R.string.terminal_update), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        if (updateFirmware()) {
                            SweetDialogUtils.changeAlertType(UpdateActivity.this, "Update successed", SweetAlertDialog.SUCCESS_TYPE);
                        } else {
                            SweetDialogUtils.changeAlertType(UpdateActivity.this, "Update failed", SweetAlertDialog.ERROR_TYPE);
                        }
                    }
                });
                break;
            case R.id.btn_verifySign:
                if (!Controler.posConnected()) {
                    SweetDialogUtils.showError(UpdateActivity.this, getString(R.string.connect_device_first));
                    return;
                }

                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Controler.loadVerifySignData(readSignData())) {
                            SweetDialogUtils.changeAlertType(UpdateActivity.this, "Load Sign Success", SweetAlertDialog.SUCCESS_TYPE);
                        } else {
                            SweetDialogUtils.changeAlertType(UpdateActivity.this, "Load Sign failed", SweetAlertDialog.ERROR_TYPE);
                        }
                    }
                });
                break;
            case R.id.btn_chooseSignFile:
                showFileChooser(REQUEST_CODE_SIGN_FILE);
                break;
            case R.id.btn_chooseFile:
                showFileChooser(REQUEST_CODE_UPDATE_FIEL);
                break;
        }
    }

    @Override
    protected void setButtonName() {
    }

    public class UpdatePosProc implements IUpdatePosProc {
        InputStream fs;

        public UpdatePosProc(InputStream fs) {
            // TODO Auto-generated constructor stub
            this.fs = fs;
        }

        @Override
        public void UpdateProcess(final int totalSize, final int alreadySize) {
            setProgress("Upgrading..." +  alreadySize  + "/" + totalSize);
        }

        @Override
        public int totalsize() throws IOException {
            // TODO Auto-generated method stub
            return this.fs.available();
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            // TODO Auto-generated method stub
            return this.fs.read(buffer, byteOffset, byteCount);
        }
    }

    public byte[] readSignData() {
        try {
            InputStream inputStream;
            File file = new File(mSignFilePath);
            if (!file.exists()) {
                return null;
            }
            Log.d(TAG, "updateFirmware from storage");
            inputStream  = new FileInputStream(file);
            return BytesUtil.input2byte(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean updateFirmware() {
        AssetManager manager = getResources().getAssets();
        try {
            InputStream inputStream;
            File file = new File(mUpdateFilePath);
            if (!file.exists()) {
                return false;
            }
            Log.d(TAG, "updateFirmware from storage");
            inputStream  = new FileInputStream(file);

            UpdatePosProc updateProc = new UpdatePosProc(inputStream);
            UpdatePosResult result = Controler.UpdatePos(updateProc);
            if (result.isComplete()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setProgress(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SweetDialogUtils.setProgressText(UpdateActivity.this, result);
            }
        });
    }

    private void showFileChooser(int requestCode) {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(requestCode)
                .withTitle("选择文件")
                .withMutilyMode(false)
                .withStartPath("/mnt/sdcard")//指定初始显示路径
                .withNotFoundBooks("至少选择一个文件")
                .withChooseMode(true)//文件选择模式
                //.withFileFilter(new String[]{".bin"})
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
            if (requestCode == REQUEST_CODE_SIGN_FILE) {
                mSignFilePath =  list.get(0);
                textView.append("Sign File Path:" + mSignFilePath + "\n");
            } else if (requestCode == REQUEST_CODE_UPDATE_FIEL) {
                mUpdateFilePath = list.get(0);
                textView.append("Update File Path:" + mUpdateFilePath + "\n");
            }
        }
    }
}