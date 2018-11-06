package com.seikoshadow.apps.textthrough.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.seikoshadow.apps.textthrough.Database.Alert;
import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.constants;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SMSHandlerService extends IntentService {
    private static final String TAG = SMSHandlerService.class.getName();
    private Context context;
    private Alert relatedAlert;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int originalVolume;

    public SMSHandlerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
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
        // If the Audio Manager and media Player are setup correctly then start to play the alert
        if (setupAudio()) {
            // Bit of a hacky workaround so the tone will play after any default handlers and notification sounds
            Thread alertToneThread = new Thread(() -> {
                try {
                    Thread.sleep(4000);
                    mediaPlayer.start();
                    executeDelayedAlertStop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            alertToneThread.start();
        }
    }

    /**
     * Sets up the Audio Manager for use by the Media Player
     * @return True if audio player set up fully, false otherwise
     */
    private boolean setupAudio() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if(audioManager != null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            // Set the volume to max
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            int newVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            // Request that the system grants temporary focus to alert audio
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);

            // Create a media player which will play the alert tone
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setVolume(100, 100);
            mediaPlayer.setLooping(true);
            try {
                mediaPlayer.setDataSource(context, Uri.parse(relatedAlert.getRingtoneUri()));
                mediaPlayer.prepare();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Stops the media player if it's ringing
     */
    private void stopRinging() {
        if(mediaPlayer.isPlaying() && mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * In a seperate thread waits the specified amount of time then stops the alert if running
     */
    private void executeDelayedAlertStop() {
        // Stop the ringtone after the time specified in the alert
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                if(relatedAlert != null) {
                    Thread.sleep(relatedAlert.getSecondsToRingFor() * 1000);
                    stopRinging();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
