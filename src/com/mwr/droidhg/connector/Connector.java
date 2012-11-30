package com.mwr.droidhg.connector;

import java.util.Collection;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.SessionService;
import com.mwr.droidhg.api.ConnectorParameters;
import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Protobuf.Message;

public abstract class Connector extends Thread {

	public volatile boolean running = false;
	protected volatile Connection connection = null;
	private HashMap<String,Session> sessions = new HashMap<String,Session>();
	private SessionServiceConnection session_service_connection = null;
	
	public static class SessionServiceConnection implements ServiceConnection {
    	
    	private Messenger service = null;
    	private boolean bound = false;
    	
    	public boolean isBound() {
    		return this.bound;
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
	
	public Connector() {
		this.session_service_connection = new SessionServiceConnection();
		
		SessionService.startAndBindToService(Agent.getContext(), this.session_service_connection);
	}
	
	public boolean checkForLiveness() {
		return true;
	}
	
	protected void createConnection(Transport transport) {
		this.connection = new Connection(this, transport);
		this.connection.start();
	}
	
	public boolean dieWithLastSession() {
		return false;
	}
	
	public Session getSession(String session_id) {
		return this.sessions.get(session_id);
	}
	
	public Collection<Session> getSessions() {
		return this.sessions.values();
	}
	
	public boolean hasSessions() {
		return !this.sessions.isEmpty();
	}
	
	public boolean mustBind() {
		return true;
	}
	
	private void notifySessionStarted(Session session) {
		try {
			this.session_service_connection.send(android.os.Message.obtain(null, SessionService.MSG_START_SESSION, session.getSessionId()));
		} 
		catch (RemoteException e) {
			Log.e("connector", "failed to send session started notification");
		}
	}
	
	private void notifySessionStopped(Session session) {
		try {
			this.session_service_connection.send(android.os.Message.obtain(null, SessionService.MSG_STOP_SESSION, session.getSessionId()));
		}
		catch (RemoteException e) {
			Log.e("connector", "failed to send session stopped notification");
		}
	}
	
	public void send(Message message) {
		this.connection.send(message);
	}
	
	public Session startSession() {
		Session session = new Session(this);
		
		this.sessions.put(session.getSessionId(), session);
		session.start();
		
		this.setStatus(Status.ACTIVE);
		this.notifySessionStarted(session);
		
		return session;
	}
	
	public void resetConnection() {
		this.connection = null;
	}
	
	public abstract void setStatus(ConnectorParameters.Status status);
	
	protected void stopConnection() {
		if(this.connection != null)
			this.connection.stopConnection();
	}
	
	protected void stopConnector() {
		this.running = false;
		
		this.stopConnection();
	}
	
	public Session stopSession(String session_id) {
		Session session = this.sessions.get(session_id);
		
		if(session != null) {
			try {
				session.stopSession();
				session.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
			this.sessions.remove(session_id);
			
			this.setStatus(Status.ONLINE);
			this.notifySessionStopped(session);
		}
			
		if(this.dieWithLastSession() && !this.hasSessions())
			this.stopConnection();
		
		return session;
	}
	
	public void stopSessions() {
		String[] keys = this.sessions.keySet().toArray(new String[] {});
		
		for(String session_id : keys)
			this.stopSession(session_id);
	}

}
