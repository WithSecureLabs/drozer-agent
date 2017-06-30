package com.mwr.jdiesel.api.connectors;

import java.util.Observable;

import com.mwr.jdiesel.logger.Logger;

public abstract class Connector extends Observable {
	
	public static final String CONNECTOR_CONNECTED = "connector:connected";
	public static final String CONNECTOR_ENABLED = "connector:enabled";
	public static final String CONNECTOR_LOG_MESSAGE = "connector:logmessage";
	public static final String CONNECTOR_OPEN_SESSIONS = "connector:opensessions";
	public static final String CONNECTOR_SSL_FINGERPRINT = "certificate:fingerprint";
	
	public enum Status { ACTIVE, CONNECTING, UNKNOWN, UPDATING, ONLINE, OFFLINE };
	
	public volatile boolean enabled = false;
	public volatile Status status = Status.UNKNOWN;
	
	private Logger<Connector> logger = new Logger<Connector>(this);
	
	public synchronized boolean isEnabled() {
		return this.enabled;
	}
	
	public Logger<Connector> getLogger() {
		return this.logger;
	}
	
	public synchronized Status getStatus() {
		return this.status;
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
