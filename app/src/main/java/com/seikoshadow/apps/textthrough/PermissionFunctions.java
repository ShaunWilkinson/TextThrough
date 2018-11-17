package com.seikoshadow.apps.textthrough;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.Context.MODE_PRIVATE;
import static com.seikoshadow.apps.textthrough.constants.SMS_PERMISSION_CODE;

/**
 * Created by Shaun on 22/05/2018.
 */

public class PermissionFunctions {
    // List of 'protected app' type programs on mobiles that will stop the BroadcastReceiver if not handled
    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))};

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
     * Displays a dialog describing why the read permission is required
     */
    public static void showRequestReadSmsPermissionDialog(final Activity activity) {
        // Create the alert dialog and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.read_sms_request_title);
        builder.setMessage(R.string.read_sms_request_message);

        // Handle button click
        builder.setPositiveButton(R.string.action_ok, (dialog, which) -> {
            dialog.dismiss();

            // Display permission request
            PermissionFunctions.requestReadSmsPermission(activity);
        });

        builder.setCancelable(false);
        builder.show();
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

    public static void checkThatAppIsProtected(Context context) {
        SharedPreferences.Editor pref = context.getSharedPreferences("allow_notify", MODE_PRIVATE).edit();
        pref.apply();

        SharedPreferences sp = context.getSharedPreferences("allow_notify", MODE_PRIVATE);

        if(!sp.getBoolean("protected",false)) {
            for (final Intent intent : POWERMANAGER_INTENTS) {
                if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.protected_status_title)).setMessage(context.getString(R.string.protected_status_description))
                            .setPositiveButton("Ok", (dialogInterface, i) -> sp.edit().putBoolean("protected", true).apply())
                            .setCancelable(false)
                            .setNegativeButton("Cancel", (dialog, which) -> {})
                            .create()
                            .show();
                    break;
                }
            }
        }
    }

}
