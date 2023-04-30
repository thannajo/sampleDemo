package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetEmvParamActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et)
    EditText etText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_emv_param);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick() {
        if (Controler.posConnected()) {
            runOnThread(new Runnable() {
                @Override
                public void run() {
                    boolean ret = setTlv(etText.getText().toString());
                    if (ret) {
                        SweetDialogUtils.showSuccess(SetEmvParamActivity.this, getString(R.string.setting_success));
                    } else {
                        SweetDialogUtils.showSuccess(SetEmvParamActivity.this, getString(R.string.setting_fail));
                    }
                }
            });
        } else {
            SweetDialogUtils.showError(SetEmvParamActivity.this, getString(R.string.device_not_connect));
        }
    }

    @Override
    protected void setButtonName() {

    }
    /*
     * Set the emv tag value and modify some tag values as needed.
     * This function is recommended to initialize once when your application starts.
     * TLV such as:9F01063132333435369F40057000F0A0019F150230319F160F3132333435363738393031323334359F3901059F33036000009F1A0208409F1C0831323334353637389F3501225F2A0208405F3601029F3C0208409F3D01029F1E086D665F36306220209F660434000080
     *
     * |__9F01(06)==313233343536 (Acquirer Identifier)
     * |__9F40(05)==7000F0A001  (Additional Terminal Capability)
     * |__9F15(02)==3031 (Merchant Category Code)
     * |__9F16(0F)==313233343536373839303132333435  (Merchant Identifier)
     * |__9F39(01)==05  (Point-of-Service Entry Mode)
     * |__9F33(03)==600000  (Terminal Capabilities)
     * |__9F1A(02)==0840  (Terminal Country Code)
     * |__9F1C(08)==3132333435363738  (Terminal Identification)
     * |__9F35(01)==22  (Terminal Type)
     * |__5F2A(02)==0840  (Transaction Currency Code)
     * |__5F36(01)==02  (Transaction Currency Exponent)
     * |__9F3C(02)==0840  (Transaction Reference Currency Code)
     * |__9F3D(01)==02  (Transaction Reference Currency Exponent)
     * |__9F1E(08)==6D665F3630622020  (Interface Device Serial Number)
     * |__9F66(04)==34000080 (Terminal transaction attribute)
     *
     * For example, if you need to modify the currency code,
     * you can achieve this by changing the three tags: 9F1A, 5F2A, 9F3C.
     * @param emvParam
     */

    private boolean setTlv(String emvParam) {
        return Controler.SetEmvParamTlv(emvParam);
    }

}