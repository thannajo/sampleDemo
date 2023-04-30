package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.EmvDealOnlineRspResult;
import com.mf.mpos.pub.result.GetEmvDataResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OnlineAuthActivity extends BaseActivity {

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.button)
    Button button;

    @BindView(R.id.et_arqcScript)
    EditText et_arqcScript;

    @BindView(R.id.et_responseCode)
    EditText et_responseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_auth);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick() {
        runOnThread(new Runnable() {
            @Override
            public void run() {
                onlineAuth();
            }
        });
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.perform_online_authorization));
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(result);
            }
        });
    }

    private void onlineAuth() {
        showResult(getString(R.string.perform_online_authorization));

        try {
            byte[] arqcScript = Misc.asc2hex(et_arqcScript.getText().toString());
            String responseCode = et_responseCode.getText().toString();

            List<byte[]> tags = new ArrayList<byte[]>();

            EmvDealOnlineRspResult r = Controler.EmvDealOnlineRsp(true, arqcScript, arqcScript.length, responseCode);

            if (r.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                if (r.authResult.equals(CommEnum.EMVDEALONLINERSP.SUCC)) {
                    tags.add(new byte[]{(byte) 0x9F, (byte) 0x26});
                    tags.add(new byte[]{(byte) 0x95});
                    tags.add(new byte[]{(byte) 0x4F});
                    tags.add(new byte[]{(byte) 0x5F, (byte) 0x34});
                    tags.add(new byte[]{(byte) 0x9B});
                    tags.add(new byte[]{(byte) 0x9F, (byte) 0x36});
                    tags.add(new byte[]{(byte) 0x82});
                    tags.add(new byte[]{(byte) 0x9F, (byte) 0x37});
                    tags.add(new byte[]{(byte) 0x50});

                    GetEmvDataResult rdata = Controler.GetEmvData(tags, false);
                    if (rdata.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                        showResult(Misc.hex2asc(rdata.tlvData));
                    } else {
                        showResult(r.commResult.toDisplayName());
                    }
                } else if (r.authResult.equals(CommEnum.EMVDEALONLINERSP.GAC2_AAC)) {
                    showResult(r.authResult.name());
                } else if (r.authResult.equals(CommEnum.EMVDEALONLINERSP.GOTOONLINE)) {
                    showResult(r.authResult.name());
                } else if (r.authResult.equals(CommEnum.EMVDEALONLINERSP.GOTOSTRIPE)) {
                    showResult(r.authResult.name());
                } else if (r.authResult.equals(CommEnum.EMVDEALONLINERSP.REJECT)) {
                    showResult(r.authResult.name());
                } else {
                    showResult(r.authResult.name());
                }
            } else {
                showResult(r.commResult.toDisplayName());
            }
        } catch (NullPointerException e) {
            showResult(e.getMessage());
        } catch (Exception e) {
            showResult(e.getMessage());
        } finally {
            Controler.EndEmv();
        }
    }
}