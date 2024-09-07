package com.WithSecure.dz.activities;

import android.app.Activity;
import android.os.Bundle;

import com.WithSecure.dz.models.GlobalSettings;

public class BaseActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(GlobalSettings.themeFromString(GlobalSettings.get("theme")));

        super.onCreate(savedInstanceState);
    }
}

