package com.seikoshadow.apps.textthrough.Dialogs;

import android.app.Activity;
import android.os.Bundle;

import com.seikoshadow.apps.textthrough.R;

public class AlarmRingingDialog extends Activity {

    /** Called when the activity is first created **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_ringing_dialog);
    }

    //TODO move alarm logic here, create dialog to stop alarm
}
