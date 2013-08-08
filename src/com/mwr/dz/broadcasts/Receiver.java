package com.mwr.dz.broadcasts;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
	
	public static final String PWN_INTENT = "com.mwr.dz.PWN";

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent out = new Intent();
		out.putExtras(intent);
		Log.i("com.mwr.dz", "Received broadcast");
		
		if(intent.getCategories().contains("com.mwr.dz.START_EMBEDDED")){
			out.addCategory("com.mwr.dz.START_EMBEDDED");
			out.setComponent(new ComponentName("com.mwr.dz", "com.mwr.dz.services.ServerService"));
		}else{
			if(intent.getCategories().contains("com.mwr.dz.CREATE_ENDPOINT"))
				out.addCategory("com.mwr.dz.CREATE_ENDPOINT");
			if(intent.getCategories().contains("com.mwr.dz.START_ENDPOINT"))
				out.addCategory("com.mwr.dz.START_ENDPOINT");
			out.setComponent(new ComponentName("com.mwr.dz", "com.mwr.dz.services.ClientService"));
		}
		Log.i("com.mwr.dz", "sending intent: " + out.toString());
		context.startService(out);
	}

}
