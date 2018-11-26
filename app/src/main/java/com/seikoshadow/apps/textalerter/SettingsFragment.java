package com.seikoshadow.apps.textalerter;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.PermissionListener;
import com.seikoshadow.apps.textalerter.Helpers.AppPermissionListener;
import com.seikoshadow.apps.textalerter.Helpers.BatteryManagerPermission;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static String TAG = "SettingsFragment";
    private PermissionListener permissionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the permission listener for requesting SMS Permission
        permissionListener = new AppPermissionListener(getActivity());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Handles clicking the 'Check Permissions' setting button
        Preference checkPermissionsGrantedButton = findPreference("permissionsCheck");
        if(checkPermissionsGrantedButton != null) {
            // When the button is clicked check for permissions and request if necessary
            checkPermissionsGrantedButton.setOnPreferenceClickListener(p -> {
                requestPermissions();
                return false;
            });
        }
    }

    /**
     * Requests permission to handle received sms
     */
    private void requestPermissions() {
        Activity activity = getActivity();

        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.RECEIVE_SMS)
                .withListener(permissionListener)
                .onSameThread()
                .check();

        BatteryManagerPermission.checkThatAppIsProtected(activity.getApplicationContext());
    }
}
