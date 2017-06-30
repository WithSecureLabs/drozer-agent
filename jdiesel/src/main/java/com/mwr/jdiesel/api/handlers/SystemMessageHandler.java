package com.mwr.jdiesel.api.handlers;

import com.mwr.jdiesel.api.DeviceInfo;
import com.mwr.jdiesel.api.InvalidMessageException;
import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.builders.MessageFactory;
import com.mwr.jdiesel.api.builders.SystemResponseFactory;
import com.mwr.jdiesel.api.connectors.Connection;
import com.mwr.jdiesel.api.sessions.Session;

public class SystemMessageHandler implements MessageHandler {
	
	private Connection connection = null;
	private DeviceInfo device_info;
	
	public SystemMessageHandler(Connection connection, DeviceInfo device_info) {
		this.connection = connection;
		this.device_info = device_info;
	}
	
	@Override
	public Message handle(Message message) throws InvalidMessageException {
		if(message.getType() != Message.MessageType.SYSTEM_REQUEST)
			throw new InvalidMessageException(message);
		if(!message.hasSystemRequest())
			throw new InvalidMessageException(message);
		
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
			throw new InvalidMessageException(message);
		}
	}
	
	protected Message handleListDevices(Message message) throws InvalidMessageException {
		MessageFactory factory = new MessageFactory(SystemResponseFactory.deviceList(message).addDevice(
				this.device_info.getAndroidID(),
				this.device_info.getManufacturer(),
				this.device_info.getModel(),
				this.device_info.getSoftware()));
		
		factory.inReplyTo(message);
		
		return factory.build();
	}
	
	protected Message handleListSessions(Message message) throws InvalidMessageException {
		SystemResponseFactory response = SystemResponseFactory.sessionList(message);
		
		//for(Session session : this.connection.getSessions())
		//	response.addSession(session);
		
		MessageFactory factory = new MessageFactory(response);
		
		factory.inReplyTo(message);
		
		return factory.build();
	}
	
	protected Message handlePing(Message message) throws InvalidMessageException {
		MessageFactory factory = new MessageFactory(SystemResponseFactory.pong(message));
		
		factory.inReplyTo(message);
		
		return factory.build();
	}
	
	protected Message startSession(Message message) throws InvalidMessageException {
		Session session = (Session)this.connection.startSession(message.getSystemRequest().getPassword());
		
		if(session != null) {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(session.getSessionId()));
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
		else {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(Session.nullSession().getSessionId()).isError());
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
	}
	
	protected Message stopSession(Message message) throws InvalidMessageException {
		Session session = (Session)this.connection.stopSession(message.getSystemRequest().getSessionId());
		
		if(session != null) {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(session.getSessionId()));
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
		else {
			MessageFactory factory = new MessageFactory(SystemResponseFactory.session(Session.nullSession().getSessionId()).isError());
			
			factory.inReplyTo(message);
			
			return factory.build();
		}
	}
	
}
