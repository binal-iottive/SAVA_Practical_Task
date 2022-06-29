package com.savapractical.model;

import android.bluetooth.BluetoothDevice;

public class DeviceModel {
    public String deviceMacAddress;
    public BluetoothDevice bluetoothDevice;

    public DeviceModel( String deviceMacAddress, BluetoothDevice bluetoothDevice) {
        this.deviceMacAddress = deviceMacAddress;
        this.bluetoothDevice = bluetoothDevice;
    }
}
