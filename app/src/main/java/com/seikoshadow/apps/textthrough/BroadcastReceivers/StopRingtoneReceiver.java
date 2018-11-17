package com.seikoshadow.apps.textthrough.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.seikoshadow.apps.textthrough.AlarmControl;

public class StopRingtoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmControl.getInstance(context).stopMusic();
    }
}
