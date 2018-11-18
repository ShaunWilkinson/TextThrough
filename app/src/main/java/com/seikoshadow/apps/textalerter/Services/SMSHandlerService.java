package com.seikoshadow.apps.textalerter.Services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.seikoshadow.apps.textalerter.BroadcastReceivers.SmsBroadcastReceiver;
import com.seikoshadow.apps.textalerter.BroadcastReceivers.StopRingtoneReceiver;
import com.seikoshadow.apps.textalerter.Database.Alert;
import com.seikoshadow.apps.textalerter.Database.AppDatabase;
import com.seikoshadow.apps.textalerter.Helpers.AlarmControl;
import com.seikoshadow.apps.textalerter.R;
import com.seikoshadow.apps.textalerter.constants;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.seikoshadow.apps.textalerter.constants.NOTIFICATION_CHANNEL_ID;
import static com.seikoshadow.apps.textalerter.constants.SMS_SERVICE_RUNNING_ID;

public class SMSHandlerService extends IntentService {
    private static final String TAG = SMSHandlerService.class.getName();
    private Alert relatedAlert;
    private Context mContext;
    private int msToDelay = 3000;
    ComponentName receiverName;

    public SMSHandlerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        // Set the receiver name and stop it until the service is done
        receiverName = new ComponentName(this, SmsBroadcastReceiver.class);
    }

    /**
     * OnDestroy restart the broadcast receiver
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "SMSHandlerService handling intent");

        if(intent != null) {
            final String action = intent.getAction();

            if(action!= null && action.equals(constants.SMS_RECEIVED)) {
                handleSMSReceived(intent);
            } else {
                Log.d(TAG, "Not implemented handler for " + action);
            }
        }
    }

    /**
     * Retrieves the received SMS data and then compares to list of alerts
     * @param intent the passed intent
     */
    private void handleSMSReceived(Intent intent) {
        Bundle smsBundle = intent.getExtras();
        if (smsBundle != null) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            if(messages != null) {
                String smsSender = messages[0].getOriginatingAddress();

                relatedAlert = retrieveRelatedAlert(smsSender);

                if(relatedAlert == null || smsSender == null) {
                    return;
                }

                if(relatedAlert.isAlertActive()) {
                    handleMatchingSMS();
                }
            }
        }
    }

    /**
     * Retrieves a related alert based on the SMSSender
     * @param smsSender The originator of the received SMS
     * @return Returns alert if found, null otherwise
     */
    private Alert retrieveRelatedAlert(String smsSender) {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        return db.alertModel().findByPhoneNumber(smsSender);
    }

    /**
     * Called when the received SMS sender matches one of the alerts
     */
    private void handleMatchingSMS() {
        startAudioAlert();
        startNotification();
    }


    /**
     * Starts an audio alert using the ringtone defined in the related alert
     */
    private void startAudioAlert() {
        // Bit of a hacky workaround so the tone will play after any default handlers and notification sounds
        Thread alertToneThread = new Thread(() -> {
            try {
                Thread.sleep(msToDelay);
                AlarmControl alarm = AlarmControl.getInstance(mContext);
                alarm.playMusic(relatedAlert);

                Thread.sleep(relatedAlert.getSecondsToRingFor() * 1000);
                alarm.stopMusic();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        alertToneThread.start();
    }

    /**
     * Starts a notification when ringing
     */
    private void startNotification() {
        // Create an intent to open the app
        Intent openAppIntent = new Intent(mContext, StopRingtoneReceiver.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        openAppIntent.putExtra("Action", "Open App");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create an intent to stop the alarm
        Intent stopAlarmIntent = new Intent(mContext, StopRingtoneReceiver.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent stopAlarmPendingIntent = PendingIntent.getBroadcast(mContext, 0, stopAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Text Alerter") //Finish
                .setContentText("Tap to stop alert sound")
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.notification_icon, getString(R.string.stopRinging), stopAlarmPendingIntent)
                .setTimeoutAfter((relatedAlert.getSecondsToRingFor() * 1000) + msToDelay)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(SMS_SERVICE_RUNNING_ID, mBuilder.build());
    }
}
