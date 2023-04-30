package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.InputAmountResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.ToastUtils;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InputAmountActivity extends BaseActivity {
    @BindView(R.id.tv_amount)
    TextView tvAmount;

    private volatile boolean bCancelInputAmount = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_amount);
        ButterKnife.bind(this);
        inputAmount();
    }

    @Override
    protected void setButtonName() {

    }

    @Override
    public void onBackPressed() {
        cancel();
        super.onBackPressed();
    }

    @OnClick({R.id.iv_back})
    public void onClick() {
        onBackPressed();
    }

    private void inputAmount() {
        bCancelInputAmount = false;
        if (!Controler.posConnected()) {
            ToastUtils.show(this, getString(R.string.device_not_connect));
            return;
        }
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                int timeOut = 60 * 3;
                while (Controler.posConnected() && !bCancelInputAmount) {
                    InputAmountResult inputAmountResult = Controler.InputAmount(timeOut, "Consume");
                    if (inputAmountResult.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                        if (inputAmountResult.binput == 0) {
                            setAmount(inputAmountResult.amount);
                        }
                    }
                }
            }
        });
    }

    public void cancel() {
        bCancelInputAmount = true;
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                Controler.CancelComm();
            }
        });
    }

    private void setAmount(String amount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAmount.setText(getReadableAmount(amount));
            }
        });
    }

    private String getReadableAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return "0.00";
        }
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(Double.parseDouble(amount) / 100);
    }
}