package com.seikoshadow.apps.textthrough.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Handles the fullscreen dialog for creating new alerts
 */
public class CreateAlertDialogFragment extends DialogFragment {
    public static String TAG = "CreateAlertDialogFragment";
    private View view;
    private AppDatabase db;
    private Alert newAlert;

    /**
     * Called first, on creation set the style to fullscreen and initiate a reference to the database
     * @param savedInstanceState the savedInstanceState bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        db = AppDatabase.getInstance(getContext());
    }

    /**
     * inflates the view, sets up the toolbar and sets up the ringtone spinner
     * @return the inflated view
     */
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

        // Setup the ringtone Spinner selector
        Spinner ringtoneSpinner = view.findViewById(R.id.ringtoneSpinner);
        RingtoneSpinnerAdapter adapter = new RingtoneSpinnerAdapter(getContext(), getRingtones());
        ringtoneSpinner.setAdapter(adapter);

        return view;
    }

    /**
     * Ensure the dialog is full screen
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog.getWindow() != null) {
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
        //TODO ensure that the phone number is unique
        return true;
    }

    private void saveAlert() {
        EditText alertNameEditText = view.findViewById(R.id.nameEditText);
        EditText phoneNumberEditText = view.findViewById(R.id.numberEditText);
        Spinner ringtoneSelector = view.findViewById(R.id.ringtoneSpinner);
        EditText numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        Switch vibrateSwitch = view.findViewById(R.id.vibrateSwitch);

        Ringtone selectedRingtone = (Ringtone)ringtoneSelector.getSelectedItem();

        newAlert = new Alert(
                alertNameEditText.getText().toString(),
                phoneNumberEditText.getText().toString(),
                selectedRingtone.getRingtoneUri(),
                Integer.parseInt(numberOfRingsEditText.getText().toString()),
                vibrateSwitch.isChecked());
        //TODO don't think I'm retrieving ringtone correctly

        // Run the insert on a separate thread
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Make sure theres not an existing record with the same phone number
                AlertDao alertDao = db.alertDao();
                if(alertDao.findByPhoneNumber(newAlert.getPhoneNumber()) == null)
                    db.alertDao().insertAll(newAlert);
            }
        });

        this.dismiss();
    }
}
