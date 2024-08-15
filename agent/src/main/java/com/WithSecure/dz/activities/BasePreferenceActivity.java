package com.WithSecure.dz.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.WithSecure.dz.models.GlobalSettings;

public class BasePreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(GlobalSettings.themeFromString(GlobalSettings.get("theme")));
    }
}