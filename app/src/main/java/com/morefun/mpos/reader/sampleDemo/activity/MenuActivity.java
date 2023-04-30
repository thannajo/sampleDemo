package com.morefun.mpos.reader.sampleDemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mf.mpos.pub.Controler;
import com.morefun.mpos.reader.sampleDemo.MyApplication;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.activity.BaseActivity;
import com.morefun.mpos.reader.sampleDemo.model.Catalog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.id)
    TextView id;

    @BindView(R.id.version)
    TextView version;

    private ListView lvCatalog;
    private List<Catalog> catalogs;
    private ArrayAdapter<Catalog> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        lvCatalog = (ListView) findViewById(R.id.lvCatalog);
        init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        setTitle();
    }

    @Override
    protected void setButtonName() {

    }
    private void addCatalog() {
        catalogs = new ArrayList<Catalog>();

        /*
        if (MyApplication.getConnectedMode().equals(getString(R.string.bluetooth))) {
            catalogs.add(new Catalog(getString(R.string.device_connect), BluetoothConnectActivity.class));
        } else if (MyApplication.getConnectedMode().equals(getString(R.string.usb_hid))) {
            catalogs.add(new Catalog(getString(R.string.device_connect), UsbHidConnectActivity.class));
        } else if (MyApplication.getConnectedMode().equals(getString(R.string.ble))) {
            catalogs.add(new Catalog(getString(R.string.device_connect), BleConnectActivity.class));
        } else {
            catalogs.add(new Catalog(getString(R.string.device_connect), BluetoothConnectActivity.class));
        }
        */
        catalogs.add(new Catalog(getString(R.string.load_kek), LoadKekActivity.class));
        catalogs.add(new Catalog(getString(R.string.download_master_key), UpdateMasterKeyActivity.class));
        catalogs.add(new Catalog(getString(R.string.download_work_key), UpdateWorkKeyActivity.class));
        //catalogs.add(new Catalog(getString(R.string.read_card), StartSwiperActivity.class));
        //catalogs.add(new Catalog(getString(R.string.input_pin), InputPinActivity.class));
        //catalogs.add(new Catalog(getString(R.string.input_amount), InputAmountActivity.class));
        catalogs.add(new Catalog(getString(R.string.perform_online_authorization), OnlineAuthActivity.class));
        catalogs.add(new Catalog(getString(R.string.calc_mac), CalcMacActivity.class));
        catalogs.add(new Catalog(getString(R.string.download_aid), DownloadAidActivity.class));
        catalogs.add(new Catalog(getString(R.string.download_capk), DownloadCapkActivity.class));
        catalogs.add(new Catalog(getString(R.string.load_dukpt), LoadDukptActivity.class));
        catalogs.add(new Catalog(getString(R.string.set_emv_param), SetEmvParamActivity.class));
        //catalogs.add(new Catalog(getString(R.string.mpos_ui_display), SetBitmapActivity.class));
        catalogs.add(new Catalog(getString(R.string.get_deviceinof), GetDeviceInfoActivity.class));
        //catalogs.add(new Catalog(getString(R.string.terminal_update), UpdateActivity.class));
        //catalogs.add(new Catalog(getString(R.string.read_card_pressure), ReadCardPressureActivity.class));
        //catalogs.add(new Catalog(getString(R.string.m1_card), M1CardActivity.class));
        catalogs.add(new Catalog(getString(R.string.set_sleep_time), SettingSleepTimeActivity.class));
        //catalogs.add(new Catalog(getString(R.string.card), RFCardActivity.class));
        //catalogs.add(new Catalog(getString(R.string.qrcode), QrCodeActivity.class));
        //catalogs.add(new Catalog(getString(R.string.get_random_numbers), GetRandomActivity.class));
        catalogs.add(new Catalog(getString(R.string.load_public_key), LoadPublicKeyActivity.class));
        //catalogs.add(new Catalog(getString(R.string.load_rki), LoadRKIActivity.class));
        //catalogs.add(new Catalog(getString(R.string.ble_awake), BleAwakeActivity.class));
        //catalogs.add(new Catalog(getString(R.string.vendor_verify), VendorVerifyActivity.class));
        //catalogs.add(new Catalog(getString(R.string.show_bitmap), ShowBitmapActivity.class));
    }

    private void init() {
        addCatalog();
        adapter = new ArrayAdapter<Catalog>(this, R.layout.menu_list_item, catalogs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.menu_list_item, null);

                Catalog catalog = getItem(position);
                TextView name = (TextView) view.findViewById(R.id.value);
                TextView index = (TextView) view.findViewById(R.id.index);

                name.setText(catalog.name);
                index.setText(position + 1 + ".");
                return view;
            }

            @Override
            public int getCount() {
                return catalogs.size();
            }

        };

        lvCatalog.setAdapter(adapter);
        lvCatalog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (catalogs.get(position).cls != null) {
                    startActivity(new Intent(MenuActivity.this, catalogs.get(position).cls));
                } else {
                    if (catalogs.get(position).name.equals(getString(R.string.set_currency_code))) {
                        setCurrencyCode("0356");
                    }
                }
            }
        });

        id.setText("ID:" + MyApplication.getManufacturerID());
        version.setText(getVersionName(this));
    }

    private void setCurrencyCode(String currencyCode) {
        Controler.SetEmvParam(currencyCode);
    }

    private void setTitle() {
        String connectMode = MyApplication.getConnectedMode();
        String connected = "[" + getString(R.string.connected) + "]";
        String disconnected = "[" + getString(R.string.disconnect) + "]";
        title.setText(Controler.posConnected() ? connected : disconnected);
    }
}