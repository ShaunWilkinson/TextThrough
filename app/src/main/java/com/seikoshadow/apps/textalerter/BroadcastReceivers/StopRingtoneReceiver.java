package com.seikoshadow.apps.textalerter.BroadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.seikoshadow.apps.textalerter.AlarmControl;
import com.seikoshadow.apps.textalerter.MainActivity;

import static com.seikoshadow.apps.textalerter.constants.SMS_SERVICE_RUNNING_ID;

/**
 * Simply gets an instance of the alarm controller and stops any playing sound
 */
public class StopRingtoneReceiver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        // Get any actions
        Bundle bundle = intent.getExtras();
        String action = null;
        if(bundle != null) {
            action = intent.getExtras().getString("Action");
        }

        // Stop the ringing sound
        AlarmControl.getInstance(context).stopMusic();

        // Stop the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            notificationManager.cancel(SMS_SERVICE_RUNNING_ID);
        }

        // If the extra action was to open app then open the app
        if(action != null && action.equals("Open App")) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
