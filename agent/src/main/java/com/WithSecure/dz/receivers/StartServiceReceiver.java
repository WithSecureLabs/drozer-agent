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
		
		if(intent.getCategories().contains("com.withsecure.dz.START_EMBEDDED")) {
			start_service.addCategory("com.withsecure.dz.START_EMBEDDED");
			start_service.setComponent(new ComponentName("com.withsecure.dz", "com.withsecure.dz.services.ServerService"));
		}
		else {
			if(intent.getCategories().contains("com.withsecure.dz.CREATE_ENDPOINT"))
				start_service.addCategory("com.withsecure.dz.CREATE_ENDPOINT");
			if(intent.getCategories().contains("com.withsecure.dz.START_ENDPOINT"))
				start_service.addCategory("com.withsecure.dz.START_ENDPOINT");
			
			start_service.setComponent(new ComponentName("com.withsecure.dz", "com.withsecure.dz.services.ClientService"));
		}

		context.startService(start_service);
	}

}
