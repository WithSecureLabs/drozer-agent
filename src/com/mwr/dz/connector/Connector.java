package com.mwr.dz.connector;


import com.mwr.common.logging.LogMessage;
import com.mwr.common.logging.Logger;
import com.mwr.jdiesel.api.transport.Transport;
import com.mwr.jdiesel.connection.AbstractConnector;


public abstract class Connector extends AbstractConnector {
	
	protected ConnectorParameters parameters = null;
	
	private Logger logger = null;
	
	public Connector(ConnectorParameters parameters) {
		this.parameters = parameters;
		this.setSessionCollection(new SessionCollection(this));
	}
	
	public abstract void setStatus(ConnectorParameters.Status status);
	
	@Override
	protected void createConnection(Transport transport) {
		if(transport.isLive()) {
			this.connection = new Connection(this, transport);
			this.connection.start();
		}
	}
	
	@Override
	public Session getSession(String session_id) {
		return (Session)super.getSession(session_id);
	}
	
	public void log(String message) {
		this.log(new LogMessage(message));
	}

	public void log(int level, String message) {
		this.log(new LogMessage(level, message));		
	}
	
	public void log(LogMessage message) {
		if(this.logger != null)
			this.logger.log(this.parameters, message);
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public Session startSession(String password) {
		if(this.parameters.verifyPassword(password))
			return (Session)this.createSession();
		else
			return null;
	}

}
