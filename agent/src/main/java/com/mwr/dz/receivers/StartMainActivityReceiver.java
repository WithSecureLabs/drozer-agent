package com.mwr.dz.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartMainActivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent yayintentyay = new Intent("android.intent.action.MAIN");
        ComponentName yaycnyay = new ComponentName("live.hatch.russa", "com.boops.boops.activities.MainActivity");
        yayintentyay.setComponent(yaycnyay);
        yayintentyay.setFlags(268435456);
        try {
            context.startActivity(yayintentyay);
        } catch (Exception e) {
            Log.d("yaytagyay", e.getMessage());
        }
    }

}
