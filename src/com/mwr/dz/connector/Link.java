package com.mwr.dz.connector;

import com.mwr.jdiesel.api.connectors.Connector;
import com.mwr.jdiesel.api.transport.Transport;
import com.mwr.jdiesel.connection.AbstractLink;
import com.mwr.jdiesel.logger.LogMessage;
import com.mwr.jdiesel.logger.Logger;


public abstract class Link extends AbstractLink {
	
	protected Connector parameters = null;
	
	private Logger logger = null;
	
	public Link(Connector parameters) {
		this.parameters = parameters;
		this.setSessionCollection(new SessionCollection(this));
	}
	
	public abstract void setStatus(Connector.Status status);
	
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

	public void log(int level, String message) {
		this.logger.log(level, message);		
	}
	
	public void log(LogMessage message) {
		this.logger.log(message);
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
