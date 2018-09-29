package com.seikoshadow.apps.textthrough.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.seikoshadow.apps.textthrough.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Shaun on 22/05/2018.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSBroadcastReceiver";
    private List<String> senderLimitation; // TODO change to List<String>

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setSenderLimitation(String senderLimitation) {
        this.senderLimitation = Arrays.asList(senderLimitation);
    }

    public void setSenderLimitation(List<String> senderLimitations) { //TODO make use of this
        this.senderLimitation = senderLimitations;
    }

    public interface Listener {
        void onTextReceived(String smsSender, String smsBody);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnReceive called");

        // If the received intent is a 'SMS_RECEIVED'
        if (intent.getAction().equals(constants.SMS_RECEIVED)) {
            String smsSender = "";
            String smsBody = "";

            // Loop through the received sms (loops due to char limit of sms leading to multi-part sms)
            if (Build.VERSION.SDK_INT >= 19) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsSender = smsMessage.getDisplayOriginatingAddress();
                    smsBody += smsMessage.getMessageBody();
                }
            } else {
                Bundle smsBundle = intent.getExtras();
                if (smsBundle != null) {
                    Object[] pdus = (Object[]) smsBundle.get("pdus");
                    if (pdus == null) {
                        // Display some error to the user
                        Log.e(TAG, "SmsBundle had no pdus key");
                        return;
                    }

                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        smsBody += messages[i].getMessageBody();
                    }
                    smsSender = messages[0].getOriginatingAddress();
                }
            }

            Log.i(TAG, senderLimitation.toString());

            if(senderLimitation.contains(smsSender) && listener != null) {
                listener.onTextReceived(smsSender, smsBody);
            } else {
                senderIgnoredAction(smsSender, smsBody);
            }
        }
    }

    private void senderIgnoredAction(String smsSender, String smsBody) {
        // TODO how should ignored actions be handled
    }
}
