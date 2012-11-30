package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;

import android.util.Log;

import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Protobuf.Message;
import com.mwr.droidhg.api.builders.MessageFactory;
import com.mwr.droidhg.api.builders.SystemRequestFactory;
import com.mwr.droidhg.api.Handler;
import com.mwr.droidhg.api.SystemMessageHandler;

public class Connection extends Thread {
	
	private static final long LIVENESS_THRESHOLD = 30000L; // milliseconds
	
	private Connector connector = null;
	private long last_message_at = 0;
	public volatile boolean running = false;
	public volatile boolean started = false;
	private Handler system_message_handler = new SystemMessageHandler(this);
	private Transport transport = null;
	
	public Connection(Connector connector, Transport transport) {
		this.connector = connector;
		this.transport = transport;
	}
	
	private boolean bindToServer() {
		if(this.mustBind()) {
			this.send(new MessageFactory(SystemRequestFactory.bind().setDevice()).setId(1).build());
			
	//		while(true) {
				Message message = this.receive();
				
				if(message != null && 
						message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
						message.hasSystemResponse() &&
						message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
						message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.BOUND) {
					this.connector.setStatus(Status.ONLINE);
				
					return true;
				}
				// TODO: reintroduce the loop and add a timeout - we may not get a response in the first
				// socket timeout, but if we don't get a response fairly sharpish, it isn't coming
	//		}
				
			return false;
		}
		else
			return true;
	}
	
	private void checkForLiveness() {
		if(this.connector.checkForLiveness() && System.currentTimeMillis() - this.last_message_at > LIVENESS_THRESHOLD) {
			Log.i("connection", "connection was reset, no message for " + (System.currentTimeMillis() - this.last_message_at) + "ms");
			
			this.stopConnection(false);
		}
	}
	
	public Collection<Session> getSessions() {
		return this.connector.getSessions();
	}
	
	private void handleMessage(Message message) {
		try {
			Message response = null;
			
			switch(message.getType()) {
			case REFLECTION_REQUEST:
				Session session = this.connector.getSession(message.getReflectionRequest().getSessionId());
				
				if(session != null) {
					session.deliverMessage(message);
				}
				else {
					Log.e("connection", "got message for session " + message.getReflectionRequest().getSessionId() + ", which does not exist");
				}
				break;
				
			case SYSTEM_REQUEST:
				response = this.system_message_handler.handle(message);
				break;

			case REFLECTION_RESPONSE:
			case SYSTEM_RESPONSE:
			default:
				Log.e("connection", "got unexpected message type: " + message.getType().toString());
				break;
			}
			
			if(response != null)
				this.send(response);
		}
		catch(InvalidMessageException e) {
			Log.e("connection", "dropped invalid request: " + e.getMessage());
		}
		
		this.last_message_at = System.currentTimeMillis();
	}
	
	public boolean hasSessions() {
		return this.connector.hasSessions();
	}
	
	private boolean mustBind() {
		return this.connector.mustBind();
	}
	
	public Message receive() {
		try {
			Frame f = this.transport.receive();
			
			return f != null ? f.getPayload() : null;
		}
		catch(SocketTimeoutException e) {
			return null;
		}
		catch(IOException e) {
			Log.e("connection", "IOException whilst reading frame: " + e.getMessage());
			Log.d("connection", Log.getStackTraceString(e));
			
			this.stopConnection();
			
			return null;
		}
		catch(APIVersionException e) {
			Log.e("connection", "unexpected API version whilst reading frame: " + e.getMessage());
			Log.d("connection", Log.getStackTraceString(e));
			
			this.stopConnection();
			
			return null;
		}
	}
	
	public void run() {
		this.running = true;
		this.started = true;
		
		this.last_message_at = System.currentTimeMillis();
		
		Message request = null;
		
		if(!this.bindToServer())
			this.stopConnection();

		while(this.running) {
			request = this.receive();
			
			if(request != null) {
				this.handleMessage(request);
			
				request = null;
			}
			
			this.checkForLiveness();
			
			Thread.yield();
		}
		
		this.unbindFromServer();
	}
	
	public void send(Message message) {
		try {
			this.transport.send(new Frame(message));
		}
		catch(IOException e) {
			Log.e("connection", "IOException whilst transmitting frame: " + e.getMessage());
			Log.d("connection", Log.getStackTraceString(e));
			
			this.stopConnection(false);
		}
	}
	
	public Session startSession() {
		return this.connector.startSession();
	}
	
	public void stopConnection() {
		this.stopConnection(true);
	}
	
	public void stopConnection(boolean kill_sessions) {
		this.running = false;
		
		if(kill_sessions)
			this.stopSessions();
	}
	
	public Session stopSession(String session_id) {
		return this.connector.stopSession(session_id);
	}
	
	public void stopSessions() {
		this.connector.stopSessions();
	}
	
	private void unbindFromServer() {
		if(this.mustBind()) {
			this.send(new MessageFactory(SystemRequestFactory.unbind().setDevice()).setId(1).build());
			
	//		while(true) {
				Message message = this.receive();
				
				if(message != null && 
						message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
						message.hasSystemResponse() &&
						message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
						message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.UNBOUND) {
					this.connector.setStatus(Status.OFFLINE);
				}
	//		}
	//			TODO: loop, and timeouts
		}
	}

}
