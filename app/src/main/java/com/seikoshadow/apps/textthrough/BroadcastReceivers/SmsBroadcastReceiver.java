package com.seikoshadow.apps.textthrough.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.seikoshadow.apps.textthrough.SMSWatchService;

/**
 * Created by Shaun on 22/05/2018.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";
    private String senderLimitation;

    private Listener listener;

    /*
    public SmsBroadcastReceiver() {
        this.senderLimitation = null;
    }
    */

    /*
    public SmsBroadcastReceiver(String number) {
        this.senderLimitation = number;
    }
    */

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setSenderLimitation(String senderLimitation) {
        this.senderLimitation = senderLimitation;
    }

    public interface Listener {
        void onTextReceived(String smsSender, String smsBody);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnReceive called");

        //context.startService(new Intent(context, SMSWatchService.class));

        // If the received intent is a 'SMS_RECEIVED'
        if (intent.getAction().equals(SMS_RECEIVED)) {
            String smsSender = "";
            String smsBody = "";

            // Loop through the received sms (loops due to char limit of sms leading to multi-part sms)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

            Log.i(TAG, senderLimitation);
            if (smsSender.equalsIgnoreCase(senderLimitation)) {
                if (listener != null) {
                    listener.onTextReceived(smsSender, smsBody);
                } else {
                    Log.e(TAG, "Failed to find a Listener");
                }
            } else {
                Log.d(TAG, "Ignored sms from " + smsSender);
            }
        }
    }

    public void processTextAction(String smsSender, String smsBody) {
        Log.d(TAG, "Processing text from " + smsSender);

    }
}
