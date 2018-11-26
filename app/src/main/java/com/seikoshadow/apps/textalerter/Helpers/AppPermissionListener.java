package com.seikoshadow.apps.textalerter.Helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.seikoshadow.apps.textalerter.R;
import com.seikoshadow.apps.textalerter.SettingsActivity;

import androidx.appcompat.app.AlertDialog;

public class AppPermissionListener implements PermissionListener {
    private Activity activity;

    public AppPermissionListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {

        if(response.getPermissionName().equals(Manifest.permission.RECEIVE_SMS)) {
            // Show a success message
            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.all_required_permissions_granted), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {

        // If the RECEIVE_SMS permission is denied then notify the user or send user to app settings if permanent denial
        if(response.getPermissionName().equals(Manifest.permission.RECEIVE_SMS)) {

            if (response.isPermanentlyDenied()) {
                // Notify the user of the issue
                Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.denied_sms_permission_permanently), Toast.LENGTH_LONG).show();

                // Navigate the user to the app settings on system
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            } else {

                Intent settings = new Intent(activity, SettingsActivity.class);
                activity.startActivity(settings);

                Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.denied_sms_permission), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.rational_sms_title))
                .setMessage(activity.getString(R.string.rational_sms_description))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    token.cancelPermissionRequest();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    token.continuePermissionRequest();
                })
                .setOnDismissListener((dialog) -> token.cancelPermissionRequest())
                .show();
    }
}
