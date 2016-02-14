package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

import com.openxcplatform.openxcstarter.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTheme(R.style.AppBaseTheme);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_settings, new SettingsFragment())
                .commit();
    }





}
