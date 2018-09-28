package com.seikoshadow.apps.textthrough.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seikoshadow.apps.textthrough.SMSWatchService;

public class RestartService extends BroadcastReceiver {
    private static final String TAG = "RestartService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Service Stopped, restarting");

        context.startService(new Intent(context, SMSWatchService.class));
    }
}

