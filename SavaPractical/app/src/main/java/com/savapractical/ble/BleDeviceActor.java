package com.savapractical.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.savapractical.constats.AppConstants;
import com.savapractical.listener.OnScanCallbackListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BleDeviceActor implements Runnable {
    private static Context mContext;
    private BluetoothDevice mBluetoothDevice;
    private static BluetoothGatt mBluetoothGatt;
    private Thread thread;
    public static boolean isConnected = false;
    public static boolean isScanning = false;
    private BluetoothLeScanner btScanner;
    private BluetoothManager btManager;
    private OnScanCallbackListener onScanCallbackListener = null;
    private BluetoothAdapter btAdapter;

    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public BleDeviceActor(Context mContext) {
        this.mContext = mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void connectToDevice(BluetoothDevice bluetoothDevice) {
        Log.d(AppConstants.TAG_BLE, "connectToDevice");
        mBluetoothDevice = bluetoothDevice;

        BluetoothManager btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter btAdapter = btManager.getAdapter();
        if (!btAdapter.isEnabled()) {
            return;
        }

        try {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
            } else {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            }
        } else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothGatt != null) {
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectDevice() {
        try {
            if (mBluetoothGatt != null && isConnected) {
                mBluetoothGatt.disconnect();
                isConnected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(AppConstants.TAG_BLE, "onConnectionStateChange: " + newState + ", status:" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else {
                isConnected = false;
                isScanning = false;
                mBluetoothGatt = null;
                broadcastUpdate(AppConstants.ACTION_DEVICE_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(AppConstants.TAG_BLE, "onServicesDiscovered");
            if (status == gatt.GATT_SUCCESS) {
                if (gatt != null) {
                    Log.d(AppConstants.TAG_BLE, "onServicesDiscovered: success");
                    mBluetoothGatt = gatt;
                    isConnected = true;
                    BleCharacteristic.enableNotifychar(mContext);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(AppConstants.TAG_BLE, "onCharacteristicRead: " + Arrays.toString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(AppConstants.TAG_BLE, "onCharacteristicWrite: " + Arrays.toString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(AppConstants.TAG_BLE, "onCharacteristicChanged: " + Arrays.toString(characteristic.getValue()));
            if (characteristic != null) {
                byte[] data = characteristic.getValue();
                if (data != null) {
                    switch (characteristic.getUuid().toString().toLowerCase()) {
                        case AppConstants.RESPONSE_UUID:
                            broadcastUpdate(AppConstants.ACTION_CHARACTERISTIC_CHANGED, data);
                            break;
                    }
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(AppConstants.TAG_BLE, "onDescriptorWrite: success " + descriptor.getCharacteristic().getUuid().toString() + "   value: " + descriptor.getValue()[0]);
                broadcastUpdate(AppConstants.ACTION_DEVICE_CONNECTED);

            } else {
                Log.e(AppConstants.TAG_BLE, "onDescriptorWrite: false status: " + status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == gatt.GATT_SUCCESS) {
            }
        }
    };

    public void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    public void stopThread() {
        if (thread != null) {
            final Thread tempThread = thread;
            thread = null;
            tempThread.interrupt();
        }
    }

    public static void broadcastUpdate(final String action, byte[] data) {
        final Intent intent = new Intent(action);
        intent.putExtra("data", data);
        mContext.sendBroadcast(intent);
    }

    public static void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    public void startScan(OnScanCallbackListener onScanCallbackListener) {
        this.onScanCallbackListener = onScanCallbackListener;
        startThread();
    }

    @Override
    public void run() {
        stopScan();
        btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btScanner = btAdapter.getBluetoothLeScanner();
            ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
            scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            List<ScanFilter> filters = new ArrayList<ScanFilter>();
            if (btScanner != null) {
                btScanner.startScan(filters, scanSettingsBuilder.build(), ScanCallback);
            } else {
                return;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScan() {
        isScanning = false;
        try {
            if (btAdapter.isEnabled()) {
                if (btScanner != null)
                    btScanner.stopScan(ScanCallback);
            }
        } catch (Exception e) {

        }
    }

    private android.bluetooth.le.ScanCallback ScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            isScanning = true;
            super.onScanResult(callbackType, result);
            BluetoothDevice scannedDevice = result.getDevice();
            Log.d(AppConstants.TAG_BLE, "onScanResult: " + scannedDevice.getAddress() + ", name:" + result.getScanRecord().getDeviceName());
            if (onScanCallbackListener != null) {
                onScanCallbackListener.onScanResult(callbackType, result);
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(AppConstants.TAG_BLE, "Scan Fail");
            isScanning = false;
            if (onScanCallbackListener != null) {
                onScanCallbackListener.onScanFailed(errorCode);
            }
        }
    };
}
