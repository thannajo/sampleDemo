package com.morefun.mpos.reader.sampleDemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.EmvTagDef;
import com.mf.mpos.pub.param.ReadCardParam;
import com.mf.mpos.pub.result.GetEmvDataResult;
import com.mf.mpos.pub.result.ReadCardResult;
import com.morefun.mpos.reader.sampleDemo.MyApplication;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.retrofit.APIClient;
import com.morefun.mpos.reader.sampleDemo.retrofit.APIInterface;
import com.morefun.mpos.reader.sampleDemo.retrofit.OnStringResponse;
import com.morefun.mpos.reader.sampleDemo.utils.BytesUtil;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;
import com.morefun.mpos.reader.sampleDemo.utils.ToastUtils;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartSwiperActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.version)
    TextView version;

    @BindView(R.id.id)
    TextView id;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.et_transAmount)
    EditText et_transAmount;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @BindView(R.id.et_cedula)
    EditText cedula;

    @BindView(R.id.menuBtn)
    Button menu;

    private final String TAG = "JGONZALEZ -> [" + this.getClass().getName() + "]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiper_card);
        ButterKnife.bind(this);
        setButtonName();

        initView();
    }

    @OnClick({R.id.readCard, R.id.cancelButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.readCard:
                if (Controler.posConnected()) {
                    runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            if (checkValue()) {
                                SweetDialogUtils.showProgress(StartSwiperActivity.this, getString(R.string.read_card), false);
                                Controler.ResetPos();
                                startSwiper();
                                scrollFocusDown();
                            }
                        }
                    });
                } else {
                    SweetDialogUtils.showError(StartSwiperActivity.this, getString(R.string.device_not_connect));
                }
                break;
            case R.id.cancelButton:
                Controler.CancelComm();
                textView.setText(getString(R.string.cancel_swiper));
                break;
        }
    }

    @Override
    protected void setButtonName() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle();
        setVersion();
        setID();
    }

    private void initView() {
        Log.d(TAG, "initView");

        enableEditText(cedula);
        enableEditText(et_transAmount);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartSwiperActivity.this, MenuActivity.class));
            }
        });
        scrollFocusDown();
    }

    private void disableEditText(EditText editText) {
        Log.d(TAG, "disableEditText: [" + editText.getText().toString() + "]");
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);

    }

    private void enableEditText(EditText editText) {
        Log.d(TAG, "enableEditText: [" + editText.getText().toString() + "]");
        editText.setFocusable(true);
        editText.setEnabled(true);
        editText.setCursorVisible(true);

    }

    private ReadCardParam getReadCardParam(ReadCardParam.onStepListener listener) {
        Log.d(TAG, "getReadCardParam");

        ReadCardParam param = new ReadCardParam();

        param.setTrackEncryptType((byte) 0x1);
        //FALLBACK ENABLE
        param.setAllowfallback(true);
        param.setAmount(getTransAmount());
        //PININPUT ENABLE
        param.setPinInput((byte) 1);
        param.setPinMaxLen((byte) 6);
        param.setCardTimeout(getReadCardTimeout());
        //FORCE ONLINE
        param.setForceonline(true);
        //TRANS NAME - CONSUMO
        param.setTransName(getTransName());
        param.setTags(getTags());
        param.setOnSteplistener(listener);
        param.setCardmode(getCardMode());
        //TRANS TYPE
        param.setEmvTransactionType((byte) 0x00);

        /**
         * PINPAD PROCESS LINE
         */
        param.setInputPinLine1Tip("");
        param.setInputPinLine2Tip("PLEASE INSERT");
        param.setInputPinLine3Tip("PIN:");
        param.setInputPinLine4Tip("");

        /**
         * READ PROCESS LINE
         */
        param.setSwipeInsertTapCardTip("Use the Card");
        param.setSwipeCardTip("Swipe Card");
        param.setInsertCardTip("Insert Card");
        param.setInsertTapCardTip("Insert/Tap Card");

        /*
         * AMOUNT LINE
         */
        param.setReadCardLine2Tip("AMOUNT: [" + String.valueOf(getTransAmount()) + "]");

        /*
         * CEDULA
         */
        getCedula();

        return param;
    }

    private List<byte[]> getTags() {
        Log.d(TAG, "getTags");

        List<byte[]> tags = new ArrayList<byte[]>();

        tags.add(EmvTagDef.EMV_TAG_4F_IC_AID);
        tags.add(EmvTagDef.EMV_TAG_50_IC_APPLABEL);
        tags.add(EmvTagDef.EMV_TAG_57_IC_TRACK2EQUDATA);
        tags.add(EmvTagDef.EMV_TAG_5A_IC_PAN);
        tags.add(EmvTagDef.EMV_TAG_5F20_IC_HOLDERNAME);
        tags.add(EmvTagDef.EMV_TAG_5F24_IC_APPEXPIREDATE);
        tags.add(EmvTagDef.EMV_TAG_5F25_IC_APPEFFECTDATE);
        tags.add(EmvTagDef.EMV_TAG_5F2A_TM_CURCODE);
        tags.add(EmvTagDef.EMV_TAG_5F30_IC_SERVICECODE);
        tags.add(EmvTagDef.EMV_TAG_5F34_IC_PANSN);
        tags.add(EmvTagDef.EMV_TAG_82_IC_AIP);
        tags.add(EmvTagDef.EMV_TAG_84_IC_DFNAME);
        tags.add(EmvTagDef.EMV_TAG_8E_IC_CVMLIST);
        tags.add(EmvTagDef.EMV_TAG_95_TM_TVR);
        tags.add(EmvTagDef.EMV_TAG_9A_TM_TRANSDATE);
        tags.add(EmvTagDef.EMV_TAG_9B_TM_TSI);
        tags.add(EmvTagDef.EMV_TAG_9C_TM_TRANSTYPE);
        tags.add(EmvTagDef.EMV_TAG_9F01_TM_ACQID);
        tags.add(EmvTagDef.EMV_TAG_9F02_TM_AUTHAMNTN);
        tags.add(EmvTagDef.EMV_TAG_9F03_TM_OTHERAMNTN);
        tags.add(EmvTagDef.EMV_TAG_9F06_TM_AID);
        tags.add(EmvTagDef.EMV_TAG_9F09_TM_APPVERNO);
        tags.add(EmvTagDef.EMV_TAG_9F0D_IC_IAC_DEFAULT);
        tags.add(EmvTagDef.EMV_TAG_9F0E_IC_IAC_DENIAL);
        tags.add(EmvTagDef.EMV_TAG_9F0F_IC_IAC_ONLINE);
        tags.add(EmvTagDef.EMV_TAG_9F10_IC_ISSAPPDATA);
        tags.add(EmvTagDef.EMV_TAG_9F12_IC_APNAME);
        tags.add(EmvTagDef.EMV_TAG_9F16_TM_MCHID);
        tags.add(EmvTagDef.EMV_TAG_9F1A_TM_CNTRYCODE);
        tags.add(EmvTagDef.EMV_TAG_9F1C_TM_TERMID);
        tags.add(EmvTagDef.EMV_TAG_9F1E_TM_IFDSN);
        tags.add(EmvTagDef.EMV_TAG_9F21_TM_TRANSTIME);
        tags.add(EmvTagDef.EMV_TAG_9F26_IC_AC);
        tags.add(EmvTagDef.EMV_TAG_9F27_IC_CID);
        tags.add(EmvTagDef.EMV_TAG_9F33_TM_CAP);
        tags.add(EmvTagDef.EMV_TAG_9F34_TM_CVMRESULT);
        tags.add(EmvTagDef.EMV_TAG_9F35_TM_TERMTYPE);
        tags.add(EmvTagDef.EMV_TAG_9F36_IC_ATC);
        tags.add(EmvTagDef.EMV_TAG_9F37_TM_UNPNUM);
        tags.add(EmvTagDef.EMV_TAG_9F39_TM_POSENTMODE);
        tags.add(EmvTagDef.EMV_TAG_9F40_TM_CAP_AD);
        tags.add(EmvTagDef.EMV_TAG_9F41_TM_TRSEQCNTR);
        tags.add(EmvTagDef.EMV_TAG_9F4E_TM_NAMELOC);

        return tags;
    }

    private ReadCardParam.onStepListener stepListener = new ReadCardParam.onStepListener() {
        @Override
        public void onStep(byte step) {
            Log.d(TAG, "stepListener - onStep [" + step + "]");
            switch (step) {
                case 1://waiting read card
                    SweetDialogUtils.setProgressText(StartSwiperActivity.this, getString(R.string.wait_read_card));
                    break;
                case 2://Reading card
                    SweetDialogUtils.setProgressText(StartSwiperActivity.this, getString(R.string.reading_card));
                    break;
                case 3://Waiting enter the password
                    SweetDialogUtils.setProgressText(StartSwiperActivity.this, getString(R.string.waiting_input_pin));
                    break;
                case 4://Waiting enter the amount
                    break;
            }
        }
    };

    public synchronized void startSwiper() {
        Log.d(TAG, "startSwiper");
        ReadCardResult result = Controler.ReadCard(getReadCardParam(stepListener));

        if (!result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.read_card_error), SweetAlertDialog.ERROR_TYPE);
            return;
        }

        switch (result.cardType) {
            case 0:
                SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.user_cancel), SweetAlertDialog.ERROR_TYPE);
                break;
            case 1:
            case 2:
            case 3:
                /**
                 * EN result hay data valiosa
                 */

                StringBuilder builder = new StringBuilder();

                builder.append(getTransAmount() > 0 ? "amount: [" + getTransAmount() + "]\n" : "");
                builder.append(getCedula() != null ? "cedula: [" + getCedula() + "]\n" : null);
                builder.append(result.cardType > 0 ? "cardType: [" + result.cardType + "]\n" : null);
                builder.append(result.getCartTypeName() != null ? "cardTypeName: [" + result.getCartTypeName() + "]\n" : "");
                builder.append(result.pan != null ? "pan: [" + result.pan + "]\n" : "");
                builder.append(result.pansn != null ? "panSn: [" + result.pansn + "]\n" : "");
                builder.append(result.pinblock != null ? "pinBlock: [" + result.pinblock + "]\n" : "");
                builder.append(result.track2 != null ? "track2: [" + result.track2 + "]\n" : "");
                builder.append(result.icData != null ? "icData: [" + result.icData + "]\n" : "");
                builder.append(result.expData != null ? "expData: [" + result.expData + "]\n" : "");
                builder.append(result.mac_ksn != null ? "mac_ksn: [" + result.mac_ksn + "]\n" : "");
                builder.append(result.mag_ksn != null ? "mag_ksn: [" + result.mag_ksn + "]\n" : "");
                builder.append(result.pin_ksn != null ? "pin_ksn: [" + result.pin_ksn + "]\n" : "");
                builder.append(result.encryptPan != null ? "encryptPan: [" + result.encryptPan + "]\n" : "");
                /***
                 * 0x01 approve
                 * 0x02 online
                 * 0x03 reject
                 * 0x04 termination
                 */
                builder.append(result.emvResult > 0 ? "emvResult: [" + result.emvResult + "]\n" : "");
                builder.append(result.serviceCode != null ? "serviceCode: [" + result.serviceCode + "]\n" : "");

                //Get IC TAG
                if (result.cardType == 2 || result.cardType == 3) {
                    try {
                        GetEmvDataResult getEmvDataResult = Controler.GetEmvData(getTags());
                        /**
                         * En getEmvDataResult esta el TLV
                         */
                        byte[] tlvData = getEmvDataResult.tlvData;
                        String transaction = BytesUtil.bytes2Hex(tlvData);
                        Log.d(TAG, transaction);

                        String serial = Controler.ReadPosInfo2().sn;
                        Log.d(TAG, "serial: [" + serial + "]");
                        Log.d(TAG, "serialHex: [" + BytesUtil.bytes2Hex(serial.getBytes()) + "]");

                        String tag_DF834F = "DF834F".concat("0").concat(Integer.toHexString(serial.length()).toUpperCase()).concat(BytesUtil.bytes2Hex(serial.getBytes()));
                        transaction = transaction.concat(tag_DF834F);

                        send(transaction, new OnStringResponse() {
                            @Override
                            public void stringResult(String string) {
                                if (string.startsWith("8a023030")) {
                                    SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.operation_success), SweetAlertDialog.SUCCESS_TYPE);
                                } else {
                                    SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.operation_fail), SweetAlertDialog.ERROR_TYPE);
                                }
                                showResult(string.substring(15,string.length()-1));
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 4:
                SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.need_insert_iccard), SweetAlertDialog.ERROR_TYPE);
                //Need Insert ICCard
                break;
            case 5:
                //TimeOut
                SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.timeout), SweetAlertDialog.ERROR_TYPE);
                break;
            case 6:
                SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.read_card_error), SweetAlertDialog.ERROR_TYPE);
                //read error
                break;
            default:
                SweetDialogUtils.changeAlertType(StartSwiperActivity.this, getString(R.string.read_card_error), SweetAlertDialog.ERROR_TYPE);
                break;
        }
    }

    private byte getCardMode() {
        Log.d(TAG, "getCardMode");

        byte cardMode = 0;
        cardMode += 1;
        cardMode += 2;
        cardMode += 4;

        Log.d(TAG, "carMode [" + cardMode + "]");

        return cardMode;
    }

    private String getTransName() {
        Log.d(TAG, "getTransName");

        return "CONSUME";
    }

    private CommEnum.TRANSTYPE getTransType() {
        Log.d(TAG, "getTransType");

        return CommEnum.TRANSTYPE.FUNC_SALE;
    }

    private long getTransAmount() {
        Log.d(TAG, "getTransAmount");
        try {
            long amount = Long.parseLong(et_transAmount.getText().toString());
            return amount;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getCedula() {
        Log.d(TAG, "getCedula");
        return cedula.getText().toString();
    }

    private byte getReadCardTimeout() {
        Log.d(TAG, "getReadCardTimeout");

        try {
            byte timeout = (byte) Integer.parseInt("60".toString());
            return timeout;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return (byte) 30;
    }

    private void showResult(final String result) {
        Log.d(TAG, result);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(result);
            }
        });
    }

    private boolean checkValue() {
        Log.d(TAG, "checkValue");
        if (getTransType() == CommEnum.TRANSTYPE.FUNC_SALE && getTransAmount() == 0) {
            ToastUtils.show(this, getString(R.string.please_input_amount));
            return false;
        }
        return true;
    }

    private void scrollFocusDown() {
        Log.d(TAG, "scrollFocusDown");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }, 100);
            }
        });
    }

    private void send(String transaction, OnStringResponse callback) {
        Log.d(TAG, "send");
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<String> call = apiInterface.getString(transaction);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "[send - onResponse: [" + response.body() + "]");
                callback.stringResult(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String failResponse = null;

                if (t instanceof SocketTimeoutException) {
                    failResponse = "8a02544F";
                }

                callback.stringResult(failResponse);
            }
        });
    }

    private void setTitle() {
        Log.d(TAG, "setTitle");
        String connectMode = MyApplication.getConnectedMode();
        String connected = "[" + getString(R.string.connected) + "]";
        String disconnected = "[" + getString(R.string.disconnect) + "]";
        title.setText(Controler.posConnected() ? connected : disconnected);

    }

    private void setVersion() {
        Log.d(TAG, "setVersion");
        version.setText(getVersionName(this));
    }

    private void setID() {
        Log.d(TAG, "setID");
        id.setText("ID:" + MyApplication.getManufacturerID());
    }
}