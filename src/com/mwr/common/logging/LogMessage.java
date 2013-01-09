package com.mwr.common.logging;

import android.os.Bundle;

public class LogMessage {
	
	public static final String LEVEL = "log:level";
	public static final String MESSAGE = "log:message";
	
	public static final int VERBOSE = 0x00000002;
	public static final int DEBUG = 0x00000003;
	public static final int INFO = 0x00000004;
	public static final int WARN = 0x00000005;
	public static final int ERROR = 0x00000006;
	public static final int ASSERT = 0x00000007;
	
	private int level;
	private String message;

	public LogMessage(String message) {
		this(INFO, message);
	}
	
	public LogMessage(int level, String message) {
		this.level = level;
		this.message = message;
	}

	public static LogMessage fromBundle(Bundle bundle) {
		return new LogMessage(bundle.getInt(LEVEL), bundle.getString(MESSAGE));
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public String getMessage() {
		return this.message != null ? this.message : "No message.";
	}

	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		
		bundle.putInt(LEVEL, this.getLevel());
		bundle.putString(MESSAGE, this.getMessage());
		
		return bundle;
	}
	
}
