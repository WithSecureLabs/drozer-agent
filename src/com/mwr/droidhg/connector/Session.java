package com.mwr.droidhg.connector;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.mwr.droidhg.api.Handler;
import com.mwr.droidhg.api.ReflectionMessageHandler;
import com.mwr.droidhg.api.Protobuf.Message;
import com.mwr.droidhg.reflection.ObjectStore;

public class Session extends Thread {
	
	private Connector connector = null;
	private BlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
	public ObjectStore object_store = new ObjectStore();
	private String session_id = null;
	private static SecureRandom random = new SecureRandom();
	private Handler reflection_message_handler = new ReflectionMessageHandler(this);
	public volatile boolean running = false;
	
	public Session(Connector connector) {
		this.connector = connector;
		this.session_id = new BigInteger(130, Session.random).toString(32);
	}
	
	private Session(String session_id) {
		this.session_id = session_id;
	}
	
	public void deliverMessage(Message message) {
		this.messages.offer(message);
	}
	
	public String getSessionId() {
		return this.session_id;
	}
	
	public static Session nullSession() {
		return new Session("null");
	}
	
	@Override
	public void run() {
		this.running = true;
		
		while(this.running) {
			Message message = null;
			
			try {
				message = this.messages.take();
			}
			catch (InterruptedException e) {}
			
			if(message != null) {
				try {
					Message response = this.reflection_message_handler.handle(message);
					
					if(response != null) {
						this.send(response);
					}
				}
				catch(InvalidMessageException e) {
					Log.e("session - " + this.session_id, "dropped invalid request: " + e.getMessage());
				}
			}
		}
	}
	
	public void send(Message message) {
		this.connector.send(message);
	}
	
	public void stopSession() {
		this.running = false;
		
		this.interrupt();
	}

}
