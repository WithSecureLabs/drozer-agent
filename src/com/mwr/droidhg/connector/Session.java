package com.mwr.droidhg.connector;

import com.mwr.cinnibar.api.Protobuf.Message;
import com.mwr.cinnibar.api.handlers.MessageHandler;
import com.mwr.cinnibar.connection.AbstractSession;
import com.mwr.cinnibar.reflection.ObjectStore;

import com.mwr.droidhg.api.ReflectionMessageHandler;

public class Session extends AbstractSession {
	
	private Connector connector = null;
	public ObjectStore object_store = new ObjectStore();
	private MessageHandler reflection_message_handler = new ReflectionMessageHandler(this);
	
	public Session(Connector connector) {
		super();
		
		this.connector = connector;
	}
	
	protected Session(String session_id) {
		super(session_id);
	}
	
	public static Session nullSession() {
		return new Session("null");
	}
	
	@Override
	protected Message handleMessage(Message message) {
		return this.reflection_message_handler.handle(message);
	}
	
	@Override
	public void send(Message message) {
		this.connector.send(message);
	}

}
