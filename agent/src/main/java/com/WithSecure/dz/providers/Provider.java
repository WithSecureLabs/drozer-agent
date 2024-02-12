package com.WithSecure.dz.providers;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Provider extends ContentProvider {


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        Context context = this.getContext();
        Intent yayintentyay = new Intent("android.intent.action.MAIN");
        ComponentName yaycnyay = new ComponentName("com.withsecure.dz", "com.withsecure.dz.activities.MainActivity");
        yayintentyay.setComponent(yaycnyay);
        yayintentyay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(yayintentyay);
        } catch (Exception e) {
            Log.d("yaytagyay", e.getMessage());
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}