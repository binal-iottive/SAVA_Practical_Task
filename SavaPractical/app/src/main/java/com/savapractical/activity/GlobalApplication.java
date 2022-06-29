package com.savapractical.activity;

import android.app.Application;

import com.savapractical.ble.BleDeviceActor;

public class GlobalApplication extends Application {
    public static BleDeviceActor bleDeviceActor = null;
    public static String connectedDeviceMacAddress = "";

    @Override
    public void onCreate() {
        super.onCreate();
        bleDeviceActor = new BleDeviceActor(GlobalApplication.this);
    }
}
