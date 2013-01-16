package com.mwr.droidhg.api;

import com.mwr.cinnibar.api.Protobuf.Message;
import com.mwr.droidhg.api.builders.MessageFactory;
import com.mwr.droidhg.api.builders.SystemResponseFactory;
import com.mwr.droidhg.connector.Connection;
import com.mwr.droidhg.connector.InvalidMessageException;
import com.mwr.droidhg.connector.Session;

public class SystemMessageHandler implements Handler {
	
	private Connection connection = null;
	
	public SystemMessageHandler(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Message handle(Message message) throws InvalidMessageException {
		if(message.getType() != Message.MessageType.SYSTEM_REQUEST)
			throw new InvalidMessageException("is not a SYSTEM_REQUEST", message);
		if(!message.hasSystemRequest())
			throw new InvalidMessageException("does not contain a SYSTEM_REQUEST", message);
		
		switch(message.getSystemRequest().getType()) {
		case LIST_DEVICES:
			return this.handleListDevices(message);
		
		case LIST_SESSIONS:
			return this.handleListSessions(message);
			
		case PING:
			return this.handlePing(message);
		
		case START_SESSION:
			return this.startSession(message);
			
		case STOP_SESSION:
			return this.stopSession(message);
			
		default:
			throw new InvalidMessageException("unhandled SYSTEM_REQUEST type: " + message.getSystemRequest().getType().toString(), message);
		}
	}
	
	private Message handleListDevices(Message message) throws InvalidMessageException {
		MessageFactory factory = new MessageFactory(SystemResponseFactory.deviceList(message));
		
		factory.inReplyTo(message);
		
		return factory.build();
	}
	
	private Message handleListSessions(Message message) throws InvalidMessageException {
		SystemResponseFactory response = SystemResponseFactory.sessionList(message);
		
		//for(Session session : this.connection.getSessions())
		//	response.addSession(session);
		
		MessageFactory factory = new MessageFactory(response);
		
		factory.inReplyTo(message);
		
		return factory.build();
	}
	
	private Message handlePing(Message message) throws InvalidMessageException {
		MessageFactory factory = new MessageFactory(SystemResponseFactory.pong(message));
		
		factory.inReplyTo(message);
		
		return factory.build();
	}
	
	private Message startSession(Message message) throws InvalidMessageException {
		Session session = this.connection.startSession(message.getSystemRequest().getPassword());
		
		if(session != null) {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(session));
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
		else {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(Session.nullSession()).isError());
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
	}
	
	private Message stopSession(Message message) throws InvalidMessageException {
		Session session = this.connection.stopSession(message.getSystemRequest().getSessionId());
		
		if(session != null) {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(session));
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
		else {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(Session.nullSession()).isError());
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
	}
	
}
