package com.mwr.jdiesel.connection;

import java.util.Collection;
import java.util.HashMap;

import com.mwr.jdiesel.connection.AbstractSession.OnSessionStatusListener;

public abstract class AbstractSessionCollection implements OnSessionStatusListener {
	
	private HashMap<String,AbstractSession> sessions = new HashMap<String,AbstractSession>();
	
	public Collection<AbstractSession> all() {
		return this.sessions.values();
	}

	public boolean any() {
		return !this.sessions.isEmpty();
	}

	public abstract AbstractSession create();
	
	protected AbstractSession storeSession(AbstractSession session) {
		this.sessions.put(session.getSessionId(), session);
		
		session.addOnSessionStatusListener(this);
		session.start();
			
		return session;
	}

	public AbstractSession get(String session_id) {
		return this.sessions.get(session_id);
	}

	public AbstractSession stop(String session_id) {
		AbstractSession session = this.sessions.get(session_id);

		if(session != null) {
			this.sessions.remove(session_id);
			
			try {
				session.stopSession();
				session.join();
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return session;
	}

	public void stopAll() {
		String[] keys = this.sessions.keySet().toArray(new String[] {});
		
		for(String session_id : keys)
			this.stop(session_id);
	}

}
