package com.seikoshadow.apps.textalerter.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.seikoshadow.apps.textalerter.Database.Alert;
import com.seikoshadow.apps.textalerter.Database.AlertModel;
import com.seikoshadow.apps.textalerter.Database.AppDatabase;
import com.seikoshadow.apps.textalerter.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
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
    private SwitchCompat vibrateSwitch;
    private SwitchCompat activeSwitch;

    /**
     * Called first, on creation set the style to fullscreen and initiate a reference to the database
     * @param savedInstanceState the savedInstanceState bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        db = AppDatabase.getInstance(getActivity());

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

        view = inflater.inflate(R.layout.dialog_alert, container, false);

        setupToolbar();
        populateFields();

        setupRingtonePickerButton(view);
        setupContactPickerButton(view);

        return view;
    }

    private void setupRingtonePickerButton(View view) {
        ImageButton ringtoneSelectButton = view.findViewById(R.id.ringtoneSelectBtn);
        TextView ringtoneSelectValue = view.findViewById(R.id.ringtoneSelectValue);

        ringtoneSelectValue.setText(alert.getRingtoneName());

        ringtoneSelectButton.setOnClickListener(view1 -> selectRingtone(view));
    }

    private void setupContactPickerButton(View view) {
        ImageButton contactPicker = view.findViewById(R.id.contactPickerBtn);

        contactPicker.setOnClickListener(this::selectContact);
    }

    private void populateFields() {
        alertNameEditText = view.findViewById(R.id.nameEditText);
        phoneNumberEditText = view.findViewById(R.id.numberEditText);
        numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        vibrateSwitch = view.findViewById(R.id.vibrateSwitch);
        activeSwitch = view.findViewById(R.id.activeSwitch);

        activeSwitch.setVisibility(View.VISIBLE);

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
        toolbar.setNavigationIcon(R.drawable.dialog_close_icon);
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
                new AlertDialog.Builder(getActivity())
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

    /**
     * Very basic validation of field inputs
     * @return true if valid, false otherwise
     */
    private boolean validateFields() {
        boolean valid = true;

        if(alertNameEditText.getText().toString().equals("") || alertNameEditText.getText().length() > 50) {
            alertNameEditText.setError(getString(R.string.alert_name_error));
            valid = false;
        }

        String phoneNumberInput = phoneNumberEditText.getText().toString();
        // Ensure phone number isn't blank or too long
        if(phoneNumberInput.equals("") || phoneNumberInput.length() > 30) {
            phoneNumberEditText.setError(getString(R.string.alert_phone_number_error));
            valid = false;

            // Ensure phone number doesn't contain special characters (uses + or ( to check if it's a number)
        } else if (phoneNumberInput.substring(0, 1).equals("+") || phoneNumberInput.substring(0, 1).equals("(")) {
            Pattern p = Pattern.compile("^[a-zA-Z0-9äöüÄÖÜ+]*$", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(phoneNumberInput);
            if (!m.find()) {
                phoneNumberEditText.setError(getString(R.string.alert_phone_number_character_error));
                valid = false;
            }
        }

        // If the seconds to ring isn't blank
        if(!numberOfRingsEditText.getText().toString().equals("")) {
            try {
                Integer.parseInt(numberOfRingsEditText.getText().toString());
            } catch (NumberFormatException exception) {
                numberOfRingsEditText.setError(getString(R.string.alert_phone_number_error));
                valid = false;
            }
        } else {
            numberOfRingsEditText.setError(getString(R.string.alert_seconds_to_ring_error));
            valid = false;
        }

        return valid;
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

    /**
    * Called by clicking the select contact button
    * @param view
    */
    private void selectContact(View view) {
        // Start a contact picker
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 998);
    }

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
                Ringtone selectedRingtone = RingtoneManager.getRingtone(getActivity(), uri);
                String name = selectedRingtone.getTitle(getActivity());
                selectedRingtone.stop();
                if(uri != null) {
                    TextView ringtoneSelectValue = view.findViewById(R.id.ringtoneSelectValue);
                    ringtoneSelectValue.setText(name);
                    alert.setRingtoneName(name);
                    alert.setRingtoneUri(uri);
                }
            }
        }
    }

}