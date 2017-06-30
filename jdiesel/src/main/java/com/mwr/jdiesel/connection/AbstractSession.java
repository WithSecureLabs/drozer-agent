package com.mwr.jdiesel.connection;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mwr.jdiesel.api.InvalidMessageException;
import com.mwr.jdiesel.api.Protobuf.Message;

public abstract class AbstractSession extends Thread {
	
	public interface OnSessionStatusListener {
		
		public void onSessionStarted(AbstractSession session);
		
		public void onSessionStopped(AbstractSession session);
		
	}

	private BlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
	private String session_id = null;
	public volatile boolean running = false;
	
	private Set<OnSessionStatusListener> on_session_status_listeners = new HashSet<OnSessionStatusListener>();
	
	public AbstractSession() {
		this.session_id = this.generateSessionId();
	}

	protected AbstractSession(String session_id) {
		this.session_id = session_id;
	}
	
	public void addOnSessionStatusListener(OnSessionStatusListener listener) {
		this.on_session_status_listeners.add(listener);
	}
	
	public void deliverMessage(Message message) {
		this.messages.offer(message);
	}
		
	private String generateSessionId() {
		return new BigInteger(130, new SecureRandom()).toString(32);
	}

	public String getSessionId() {
		return this.session_id;
	}
	
	protected abstract Message handleMessage(Message message) throws InvalidMessageException;
	
	public void removeOnSessionStatusListener(OnSessionStatusListener listener) {
		this.on_session_status_listeners.remove(listener);
	}
		
	@Override
	public void run() {
		this.running = true;
		
		for(OnSessionStatusListener l : this.on_session_status_listeners)
			l.onSessionStarted(this);
		
		while(this.running) {
			Message message = null;
			
			try {
				message = this.messages.take();
			}
			catch (InterruptedException e) {}
			
			if(message != null) {
				try {
					Message response = this.handleMessage(message);
					
					if(response != null)
						this.send(response);
				}
				catch(InvalidMessageException e) {}
			}
		}
		
		for(OnSessionStatusListener l : this.on_session_status_listeners)
			l.onSessionStopped(this);
	}
	
	public abstract void send(Message message);
	
	public void stopSession() {
		this.running = false;
		
		this.interrupt();
	}

}
