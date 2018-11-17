package com.seikoshadow.apps.textthrough.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.seikoshadow.apps.textthrough.AlarmControl;
import com.seikoshadow.apps.textthrough.Database.Alert;
import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.constants;

public class SMSHandlerService extends IntentService {
    private static final String TAG = SMSHandlerService.class.getName();
    private Alert relatedAlert;

    public SMSHandlerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

                handleMatchingSMS();
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
        Log.d(TAG, "Found Matching Alert");
        startAudioAlert();
    }


    /**
     * Starts an audio alert using the ringtone defined in the related alert
     */
    private void startAudioAlert() {
        // Bit of a hacky workaround so the tone will play after any default handlers and notification sounds
        Thread alertToneThread = new Thread(() -> {
            try {
                Thread.sleep(4000);
                AlarmControl alarm = AlarmControl.getInstance(this);
                alarm.playMusic(relatedAlert);

                Thread.sleep(relatedAlert.getSecondsToRingFor() * 1000);
                alarm.stopMusic();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        alertToneThread.start();

        /*
        // Create an intent to open the app
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0);

        // Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Test")
                .setContentText("Tap to stop alert sound")
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(relatedAlert.getSecondsToRingFor() * 1000)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(SMS_SERVICE_RUNNING_ID, mBuilder.build());
    */
    }
}
