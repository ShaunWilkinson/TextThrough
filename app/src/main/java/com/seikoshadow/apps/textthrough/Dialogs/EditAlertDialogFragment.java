package com.seikoshadow.apps.textthrough.Dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.seikoshadow.apps.textthrough.Database.Alert;
import com.seikoshadow.apps.textthrough.Database.AlertModel;
import com.seikoshadow.apps.textthrough.Database.AlertViewModel;
import com.seikoshadow.apps.textthrough.Database.AppDatabase;
import com.seikoshadow.apps.textthrough.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import static android.app.Activity.RESULT_OK;

public class EditAlertDialogFragment extends DialogFragment {
    public static String TAG = "EditAlertDialogFragment";
    private View view;
    private AppDatabase db;
    private Alert alert;
    private Uri ringtoneUri;

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

        TextView ringtoneSelector = view.findViewById(R.id.ringtoneEditText);
        ringtoneSelector.setText(alert.getRingtoneName());

        return view;
    }


    private void setupToolbar() {
        // Setup the toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.editAlertTitle));

        // Add an exit button
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(view -> getDialog().dismiss());

        // Inflate the menu for save
        toolbar.inflateMenu(R.menu.menu_create_alert);
        toolbar.setOnMenuItemClickListener(item -> {
            if(validateFields()) {
                selectRingtone();
                return true;
            } else {
                return false;
            }
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
     * Generates a Ringtone Picker
     */
    private void selectRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtonePickerTitle));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
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
                    saveAlert(name, uri);
                }
            }
        }
    }

    /**
     * Gets the values from the input fields and then saves the result
     */
    public void saveAlert() {
        EditText alertNameEditText = view.findViewById(R.id.nameEditText);
        EditText phoneNumberEditText = view.findViewById(R.id.numberEditText);
        EditText numberOfRingsEditText = view.findViewById(R.id.numOfRingsEditText);
        Switch vibrateSwitch = view.findViewById(R.id.vibrateSwitch);

        /*
        alert = new Alert(
                alertNameEditText.getText().toString(),
                phoneNumberEditText.getText().toString(),
                ringtoneName,
                selectedRingtone.toString(),
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

*/
        this.dismiss();
    }

}
