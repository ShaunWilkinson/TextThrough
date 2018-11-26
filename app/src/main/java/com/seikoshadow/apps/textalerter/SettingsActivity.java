package com.seikoshadow.apps.textalerter;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Activity container for showing settings
 */
public class SettingsActivity extends AppCompatActivity {
    //TODO implement useful settings such as default values etc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

        setupToolbar();
    }

    private void setupToolbar() {
        // Setup the toolbar
        View view = this.findViewById(R.id.settings_layout);

        Toolbar toolbar = view.findViewById(R.id.mainToolbar);
        toolbar.setTitle(getString(R.string.editAlertTitle));
    }


}
