package com.seikoshadow.apps.textthrough;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import static com.seikoshadow.apps.textthrough.constants.SMS_PERMISSION_CODE;

/**
 * Created by Shaun on 22/05/2018.
 */

public class SmsFunctions {

    /**
     * Checks whether the READ SMS permission has been granted
     *
     * @return True if permission given, false otherwise
     */
    public static boolean isReadSmsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
            == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Handles requesting READ SMS permissions for the app
     */
    public static void requestReadSmsPermission(Activity activity) {
        // If the user has previously denied Read_SMS permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
            Manifest.permission.READ_SMS)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }

        // Request the permissions
        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_SMS },
            SMS_PERMISSION_CODE);
    }

    /**
     * Checks whether the Receive SMS permission has been granted
     *
     * @return True if permission given, false otherwise
     */
    public static boolean isReceiveSmsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
            == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Handles requesting READ SMS permissions for the app
     */
    public static void requestReceiveSmsPermission(Activity activity) {
        // If the user has previously denied Read_SMS permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
            Manifest.permission.RECEIVE_SMS)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }

        // Request the permissions
        ActivityCompat.requestPermissions(activity,
            new String[] { Manifest.permission.RECEIVE_SMS }, SMS_PERMISSION_CODE);
    }
}
