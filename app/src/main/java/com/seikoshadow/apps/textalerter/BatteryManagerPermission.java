package com.seikoshadow.apps.textalerter;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import static android.content.Context.MODE_PRIVATE;


/**
 * Checks that the app is added to power managers, this ensures the broadcast receiver is not killed
 */
public class BatteryManagerPermission {
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
