package com.savapractical.listener;

import android.bluetooth.le.ScanResult;

public interface OnScanCallbackListener {
    public void onScanResult(int callbackType, ScanResult result);
    public void onScanFailed(int errorCode);
}
