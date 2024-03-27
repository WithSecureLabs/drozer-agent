package com.WithSecure.jsolar.api.sessions;

import com.WithSecure.jsolar.api.connectors.Connector;
import com.WithSecure.jsolar.api.links.Link;
import com.WithSecure.jsolar.connection.AbstractSession;
import com.WithSecure.jsolar.connection.AbstractSessionCollection;

public class SessionCollection extends AbstractSessionCollection {

    private Link connector = null;
//	private SessionServiceConnection session_service_connection = null;

    public SessionCollection(Link connector) {
        this.connector = connector;
//		this.session_service_connection = new SessionServiceConnection();

//		SessionService.startAndBindToService(Agent.getInstance().getMercuryContext(), this.session_service_connection);
    }

    @Override
    public Session create() {
        return (Session)this.storeSession(new Session(this.connector));
    }

//	public SessionServiceConnection getSessionService() {
//		return this.session_service_connection;
//	}

    @Override
    public void onSessionStarted(AbstractSession session) {
        this.connector.setStatus(Connector.Status.ACTIVE);

//		this.getSessionService().notifySessionStarted(session.getSessionId());
    }

    @Override
    public void onSessionStopped(AbstractSession session) {
//		this.getSessionService().notifySessionStopped(session.getSessionId());
        this.connector.setStatus(Connector.Status.ONLINE);

        if(!this.any())
            this.connector.lastSessionStopped();
    }

}
