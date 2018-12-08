package com.seikoshadow.apps.textalerter;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.seikoshadow.apps.textalerter.Adapters.AlertsExpandableListAdapter;
import com.seikoshadow.apps.textalerter.BroadcastReceivers.SmsBroadcastReceiver;
import com.seikoshadow.apps.textalerter.Database.AlertViewModel;
import com.seikoshadow.apps.textalerter.Dialogs.CreateAlertDialogFragment;
import com.seikoshadow.apps.textalerter.Dialogs.EditAlertDialogFragment;
import com.seikoshadow.apps.textalerter.Helpers.AppPermissionListener;
import com.seikoshadow.apps.textalerter.Helpers.BatteryManagerPermission;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Toolbar mainToolbar;
    private ComponentName receiverName;
    private ExpandableListView alertsList;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        // Notifies the system to expect notifications
        createNotificationChannel();

        initAlertsListView();

        requestPermissions();
    }


    private void initToolbar() {
        mainToolbar = findViewById(R.id.mainToolbar);
        mainToolbar.setSubtitle(R.string.alerts);
        setSupportActionBar(mainToolbar);

        receiverName = new ComponentName(this, SmsBroadcastReceiver.class);
    }


    /**
     * Requests permission to handle received sms
     */
    public void requestPermissions() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Create the permission listener for requesting SMS Permission
            AppPermissionListener permissionListener = new AppPermissionListener(this);

            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.RECEIVE_SMS)
                    .withListener(permissionListener)
                    .onSameThread()
                    .check();
        }

        BatteryManagerPermission.checkThatAppIsProtected(getApplicationContext());

    }

    /* Excluded as added manifest permission
    private void showOptimizationWarning() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean showDialog = sharedPreferences.getBoolean("showOptimizationWarningDialog", true);

        if(showDialog) {
            new AlertDialog.Builder(this)
                    .setTitle("Battery Optimisation Warning")
                    .setMessage(getString(R.string.optimization_message))
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        editor.putBoolean("showOptimizationWarningDialog", false);
                        editor.apply();
                    })
                    .show();
        }
    }
    */

    /**
     * Initiates and populates the alerts listview
     */
    private void initAlertsListView() {
        alertsList = findViewById(R.id.alertsList);
        AlertsExpandableListAdapter listAdapter = new AlertsExpandableListAdapter(this, new ArrayList<>());
        alertsList.setAdapter(listAdapter);

        // Create a viewmodel and then observe and wait for changes before applying to list
        AlertViewModel viewModel = ViewModelProviders.of(this).get(AlertViewModel.class);
        viewModel.getAlertsList().observe(this, listAdapter::addItems);

        alertsList.setOnItemLongClickListener((adapterView, view, i, id) -> {
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
            editAlert(groupPosition);
            return true;
        });

        // Allows only a single item to be expanded at a time
        alertsList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            // Keep track of previous expanded parent
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                // Collapse previous parent if expanded.
                if ((previousGroup != -1) && (groupPosition != previousGroup)) {
                    alertsList.collapseGroup(previousGroup);
                }
                previousGroup = groupPosition;
            }
        });


        // Sets a message if no items in list
        alertsList.setEmptyView(findViewById(R.id.emptyElement));
    }

    /**
     * Inflates the toolbar menu
     * @param menu the menu containing the view to inflate
     * @return true if the event is consumed, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        setStartStopReceiverMenuVisibility();
        return true;
    }


    /**
     * Handles clicks of any of the menu items
     * @param item the Menuitem that was clicked
     * @return true if method has consumed the event, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
                return false;
            case R.id.start_receiver:
                // Start the receiver and update menu
                getApplicationContext().getPackageManager().setComponentEnabledSetting(receiverName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
                setStartStopReceiverMenuVisibility();
                Toast.makeText(getApplicationContext(), getString(R.string.startServiceDescription), Toast.LENGTH_LONG).show();
                return true;
            case R.id.stop_receiver:
                // Stop the receiver and update menu
                getApplicationContext().getPackageManager().setComponentEnabledSetting(receiverName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
                setStartStopReceiverMenuVisibility();
                Toast.makeText(getApplicationContext(), getString(R.string.stopServiceDescription), Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + getString(R.string.requestFeature));
                emailIntent.setData(Uri.parse("mailto:" + getString(R.string.developerEmail)));
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, getString(R.string.sendEmailError), Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_about:
                Intent about = new Intent(MainActivity.this, About.class);
                startActivity(about);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether the broadcast receiver is active and modifies options menu
     */
    private void setStartStopReceiverMenuVisibility() {
        MenuItem startServiceItem = mainToolbar.getMenu().findItem(R.id.start_receiver);
        MenuItem stopServiceItem = mainToolbar.getMenu().findItem(R.id.stop_receiver);

        if(startServiceItem != null && stopServiceItem != null) {
            // Check if the broadcast receiver is enabled currently
            int status = this.getPackageManager().getComponentEnabledSetting(receiverName);

            if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || status == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                // Enabled
                startServiceItem.setVisible(false);
                stopServiceItem.setVisible(true);
            } else if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                // Disabled
                startServiceItem.setVisible(true);
                stopServiceItem.setVisible(false);
            }
        }
    }


    /**
     * Sets up the notification channel so that the app can display notifications
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = constants.NOTIFICATION_CHANNEL_NAME;
            String description = getString(R.string.serviceRunningDesc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(constants.NOTIFICATION_CHANNEL_ID, name, importance);

            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.e(TAG, "failed to get the notification manager");
            }
        }
    }

    public void createAlert(View view) {
        CreateAlertDialogFragment dialog = new CreateAlertDialogFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        dialog.show(fragmentTransaction, CreateAlertDialogFragment.TAG);
    }

    public void editAlert(long alertId) {
        Bundle bundle = new Bundle();
        bundle.putLong("Alert Id", alertId);

        EditAlertDialogFragment dialog = new EditAlertDialogFragment();
        dialog.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        dialog.show(fragmentTransaction, EditAlertDialogFragment.TAG);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        alertsList.setIndicatorBoundsRelative(width - convertDpToPixel(45), width - convertDpToPixel(10));
    }

    /**
     * Helper function to convert dp to pixels
     * @param dp The dp value to convert
     * @return The pixel value
     */
    public int convertDpToPixel(float dp){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        return (int) (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
