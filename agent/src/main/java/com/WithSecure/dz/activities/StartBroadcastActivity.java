package com.WithSecure.dz.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

public class StartBroadcastActivity extends BaseActivity {

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        startChooser(getIntent());
        finish();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public void onRestart() {
        super.onRestart();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
    }

    private void startChooser(Intent intent) {
        if (intent != null && intent.getExtras().containsKey("yayintentyay")) {
            try {
                ArrayList<ComponentName> arrayList = new ArrayList<>();
                arrayList.add(new ComponentName(this.getPackageName(), StartBroadcastActivity.class.getName()));

                Intent yayparcelableyay = intent.getParcelableExtra("yayintentyay");
                Intent createChooser = Intent.createChooser((Intent) yayparcelableyay, "Share");

                createChooser.putExtra("android.intent.extra.EXCLUDE_COMPONENTS", (Parcelable[]) arrayList.toArray(new Parcelable[0]));

                createChooser.setComponent(yayparcelableyay.getComponent());
                createChooser.putExtras(yayparcelableyay.getExtras());
                createChooser.setAction(yayparcelableyay.getAction());

                hooking(createChooser);

                startActivity(createChooser);
            } catch (Exception e) {
                Log.d("yaytagyay", e.getMessage());
            }
        }
    }

    private void hooking(Intent intent){
        Log.i("yaytagyay", "intent");
    }
}
