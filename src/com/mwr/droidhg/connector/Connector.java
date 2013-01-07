package com.mwr.droidhg.connector;

import java.util.Collection;

import com.mwr.common.logging.LogMessage;
import com.mwr.common.logging.Logger;
import com.mwr.droidhg.api.ConnectorParameters;
import com.mwr.droidhg.api.Protobuf.Message;

public abstract class Connector extends Thread {

	public volatile boolean running = false;
	protected volatile Connection connection = null;
	protected ConnectorParameters parameters = null;
	private SessionCollection sessions = null;
	
	private Logger logger = null;
	
	public Connector(ConnectorParameters parameters) {
		this.parameters = parameters;
		this.sessions = new SessionCollection(this);
	}
	
	public abstract void setStatus(ConnectorParameters.Status status);
	
	public boolean checkForLiveness() { return true; }
	public boolean dieWithLastSession() { return false; }
	public boolean mustBind() { return true; }
	
	protected void createConnection(Transport transport) {
		if(transport.isLive()) {
			this.connection = new Connection(this, transport);
			this.connection.start();
		}
	}
	
	public Session getSession(String session_id) {
		return this.sessions.get(session_id);
	}
	
	public Collection<Session> getSessions() {
		return this.sessions.all();
	}
	
	public boolean hasSessions() {
		return this.sessions.any();
	}
	
	public void lastSessionStopped() {
		if(this.dieWithLastSession())
			this.stopConnection();
	}
	
	public void log(String message) {
		this.log(new LogMessage(message));
	}
	public void log(LogMessage message) {
		if(this.logger != null)
			this.logger.log(this.parameters, message);
	}
	
	public void send(Message message) {
		this.connection.send(message);
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public Session startSession() {
		return this.startSession(null);
	}
	
	public Session startSession(String password) {
		if(this.parameters.verifyPassword(password))
			return this.sessions.create();
		else
			return null;
	}
	
	public void resetConnection() {
		this.connection = null;
	}
	
	protected void stopConnection() {
		if(this.connection != null)
			this.connection.stopConnection();
	}
	
	public void stopConnector() {
		this.running = false;
		
		this.log("Stopping.");
		this.stopConnection();
	}
	
	public Session stopSession(String session_id) {
		return this.sessions.stop(session_id);
	}
	
	public void stopSessions() {
		this.sessions.stopAll();
	}

}
