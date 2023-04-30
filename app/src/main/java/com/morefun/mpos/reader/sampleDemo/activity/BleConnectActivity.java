package com.morefun.mpos.reader.sampleDemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BleConnectActivity extends Activity {
    @BindView(R.id.buttonscan)
    Button btnScan;

    private Handler mHandler;
    private ListView listview;
    private ExecutorService executor;
    private List<String> devs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ble);
        ButterKnife.bind(this);

        executor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(getMainLooper());

        listview = (ListView) findViewById(R.id.listView);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = listview.getAdapter().getItem(position);
                String[] items = item.toString().split(" ");
                final String mac = items[0];

                SweetDialogUtils.showProgress(BleConnectActivity.this, getString(R.string.device_connect), false);

                executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        Controler.connectPos(mac);
                        final boolean bret = Controler.posConnected();

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (Controler.posConnected()) {
                                    SweetDialogUtils.changeAlertType(BleConnectActivity.this, getString(R.string.device_already_connect), SweetAlertDialog.SUCCESS_TYPE);
                                } else {
                                    SweetDialogUtils.changeAlertType(BleConnectActivity.this, getString(R.string.device_not_connect), SweetAlertDialog.ERROR_TYPE);
                                }
                            }
                        }, 100);
                        return bret;
                    }
                });
            }
        });
    }

    @OnClick({R.id.buttonscan, R.id.isConnect, R.id.disconnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonscan:
                BluetoothManager blueManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                blueManager.getAdapter().enable();
                final BluetoothLeScanner scanner = blueManager.getAdapter().getBluetoothLeScanner();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnScan.setEnabled(true);
                        scanner.stopScan(mLeScanCallback);

                    }
                }, 5000);

                devs.clear();
                scanner.startScan(mLeScanCallback);
                btnScan.setEnabled(false);
                break;
            case R.id.isConnect:
                if (Controler.posConnected()) {
                    new SweetAlertDialog(BleConnectActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setContentText(getString(R.string.device_already_connect))
                            .show();
                } else {
                    new SweetAlertDialog(BleConnectActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setContentText(getString(R.string.device_not_connect))
                            .show();
                }
                break;
            case R.id.disconnect:
                Controler.disconnectPos();
                break;
            default:
                break;
        }
    }

    void refrushlistview() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devs);
        listview.setAdapter(adapter);
    }

    public ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int index = 0;
            for (; index < devs.size(); index++) {
                if (devs.get(index).indexOf(device.getAddress()) >= 0) {
                    break;
                }
            }

            String item = device.getAddress() + " " + device.getName();
            if (index < devs.size()) {
                devs.set(index, item);
            } else {
                devs.add(item);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    refrushlistview();
                }
            });
        }
    };
}
