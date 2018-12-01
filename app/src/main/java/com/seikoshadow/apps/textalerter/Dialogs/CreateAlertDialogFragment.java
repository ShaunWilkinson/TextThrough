package com.seikoshadow.apps.textalerter.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import static android.app.Activity.RESULT_OK;

/**
 * Handles the fullscreen dialog for creating new alerts
 */
public class CreateAlertDialogFragment extends DialogFragment {
    public static String TAG = "CreateAlertDialogFragment";
    private View view;
    private AppDatabase db;

    private String ringtoneName;
    private Uri ringtoneUri;

    private EditText alertNameEditText;
    private EditText phoneNumberEditText;
    private EditText numberOfRingsEditText;
    private SwitchCompat vibrateSwitch;

    /**
     * Called first, on creation set the style to fullscreen and initiate a reference to the database
     * @param savedInstanceState the savedInstanceState bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        db = AppDatabase.getInstance(getActivity());
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

        alertNameEditText = view.findViewById(R.id.nameEditText);
        phoneNumberEditText = view.findViewById(R.id.numberEditText);
        numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        vibrateSwitch = view.findViewById(R.id.vibrateSwitch);

        setupRingtonePickerButton(view);
        setupContactPickerButton(view);

        return view;
    }

    private void setupRingtonePickerButton(View view) {
        ImageButton ringtoneSelectButton = view.findViewById(R.id.ringtoneSelectBtn);
        TextView ringtoneSelectValue = view.findViewById(R.id.ringtoneSelectValue);

        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity().getApplicationContext(), RingtoneManager.TYPE_ALARM);
        Ringtone defaultRingtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);

        this.ringtoneUri = ringtoneUri;
        ringtoneName = defaultRingtone.getTitle(getActivity());

        ringtoneSelectValue.setText(defaultRingtone.getTitle(getActivity()));
        ringtoneSelectButton.setOnClickListener(this::selectRingtone);
    }

    private void setupContactPickerButton(View view) {
        ImageButton contactPicker = view.findViewById(R.id.contactPickerBtn);

        contactPicker.setOnClickListener(this::selectContact);
    }

    private void setupToolbar() {
        // Setup the toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.createAlertTitle));

        // Add an exit button
        toolbar.setNavigationIcon(R.drawable.dialog_close_icon);
        toolbar.setNavigationOnClickListener(view -> getDialog().dismiss());

        // Inflate the menu for save
        toolbar.inflateMenu(R.menu.menu_create_alert);
        toolbar.setOnMenuItemClickListener(item -> {
            if(validateFields()) {
                saveAlert();
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
            phoneNumberEditText.setText(phoneNumberEditText.getText().toString().replace(" ", ""));

            Pattern p = Pattern.compile("^[a-zA-Z0-9äöüÄÖÜ+]*$", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(phoneNumberEditText.getText().toString());
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
     * Called by clicking the select contact button
     * @param view
     */
    private void selectContact(View view) {
        //TODO code to select contact and populate phone number
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
        if(resultCode == RESULT_OK) {

            // RINGTONE PICKER
            if (requestCode == 999) {
                // Get the selected ringtone and pass to saveAlert
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Ringtone selectedRingtone = RingtoneManager.getRingtone(getActivity(), uri);
                String name = selectedRingtone.getTitle(getActivity());
                selectedRingtone.stop();
                if (uri != null) {
                    TextView ringtoneSelectValue = view.findViewById(R.id.ringtoneSelectValue);
                    ringtoneSelectValue.setText(name);
                    ringtoneName = name;
                    ringtoneUri = uri;
                }
            }

            // CONTACT PICKER
            if (requestCode == 998) {
                Uri contactData = data.getData();
                if(contactData != null) {
                    Cursor cursor = getActivity().getContentResolver().query(contactData, null, null, null, null);

                    if(cursor!= null) {
                        cursor.moveToFirst();
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        Log.d(TAG, "Retrieved " + number);
                        EditText phoneNumberField = view.findViewById(R.id.numberEditText);
                        phoneNumberField.setText(number);

                        cursor.close();
                    } else {
                        Log.e(TAG, "Failed to get cursor");
                    }
                } else {
                    Log.e(TAG, "Failed to get contact data");
                }
            }

        }
    }

    /**
     * Gets the values from the input fields and then saves the result
     */
    public void saveAlert() {
        Alert alert = new Alert(
                alertNameEditText.getText().toString(),
                phoneNumberEditText.getText().toString(),
                ringtoneName,
                ringtoneUri,
                Integer.parseInt(numberOfRingsEditText.getText().toString()),
                vibrateSwitch.isChecked());

        // Run the insert on a separate thread
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Make sure theres not an existing record with the same phone number
            AlertModel alertModel = db.alertModel();
            if(alertModel.findByPhoneNumber(alert.getPhoneNumber()) == null)
                db.alertModel().insertAll(alert);
        });

        this.dismiss();
    }
}
