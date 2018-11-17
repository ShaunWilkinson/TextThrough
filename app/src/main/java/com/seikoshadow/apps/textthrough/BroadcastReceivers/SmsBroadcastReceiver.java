package com.seikoshadow.apps.textthrough.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seikoshadow.apps.textthrough.Services.SMSHandlerService;

/**
 * Created by Shaun on 22/05/2018.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnReceive called");

        intent.setClass(context, SMSHandlerService.class);
        intent.putExtra("result", getResultCode());
        context.startService(intent);
    }
}





