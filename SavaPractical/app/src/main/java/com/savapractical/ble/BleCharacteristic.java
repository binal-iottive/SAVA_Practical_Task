package com.savapractical.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.savapractical.R;
import com.savapractical.activity.GlobalApplication;
import com.savapractical.constats.AppConstants;
import com.savapractical.constats.AppMethods;
import com.savapractical.constats.CheckSelfPermission;

import java.util.UUID;

public class BleCharacteristic {
    public static boolean WriteCharacteristic(Context context, byte[] byteValue) {
        BluetoothGatt mBluetoothGatt = GlobalApplication.bleDeviceActor.getmBluetoothGatt();
        if (!canReadWrite(context, mBluetoothGatt)) {
            return false;
        }
        BluetoothGattService service = mBluetoothGatt
                .getService(UUID.fromString(AppConstants.SERVICE_UUID));
        if (service != null) {
            BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(UUID.fromString(AppConstants.COMMAND_UUID));
            if (characteristic != null) {
                characteristic.setValue(byteValue);
                boolean isWrite = mBluetoothGatt.writeCharacteristic(characteristic);
                Log.d(AppConstants.TAG_BLE, "writeCharacteristic: " + isWrite);
                return isWrite;
            } else {
                AppMethods.hideProgressDialog();
                AppMethods.setAlertDialog(context, AppConstants.COMMAND_UUID + " " + context.getString(R.string.alert_characteristic_not_found));
            }
        } else {
            AppMethods.hideProgressDialog();
            AppMethods.setAlertDialog(context, AppConstants.SERVICE_UUID + " " + context.getString(R.string.alert_service_not_found));
        }
        return false;
    }

    public static void enableNotifychar(Context context) {
        BluetoothGatt mBluetoothGatt = GlobalApplication.bleDeviceActor.getmBluetoothGatt();
        if (!canReadWrite(context, mBluetoothGatt)) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt
                .getService(UUID.fromString(AppConstants.SERVICE_UUID));
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service
                    .getCharacteristic(UUID.fromString(AppConstants.RESPONSE_UUID));
            if (characteristic != null) {
                BluetoothGattDescriptor descriptor =
                        characteristic.getDescriptor(UUID.fromString(AppConstants.DESC_UUID));
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean isWrite = mBluetoothGatt.writeDescriptor(descriptor);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    Log.d("ble==> ", "enableNotify: " + isWrite);
                } else {
                    AppMethods.hideProgressDialog();
                    AppMethods.setAlertDialog(context, AppConstants.RESPONSE_UUID + " " + context.getString(R.string.alert_descriptor_not_found));
                }
            } else {
                AppMethods.hideProgressDialog();
                AppMethods.setAlertDialog(context, AppConstants.RESPONSE_UUID + " " + context.getString(R.string.alert_characteristic_not_found));
            }
        } else {
            AppMethods.hideProgressDialog();
            AppMethods.setAlertDialog(context, AppConstants.SERVICE_UUID + " " + context.getString(R.string.alert_service_not_found));
        }
    }

    public static boolean canReadWrite(Context context, BluetoothGatt mBluetoothGatt) {
        if (!CheckSelfPermission.isBluetoothOn(context, false)) {
            AppMethods.hideProgressDialog();
            AppMethods.setAlertDialog(context, context.getString(R.string.alert_enable_bluetooth));
            return false;
        } else if (!BleDeviceActor.isConnected) {
            AppMethods.hideProgressDialog();
            AppMethods.setAlertDialog(context, context.getString(R.string.alert_device_disconnected));
            return false;
        } else if (mBluetoothGatt == null) {
            return false;
        } else {
            return true;
        }
    }
}
