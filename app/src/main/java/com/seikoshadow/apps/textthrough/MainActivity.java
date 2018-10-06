package com.seikoshadow.apps.textthrough;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.Dialogs.CreateAlertDialogFragment;
import com.seikoshadow.apps.textthrough.Services.SMSWatchService;
import com.seikoshadow.apps.textthrough.Services.SmsFunctionsServiceManager;

//TODO finish layout_create_alert

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private Intent mServiceIntent;
    private AppDatabase db;
    private RecyclerView alertsList;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request READ SMS permission if not already granted
        if (!SmsFunctions.isReadSmsPermissionGranted(this))
            showRequestReadSmsPermissionDialog(this);

        // Request RECEIVE SMS permission if not already granted
        if (!SmsFunctions.isReceiveSmsPermissionGranted(this))
            showRequestReceiveSmsPermissionDialog(this);

        // Notifies the system to expect notifications
        createNotificationChannel();

        db = AppDatabase.getInstance(getApplicationContext());

        // TODO update the listview on change
        alertsList = findViewById(R.id.alertsList);
    }

    // Called by Start Service Button
    public void startService(View view) {
        startSmsService();
    }

    // Called by Start Service Button
    public void stopService(View view) {
        stopSmsService();
    }


    //TODO make it started and stopped via button

    /**
     * Starts the SMS Service so long as there is saved numbers to compare
     */
    protected void startSmsService() {
        boolean serviceIsRunning = SmsFunctionsServiceManager.isMyServiceRunning;

        if(db.alertDao().isTherePhoneNumber() != null && !serviceIsRunning) {
            // Create an SMSWatchService then if not already started then start it
            SMSWatchService smsWatchService = new SMSWatchService();
            mServiceIntent = new Intent(MainActivity.this, smsWatchService.getClass());
            startService(mServiceIntent);

        } else {
            Toast.makeText(this, getString(R.string.noNumbersFound), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * If it's running this stops the SMS Service watcher
     */
    protected void stopSmsService() {
        // TODO finish ability to stop service
        if(SmsFunctionsServiceManager.isMyServiceRunning) {
            stopService(mServiceIntent);
        }
    }

    /**
     * When the app is properly closed stop the service so that it calls its own create
     */
    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        super.onDestroy();
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
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Display permission request
                SmsFunctions.requestReadSmsPermission(activity);
            }
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
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Display permission request
                SmsFunctions.requestReceiveSmsPermission(activity);
            }
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
        //TODO create an alert
        CreateAlertDialogFragment dialog = new CreateAlertDialogFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        dialog.show(fragmentTransaction, CreateAlertDialogFragment.TAG);
    }
}
