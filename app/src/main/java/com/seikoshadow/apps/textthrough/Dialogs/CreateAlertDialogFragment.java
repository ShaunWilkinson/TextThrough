package com.seikoshadow.apps.textthrough.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;
import android.widget.EditText;
import android.widget.Switch;

import com.seikoshadow.apps.textthrough.Database.AlertDao;
import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.Entities.Alert;
import com.seikoshadow.apps.textthrough.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

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
                    selectRingtone();
                    return true;
                } else {
                    return false;
                }
            }
        });

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

    private boolean validateFields() {
        //TODO validation of fields
        //TODO ensure that the phone number is unique
        return true;
    }

    private void selectRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtonePickerTitle));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
            this.startActivityForResult(intent,999);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the result comes from the ringtone picker
        if(requestCode == 999) {
            if(resultCode == RESULT_OK) {
                // Get the selected ringtone and pass to saveAlert
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if(uri != null) {
                    saveAlert(uri);
                }
            } else {
                //TODO handle cancelling ringtone selector
            }
        }
    }

    public void saveAlert(Uri selectedRingtone) {
        EditText alertNameEditText = view.findViewById(R.id.nameEditText);
        EditText phoneNumberEditText = view.findViewById(R.id.numberEditText);
        EditText numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        Switch vibrateSwitch = view.findViewById(R.id.vibrateSwitch);

        newAlert = new Alert(
                alertNameEditText.getText().toString(),
                phoneNumberEditText.getText().toString(),
                selectedRingtone.toString(),
                Integer.parseInt(numberOfRingsEditText.getText().toString()),
                vibrateSwitch.isChecked());

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
