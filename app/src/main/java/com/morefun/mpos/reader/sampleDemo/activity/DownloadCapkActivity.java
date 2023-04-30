package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ICPublicKeyResult;
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

public class DownloadCapkActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.button)
    Button button;

    private final String[] rids = new String[]{
            "9F0605A0000003339F220102DF050420211231DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060",
            "9F0605A0000003339F220103DF050420221231DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26",
            "9F0605A0000003339F220104DF050420221231DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capk);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button, R.id.readCapkList, R.id.readCapk, R.id.clear})
    public void onClick(View view) {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(DownloadCapkActivity.this, getString(R.string.device_not_connect));
            return;
        }
        switch (view.getId()) {
            case R.id.button:
                SweetDialogUtils.showProgress(DownloadCapkActivity.this, getString(R.string.download_capk), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadCapk();
                    }
                });
                break;
            case R.id.readCapkList:
                SweetDialogUtils.showProgress(DownloadCapkActivity.this, getString(R.string.read_capk_list), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        readCapkList();
                    }
                });
                break;
            case R.id.readCapk:
                SweetDialogUtils.showProgress(DownloadCapkActivity.this, getString(R.string.read_capk_point), false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        readCapk();
                    }
                });
                break;
            case R.id.clear:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        clearCapk();
                    }
                });
                break;
        }
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.download_capk));
    }

    private void clearCapk() {
        ICPublicKeyResult result = Controler.ICPublicKeyManage(CommEnum.ICPUBLICKEYACTION.CLEAR, null);
        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            SweetDialogUtils.showSuccess(DownloadCapkActivity.this, getString(R.string.operation_success));
        } else {
            SweetDialogUtils.showError(DownloadCapkActivity.this, getString(R.string.operation_fail));
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

    private void setProgress(final String result) {
        SweetDialogUtils.setProgressText(DownloadCapkActivity.this, result);
    }

    private void downloadCapk() {
        for (int j = 0; j < rids.length; j++) {
            String rid = rids[j];
            setProgress("Downlaod Capk(" + (j + 1) + "/" + rids.length + ")");
            ICPublicKeyResult result = Controler.ICPublicKeyManage(CommEnum.ICPUBLICKEYACTION.ADD, Misc.asc2hex(rid));
            if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
                return;
            }
        }
        SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.download_capk_success), SweetAlertDialog.SUCCESS_TYPE);
    }

    private void readCapkList() {
        ICPublicKeyResult result = Controler.ICPublicKeyManage(CommEnum.ICPUBLICKEYACTION.READLIST, null);
        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            StringBuilder builder = new StringBuilder();
            builder.append("Capk size:" + result.publicKeyLen);
            builder.append("\r\nCapk list:" + Misc.hex2asc(result.publicKey));
            showResult(builder.toString());
            SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.trans_success), SweetAlertDialog.SUCCESS_TYPE);
        } else {
            SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
        }
    }

    private void readCapk() {
        ICPublicKeyResult publicKeyResult = Controler.ICPublicKeyManage(CommEnum.ICPUBLICKEYACTION.READLIST, null);
        if (publicKeyResult.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            if (publicKeyResult.publicKeyLen > 0) {
                List<String> listCapk = new ArrayList<>();
                String capkString = BytesUtil.bytes2Hex(publicKeyResult.publicKey);
                String[] capkArray = capkString.split("9F06");
                StringBuilder builder = new StringBuilder();

                for (String capk: capkArray) {
                    if (capk != null && !capk.isEmpty()) {
                        listCapk.add(capk);
                    }
                }

                for (int i = 0; i < listCapk.size(); i++) {
                    setProgress("Read Capk(" + (i + 1) + "/" + (listCapk.size()) + ")");
                    String tlvCapk = "9F06" + listCapk.get(i);
                    ICPublicKeyResult result = Controler.ICPublicKeyManage(CommEnum.ICPUBLICKEYACTION.READAPPOINT, BytesUtil.hexString2ByteArray(tlvCapk));
                    if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                        SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
                        return;
                    }
                    builder.append("capk[").append(i).append("]").append(Misc.hex2asc(result.publicKey)).append("\r\n");
                }
                showResult(builder.toString());
                SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.operation_success), SweetAlertDialog.SUCCESS_TYPE);
            }
        } else {
            SweetDialogUtils.changeAlertType(DownloadCapkActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
        }
    }
}