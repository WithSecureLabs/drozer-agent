package com.mwr.droidhg.api;

import java.util.Observable;

public abstract class ConnectorParameters extends Observable {
	
	public enum Status { ACTIVE, CONNECTING, UNKNOWN, UPDATING, ONLINE, OFFLINE };
	
	public volatile boolean enabled = false;
	public volatile Status status = Status.UNKNOWN;
	
	public synchronized boolean isEnabled() {
		return this.enabled;
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

}
