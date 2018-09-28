package com.seikoshadow.apps.textthrough;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import com.seikoshadow.apps.textthrough.BroadcastReceivers.SmsBroadcastReceiver;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Shaun on 24/05/2018.
 */

public class SMSWatchService extends Service {
    public SmsBroadcastReceiver smsBroadcastReceiver;
    private final static String TAG = "SMSWatchService";

    public SMSWatchService(Context applicationContext) {
        super();
        Log.i(TAG, "Service Called");
    }

    public SMSWatchService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something useful
        super.onStartCommand(intent, flags, startId);

        //startTimer();

        smsBroadcastReceiver = new SmsBroadcastReceiver();
        registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        smsBroadcastReceiver.setSenderLimitation("6505551212");
        Log.d(TAG, "Started SMSBroadcastListener");

        smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
            @Override public void onTextReceived(String smsSender, String smsBody) {
                Log.d(TAG, "Received text - " + smsSender + ", " + smsBody);
                processTextAction(smsSender, smsBody);
            }
        });

        return START_STICKY;
        //return Service.START_REDELIVER_INTENT; // Restart if service is killed and pass original intent
    }

    // When service is destroyed create a the broadcast receiver
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        Intent broadcastIntent = new Intent("com.seikoshadow.apps.textthrough.restartBroadcastReceiver");
        sendBroadcast(broadcastIntent);
        stoptimertask();
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

    }


    public int counter=0;
    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));

            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
