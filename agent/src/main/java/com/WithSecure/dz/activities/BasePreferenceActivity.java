package com.WithSecure.dz.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.WithSecure.dz.models.GlobalSettings;

public class BasePreferenceActivity extends PreferenceActivity {
    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        setTheme(GlobalSettings.themeFromString(GlobalSettings.get("theme")));

        super.onCreate(savedInstanceState);
    }
}
