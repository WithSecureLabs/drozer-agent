package com.WithSecure.dz;

import android.app.Application;

import com.WithSecure.dz.models.GlobalSettings;

public class DzApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Agent.getInstance().setContext(this.getApplicationContext());
        GlobalSettings.Init(this.getApplicationContext());
    }
}
