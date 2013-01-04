package com.mwr.droidhg.api;

import java.util.Observable;

public abstract class ConnectorParameters extends Observable {
	
	public interface OnLogMessageListener {
		
		public void onLogMessage(String message);
		
	}
	
	public enum Status { ACTIVE, CONNECTING, UNKNOWN, UPDATING, ONLINE, OFFLINE };
	
	public volatile boolean enabled = false;
	private OnLogMessageListener on_log_message_listener;
	public volatile Status status = Status.UNKNOWN;
	
	public synchronized boolean isEnabled() {
		return this.enabled;
	}
	
	public synchronized Status getStatus() {
		return this.status;
	}
	
	public void logMessage(String message) {
		if(this.on_log_message_listener != null)
			this.on_log_message_listener.onLogMessage(message);
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
