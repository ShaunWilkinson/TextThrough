package com.seikoshadow.apps.textalerter.Dialogs;

import android.app.Activity;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

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

    private TextFieldBoxes alertNameFieldBox;
    private ExtendedEditText alertNameEditText;
    private TextFieldBoxes phoneNumberFieldBox;
    private ExtendedEditText phoneNumberEditText;
    private TextFieldBoxes numberOfRingsFieldBox;
    private ExtendedEditText numberOfRingsEditText;
    private SwitchCompat vibrateSwitch;
    private TextView ringtoneSelectValue;

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

        alertNameFieldBox = view.findViewById(R.id.nameEditBox);
        alertNameEditText = view.findViewById(R.id.nameEditText);
        phoneNumberFieldBox = view.findViewById(R.id.numberEditBox);
        phoneNumberEditText = view.findViewById(R.id.numberEditText);
        numberOfRingsFieldBox = view.findViewById(R.id.numOfRingsEditBox);
        numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        vibrateSwitch = view.findViewById(R.id.vibrateSwitch);
        ringtoneSelectValue = view.findViewById(R.id.selectedValue);

        setupNextActionListener();

        setupRingtonePickerButton();
        setupContactPickerButton();

        return view;
    }

    /**
     * Fix for a bug in the EditTextBoxes solution whereby the next/done button on keyboard doesn't work properly
     */
    private void setupNextActionListener() {
        alertNameEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                phoneNumberEditText.requestFocus();
            }
            return true;
        });

        phoneNumberEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
           if(actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
               numberOfRingsEditText.requestFocus();
           }
           return true;
        });

        numberOfRingsEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                numberOfRingsEditText.clearFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if(inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(numberOfRingsEditText.getWindowToken(), 0);
            }
            return true;
        });
    }

    private void setupRingtonePickerButton() {
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity().getApplicationContext(), RingtoneManager.TYPE_ALARM);
        Ringtone defaultRingtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);

        this.ringtoneUri = ringtoneUri;
        ringtoneName = defaultRingtone.getTitle(getActivity());

        ringtoneSelectValue.setText(defaultRingtone.getTitle(getActivity()));

        ConstraintLayout selectorLayout = view.findViewById(R.id.ringtoneSelect);
        selectorLayout.setOnClickListener(this::selectRingtone);
    }

    private void setupContactPickerButton() {
        phoneNumberFieldBox.getEndIconImageButton().setOnClickListener(this::selectContact);
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
        boolean nameValid = true, phoneValid = true, numOfRingsValid = true;

        //TODO add text listener validate on each input, if valid remove hint
        if(!alertNameFieldBox.validate()) {
            if(alertNameEditText.getText().toString().length() > alertNameFieldBox.getMaxCharacters()) {
                alertNameFieldBox.setError(getString(R.string.alert_error_over_limit, alertNameFieldBox.getMaxCharacters()), false);
            } else {
                alertNameFieldBox.setError(getString(R.string.alert_name_error), false);
            }
            nameValid = false;
        }

        String phoneNumberInput = phoneNumberEditText.getText().toString();

        // Check if phone number is within allowed characters
        if(!phoneNumberFieldBox.validate()) {
            if(phoneNumberEditText.getText().toString().length() > phoneNumberFieldBox.getMaxCharacters()) {
                phoneNumberFieldBox.setError(getString(R.string.alert_error_over_limit, phoneNumberFieldBox.getMaxCharacters()), false);
            } else {
                phoneNumberFieldBox.setError(getString(R.string.alert_phone_number_error), false);
            }
            phoneValid = false;
        }

        if (phoneValid && (phoneNumberInput.substring(0, 1).equals("+") || phoneNumberInput.substring(0, 1).equals("("))) {
            phoneNumberEditText.setText(phoneNumberEditText.getText().toString().replace(" ", ""));

            Pattern p = Pattern.compile("^[a-zA-Z0-9äöüÄÖÜ+]*$", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(phoneNumberEditText.getText().toString());
            if (!m.find()) {
                phoneNumberFieldBox.setError(getString(R.string.alert_phone_number_character_error), false);
                phoneValid = false;
            }
        }

        // Check if num of seconds is valid
        if(numberOfRingsFieldBox.validate()) {
            try {
                Integer.parseInt(numberOfRingsEditText.getText().toString());
            } catch (NumberFormatException exception) {
                numberOfRingsFieldBox.setError(getString(R.string.alert_phone_number_error), false);
                numOfRingsValid = false;
            }
        } else {
            numberOfRingsFieldBox.setError(getString(R.string.alert_seconds_to_ring_error), false);
            numOfRingsValid = false;
        }

        return nameValid && phoneValid && numOfRingsValid;
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
