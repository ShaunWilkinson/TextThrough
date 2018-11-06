package com.seikoshadow.apps.textthrough;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.seikoshadow.apps.textthrough.Adapters.AlertsExpandableListAdapter;
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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        // Request READ SMS permission if not already granted
        if (!PermissionFunctions.isReadSmsPermissionGranted(this))
            showRequestReadSmsPermissionDialog(this);

        // Request RECEIVE SMS permission if not already granted
        /*if (!PermissionFunctions.isReceiveSmsPermissionGranted(this))
            showRequestReceiveSmsPermissionDialog(this);*/

        PermissionFunctions.checkThatAppIsProtected(getApplicationContext());

        // Notifies the system to expect notifications
        createNotificationChannel();

        initAlertsListView();
    }

    public void initToolbar() {
        Toolbar mainToolbar = findViewById(R.id.mainToolbar);
        mainToolbar.setSubtitle(R.string.alerts);
        setSupportActionBar(mainToolbar);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initAlertsListView() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    // Called by Start Service Button
    public void startService(View view) {
        //startSmsService();
    }


    // Called by Start Service Button
    public void stopService(View view) {
        //forceKillService();
    }


    /**
     * Displays a dialog describing why the read permission is required
     */
    public void showRequestReadSmsPermissionDialog(final Activity activity) {
        // Create the alert dialog and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
     * Creates an AlertDialog explaining why the RECEIVE_SMS permission is required then asks for
     * permission
     */
    public void showRequestReceiveSmsPermissionDialog(final Activity activity) {
        // Create the alert dialog and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.receive_sms_request_title);
        builder.setMessage(R.string.receive_sms_request_message);

        // Handle button click
        builder.setPositiveButton(R.string.action_ok, (dialog, which) -> {
            dialog.dismiss();

            // Display permission request
            PermissionFunctions.requestReceiveSmsPermission(activity);
        });

        builder.setCancelable(false);
        builder.show();
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
            notificationManager.createNotificationChannel(channel);
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
