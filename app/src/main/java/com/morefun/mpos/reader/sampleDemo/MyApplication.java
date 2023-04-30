package com.morefun.mpos.reader.sampleDemo;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class MyApplication extends Application {

    private static MyApplication mInstance;

    public static MyApplication getInstance() {
        return mInstance;
    }

    private static SharedPreferences mPerferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initApplication();
    }

    public void initApplication() {
        mPerferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static void setManufacturerID(int id) {
        SharedPreferences.Editor mEditor = mPerferences.edit();
        mEditor.putInt("manufacturerID", id);
        mEditor.commit();
    }

    public static int getManufacturerID() {
        /**
         * AQUI ESTOY SET MANUFACTURERID DEFAULT IN 8
         */
        return mPerferences.getInt("manufacturerID", 8);
    }

    public static void setConnectedMode(String connectedMode) {
        SharedPreferences.Editor mEditor = mPerferences.edit();
        mEditor.putString("connectedMode", connectedMode);
        mEditor.commit();
    }

    public static String getConnectedMode() {
        return mPerferences.getString("connectedMode", mInstance.getString(R.string.bluetooth));
    }

    public static void setBluetoothMac(String mac) {
        SharedPreferences.Editor mEditor = mPerferences.edit();
        mEditor.putString("BluetoothMac", mac);
        mEditor.commit();
    }

    public static String getBluetoothMac() {
        return mPerferences.getString("BluetoothMac", "");
    }

    public static void setBluetoothName(String name) {
        SharedPreferences.Editor mEditor = mPerferences.edit();
        mEditor.putString("BluetoothName", name);
        mEditor.commit();
    }

    public static String getBluetoothName() {
        return mPerferences.getString("BluetoothName", "");
    }

}
