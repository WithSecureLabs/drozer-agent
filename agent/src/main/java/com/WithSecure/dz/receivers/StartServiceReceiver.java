package com.WithSecure.dz.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class StartServiceReceiver extends BroadcastReceiver {
	
	public static final String PWN_INTENT = "com.withsecure.dz.PWN";

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent start_service = new Intent();
		start_service.putExtras(intent);
		
		if(intent.getCategories().contains("com.WithSecure.dz.START_EMBEDDED")) {
			start_service.addCategory("com.WithSecure.dz.START_EMBEDDED");
			start_service.setComponent(new ComponentName(context.getPackageName(), "com.WithSecure.dz.services.ServerService"));
		}
		else {
			if(intent.getCategories().contains("com.WithSecure.dz.CREATE_ENDPOINT"))
				start_service.addCategory("com.WithSecure.dz.CREATE_ENDPOINT");
			if(intent.getCategories().contains("com.WithSecure.dz.START_ENDPOINT"))
				start_service.addCategory("com.WithSecure.dz.START_ENDPOINT");
			
			start_service.setComponent(new ComponentName(context.getPackageName(), "com.WithSecure.dz.services.ClientService"));
		}

		context.startService(start_service);
	}

}
