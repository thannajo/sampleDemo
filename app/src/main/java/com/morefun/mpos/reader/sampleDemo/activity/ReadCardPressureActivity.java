package com.morefun.mpos.reader.sampleDemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.EmvTagDef;
import com.mf.mpos.pub.param.ReadCardParam;
import com.mf.mpos.pub.result.ReadCardResult;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadCardPressureActivity extends BaseActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.button)
    Button button;

    @BindView(R.id.currentTimes)
    TextView tvCurrentTime;

    @BindView(R.id.successTime)
    TextView tvSuccessTime;

    @BindView(R.id.failTime)
    TextView tvFailTime;

    @BindView(R.id.TestTimes)
    EditText etTestTime;

    @BindView(R.id.failList)
    TextView tvFailList;

    @BindView(R.id.startTime)
    TextView tvStartTime;

    @BindView(R.id.endTime)
    TextView tvEndTime;

    private int failTimes = 0;
    private int successTimes = 0;
    private int currentTimes = 0;

    private List<String> failList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readcard_pressure);
        ButterKnife.bind(this);
        setButtonName();
    }

    @OnClick({R.id.button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                initTest();

                if (Controler.posConnected()) {
                    runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            showStartTime();
                            for (currentTimes = 0; currentTimes < Integer.parseInt(etTestTime.getText().toString()); currentTimes++) {
                                try {
                                    if (Controler.posConnected()) {
                                        showCurrentTime(currentTimes + 1);
                                        showResult("(" + currentTimes + ")" + "Start Test " + getCurrentTime());
                                        startSwiper(0, "000000000001", 30, "000001");
                                        Thread.sleep(5 * 1000);
                                    } else {
                                        showEndTime();
                                        return;
                                    }

                                } catch (InterruptedException e) {

                                }
                            }
                            showEndTime();
                        }
                    });

                } else {
                    SweetDialogUtils.showError(ReadCardPressureActivity.this, getString(R.string.device_not_connect));
                }
                break;

        }
    }

    @Override
    protected void setButtonName() {
        button.setText(R.string.read_card_pressure);
    }

    public synchronized void startSwiper(int transType, String amt, int time, String traceNo) {
        ReadCardParam param = new ReadCardParam();

        param.setAllowfallback(true);
        param.setAmount(Long.parseLong(amt));
        param.setPinInput((byte) 0x00);
        param.setPinMaxLen((byte) 6);
        param.setCardTimeout((byte) time);
        param.setEmvTransactionType((byte) 0x01);

        List<byte[]> tags = new ArrayList<byte[]>();

        tags.add(EmvTagDef.EMV_TAG_9F02_TM_AUTHAMNTN);
        tags.add(EmvTagDef.EMV_TAG_9F26_IC_AC);
        tags.add(EmvTagDef.EMV_TAG_9F27_IC_CID);
        tags.add(EmvTagDef.EMV_TAG_9F10_IC_ISSAPPDATA);
        tags.add(EmvTagDef.EMV_TAG_9F37_TM_UNPNUM);
        tags.add(EmvTagDef.EMV_TAG_9F36_IC_ATC);
        tags.add(EmvTagDef.EMV_TAG_95_TM_TVR);
        tags.add(EmvTagDef.EMV_TAG_9A_TM_TRANSDATE);
        tags.add(EmvTagDef.EMV_TAG_9C_TM_TRANSTYPE);
        tags.add(EmvTagDef.EMV_TAG_5F2A_TM_CURCODE);
        tags.add(EmvTagDef.EMV_TAG_82_IC_AIP);
        tags.add(EmvTagDef.EMV_TAG_9F1A_TM_CNTRYCODE);
        tags.add(EmvTagDef.EMV_TAG_9F03_TM_OTHERAMNTN);
        tags.add(EmvTagDef.EMV_TAG_9F33_TM_CAP);
        tags.add(EmvTagDef.EMV_TAG_9F34_TM_CVMRESULT);
        tags.add(EmvTagDef.EMV_TAG_9F35_TM_TERMTYPE);
        tags.add(EmvTagDef.EMV_TAG_9F1E_TM_IFDSN);
        tags.add(EmvTagDef.EMV_TAG_84_IC_DFNAME);
        tags.add(EmvTagDef.EMV_TAG_9F09_TM_APPVERNO);
        tags.add(EmvTagDef.EMV_TAG_9F63_TM_BIN);
        tags.add(EmvTagDef.EMV_TAG_9F41_TM_TRSEQCNTR);

        param.setTags(tags);

        switch (transType) {
            case 0:
                //Set transaction type
                param.setTransType(CommEnum.TRANSTYPE.FUNC_SALE);
                break;
            case 1:
                param.setTransType(CommEnum.TRANSTYPE.FUNC_BALANCE);
                break;
            case 2:
                param.setTransType(CommEnum.TRANSTYPE.FUNC_VOID_SALE);
                break;
            case 3:
                param.setTransName("");
                break;
            default:
                param.setTransName("");
                break;
        }

        param.setOnSteplistener(new ReadCardParam.onStepListener() {
            @Override
            public void onStep(byte step) {
                switch (step) {
                    case 1://waiting read card
                        showResult(getString(R.string.wait_read_card));
                        break;
                    case 2://Reading card
                        showResult(getString(R.string.reading_card));
                        break;
                    case 3://Waiting enter the password
                        showResult(getString(R.string.waiting_input_pin));
                        break;
                    case 4://Waiting enter the amount
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                }
            }
        });

        ReadCardResult result = Controler.ReadCard(param);

        if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            showResult(getString(R.string.read_card_error));
        } else {
            switch (result.cardType) {
                case 0:
                    showResult(getString(R.string.user_cancel));
                    break;
                case 1:
                case 2:
                case 3:
                    StringBuilder builder = new StringBuilder();
                    if (result.cardType == 1) {
                        builder.append("cardType:" + "Mag Card");
                    } else if (result.cardType == 2) {
                        builder.append("cardType:" + "IC Card");
                    } else if (result.cardType == 3) {
                        builder.append("cardType:" + "RF Card");
                    }

                    builder.append("\npan:" + result.pan);
                    builder.append("\ntrack2:" + result.track2);
                    builder.append("\nicData:" + result.icData);

                    successTimes++;
                    showSuccessTime(successTimes);

                    showResult(builder.toString());
                    return;

                case 4:
                    showResult(getString(R.string.need_insert_iccard));
                    //Need Insert ICCard
                    break;
                case 5:
                    //TimeOut
                    showResult(getString(R.string.timeout));
                    break;
                case 6:
                    showResult(getString(R.string.read_card_error));
                    //read error
                    break;
                default:
                    break;
            }
        }

        failTimes++;
        showFailTime(failTimes);
        showFailList();
    }

    private void showResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(result + "\r\n");
            }
        });
    }

    private void showCurrentTime(int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCurrentTime.setText("Current:" + i);
            }
        });
    }

    private void showSuccessTime(int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSuccessTime.setText("Success:" + i);
            }
        });
    }

    private void showFailTime(int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvFailTime.setText("Fail:" + i);
            }
        });
        if (i != 0) {
            failList.add(currentTimes + "");
        }
    }

    private void showFailList() {
        if (failList.size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvFailList.setText("失败列表:" + failList.toString());
                }
            });
        }
    }

    private void initTest() {
        showCurrentTime(0);
        showFailTime(0);
        showSuccessTime(0);
        failTimes = 0;
        successTimes = 0;
        currentTimes = 0;

        textView.setText("");
    }

    private void showStartTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStartTime.setText("Start:" + getCurrentTime());
            }
        });

    }

    private void showEndTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEndTime.setText("Finish:" + getCurrentTime());
            }
        });
    }

    private String getCurrentTime() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("MM/dd HH:mm:ss");
        String time = format.format(date);
        return time;
    }

}