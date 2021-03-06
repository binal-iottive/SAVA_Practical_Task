package com.savapractical.constats;

import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.savapractical.R;

public class CheckSelfPermission {
    public static AlertDialog permissiondiaog = null;

    public static boolean checkLocationPermission(Context context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.REQUEST_LOCATION_PERMISSION
            );
            return false;
        }
        return true;
    }

    public static boolean checkLocationPermissionRetional(Context context) {
        try {
            if (permissiondiaog != null) {
                if (permissiondiaog.isShowing()) {
                    permissiondiaog.dismiss();
                }
            }
        } catch (Exception e) {
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ShowPermissionAlert(context, context.getString(R.string.enable_location_permission));
                return false;
            }
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.REQUEST_LOCATION_PERMISSION
            );
            return false;
        }
        return true;
    }


    public static AlertDialog locationOnDialog = null;

    public static boolean isLocationOn(final Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled) {
            hideLocationDialog();
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setMessage(R.string.enable_location_setting)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            hideLocationDialog();
                            ((Activity) context).startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), AppConstants.REQUEST_ENABLE_LOCATION);
                        }
                    });
            locationOnDialog = builder.create();
            locationOnDialog.setCancelable(false);
            locationOnDialog.show();

            return false;
        }
        return true;
    }

    public static void hideLocationDialog() {
        try {
            if (locationOnDialog != null && locationOnDialog.isShowing()) {
                locationOnDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isBluetoothOn(Context context, boolean isShowAlert) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, AppConstants.REQUEST_ENABLE_BLUETOOTH);
            return false;
        } else {
            return true;
        }
    }

    public static void ShowPermissionAlert(final Context context, String msg) {
        try {
            if (permissiondiaog != null) {
                if (permissiondiaog.isShowing()) {
                    permissiondiaog.dismiss();
                }
            }
        } catch (Exception e) {

        }
        permissiondiaog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.app_name))
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        openPermissionsSettings(context.getPackageName(), context);
                    }
                }).show();

        permissiondiaog.setCancelable(false);
    }

    public static void openPermissionsSettings(@NonNull String packageName, Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                ActivityCompat.startActivityForResult((Activity) context, intent, AppConstants.MY_MARSHMELLO_PERMISSION, null);
            }
        } catch (Exception e) {
        }
    }
}
