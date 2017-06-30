package com.mwr.jdiesel.api.handlers;

import java.util.List;


import com.mwr.jdiesel.api.InvalidMessageException;
import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.builders.MessageFactory;
import com.mwr.jdiesel.api.builders.ReflectionResponseFactory;
import com.mwr.jdiesel.api.sessions.Session;
import com.mwr.jdiesel.reflection.Reflector;
import com.mwr.jdiesel.reflection.types.ReflectedType;

public class ReflectionMessageHandler implements MessageHandler {
	
	private Session session = null;
	
	public ReflectionMessageHandler(Session session) {
		this.session = session;
	}
	
	private Message createResponse(Message request, ReflectionResponseFactory response_factory) {
		return new MessageFactory(response_factory.setSessionId(this.session.getSessionId())).inReplyTo(request).build();
	}
	
	@Override
	public Message handle(Message message) throws InvalidMessageException {
		if(message.getType() != Message.MessageType.REFLECTION_REQUEST)
			throw new InvalidMessageException(message);
		if(!message.hasReflectionRequest())
			throw new InvalidMessageException(message);
		
		try {
			switch(message.getReflectionRequest().getType()) {
			case CONSTRUCT:
				if(!message.getReflectionRequest().hasConstruct())
					throw new InvalidMessageException(message);
				
				return this.handleConstruct(message);
				
			case DELETE:
				if(!message.getReflectionRequest().hasDelete())
					throw new InvalidMessageException(message);
				
				return this.handleDelete(message);
				
			case DELETE_ALL:
				return this.handleDeleteAll(message);
				
			case GET_PROPERTY:
				if(!message.getReflectionRequest().hasGetProperty())
					throw new InvalidMessageException(message);
				
				return this.handleGetProperty(message);
				
			case INVOKE:
				if(!message.getReflectionRequest().hasInvoke())
					throw new InvalidMessageException(message);
				
				return this.handleInvoke(message);
				
			case RESOLVE:
				if(!message.getReflectionRequest().hasResolve())
					throw new InvalidMessageException(message);
				
				return this.handleResolve(message);
						
			case SET_PROPERTY:
				if(!message.getReflectionRequest().hasSetProperty())
					throw new InvalidMessageException(message);
				
				return this.handleSetProperty(message);
			
			default:
				throw new InvalidMessageException(message);
			}
		}
		catch(Exception e) {
			if(e.getMessage() != null)
				return this.handleError(message, e.getMessage());
			else if(e.getCause() != null && e.getCause().getMessage() != null)
				return this.handleError(message, e.getCause().getMessage());
			else
				return this.handleError(message, "Unknown Exception");
		}
	}
	
	protected Message handleConstruct(Message message) throws InvalidMessageException {
		Object klass = this.session.object_store.get(message.getReflectionRequest().getConstruct().getObject().getReference());
		
		if(klass != null) {
			ReflectedType[] arguments = this.parseArguments(message.getReflectionRequest().getConstruct().getArgumentList());
			
			try {
				Object object = Reflector.construct((Class<?>)klass, arguments);
				int ref = this.session.object_store.put(object);
				
				return this.createResponse(message, ReflectionResponseFactory.object(ref));
			}
			catch(Exception e) {
				return this.handleError(message, e);
			}
		}
		else {
			return this.handleError(message, "cannot find object " + message.getReflectionRequest().getConstruct().getObject().getReference()); 
		}
	}
	
	protected Message handleDelete(Message message) throws InvalidMessageException {
		Object object = this.session.object_store.get(message.getReflectionRequest().getDelete().getObject().getReference());
		
		if(object != null) {
			this.session.object_store.remove(object.hashCode());
				
			return this.createResponse(message, ReflectionResponseFactory.success());
		}
		else {
			return this.handleError(message, "cannot find object " + message.getReflectionRequest().getDelete().getObject().getReference()); 
		}
	}
	
	protected Message handleDeleteAll(Message message) throws InvalidMessageException {
		this.session.object_store.clear();
		
		return this.createResponse(message, ReflectionResponseFactory.success());
	}
	
	protected Message handleGetProperty(Message message) throws InvalidMessageException {
		Object object = this.session.object_store.get(message.getReflectionRequest().getGetProperty().getObject().getReference());
		
		if(object != null) {
			try {
				Object value = Reflector.getProperty(object, message.getReflectionRequest().getGetProperty().getProperty());
				
				if(Reflector.isPropertyPrimitive(object, message.getReflectionRequest().getGetProperty().getProperty()))
					return this.createResponse(message, ReflectionResponseFactory.primitive(value));
				else {
					if(value != null && this.shouldPutInStore(value))
						this.session.object_store.put(value);
					
					return this.createResponse(message, ReflectionResponseFactory.send(value));
				}
			}
			catch(Exception e) {
				return this.handleError(message, e);
			}
		}
		else {
			return this.handleError(message, "cannot find object " + message.getReflectionRequest().getGetProperty().getObject().getReference()); 
		}
	}
	
	protected Message handleError(Message request, Throwable tr) {
		if(tr.getCause() != null)
			return this.handleError(request, tr.getCause().getMessage());
		else
			return this.handleError(request, tr.getMessage());
	}
	
	protected Message handleError(Message request, String error_message) {
		return this.createResponse(request, ReflectionResponseFactory.error(error_message));
	}
	
	protected Message handleInvoke(Message message) throws InvalidMessageException {
		Object object = this.session.object_store.get(message.getReflectionRequest().getInvoke().getObject().getReference());
		
		if(object != null) {
			String method_name = message.getReflectionRequest().getInvoke().getMethod();
			ReflectedType[] arguments = this.parseArguments(message.getReflectionRequest().getInvoke().getArgumentList());
			
			try {
				if(Reflector.isMethodReturnPrimitive(object, method_name, arguments))
					return this.createResponse(message, ReflectionResponseFactory.primitive(Reflector.invoke(object, method_name, arguments)));
				else {
					Object result = Reflector.invoke(object, method_name, arguments);
					
					if(result != null && this.shouldPutInStore(result))
						this.session.object_store.put(result);
					
					return this.createResponse(message, ReflectionResponseFactory.send(result));
				}
			}
			catch(Exception e) {
				return this.handleError(message, e);
			}
		}
		else {
			return this.handleError(message, "cannot find object " + message.getReflectionRequest().getInvoke().getObject().getReference());
		}
	}
	
	protected Message handleResolve(Message message) throws InvalidMessageException {
		Class<?> klass = Reflector.resolve(message.getReflectionRequest().getResolve().getClassname());
		
		if(klass != null) {
			int ref = this.session.object_store.put(klass);
		
			return this.createResponse(message,	ReflectionResponseFactory.object(ref));
		}
		else {
			return this.handleError(message, "cannot resolve " + message.getReflectionRequest().getResolve().getClassname());
		}
	}
	
	protected Message handleSetProperty(Message message) throws InvalidMessageException {
		Object object = this.session.object_store.get(message.getReflectionRequest().getSetProperty().getObject().getReference());
		
		if(object != null) {
			try {
				Reflector.setProperty(object, message.getReflectionRequest().getSetProperty().getProperty(), ReflectedType.fromArgument(message.getReflectionRequest().getSetProperty().getValue(), this.session.object_store));
				
				return this.createResponse(message, ReflectionResponseFactory.success());
			}
			catch(Exception e) {
				return this.handleError(message, e);
			}
		}
		else {
			return this.handleError(message, "cannot find object " + message.getReflectionRequest().getSetProperty().getObject().getReference()); 
		}
	}
	
	private ReflectedType[] parseArguments(List<Message.Argument> arguments) {
		ReflectedType[] resolved = new ReflectedType[arguments.size()];
		
		for(int i=0; i<arguments.size(); i++)
			resolved[i] = ReflectedType.fromArgument(arguments.get(i), this.session.object_store);
		
		return resolved;
	}

	private boolean shouldPutInStore(Object obj) {
		return !(obj.getClass().isPrimitive() ||
				obj.getClass() == String.class ||
				obj.getClass().isArray() && obj.getClass().getComponentType().isPrimitive() ||
				obj.getClass().isArray() && obj.getClass() == String.class);
	}

}
