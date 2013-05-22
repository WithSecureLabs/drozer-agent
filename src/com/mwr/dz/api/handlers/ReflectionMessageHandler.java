package com.mwr.dz.api.handlers;

import java.util.List;

import com.mwr.cinnibar.api.InvalidMessageException;
import com.mwr.cinnibar.api.Protobuf.Message;
import com.mwr.cinnibar.api.builders.MessageFactory;
import com.mwr.cinnibar.api.builders.ReflectionResponseFactory;
import com.mwr.cinnibar.api.handlers.AbstractReflectionMessageHandler;
import com.mwr.cinnibar.reflection.Reflector;
import com.mwr.cinnibar.reflection.types.ReflectedType;

import com.mwr.dz.connector.Session;

public class ReflectionMessageHandler extends AbstractReflectionMessageHandler {
	
	private Session session = null;
	
	public ReflectionMessageHandler(Session session) {
		this.session = session;
	}
	
	private Message createResponse(Message request, ReflectionResponseFactory response_factory) {
		return new MessageFactory(response_factory.setSessionId(this.session.getSessionId())).inReplyTo(request).build();
	}
	
	@Override
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
	
	@Override
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
	
	@Override
	protected Message handleDeleteAll(Message message) throws InvalidMessageException {
		this.session.object_store.clear();
		
		return this.createResponse(message, ReflectionResponseFactory.success());
	}
	
	@Override
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
	
	@Override
	protected Message handleError(Message request, String error_message) {
		return this.createResponse(request, ReflectionResponseFactory.error(error_message));
	}
	
	@Override
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
	
	@Override
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
	
	@Override
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
