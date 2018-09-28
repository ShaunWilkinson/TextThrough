package com.seikoshadow.apps.textthrough.Services;

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

    public SMSWatchService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something useful
        super.onStartCommand(intent, flags, startId);

        smsBroadcastReceiver = new SmsBroadcastReceiver();
        registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        smsBroadcastReceiver.setSenderLimitation("6505551212");
        Log.d(TAG, "Started SMSBroadcastListener");

        // TODO handle a proper text being received
        // What to do when a text is received
        smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
            @Override public void onTextReceived(String smsSender, String smsBody) {
                Log.d(TAG, "Received text - " + smsSender + ", " + smsBody);
                processTextAction(smsSender, smsBody);
            }
        });

        return START_STICKY;
    }

    // When service is destroyed create a the broadcast receiver
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        Intent broadcastIntent = new Intent("com.seikoshadow.apps.textthrough.restartBroadcastReceiver");
        sendBroadcast(broadcastIntent);
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
}
