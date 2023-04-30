package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ICAidResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class DownloadAidActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.button)
    Button button;

    private final String TAG = "DownloadAidActivity";

    private final String[] aids = new String[]{
            //Union Pay
            "9F0608A000000333010100DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            "9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            "9F0608A000000333010102DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            "9F0608A000000333010103DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //Visa
            "9F0607A0000000031010DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000200000000",
            //Master
            "9F0607A0000000041010DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000200000000",
            //Local Master
            "9F0607D4100000012010DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //Local Visa
            "9F0607D4100000011010DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //AMERICAN EXPRESS
            "9F0608A000000025010402DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //AMERICAN EXPRESS
            "9F0608A000000025010501DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //JCB
            "9F0607A0000000651010DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //D-PAS
            "9F090200649F0607A0000001523010DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            //Rupay
            "9F090200649F0607A0000005241010DF010100DF11050000000000DF12050000000000DF130500000000009F1B04000186A0DF14039F3704DF150400000000DF160105DF170100DF1801319F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",
            "9F090200649F0608A000000524010101DF010100DF11050000000000DF12050000000000DF130500000000009F1B04000186A0DF150400000000DF160100DF170100DF14039F3704DF1801319F7B06000000010000DF1906000000010000DF2006000000050000DF2106000000004000",
            "9F090200649F0607A0000005241011DF010100DF11050000000000DF12050000000000DF130500000000009F1B04000186A0DF14039F3704DF150400000000DF160105DF170100DF1801319F7B06000000200000DF1906000000200000DF2006000002000000DF2106000000100000",

            //MAESTRO
            "9F090200029F0607A0000000043060DF010100DF1105FC50BCA000DF1205FC50BCF800DF130500000000009F1B0400000000DF14039F3704DF150400000000DF160199DF170199DF1801019F7B06999999999999DF1906999999999999DF2006999999999999DF2106999999999999",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button, R.id.readAidList, R.id.readAid, R.id.clearAid})
    public void onClick(View v) {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(DownloadAidActivity.this, getString(R.string.device_not_connect));
            return;
        }
        switch (v.getId()) {
            case R.id.button:
                SweetDialogUtils.showProgress(DownloadAidActivity.this, getString(R.string.download_aid), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadAID();
                    }
                });
                break;
            case R.id.readAidList:
                SweetDialogUtils.showProgress(DownloadAidActivity.this, getString(R.string.read_aid_list), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        readAIDList();
                    }
                });
                break;
            case R.id.readAid:
                SweetDialogUtils.showProgress(DownloadAidActivity.this, getString(R.string.read_aid), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        readAID();
                    }
                });
                break;
            case R.id.clearAid:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        clearAid();
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

    private void setProgress(final String result) {
        SweetDialogUtils.setProgressText(DownloadAidActivity.this, result);
    }

    private void clearAid() {
        ICAidResult result = Controler.ICAidManage(CommEnum.ICAIDACTION.CLEAR, null);
        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            SweetDialogUtils.showSuccess(DownloadAidActivity.this, getString(R.string.operation_success));
        } else {
            SweetDialogUtils.showError(DownloadAidActivity.this, getString(R.string.operation_fail));
        }
    }

    private void downloadAID() {
        for (int j = 0; j < aids.length; j++) {
            String aid = aids[j];
            setProgress("Start download AID(" + (j + 1) + "/" + aids.length + ")");
            ICAidResult result = Controler.ICAidManage(CommEnum.ICAIDACTION.ADD, Misc.asc2hex(aid));
            if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
                return;
            }
        }
        SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.download_aid_success), SweetAlertDialog.SUCCESS_TYPE);
    }

    private void readAIDList() {
        ICAidResult result = Controler.ICAidManage(CommEnum.ICAIDACTION.READLIST, null);
        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            StringBuilder builder = new StringBuilder();
            builder.append("aid len:" + result.aidLen);
            builder.append("\r\naid list:" + Misc.hex2asc(result.aid));
            showResult(builder.toString());
            SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.operation_success), SweetAlertDialog.SUCCESS_TYPE);
        } else {
            SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
        }
    }

    private void readAID() {
        String[] aidArray;

        ICAidResult aidResult = Controler.ICAidManage(CommEnum.ICAIDACTION.READLIST, null);
        if (aidResult.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            if (aidResult.aidLen > 0) {
                List<String> listAid = new ArrayList<>();
                String aidString = BytesUtil.bytes2Hex(aidResult.aid);
                aidArray = aidString.split("9F06");
                StringBuilder builder = new StringBuilder();

                for (String aid: aidArray) {
                    if (aid != null && !aid.isEmpty()) {
                        listAid.add(aid);
                    }
                }

                for (int i = 0; i < listAid.size(); i++) {
                    setProgress("Read Aid(" + (i + 1) + "/" + (listAid.size()) + ")");
                    String tlvAid = "9F06" + listAid.get(i);
                    ICAidResult result = Controler.ICAidManage(CommEnum.ICAIDACTION.READAPPOINT, Misc.asc2hex(tlvAid));
                    if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                        SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
                        return;
                    }
                    builder.append("aid[").append(i).append("]").append(Misc.hex2asc(result.aid)).append("\r\n");
                }
                showResult(builder.toString());
                SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.operation_success), SweetAlertDialog.SUCCESS_TYPE);
            }
        } else {
            SweetDialogUtils.changeAlertType(DownloadAidActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
        }
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