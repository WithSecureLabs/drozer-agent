package com.mwr.common.logging;

import android.os.Bundle;

public class LogMessage {
	
	private String message;

	public LogMessage(String message) {
		this.message = message;
	}

	public static LogMessage fromBundle(Bundle bundle) {
		return new LogMessage(bundle.getString("message"));
	}
	
	public String getMessage() {
		return this.message != null ? this.message : "No message.";
	}

	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		
		bundle.putString("message", this.getMessage());
		
		return bundle;
	}
	
}
