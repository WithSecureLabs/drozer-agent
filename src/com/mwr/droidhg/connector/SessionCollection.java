package com.mwr.droidhg.connector;

import java.util.Collection;
import java.util.HashMap;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.SessionService;
import com.mwr.droidhg.api.ConnectorParameters.Status;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class SessionCollection {
	
	private Connector connector = null;
	private HashMap<String,Session> sessions = new HashMap<String,Session>();
	private SessionServiceConnection session_service_connection = null;
	
	public static class SessionServiceConnection implements ServiceConnection {
    	
    	private Messenger service = null;
    	private boolean bound = false;
    	
    	public boolean isBound() {
    		return this.bound;
    	}
    	
    	public void notifySessionStarted(String sessionId) throws RemoteException {
    		this.send(android.os.Message.obtain(null, SessionService.MSG_START_SESSION, sessionId));
    	}
    	
    	public void notifySessionStopped(String sessionId) throws RemoteException {
    		this.send(android.os.Message.obtain(null, SessionService.MSG_STOP_SESSION, sessionId));
    	}
    	
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		this.service = new Messenger(service);
    		this.bound = true;
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName className) {
    		this.service = null;
    		this.bound = false;
    	}
    	
    	public void send(android.os.Message msg) throws RemoteException {
    		this.service.send(msg);
    	}
    	
    	public void unbind(Context context) {
    		if(this.bound) {
    			context.unbindService(this);
    			this.bound = false;
    		}
    	}
    	
    }
	
	public SessionCollection(Connector connector) {
		this.connector = connector;
		this.session_service_connection = new SessionServiceConnection();
		
		SessionService.startAndBindToService(Agent.getInstance().getContext(), this.session_service_connection);
	}
	
	public Collection<Session> all() {
		return this.sessions.values();
	}
	
	public boolean any() {
		return !this.sessions.isEmpty();
	}
	
	public Session create() {
		Session session = new Session(this.connector);
		
		this.sessions.put(session.getSessionId(), session);
		session.start();
		
		this.connector.setStatus(Status.ACTIVE);
		
		try {
			this.getSessionService().notifySessionStarted(session.getSessionId());
		}
		catch(RemoteException e) {}
		
		return session;
	}
	
	public Session get(String session_id) {
		return this.sessions.get(session_id);
	}
	
	public SessionServiceConnection getSessionService() {
		return this.session_service_connection;
	}
	
	public Session stop(String session_id) {
		Session session = this.sessions.get(session_id);
		
		if(session != null) {
			try {
				session.stopSession();
				session.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
			this.sessions.remove(session_id);
			
			this.connector.setStatus(Status.ONLINE);
			
			try {
				this.getSessionService().notifySessionStopped(session.getSessionId());
			}
			catch(RemoteException e) {}
		}
		
		if(!this.any())
			this.connector.lastSessionStopped();
		
		return session;
	}
	
	public void stopAll() {
		String[] keys = this.sessions.keySet().toArray(new String[] {});
		
		for(String session_id : keys)
			this.stop(session_id);
	}

}
