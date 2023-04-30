package com.morefun.mpos.reader.sampleDemo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.MyApplication;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.model.RequestUser;
import com.morefun.mpos.reader.sampleDemo.model.Response;
import com.morefun.mpos.reader.sampleDemo.retrofit.APIInterface;
import com.morefun.mpos.reader.sampleDemo.retrofit.APIClient;
import com.morefun.mpos.reader.sampleDemo.retrofit.OnJsonResponse;
import com.morefun.mpos.reader.sampleDemo.utils.PermissionHelper;

import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;


public class MainActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.version)
    TextView version;

    @BindView(R.id.id)
    TextView id;

    @BindView(R.id.btn_entrar)
    Button entrar;

    @BindView(R.id.et_user_name)
    EditText username;
    @BindView(R.id.et_user_pass)
    EditText userpass;


    private String TAG = "JGONZALEZ -> [" + this.getClass().getName() + "]";

    private String[] permissionList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();

        if (!PermissionHelper.checkPermissionAllGranted(permissionList, this)) {
            PermissionHelper.requestPermission(this, permissionList, 0);
        } else {
            startService();
        }
    }

    private void startService() {
        Log.d(TAG, "startService");
        Log.d(TAG, "Build.VERSION.SDK_INT [" + Build.VERSION.SDK_INT + "]");
        Log.d(TAG, "Build.VERSION_CODES.O [" + Build.VERSION_CODES.O + "]");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setTitle();
        setVersion();
        setID();
    }


    @Override
    protected void setButtonName() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult requestCode: [" + requestCode + "]");
        if (requestCode == 0 && grantResults != null) {
            for (int grantResult : grantResults) {
                Log.d(TAG, "onRequestPermissionsResult grantResults: [" + grantResult + "]");
                if (grantResult == -1) {
                    finish();
                    return;
                }
            }
            startService();
        }
    }


    private void init() {
        Log.d(TAG, "init");
        Log.d(TAG, "ID: [" + MyApplication.getManufacturerID() + "]");
        Controler.Init(this, CommEnum.CONNECTMODE.BLUETOOTH, MyApplication.getManufacturerID());

        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(new RequestUser(getUserName(), getUserPass()), new OnJsonResponse() {
                    @Override
                    public void jsonResult(Response response) {
                        if ("1".equals(response.getCode())) {
                            Intent intent = new Intent(MainActivity.this, BluetoothConnectActivity.class);
                            intent.putExtra("serial", response.getData().getAfiliado().getDevice().getSerial());
                            Log.d(TAG, "MP-".concat(response.getData().getAfiliado().getDevice().getSerial().substring(6, 14)));
                            startActivity(intent);
                        } else {
                            String text = response.getMessage();
                            Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    private void loginUser(RequestUser requestUser, OnJsonResponse callback) {
        Log.d(TAG, "loginUser");

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);

        Call<Response> call = apiInterface.loginUser(requestUser);
        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                callback.jsonResult(response.body());
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Response failResponse = new Response();

                if (t instanceof SocketTimeoutException) {
                    failResponse.setCode("-1");
                    failResponse.setMessage("SocketTimeoutException");
                }

                callback.jsonResult(failResponse);
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

    private String getUserName() {
        return username.getText().toString();
    }

    private String getUserPass() {
        return userpass.getText().toString();
    }

}