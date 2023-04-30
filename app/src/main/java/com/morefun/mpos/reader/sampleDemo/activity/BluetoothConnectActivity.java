package com.morefun.mpos.reader.sampleDemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ConnectPosResult;
import com.morefun.mpos.reader.sampleDemo.MyApplication;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.adapter.BluetoothItem;
import com.morefun.mpos.reader.sampleDemo.adapter.BluetoothListAdapter;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;

import java.lang.reflect.Method;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BluetoothConnectActivity extends Activity {
    @BindView(R.id.listview)
    ListView mListView;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.id)
    TextView id;

    String serial;
    private String TAG = "JGONZALEZ -> [" + this.getClass().getName() + "]";

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothListAdapter mAdapter;
    private BluetoothReceiver br;
    private String bluetoothMac = "";
    private String bluetoothName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_bluetooth);
        serial = getIntent().getExtras().getString("serial");
        ButterKnife.bind(this);
        mAdapter = new BluetoothListAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setOnItemClickListener(mListClickListener);

        btAdapter.enable();
        Log.d(TAG, "getBluetoothName(): [" + MyApplication.getBluetoothName() + "]");
        Log.d(TAG, "getBluetoothMac(): [" + MyApplication.getBluetoothName() + "]");

        init();
        registerReceiver();

    }

    private void init() {
        Log.d(TAG, "init");
        Log.d(TAG, "ID: [" + MyApplication.getManufacturerID() + "]");
        Controler.Init(this, CommEnum.CONNECTMODE.BLUETOOTH, MyApplication.getManufacturerID());

        setTitle();
        setID();

    }

    private void setTitle() {
        Log.d(TAG, "setTitle");
        String connectMode = MyApplication.getConnectedMode();
        String connected = "[" + getString(R.string.connected) + "]";
        String disconnected = "[" + getString(R.string.disconnect) + "]";
        title.setText(Controler.posConnected() ? connected : disconnected);
    }

    private void setID() {
        id.setText("ID:" + MyApplication.getManufacturerID());
    }

    @OnClick({R.id.connect, R.id.isConnect, R.id.disconnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect:
                connectDevice();
                break;
            case R.id.isConnect:
                if (Controler.posConnected()) {
                    new SweetAlertDialog(BluetoothConnectActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setContentText(getString(R.string.device_already_connect))
                            .show();
                } else {
                    new SweetAlertDialog(BluetoothConnectActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setContentText(getString(R.string.device_not_connect))
                            .show();
                }
                break;
            case R.id.disconnect:
                Controler.disconnectPos();
                setTitle();
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        btAdapter.cancelDiscovery();
        unregisterReceiver();
        finish();

    }

    private AdapterView.OnItemClickListener mListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            mAdapter.setSelected(position);
        }
    };

    private void unregisterReceiver() {
        if (br != null) {
            try {
                unregisterReceiver(br);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        br = null;
    }

    private void registerReceiver() {
        try {
            br = new BluetoothReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            registerReceiver(br, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (btDevice != null) {
                        mAdapter.addItem(new BluetoothItem(btDevice.getName(), btDevice.getAddress(), false));
                        mAdapter.notifyDataSetChanged();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startDiscovery(String bt_name) {
        Log.d(TAG, "startDiscovery");
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        mAdapter.clear();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(bt_name)) {
                mAdapter.addItem(new BluetoothItem(device.getName(), device.getAddress(), true));
            }
        }
        mAdapter.notifyDataSetChanged();
        btAdapter.startDiscovery();
    }

    private void connectDevice() {
        Log.d(TAG, "connectDevice");

        SweetDialogUtils.showProgress(BluetoothConnectActivity.this, getString(R.string.device_connect), false);
        String btSerial = "MP-".concat(serial.substring(6,14));
        Log.d(TAG, btSerial);
        MyApplication.setBluetoothName(btSerial);
        startDiscovery(btSerial);
        MyApplication.setBluetoothMac(((BluetoothItem) mAdapter.getItem(mAdapter.getSelected())).getAddress().toString());

        bluetoothName = MyApplication.getBluetoothName();
        bluetoothMac = MyApplication.getBluetoothMac();


        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Controler.posConnected()) {
                    Controler.disconnectPos();
                }
                ConnectPosResult ret = Controler.connectPos(bluetoothMac);

                if (ret.bConnected) {
                    MyApplication.setBluetoothMac(bluetoothMac);
                    MyApplication.setBluetoothName(bluetoothName);
                    SweetDialogUtils.changeAlertType(BluetoothConnectActivity.this, getString(R.string.device_already_connect), SweetAlertDialog.SUCCESS_TYPE);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle();
                            startActivity(new Intent(BluetoothConnectActivity.this, StartSwiperActivity.class));
                        }
                    });
                } else {
                    SweetDialogUtils.changeAlertType(BluetoothConnectActivity.this, getString(R.string.device_not_connect), SweetAlertDialog.ERROR_TYPE);
                }
            }
        }).start();

    }

    private void removeBondMethods() {
        Log.d(TAG,"removeBondMethods");
        Controler.disconnectPos();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice b : pairedDevices) {
            removeBondMethod(b);
        }
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private int removeBondMethod(BluetoothDevice btDev) {
        Log.d(TAG,"removeBondMethod");
        //Using reflection method calls BluetoothDevice.createBond(BluetoothDevice remoteDevice);
        Method removeBondMethod = null;
        try {
            removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            removeBondMethod.invoke(btDev);
            Log.w("removeBondMethod", "removeBondMethod  	removeBondMethod.invoke(btDev); ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}