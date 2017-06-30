package com.mwr.jdiesel.logger;

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
		this(LogMessage.INFO, message);
	}
	
	public LogMessage(int level, String message) {
		this.level = level;
		this.message = message;
	}

	public LogMessage(Bundle bundle) {
		this(bundle.getInt(LogMessage.LEVEL), bundle.getString(LogMessage.MESSAGE));
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public String getMessage() {
		return this.message != null ? this.message : "No message.";
	}

	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		
		bundle.putInt(LogMessage.LEVEL, this.getLevel());
		bundle.putString(LogMessage.MESSAGE, this.getMessage());
		
		return bundle;
	}
	
}
