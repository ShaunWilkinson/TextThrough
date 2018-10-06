package com.seikoshadow.apps.textthrough.Services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Telephony;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.seikoshadow.apps.textthrough.BroadcastReceivers.SmsBroadcastReceiver;
import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.Entities.Alert;
import com.seikoshadow.apps.textthrough.R;
import com.seikoshadow.apps.textthrough.constants;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Background service responsible for keeping the SMS Broadcast Receiver alive and passing any required data to BroadCast Receiver
 */
public class SMSWatchService extends Service {
    public SmsBroadcastReceiver smsBroadcastReceiver;
    private NotificationManagerCompat notificationManager;
    private final static String TAG = "SMSWatchService";
    private AppDatabase db;

    public SMSWatchService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something useful
        super.onStartCommand(intent, flags, startId);

        // Change the flag so service running can be checked
        SmsFunctionsServiceManager.isMyServiceRunning = true;

        // Create the Text Message Broadcast Receiver and register it to listen to text messages arriving
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

        // What to do when a text is received
        smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
            @Override public void onTextReceived(String smsSender, String smsBody) {
                // Called when the origin matches one of the defined senders
                Log.d(TAG, "got here");
                processTextAction(smsSender, smsBody);
            }
        });

        // Sets the sender limitation in the background
        setSMSNumberLimitation();

        // Start the notification
        //TODO create notification tap action - https://developer.android.com/training/notify-user/build-notification#java

        NotificationCompat.Builder serviceNotificationBuilder = new NotificationCompat.Builder(this, constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.serviceRunningTitle))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(constants.SMS_SERVICE_RUNNING_ID, serviceNotificationBuilder.build());

        return START_STICKY;
    }

    /**
     * Sets the sender limitation for the Broadcast Receiver on a background thread
     */
    public void setSMSNumberLimitation() {
        // Run a query on the database in the background then set the sender limitation
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                db = AppDatabase.getInstance(getApplicationContext());
                List<String> numbers = db.alertDao().getAllPhoneNumbers();

                if(!numbers.isEmpty()) {
                    smsBroadcastReceiver.setSenderLimitation(numbers);
                } else {
                    Log.e(TAG, "Failed to find numbers");
                }
            }
        });
    }

    /**
     * onDestroy of the service it creates a new broadcast receiver, cancels the notification if active
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.seikoshadow.apps.textthrough.restartBroadcastReceiver");

        if(notificationManager != null)
            notificationManager.cancel(constants.SMS_SERVICE_RUNNING_ID);

        sendBroadcast(broadcastIntent);

        // Change the flag so service running can be checked
        SmsFunctionsServiceManager.isMyServiceRunning = true;

        unregisterReceiver(smsBroadcastReceiver); // Stops the receiver leaking on destruction (not entirely sure this is as intended)
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO for communication return IBinder implementation
        return null;
    }

    /**
     * When a text is received this function is run
     * @param smsSender The sender of the text
     * @param smsBody The body of the received text
     */
    public void processTextAction(String smsSender, String smsBody) {
        Log.d(TAG, "Processing text from " + smsSender);

        // Retrieve the alert related to the smsSender
        Alert relatedAlert = db.alertDao().findByPhoneNumber(smsSender);
        String ringtoneLocation = relatedAlert.getRingtoneUri();
        Uri ringtoneUri = Uri.parse(ringtoneLocation); //TODO fix this

        MediaPlayer mp = MediaPlayer.create(this, ringtoneUri);
        mp.start();
        // TODO test this and test way to stop it via notification
    }
}
