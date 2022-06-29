package com.savapractical.activity;

import static com.savapractical.activity.GlobalApplication.bleDeviceActor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.savapractical.R;
import com.savapractical.adapter.AdapterDeviceList;
import com.savapractical.ble.BleDeviceActor;
import com.savapractical.constats.AppConstants;
import com.savapractical.constats.AppMethods;
import com.savapractical.constats.CheckSelfPermission;
import com.savapractical.listener.OnConnectClickListener;
import com.savapractical.listener.OnScanCallbackListener;
import com.savapractical.model.DeviceModel;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener, OnConnectClickListener, OnScanCallbackListener {
    private ImageView iv_refresh;
    private RecyclerView rcv_device_list;
    private AdapterDeviceList adapterDeviceList = null;
    private ArrayList<DeviceModel> deviceModelArrayList = new ArrayList<>();
    private boolean isFirstLocationPermission = true;
    private int connectPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        bleDeviceActor.setContext(ScanActivity.this);
        registerReceiver(mGattUpdateReceiver, AppConstants.makeIntentFilter());
        initUi();
        if (checkselfPermission()) {
            startScan();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkselfPermission()) {
            if (!BleDeviceActor.isScanning) {
                startScan();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_refresh:
                if (checkselfPermission()) {
                    startScan();
                }
                break;
        }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice scannedDevice = result.getDevice();
        if (scannedDevice != null) {
            if (result.getScanRecord() != null && result.getScanRecord().getDeviceName() != null) {
                if (!isContainDeviceList(scannedDevice.getAddress(), deviceModelArrayList)) {
                    deviceModelArrayList.add(new DeviceModel(scannedDevice.getAddress(), scannedDevice));
                    refreshAdapter();
                }
            }
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        AppMethods.setAlertDialog(ScanActivity.this, getString(R.string.alert_scan_fail));
    }

    @Override
    public void onConnectClick(int position) {
        bleDeviceActor.stopScan();
        connectPosition = position;
        DeviceModel deviceModel = deviceModelArrayList.get(position);
        if (BleDeviceActor.isConnected) {
            bleDeviceActor.disconnectDevice();
        }
        AppMethods.showProgressDialog(ScanActivity.this, getString(R.string.progress_connecting));
        bleDeviceActor.connectToDevice(deviceModel.bluetoothDevice);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppMethods.hideProgressDialog();
            final String action = intent.getAction();
            switch (action) {
                case AppConstants.ACTION_DEVICE_CONNECTED:
                    DeviceModel deviceModel = deviceModelArrayList.get(connectPosition);
                    GlobalApplication.connectedDeviceMacAddress = deviceModel.deviceMacAddress;
                    Intent mIntent = new Intent(ScanActivity.this, MainActivity.class);
                    startActivity(mIntent);
                    finish();
                    break;

                case AppConstants.ACTION_DEVICE_DISCONNECTED:
                    break;
            }
        }
    };

    private void startScan() {
        deviceModelArrayList = new ArrayList<>();
        refreshAdapter();
        if (bleDeviceActor != null) {
            bleDeviceActor.stopScan();
        }
        bleDeviceActor.startScan(this);
    }

    private void initUi() {
        iv_refresh = findViewById(R.id.iv_refresh);
        rcv_device_list = findViewById(R.id.rcv_device_list);

        iv_refresh.setOnClickListener(this::onClick);
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(this);
        rcv_device_list.setLayoutManager(LayoutManager);
    }

    private boolean checkselfPermission() {
        if (CheckSelfPermission.isBluetoothOn(ScanActivity.this, true)) {
            if (CheckSelfPermission.isLocationOn(ScanActivity.this)) {
                return checkLocationPermission();
            }
        }
        return false;
    }

    private boolean checkLocationPermission() {
        if (isFirstLocationPermission) {
            isFirstLocationPermission = false;
            return CheckSelfPermission.checkLocationPermission(ScanActivity.this);
        } else {
            return CheckSelfPermission.checkLocationPermissionRetional(ScanActivity.this);
        }
    }

    public static boolean isContainDeviceList(String macAddress, ArrayList<DeviceModel> deviceModelArrayList) {
        for (DeviceModel deviceModel : deviceModelArrayList) {
            if (deviceModel.deviceMacAddress.toLowerCase().equals(macAddress.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void refreshAdapter() {
        adapterDeviceList = new AdapterDeviceList(this, deviceModelArrayList, this::onConnectClick);
        rcv_device_list.setAdapter(adapterDeviceList);
    }

    private void unRegiterReceiver() {
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        unRegiterReceiver();
        bleDeviceActor.stopScan();
        super.onDestroy();
    }
}