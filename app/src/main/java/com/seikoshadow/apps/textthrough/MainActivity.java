package com.seikoshadow.apps.textthrough;

import android.Manifest;
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

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.seikoshadow.apps.textthrough.Adapters.AlertsExpandableListAdapter;
import com.seikoshadow.apps.textthrough.Database.AlertViewModel;
import com.seikoshadow.apps.textthrough.Dialogs.CreateAlertDialogFragment;
import com.seikoshadow.apps.textthrough.Dialogs.EditAlertDialogFragment;

import java.util.ArrayList;
import java.util.List;

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
        //permissionsCheck();

        // Notifies the system to expect notifications
        createNotificationChannel();

        initAlertsListView();

        //TODO implement Dexter permission requests
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.VIBRATE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();
    }


    public void initToolbar() {
        Toolbar mainToolbar = findViewById(R.id.mainToolbar);
        mainToolbar.setSubtitle(R.string.alerts);
        setSupportActionBar(mainToolbar);
    }

    private void permissionsCheck() {
        // Request READ SMS permission if not already granted
        if (!PermissionFunctions.isReadSmsPermissionGranted(this))
            PermissionFunctions.showRequestReadSmsPermissionDialog(this);

        // Request RECEIVE SMS permission if not already granted
        /*if (!PermissionFunctions.isReceiveSmsPermissionGranted(this))
            showRequestReceiveSmsPermissionDialog(this);*/

        PermissionFunctions.checkThatAppIsProtected(getApplicationContext());


        //TODO ask permission to vibrate
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

    /**
     * Inflates the toolbar menu
     * @param menu the menu containing the view to inflate
     * @return true if the event is consumed, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
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
