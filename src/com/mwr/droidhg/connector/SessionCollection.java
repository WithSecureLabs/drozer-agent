package com.mwr.droidhg.connector;

import com.mwr.cinnibar.connection.AbstractSession;
import com.mwr.cinnibar.connection.AbstractSessionCollection;
import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.service_connectors.SessionServiceConnection;
import com.mwr.droidhg.agent.services.SessionService;
import com.mwr.droidhg.connector.ConnectorParameters.Status;

public class SessionCollection extends AbstractSessionCollection {
	
	private Connector connector = null;
	private SessionServiceConnection session_service_connection = null;
	
	public SessionCollection(Connector connector) {
		this.connector = connector;
		this.session_service_connection = new SessionServiceConnection();
		
		SessionService.startAndBindToService(Agent.getInstance().getMercuryContext(), this.session_service_connection);
	}
	
	@Override
	public Session create() {
		return (Session)this.storeSession(new Session(this.connector));
	}
	
	public SessionServiceConnection getSessionService() {
		return this.session_service_connection;
	}
	
	@Override
	protected void onSessionStarted(AbstractSession session) {
		this.connector.setStatus(Status.ACTIVE);
		
		this.getSessionService().notifySessionStarted(session.getSessionId());
	}
	
	@Override
	protected void onSessionStopped(AbstractSession session) {
		this.connector.setStatus(Status.ONLINE);
		
		this.getSessionService().notifySessionStopped(session.getSessionId());
		
		if(!this.any())
			this.connector.lastSessionStopped();
	}
	
}
