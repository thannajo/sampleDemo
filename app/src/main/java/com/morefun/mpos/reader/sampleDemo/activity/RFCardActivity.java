package com.morefun.mpos.reader.sampleDemo.activity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import java.io.IOException;
import java.io.InputStream;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RFCardActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.rb_insertCard)
    RadioButton rb_insertCard;

    @BindView(R.id.rb_rfCard)
    RadioButton rb_rfCard;

    @BindView(R.id.rb_felicaCard)
    RadioButton rb_felicaCard;

    @BindView(R.id.etText)
    EditText editText;

    private final byte CARD_TYPE_IC = 0x01;
    private final byte CARD_TYPE_RF = 0x02;
    private final byte CARD_TYPE_FELICA = 0x03;

    private final byte CARD_POWER_ON = 0x01;
    private final byte CARD_POWER_OFF = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfcard);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.btnRFCardPowerOn, R.id.btnRFCardPowerOff, R.id.btnRFCardApdu})
    public void onClick(View view) {
        String result;
        switch (view.getId()) {
            case R.id.btnRFCardPowerOn:
                result = Controler.ic_ctrl(getCardType(), CARD_POWER_ON);
                showResult(result != null ? "Power on successful" : "Power on Fail");
                break;
            case R.id.btnRFCardPowerOff:
                result = Controler.ic_ctrl(getCardType(), CARD_POWER_OFF);
                showResult(result != null ? "Power down successful" : "Power down fail");
                break;
            case R.id.btnRFCardApdu:
                byte[] apduCmd;
                if (editText != null && !editText.getText().toString().isEmpty()) {
                    apduCmd = Misc.asc2hex(editText.getText().toString());
                } else {
                    apduCmd = new byte[]{0x00, (byte)0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00};
                }

                byte[] response = Controler.ic_cmd(getCardType(), apduCmd);
                showResult(Misc.hex2asc(response));
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

    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();

        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    private byte getCardType() {
        if (rb_insertCard.isChecked()) {
            return CARD_TYPE_IC;
        } else if (rb_rfCard.isChecked()) {
            return CARD_TYPE_RF;
        } else if (rb_felicaCard.isChecked()) {
            return CARD_TYPE_FELICA;
        }
        return CARD_TYPE_IC;
    }
}