package com.savapractical.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.savapractical.R;
import com.savapractical.adapter.AdapterLogs;
import com.savapractical.ble.BleCharacteristic;
import com.savapractical.constats.AppConstants;
import com.savapractical.constats.AppMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_download;
    private RecyclerView rcv_logs;
    private AdapterLogs adapterLogs = null;
    private ArrayList<String> logsArrayList = new ArrayList<>();
    private int noOfNotification = 0;
    private int receivedNotification = 0;
    private int preReceivedNotification = 0;
    private Timer downloadTimer = null;
    private Timer packetTimer = null;
    private Thread downloadTimerThred = null;
    private Thread packetTimerThred = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalApplication.bleDeviceActor.setContext(MainActivity.this);
        registerReceiver(mGattUpdateReceiver, AppConstants.makeIntentFilter());
        initUi();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppMethods.hideProgressDialog();
            final String action = intent.getAction();
            switch (action) {
                case AppConstants.ACTION_DEVICE_DISCONNECTED:
                    refreshAdapter(getString(R.string.alert_device_disconnected));
                    AppMethods.setAlertDialog(MainActivity.this, getString(R.string.alert_device_disconnected));
                    stopDownloadTimer();
                    break;

                case AppConstants.ACTION_CHARACTERISTIC_CHANGED:
                    byte[] data = intent.getByteArrayExtra("data");
                    parseData(data);
                    break;
            }
        }
    };

    private void parseData(byte[] data) {
        String item = "Data from device: " + AppMethods.bytesToHex(data);

        if (data.length == 16) {
            for (int i = 0; i < data.length; i = i + 4) {
                byte[] measurementSample = new byte[4];
                measurementSample[0] = data[i];
                measurementSample[1] = data[i + 1];
                measurementSample[2] = data[i + 2];
                measurementSample[3] = data[i + 3];
                byte[] timeStampByte = new byte[2];
                byte[] speedStatusByte = new byte[2];

                System.arraycopy(measurementSample, 0, timeStampByte, 0, timeStampByte.length);
                System.arraycopy(measurementSample, 2, speedStatusByte, 0, speedStatusByte.length);
                String speedStatusString = AppMethods.bytesToHex(speedStatusByte);
                int time = AppMethods.convertByteArrayToInt(timeStampByte);
                long speed = AppMethods.convertHexToInt(speedStatusString.substring(0, 3));
                long status = AppMethods.convertHexToInt(speedStatusString.substring(3, 4));
                item = item + "\nTime: " + time + ", Speed: " + speed + ", Status: " + status;
            }
            receivedNotification = receivedNotification + 1;
            item = item+"\nNotification No: "+receivedNotification;
            if (receivedNotification == noOfNotification){
                item = item + "\nAll data received";
            }
        } else if (data.length == 4) {
            byte[] responseByte = new byte[2];
            byte[] notificationByte = new byte[2];
            System.arraycopy(data, 0, responseByte, 0, responseByte.length);
            System.arraycopy(data, 2, notificationByte, 0, notificationByte.length);
            if (Arrays.equals(responseByte, AppConstants.RESPONSE_OK)) {
                startDownloadTimer();
                startPacketTimer();
                noOfNotification = AppMethods.convertByteArrayToInt(notificationByte);
                item = item + "\nNumber of Notifications: " + noOfNotification;
            } else {
                item = item + "\nresponse not ok";
            }
        } else if (data.length == 2) {
            if (Arrays.equals(data, AppConstants.NOTIFY_END_COMMAND)) {
                item = item + "\nNotify end command received";
            } else {
                item = item + "\ninvalid data";
            }
            stopDownloadTimer();
        } else {
            item = item + "\ninvalid data";
        }
        refreshAdapter(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_download:
                tv_download.setEnabled(false);
                tv_download.setAlpha(0.3f);
                receivedNotification = 0;
                boolean iswrite = BleCharacteristic.WriteCharacteristic(MainActivity.this, AppConstants.WRITE_COMMAND);
                String item = "Write " + AppMethods.bytesToHex(AppConstants.WRITE_COMMAND) + " to command_uuid ";
                if (iswrite) {
                    item = item + "success";
                } else {
                    item = item + "fail";
                }
                refreshAdapter(item);
                break;
        }
    }

    private void initUi() {
        tv_download = findViewById(R.id.tv_download);
        rcv_logs = findViewById(R.id.rcv_logs);

        tv_download.setOnClickListener(this::onClick);
        RecyclerView.LayoutManager LayoutManager = new LinearLayoutManager(this);
        rcv_logs.setLayoutManager(LayoutManager);
        adapterLogs = new AdapterLogs(this, logsArrayList);
        rcv_logs.setAdapter(adapterLogs);
    }

    private void refreshAdapter(String string) {
        string = string+"\n"+AppMethods.getCurrentTime();
        logsArrayList.add(string);
        adapterLogs.notifyDataSetChanged();
        rcv_logs.smoothScrollToPosition(logsArrayList.size()-1);
    }

    private void startDownloadTimer() {
        downloadTimerThred = new Thread(new Runnable() {
            @Override
            public void run() {
                stopDownloadTimer();
                downloadTimer = new Timer();
                TimerTask t = new TimerTask() {
                    @Override
                    public void run() {
                        AppMethods.setAlertDialog(MainActivity.this, getString(R.string.timeout));
                        stopDownloadTimer();
                    }
                };
                downloadTimer.scheduleAtFixedRate(t, 3 * 60 * 1000, 3 * 60 * 1000);
            }
        });
        downloadTimerThred.start();

    }

    private void stopDownloadTimer() {
        try {
            if (downloadTimer != null) {
                downloadTimer.cancel();
                downloadTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopDownloadTimeThred();
        stopPacketTimer();
    }

    private void stopDownloadTimeThred() {
        try {
            if (downloadTimerThred != null) {
                downloadTimerThred.interrupt();
                downloadTimerThred = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPacketTimer() {
        packetTimerThred = new Thread(new Runnable() {
            @Override
            public void run() {
                stopPacketTimer();
                packetTimer = new Timer();
                TimerTask t = new TimerTask() {
                    @Override
                    public void run() {
                        if (receivedNotification>preReceivedNotification){
                            preReceivedNotification = receivedNotification;
                        }else {
                            AppMethods.setAlertDialog(MainActivity.this, getString(R.string.timeout_second));
                            stopDownloadTimer();
                            stopPacketTimer();
                        }
                    }
                };
                packetTimer.scheduleAtFixedRate(t, 1000, 1000);
            }
        });
        packetTimerThred.start();

    }

    private void stopPacketTimer() {
        try {
            if (packetTimer != null) {
                packetTimer.cancel();
                packetTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopPacketThred();
    }

    private void stopPacketThred() {
        try {
            if (packetTimerThred != null) {
                packetTimerThred.interrupt();
                packetTimerThred = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegiterReceiver() {
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        stopDownloadTimer();
        unRegiterReceiver();
        super.onDestroy();
    }
}