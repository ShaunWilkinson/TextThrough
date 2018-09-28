package com.seikoshadow.apps.textthrough;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.seikoshadow.apps.textthrough.constants.SMS_PERMISSION_CODE;

public class MainActivity extends Activity {
    //private SmsBroadcastReceiver smsBroadcastReceiver;
    private static final String TAG = "MainActivity";
    private NotificationUtils notificationUtils;
    private Uri notification;
    private Ringtone ringtone;
    SMSWatchService smsWatchService;
    Intent mServiceIntent;

    //TODO make sms watcher service run in background
    //TODO make a way to save a list of numbers to take action on

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the ringtone sound
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

        // Request READ SMS permission if not already granted
        if (!SmsFunctions.isReadSmsPermissionGranted(this)) {
            showRequestReadSmsPermissionDialog(this);
        }

        // Request RECEIVE SMS permission if not already granted
        if (!SmsFunctions.isReceiveSmsPermissionGranted(this)) {
            showRequestReceiveSmsPermissionDialog(this);
        }

        // Create an SMSWatchService then if not already started then start it
        smsWatchService = new SMSWatchService();
        mServiceIntent = new Intent(MainActivity.this, smsWatchService.getClass());
        if(!isMyServiceRunning(smsWatchService.getClass())) {
            startService(mServiceIntent);
        }

        /*
        // Create the receiver then register it
        final SmsBroadcastReceiver smsBroadcastReceiver = new SmsBroadcastReceiver("6505551212"); //TODO replace with way to manage list of senders

        smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
            @Override public void onTextReceived(String smsSender, String smsBody) {
                Log.d(TAG, "Received text - " + smsSender + ", " + smsBody);
                Toast.makeText(MainActivity.this, "Received Text!", Toast.LENGTH_LONG).show();
            }
        });

        registerReceiver(smsBroadcastReceiver,
            new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
*/

        /*
        if(isMyServiceRunning(SMSWatchService.class)) {
            Log.d(TAG, "SMSWatchService is already running");
        }

        // Triggers the SMSWatchService Service
        Intent intent = new Intent(this, SMSWatchService.class);
        // Example of adding data to intent
        //i.putExtra("KEY1", "Value to be used by service");
        this.startService(intent);
        */

    }

    // when the app is properly closed stop the service so that it calls its own create
    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i(TAG, "Destroyed Service");
        super.onDestroy();
    }

    /**
     * Run when the stop tone button is clicked, stops the playing tone
     * @param view the calling view
     */
    public void stopTone(View view) {
        ringtone.stop();

        Button stopToneBtn = findViewById(R.id.stopRingtoneBtn);
        stopToneBtn.setVisibility(View.GONE);
    }

    /**
     * Submits the details entered on the homepage
     */
    public void submitDetails(View view) { //TODO make a way to submit numbers

        TextView enteredNumberTxt = findViewById(R.id.enteredNumber);
        String enteredNumberVal = enteredNumberTxt.getText().toString();

        /*
        // Save the entered number into SharedPreferences
        SharedPrefFunctions sharedPrefFunctions = new SharedPrefFunctions();

        // Create a list, assign any saved numbers to it
        List<String> phoneNumbers = new ArrayList<>();
        try {
            String[] numbers = sharedPrefFunctions.loadStringArray("phoneNumbers", this);
            phoneNumbers = new ArrayList<>(Arrays.asList(numbers));
        } catch (Exception e) {
            Log.e(TAG, "Error accessing numbers");
            Log.e(TAG, e.getMessage());
        }

        // Add the inserted number to the existing list after converting
        phoneNumbers.add(enteredNumberVal);
*/

        //sharedPrefFunctions.saveStringArray(numbers, "phoneNumbers", this);
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
}
