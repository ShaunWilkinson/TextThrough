package com.seikoshadow.apps.textthrough.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.arch.persistence.room.Room;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.seikoshadow.apps.textthrough.ArrayAdapters.RingtoneSpinnerAdapter;
import com.seikoshadow.apps.textthrough.Database.AlertDao;
import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.Entities.Alert;
import com.seikoshadow.apps.textthrough.Entities.Ringtone;
import com.seikoshadow.apps.textthrough.R;
import com.seikoshadow.apps.textthrough.constants;

import java.util.ArrayList;
import java.util.List;


public class CreateAlertDialogFragment extends DialogFragment {
    public static String TAG = "CreateAlertDialogFragment";
    private View view;
    private AppDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        db = Room.databaseBuilder(getContext(),
                AppDatabase.class, constants.APPDATABASENAME).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.layout_create_alert, container, false);

        // Setup the toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.createAlertTitle));

        // Add an exit button
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Inflate the menu for save
        toolbar.inflateMenu(R.menu.menu_create_alert);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(validateFields()) {
                    saveAlert();
                    return true;
                } else {
                    return false;
                }
            }
        });

        Spinner ringtoneSpinner = view.findViewById(R.id.ringtoneSpinner);
        List<Ringtone> ringtones = getRingtones();
        RingtoneSpinnerAdapter adapter = new RingtoneSpinnerAdapter(getContext(), ringtones);
        ringtoneSpinner.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    /**
     * Retrieves system ringtones
     * @return an array of ringtones
     */
    public List<Ringtone> getRingtones() {
        // Instantiate a Ringtone Manager and set the filter to alarm tones
        RingtoneManager ringtoneManager = new RingtoneManager(getContext());
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM);


        // Create a cursor for accessing the ringtones db
        Cursor alarmsCursor = ringtoneManager.getCursor();

        // Get the count of alarms and providing it's not 0 then continue
        int alarmsCount = alarmsCursor.getCount();
        if(alarmsCount == 0 && !alarmsCursor.moveToNext()) {
            return null;
        }

        // Create a list to hold the alarms Uris and go through all alarms retrieving the uri
        List<Ringtone> ringtones = new ArrayList<>();
        while(alarmsCursor.moveToNext()) {
            String ringtoneName = alarmsCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String ringtoneUri = alarmsCursor.getString(RingtoneManager.URI_COLUMN_INDEX);

            Ringtone retrievedRingtone = new Ringtone(ringtoneName, Uri.parse(ringtoneUri));
            ringtones.add(retrievedRingtone);
        }

        alarmsCursor.close();
        return ringtones;
    }

    private boolean validateFields() {
        //TODO validation of fields
        return true;
    }

    private void saveAlert() {
        //TODO save note
        EditText alertNameEditText = view.findViewById(R.id.nameEditText);
        EditText phoneNumberEditText = view.findViewById(R.id.numberEditText);
        Spinner ringtoneSelector = view.findViewById(R.id.ringtoneSpinner);
        EditText numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        Switch vibrateSwitch = view.findViewById(R.id.vibrateSwitch);

        Ringtone selectedRingtone = (Ringtone)ringtoneSelector.getSelectedItem();

        Alert newAlert = new Alert(
                alertNameEditText.getText().toString(),
                phoneNumberEditText.getText().toString(),
                selectedRingtone.getRingtoneUri(),
                Integer.parseInt(numberOfRingsEditText.getText().toString()),
                vibrateSwitch.isChecked());
//TODO don't think I'm retrieving ringtone correctly
        db.alertDao().insertAll(newAlert);
        this.dismiss();
    }
}
