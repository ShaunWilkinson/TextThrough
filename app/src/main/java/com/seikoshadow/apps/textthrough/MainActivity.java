package com.seikoshadow.apps.textthrough;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.seikoshadow.apps.textthrough.constants.SMS_PERMISSION_CODE;

public class MainActivity extends Activity {
  private SmsBroadcastReceiver smsBroadcastReceiver;
  private static final String TAG = "MainActivity";
  private Uri notification;
  private Ringtone ringtone;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Setup the ringtone sound
      notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

    // Request READ SMS permission if not already granted
    if(!isReadSmsPermissionGranted(this)) {
      showRequestReadSmsPermissionDialog();
    }

    // Request RECEIVE SMS permission if not already granted
    if(!isReceiveSmsPermissionGranted(this)) {
      showRequestReceiveSmsPermissionDialog();
    }

    // Create the receiver then register it
    smsBroadcastReceiver = new SmsBroadcastReceiver();
    registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

    smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
      @Override public void onTextReceived(String smsSender, String smsBody) {
        Log.d(TAG, "Received text - " + smsSender + ", " + smsBody);
        processTextAction(smsSender, smsBody);
      }
    });
  }

  public void processTextAction(String smsSender, String smsBody) {
      ringtone.play();

      Button stopToneBtn = findViewById(R.id.stopRingtoneBtn);
      stopToneBtn.setVisibility(View.VISIBLE);
  }

    public void stopTone(View view) {
        ringtone.stop();

        Button stopToneBtn = findViewById(R.id.stopRingtoneBtn);
        stopToneBtn.setVisibility(View.GONE);
    }

    /**
     * Submits the details entered on the homepage
     */
  public void submitDetails(View view) {
      TextView enteredNumberTxt = findViewById(R.id.enteredNumber);
      String enteredNumberVal = enteredNumberTxt.getText().toString();
      smsBroadcastReceiver.setSenderLimitation(enteredNumberVal);
  }



  /**
   * Checks whether the READ SMS permission has been granted
   * @return True if permission given, false otherwise
   */
    public boolean isReadSmsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

  /**
   * Handles requesting READ SMS permissions for the app
   */
    public void requestReadSmsPermission() {
        // If the user has previously denied Read_SMS permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
              // You may display a non-blocking explanation here, read more in the documentation:
              // https://developer.android.com/training/permissions/requesting.html
        }

        // Request the permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
    }

  /**
   * Displays a dialog describing why the read permission is required
   */
    public void showRequestReadSmsPermissionDialog() {
        // Create the alert dialog and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.read_sms_request_title);
        builder.setMessage(R.string.read_sms_request_message);

        // Handle button click
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();

              // Display permission request
              requestReadSmsPermission();
          }
        });

        builder.setCancelable(false);
        builder.show();
    }

  /**
   * Checks whether the Receive SMS permission has been granted
   * @return True if permission given, false otherwise
   */
    public boolean isReceiveSmsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

  /**
   * Handles requesting READ SMS permissions for the app
   */
    public void requestReceiveSmsPermission() {
        // If the user has previously denied Read_SMS permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
              // You may display a non-blocking explanation here, read more in the documentation:
              // https://developer.android.com/training/permissions/requesting.html
        }

        // Request the permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
    }

    /**
     * Creates an AlertDialog explaining why the RECEIVE_SMS permission is required then asks for permission
     */
    public void showRequestReceiveSmsPermissionDialog() {
        // Create the alert dialog and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.receive_sms_request_title);
        builder.setMessage(R.string.receive_sms_request_message);

        // Handle button click
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();

              // Display permission request
              requestReceiveSmsPermission();
          }
        });

        builder.setCancelable(false);
        builder.show();
    }

}
