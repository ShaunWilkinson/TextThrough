package com.seikoshadow.apps.textthrough;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.seikoshadow.apps.textthrough.Services.SMSWatchService;
import com.seikoshadow.apps.textthrough.constants;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //private SmsBroadcastReceiver smsBroadcastReceiver;
    private static final String TAG = "MainActivity";
    private Uri notification;
    private Ringtone ringtone;
    SMSWatchService smsWatchService;
    Intent mServiceIntent;
    SharedPrefFunctions sharedPrefFunctions;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the ringtone sound
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        sharedPrefFunctions = new SharedPrefFunctions();

        // Request READ SMS permission if not already granted
        if (!SmsFunctions.isReadSmsPermissionGranted(this))
            showRequestReadSmsPermissionDialog(this);

        // Request RECEIVE SMS permission if not already granted
        if (!SmsFunctions.isReceiveSmsPermissionGranted(this))
            showRequestReceiveSmsPermissionDialog(this);

        createNotificationChannel();
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
        // Load the numbers saved in SharedPrefs
        List<String> savedNumbers = sharedPrefFunctions.loadStringList(constants.PHONENUMBERKEY, this);

        // Make sure there is some saved numbers before starting the service
        if(savedNumbers != null) {
            // Create an SMSWatchService then if not already started then start it
            smsWatchService = new SMSWatchService();
            mServiceIntent = new Intent(MainActivity.this, smsWatchService.getClass());
            if(!isMyServiceRunning(smsWatchService.getClass())) {
                startService(mServiceIntent);
            }
        } else {
            Toast.makeText(this, "No numbers have been saved", Toast.LENGTH_LONG).show();
        }
    }

    protected void stopSmsService() {
        // TODO finish ability to stop service
        if(isMyServiceRunning(smsWatchService.getClass())) {
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
     * Submits the details entered on the homepage
     */
    public void submitNumber(View view) {
        TextView enteredNumberTxt = findViewById(R.id.enteredNumber);
        String enteredNumberVal = enteredNumberTxt.getText().toString();

        if(!enteredNumberVal.equals("")) {
            // If the SharedPrefs contains the saved data already then update, otherwise create
            List<String> phoneNumbers;
            if (sharedPrefFunctions.loadStringList(constants.PHONENUMBERKEY, this) != null) {
                phoneNumbers = sharedPrefFunctions.loadStringList(constants.PHONENUMBERKEY, this);
            } else {
                phoneNumbers = new ArrayList<>();
            }

            phoneNumbers.add(enteredNumberVal);
            sharedPrefFunctions.saveStringList(constants.PHONENUMBERKEY, phoneNumbers, this);

            Toast.makeText(this, "Added " + enteredNumberVal + " to list of numbers", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No number input", Toast.LENGTH_LONG).show();
        }
    }

    // TODO easy way to remove phone numbers

    public void createAlert(View view) {
        //TODO create an alert
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i (TAG, "is service running: "+true+"");
                return true;
            }
        }
        Log.i (TAG, "is service running: "+false+"");
        return false;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = constants.NOTIFICATION_CHANNEL_NAME;
            String description = getString(R.string.serviceRunningDesc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(constants.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
