package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.param.M1CardCtrlParam;
import com.mf.mpos.pub.result.M1CardCtrlResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class M1CardActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.etKey)
    EditText etKey;

    @BindView(R.id.etBlock)
    EditText etBlock;

    @BindView(R.id.etOperand)
    EditText etOperand;

    @BindView(R.id.etData)
    EditText etData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m1card);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.openBtn, R.id.closeBtn, R.id.readId, R.id.cardRequestBtn, R.id.setKeyBtn, R.id.sectorAuth, R.id.sectorRead, R.id.sectorWrite, R.id.increaseBtn, R.id.reduceBtn, R.id.copyBlockBtn, R.id.copyRamBtn})
    public void onClick(View view) {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(this, getString(R.string.device_not_connect));
            return;
        }
        StringBuilder builder = new StringBuilder();
        M1CardCtrlResult result = null;
        int cmd = 0x60;
        int operand = Integer.parseInt(etOperand.getText().toString());
        int block = Integer.parseInt(etBlock.getText().toString());

        switch (view.getId()) {
            case R.id.openBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Open());
                builder.append("open:" + result.getRet());
                break;
            case R.id.closeBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Close());
                builder.append("close:" + result.getRet());
                break;
            case R.id.cardRequestBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.CardReq());
                builder.append("card req:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.setKeyBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.SetKey(Misc.asc2hex(etKey.getText().toString())));
                builder.append("set key:" + result.getRet());
                break;
            case R.id.sectorAuth:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Auth(cmd, block));
                builder.append("block auth:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.sectorRead:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Read(block));
                builder.append("read block:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.sectorWrite:
                byte[] write = new byte[16];
                Arrays.fill(write, (byte)0x00);
                Misc.memcpy(write, Misc.asc2hex(etData.getText().toString()), 16);

                write[4] = (byte) 0xff;
                write[5] = (byte) 0xff;
                write[6] = (byte) 0xff;
                write[7] = (byte) 0xff;
                write[12] = ~0;
                write[13] = 0;
                write[14] = ~0;
                write[15] = 0;

                Log.d("M1CardActivity", "write data:" + Misc.hex2asc(write));
                result = Controler.M1CardCtrl(M1CardCtrlParam.Write(block, write));
                builder.append("write block:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.increaseBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Increment(block, operand));
                builder.append("increase:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.reduceBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Decrement(block, operand));
                builder.append("reduce:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.copyBlockBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Transfer(block));
                builder.append("Copy Block:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.copyRamBtn:
                result = Controler.M1CardCtrl(M1CardCtrlParam.Restore(block));
                builder.append("Copy Ram:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            case R.id.readId:
                result = Controler.M1CardCtrl(M1CardCtrlParam.ReadID());
                builder.append("Read ID:" + result.getRet() + "|" + Misc.hex2asc(result.getData()));
                break;
            default:
                break;
        }
        showResult(builder.toString());
    }

    @Override
    protected void setButtonName() {
    }

    private void showResult(final String result) {
        Log.d("M1CardActivity", result);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(result + "\r\n");
            }
        });
    }
}