package com.savapractical.constats;

import android.content.IntentFilter;

public class AppConstants {
    public static final int REQUEST_LOCATION_PERMISSION = 1000;
    public static final int REQUEST_ENABLE_LOCATION = 1001;
    public static final int MY_MARSHMELLO_PERMISSION = 1002;
    public static final int REQUEST_ENABLE_BLUETOOTH = 1003;

    public static final String TAG_BLE = "ble==>";

    public static final String SERVICE_UUID = "293d0000-f6e4-11ec-b939-0242ac120002";
    public static final String COMMAND_UUID = "293d0001-f6e4-11ec-b939-0242ac120002";
    public static final String RESPONSE_UUID = "293d0002-f6e4-11ec-b939-0242ac120002";
    public static final String DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static final byte[] WRITE_COMMAND = new byte[]{0x7F, 0x7F};
    public static final byte[] RESPONSE_OK = new byte[]{0x12, 0x34};
    public static final byte[] NOTIFY_END_COMMAND = new byte[]{(byte) 0xF9, (byte) 0xF9};

    public static final String ACTION_DEVICE_DISCONNECTED = "com.sava.ACTION_DEVICE_DISCONNECTED";
    public static final String ACTION_DEVICE_CONNECTED = "com.sava.ACTION_DEVICE_CONNECTED";
    public static final String ACTION_CHARACTERISTIC_CHANGED = "com.sava.ACTION_CHARACTERISTIC_CHANGED";

    public static IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        filter.addAction(ACTION_DEVICE_CONNECTED);
        filter.addAction(ACTION_DEVICE_DISCONNECTED);
        filter.addAction(ACTION_CHARACTERISTIC_CHANGED);
        return filter;
    }
}
