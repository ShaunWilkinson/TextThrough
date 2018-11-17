package com.seikoshadow.apps.textthrough;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.seikoshadow.apps.textthrough.Adapters.AlertsExpandableListAdapter;
import com.seikoshadow.apps.textthrough.BroadcastReceivers.SmsBroadcastReceiver;
import com.seikoshadow.apps.textthrough.Database.AlertViewModel;
import com.seikoshadow.apps.textthrough.Dialogs.CreateAlertDialogFragment;
import com.seikoshadow.apps.textthrough.Dialogs.EditAlertDialogFragment;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;


//TODO Tidy up all stlyes so they're defined properly

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Toolbar mainToolbar;
    private ComponentName receiverName;

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
    private void requestPermissions() {
        // TODO add description of why this is needed
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.RECEIVE_SMS)
            .withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse report) {

                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    // check for permanent denial of permission
                    if (response.isPermanentlyDenied()) {
                        // navigate user to app settings
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            })
            .onSameThread()
            .check();

        BatteryManagerPermission.checkThatAppIsProtected(getApplicationContext());
    }

    private void initAlertsListView() {
        ExpandableListView alertsList = findViewById(R.id.alertsList);
        AlertsExpandableListAdapter listAdapter = new AlertsExpandableListAdapter(this, new ArrayList<>());
        alertsList.setAdapter(listAdapter);

        // Create a viewmodel and then observe and wait for changes before applying to list
        AlertViewModel viewModel = ViewModelProviders.of(this).get(AlertViewModel.class);
        viewModel.getAlertsList().observe(this, listAdapter::addItems);

        alertsList.setOnItemLongClickListener((adapterView, view, i, id) -> {
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
            int childPosition = ExpandableListView.getPackedPositionChild(id);

           Log.d(TAG,"Group: " + (groupPosition) + ", Child: " + childPosition);
            editAlert(groupPosition);

            return true;
        });
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
                return true;
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
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
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

    // TODO easy way to remove phone numbers

    public void createAlert(View view) {
        CreateAlertDialogFragment dialog = new CreateAlertDialogFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        dialog.show(fragmentTransaction, CreateAlertDialogFragment.TAG);
    }

    public void editAlert(long alertId) {
        //TODO create an alert
        Bundle bundle = new Bundle();
        bundle.putLong("Alert Id", alertId);

        EditAlertDialogFragment dialog = new EditAlertDialogFragment();
        dialog.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        dialog.show(fragmentTransaction, EditAlertDialogFragment.TAG);
    }
}
