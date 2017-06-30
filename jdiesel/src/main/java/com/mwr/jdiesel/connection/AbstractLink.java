package com.mwr.jdiesel.connection;

import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.transport.Transport;

public abstract class AbstractLink extends Thread {

	public volatile boolean running = false;
	protected volatile AbstractConnection connection = null;
	private AbstractSessionCollection sessions;
	
	public boolean checkForLiveness() { return true; }
	
	protected abstract void createConnection(Transport transport);
	
	protected AbstractSession createSession() {
		return this.sessions.create();
	}
	
	public boolean dieWithLastSession() { return false; }
	public boolean mustBind() { return true; }
	
	public AbstractSession getSession(String session_id) {
		return this.sessions.get(session_id);
	}
	
	public boolean hasSessions() {
		return this.sessions.any();
	}
	
	public void lastSessionStopped() {
		if(this.dieWithLastSession())
			this.stopConnection();
	}
	
	public void send(Message message) {
		this.connection.send(message);
	}
	
	public AbstractSession startSession() {
		return this.startSession(null);
	}
	
	public abstract AbstractSession startSession(String password);
	
	public void resetConnection() {
		this.connection = null;
	}
	
	protected void setSessionCollection(AbstractSessionCollection sessions) {
		this.sessions = sessions;
	}
	
	protected void stopConnection() {
		if(this.connection != null)
			this.connection.stopConnection();
	}
	
	public void stopConnector() {
		this.running = false;
		
		this.stopConnection();
	}
	
	public AbstractSession stopSession(String session_id) {
		return (AbstractSession)this.sessions.stop(session_id);
	}
	
	public void stopSessions() {
		this.sessions.stopAll();
	}
	
}
