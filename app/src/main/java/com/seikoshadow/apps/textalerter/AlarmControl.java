package com.seikoshadow.apps.textalerter;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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

    public AlarmControl(Context context) {
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
     * @param relatedAlert
     */
    public void playMusic(Alert relatedAlert) {
        if(mediaPlayer == null) {
            vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

            AudioManager audioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            // Set the volume to max
            int newVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            // Request that the system grants temporary focus to alert audio
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setVolume(100, 100);
            mediaPlayer.setLooping(true);
            try {
                mediaPlayer.setDataSource(mContext, Uri.parse(relatedAlert.getRingtoneUri()));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();

            if (relatedAlert.isAlertVibrate() && vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(relatedAlert.getSecondsToRingFor() * 1000, VibrationEffect.DEFAULT_AMPLITUDE));
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
