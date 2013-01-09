package com.mwr.droidhg.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.mwr.common.logging.LogMessage;
import com.mwr.common.logging.Logger;
import com.mwr.common.logging.OnLogMessageListener;

public abstract class ConnectorParameters extends Observable implements Logger {
	
	public static final String CONNECTOR_CONNECTED = "connector:connected";
	public static final String CONNECTOR_ENABLED = "connector:enabled";
	public static final String CONNECTOR_LOG_MESSAGE = "connector:logmessage";
	public static final String CONNECTOR_OPEN_SESSIONS = "connector:opensessions";
	public static final String CONNECTOR_SSL_FINGERPRINT = "certificate:fingerprint";
	
	public enum Status { ACTIVE, CONNECTING, UNKNOWN, UPDATING, ONLINE, OFFLINE };
	
	public volatile boolean enabled = false;
	private List<LogMessage> log_messages = new ArrayList<LogMessage>();
	private OnLogMessageListener on_log_message_listener;
	public volatile Status status = Status.UNKNOWN;
	
	public synchronized boolean isEnabled() {
		return this.enabled;
	}
	
	@Override
	public List<LogMessage> getLogMessages() {
		return this.log_messages;
	}
	
	public synchronized Status getStatus() {
		return this.status;
	}
	
	public void log(String message) {
		this.log(new LogMessage(message));
	}
	
	@Override
	public void log(LogMessage message) {
		this.log_messages.add(message);
		
		if(this.on_log_message_listener != null)
			this.on_log_message_listener.onLogMessage(this, message);
	}
	
	@Override
	public void log(Logger logger, LogMessage message) {
		this.log(message);
	}
	
	public void setOnLogMessageListener(OnLogMessageListener listener) {
		this.on_log_message_listener = listener;
	}
	
	public synchronized void setStatus(Status status) {
		if(this.status != status) {
			this.status = status;
			
			this.setChanged();
			this.notifyObservers();
		}
	}

	public abstract boolean verifyPassword(String password);

}
