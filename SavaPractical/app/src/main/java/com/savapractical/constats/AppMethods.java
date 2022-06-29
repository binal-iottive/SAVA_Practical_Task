package com.savapractical.constats;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.savapractical.R;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

public class AppMethods {

    public static ProgressDialog pDialog;

    public static void showProgressDialog(Context context, String msg) {
        hideProgressDialog();
        try {
            if (context != null) {
                pDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
                pDialog.setMessage(msg);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideProgressDialog() {
        try {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAlertDialog(Context context, String msg) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context/*, R.style.AlertDialogStyle*/);
                alertDialogBuilder.setMessage(msg);
                alertDialogBuilder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.setCancelable(true);
                alertDialog.setCanceledOnTouchOutside(false);
            }
        });
    }

    public static long convertHexToInt(String hexString) {
        return Long.parseLong(hexString, 16);
    }

    public static int convertByteArrayToInt(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default
        return wrapped.getShort();
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(new java.util.Date());
    }
}
