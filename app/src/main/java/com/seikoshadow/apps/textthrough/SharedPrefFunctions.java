package com.seikoshadow.apps.textthrough;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefFunctions {

    /**
     * Saved a list of string to Shared Preferences
     * @param uniqueName The Unique name of the key-value pair
     * @param values The values to save
     * @param context The current context
     */
    public void saveStringList(String uniqueName, List<String> values, Context context) {
        Set<String> convertedValues = new HashSet<>(values);

        SharedPreferences prefs = context.getSharedPreferences(constants.SHAREDPREFKEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putStringSet(uniqueName, convertedValues);
        editor.apply();
    }

    /**
     * Retrieves a list of strings saved as a set in shared preferences
     * @param uniqueName The unique name of the saved key-pair
     * @param context The current context
     * @return Returns a List of Strings or null if nothing found
     */
    public List<String> loadStringList(String uniqueName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(constants.SHAREDPREFKEY, Context.MODE_PRIVATE);
        Set<String> retrievedValues = prefs.getStringSet(uniqueName, null);

        if(retrievedValues != null) {
            return new ArrayList<>(retrievedValues);
        } else {
            return null;
        }
    }
}
