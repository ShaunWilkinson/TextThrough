package com.seikoshadow.apps.textalerter.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.seikoshadow.apps.textalerter.Database.Alert;
import com.seikoshadow.apps.textalerter.Database.AlertModel;
import com.seikoshadow.apps.textalerter.Database.AppDatabase;
import com.seikoshadow.apps.textalerter.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import static android.app.Activity.RESULT_OK;

public class EditAlertDialogFragment extends DialogFragment {
    public static String TAG = "EditAlertDialogFragment";
    private View view;
    private AppDatabase db;
    private Alert alert;

    private EditText alertNameEditText;
    private EditText phoneNumberEditText;
    private EditText numberOfRingsEditText;
    private Switch vibrateSwitch;
    private Switch activeSwitch;

    /**
     * Called first, on creation set the style to fullscreen and initiate a reference to the database
     * @param savedInstanceState the savedInstanceState bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        //TODO finish edit alerts

        db = AppDatabase.getInstance(getContext());

        Bundle bundle = getArguments();
        alert = db.alertModel().findById((int)bundle.getLong("Alert Id"));
    }

    /**
     * inflates the view, sets up the toolbar and sets up the ringtone spinner
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.layout_create_alert, container, false);

        setupToolbar();
        populateFields();
        setupRingtonePickerButton(view);

        return view;
    }

    private void setupRingtonePickerButton(View view) {
        Button ringtoneSelectButton = view.findViewById(R.id.ringtoneSelectBtn);
        ringtoneSelectButton.setText(alert.getRingtoneName());

        ringtoneSelectButton.setOnClickListener(view1 -> {
            selectRingtone(view);
        });
    }

    private void populateFields() {
        alertNameEditText = view.findViewById(R.id.nameEditText);
        phoneNumberEditText = view.findViewById(R.id.numberEditText);
        numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        vibrateSwitch = view.findViewById(R.id.vibrateSwitch);
        activeSwitch = view.findViewById(R.id.activeSwitch);

        alertNameEditText.setText(alert.getName());
        phoneNumberEditText.setText(alert.getPhoneNumber());
        numberOfRingsEditText.setText(String.valueOf(alert.getSecondsToRingFor()));
        vibrateSwitch.setChecked(alert.isAlertVibrate());
        activeSwitch.setChecked(alert.isAlertActive());
    }

    private void setupToolbar() {
        // Setup the toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.editAlertTitle));

        // Add an exit button
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(view -> getDialog().dismiss());

        // Inflate the menu for save
        toolbar.inflateMenu(R.menu.menu_edit_alert);
        toolbar.setOnMenuItemClickListener(item -> {
            // Save Button Clicked
            if(item.getItemId() == R.id.saveAlert) {
                if (validateFields()) {
                    saveAlert();
                }
            // Delete Button Clicked
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to delete this alert?")
                        .setPositiveButton(android.R.string.yes, (confirmDialog, which) -> {
                            // Run the delete in the background
                            Executor executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                // Make sure theres not an existing record with the same phone number
                                AlertModel alertModel = db.alertModel();
                                alertModel.delete(alert);
                            });

                            this.dismiss();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }

            return true;
        });

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
        return true;
    }

    /**
     * Gets the values from the input fields and then saves the result
     */
    public void saveAlert() {
        alert.setName(alertNameEditText.getText().toString());
        alert.setPhoneNumber(phoneNumberEditText.getText().toString());
        int numberOfRings = Integer.parseInt(numberOfRingsEditText.getText().toString());
        alert.setSecondsToRingFor(numberOfRings);
        alert.setAlertVibrate(vibrateSwitch.isChecked());
        alert.setAlertActive((activeSwitch.isChecked()));

        // Run the insert on a separate thread
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Make sure theres not an existing record with the same phone number
            AlertModel alertModel = db.alertModel();
            alertModel.insertAll(alert);
        });

        this.dismiss();
    }

    //TODO finish ringtone selector (only a button right now)

    /**
     * Generates a Ringtone Picker
     */
    private void selectRingtone(View view) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtonePickerTitle));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(alert.getRingtoneUri())); // Sets the default ringtone
        this.startActivityForResult(intent,999);
    }

    /**
     * When a ringtone has been selected or otherwise check for the result then get URI and name then save
     * @param requestCode The requestCode specified when the ringtone selector is instantiated
     * @param resultCode The result of the dialog, ie was a ringtone picked or cancelled
     * @param data The data passed from the dialog
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the result comes from the ringtone picker
        if(requestCode == 999) {
            if(resultCode == RESULT_OK) {
                // Get the selected ringtone and pass to saveAlert
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Ringtone selectedRingtone = RingtoneManager.getRingtone(getContext(), uri);
                String name = selectedRingtone.getTitle(getContext());
                selectedRingtone.stop();
                if(uri != null) {
                    Button ringtoneSelectButton = view.findViewById(R.id.ringtoneSelectBtn);
                    ringtoneSelectButton.setText(name);
                    alert.setRingtoneName(name);
                    alert.setRingtoneUri(uri);
                }
            }
        }
    }

}
