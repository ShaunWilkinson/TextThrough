package com.seikoshadow.apps.textalerter.Helpers;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.seikoshadow.apps.textalerter.Database.Alert;

import java.io.IOException;

import static android.content.Context.AUDIO_SERVICE;

public class AlarmControl {
    private static AlarmControl sInstance;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private AlarmControl(Context context) {
        mContext = context;
    }

    public static AlarmControl getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AlarmControl(context);
        }
        return sInstance;
    }

    /**
     * Plays the sound specified in the related alert, ensures the mediaplayer is null to stop multiple alerts at once
     * @param relatedAlert The related alert to grab the tone to play from
     */
    public void playMusic(Alert relatedAlert) {
        //TODO request audio focus

        if(mediaPlayer == null) {
            vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

            AudioManager audioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);

            // Set the volume to max
            int newVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            // Create a media player and setup variables
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setVolume(100, 100);
            mediaPlayer.setLooping(true);
            try {
                mediaPlayer.setDataSource(mContext, Uri.parse(relatedAlert.getRingtoneUri()));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If supported set the audio type to alarm
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            }

            mediaPlayer.start();

            // Start vibrating the device if enabled
            if (relatedAlert.isAlertVibrate() && vibrator != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(relatedAlert.getSecondsToRingFor() * 1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(relatedAlert.getSecondsToRingFor() * 1000);
                }
            }
        }
    }

    /**
     * Stops any playing sounds and also sets the mediaPlayer to null to act as a simple switch
     */
    public void stopMusic() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        if(vibrator != null) {
            vibrator.cancel();
        }
    }
}
