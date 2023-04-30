package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.GetRandomResult;
import com.mf.mpos.util.Misc;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GetRandomActivity extends BaseActivity {

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.button)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick() {
        runOnThread(new Runnable() {
            @Override
            public void run() {
                getRandomNumber();
            }
        });
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(result);
            }
        });
    }

    @Override
    protected void setButtonName() {
        button.setText(getString(R.string.get_random));
    }

    private void getRandomNumber() {
        GetRandomResult r = Controler.GetRandomNum();

        if (r.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            showResult(Misc.hex2asc(r.randomNum));
            return;
        }

        SweetDialogUtils.showError(this, getString(R.string.device_not_connect));
    }

}