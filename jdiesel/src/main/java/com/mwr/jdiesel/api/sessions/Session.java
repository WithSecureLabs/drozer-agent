package com.mwr.jdiesel.api.sessions;

import android.os.Looper;

import com.mwr.jdiesel.api.InvalidMessageException;
import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.handlers.MessageHandler;
import com.mwr.jdiesel.api.handlers.ReflectionMessageHandler;
import com.mwr.jdiesel.api.links.Link;
import com.mwr.jdiesel.connection.AbstractSession;
import com.mwr.jdiesel.reflection.ObjectStore;

public class Session extends AbstractSession {
	
	private Link connector = null;
	public ObjectStore object_store = new ObjectStore();
	private MessageHandler reflection_message_handler = new ReflectionMessageHandler(this);
	
	public Session(Link connector) {
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
	protected Message handleMessage(Message message) throws InvalidMessageException {
		return this.reflection_message_handler.handle(message);
	}
	
	@Override
	public void run(){
		Looper.prepare();
		
		super.run();
	}
	
	@Override
	public void send(Message message) {
		this.connector.send(message);
	}

}
