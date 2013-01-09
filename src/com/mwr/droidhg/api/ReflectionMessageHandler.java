package com.mwr.droidhg.api;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.mwr.droidhg.api.Protobuf.Message;
import com.mwr.droidhg.api.builders.MessageFactory;
import com.mwr.droidhg.api.builders.ReflectionResponseFactory;
import com.mwr.droidhg.connector.InvalidMessageException;
import com.mwr.droidhg.connector.Session;
import com.mwr.droidhg.reflection.ReflectedType;
import com.mwr.droidhg.reflection.Reflector;

public class ReflectionMessageHandler implements Handler {
	
	private Session session = null;
	
	public ReflectionMessageHandler(Session session) {
		this.session = session;
	}

	@Override
	public Message handle(Message message) throws InvalidMessageException {
		if(message.getType() != Message.MessageType.REFLECTION_REQUEST)
			throw new InvalidMessageException("is not a REFLECTION_REQUEST", message);
		if(!message.hasReflectionRequest())
			throw new InvalidMessageException("does not contain a REFLECTION_REQUEST", message);
	
		ReflectionResponseFactory response = null;
		
		try {
			switch(message.getReflectionRequest().getType()) {
			case CONSTRUCT:
				if(!message.getReflectionRequest().hasConstruct())
					throw new InvalidMessageException("expected a CONSTRUCT message to contain a target to construct", message);
				
				response = this.construct(message.getReflectionRequest());
				break;
				
			case DELETE:
				if(!message.getReflectionRequest().hasDelete())
					throw new InvalidMessageException("expected a DELETE message to contain a target to delete", message);
				
				response = this.delete(message.getReflectionRequest());
				break;
				
			case DELETE_ALL:
				response = this.deleteAll(message.getReflectionRequest());
				break;
				
			case GET_PROPERTY:
				if(!message.getReflectionRequest().hasGetProperty())
					throw new InvalidMessageException("expected a GET_PROPERTY message to contain a target to get", message);
				
				response = this.getProperty(message.getReflectionRequest());
				break;
				
			case INVOKE:
				if(!message.getReflectionRequest().hasInvoke())
					throw new InvalidMessageException("expected an INVOKE message to contain a target to invoke", message);
				
				response = this.invoke(message.getReflectionRequest());
				break;
				
			case RESOLVE:
				if(!message.getReflectionRequest().hasResolve())
					throw new InvalidMessageException("expected a RESOLVE message to contain a target to resolve", message);
				
				response = this.resolve(message.getReflectionRequest());
				break;
						
			case SET_PROPERTY:
				if(!message.getReflectionRequest().hasSetProperty())
					throw new InvalidMessageException("expected a SET_PROPERTY message to contain a target to set", message);
				
				response = this.setProperty(message.getReflectionRequest());
				break;
			
			default:
				throw new InvalidMessageException("unhandled REFLECTION_REQUEST type: " + message.getReflectionRequest().getType().toString(), message);
			}
		}
		catch(Exception e) {
			if(e.getMessage() != null) {
				response = ReflectionResponseFactory.error(e.getMessage());
			}
			else if(e.getCause() != null && e.getCause().getMessage() != null) {
				response = ReflectionResponseFactory.error(e.getCause().getMessage());
			}
			else {
				response = ReflectionResponseFactory.error("Unknown Exception");
			}
		}
			
		if(response != null) {
			return new MessageFactory(response.setSession(this.session)).inReplyTo(message).build();
		}
		else {
			return null;
		}
	}
	
	public ReflectionResponseFactory construct(Message.ReflectionRequest request) throws InvalidMessageException {
		Object klass = this.session.object_store.get(request.getConstruct().getObject().getReference());
		
		if(klass != null) {
			ReflectedType[] arguments = this.parseArguments(request.getConstruct().getArgumentList());
			
			try {
				Object object = Reflector.construct((Class<?>)klass, arguments);
				int ref = this.session.object_store.put(object);
				
				return ReflectionResponseFactory.object(ref);
			}
			catch(IllegalAccessException e) {
				return this.handleException(e);
			}
			catch(InstantiationException e) {
				return this.handleException(e);
			}
			catch(InvocationTargetException e) {
				return this.handleException(e.getCause());
			}
			catch(NoSuchMethodException e) {
				return this.handleException(e); 
			}
		}
		else {
			return ReflectionResponseFactory.error("cannot find object " + request.getConstruct().getObject().getReference()); 
		}
	}
	
	public ReflectionResponseFactory delete(Message.ReflectionRequest request) throws InvalidMessageException {
		Object object = this.session.object_store.get(request.getDelete().getObject().getReference());
		
		if(object != null) {
			this.session.object_store.remove(object.hashCode());
				
			return ReflectionResponseFactory.success();
		}
		else {
			return ReflectionResponseFactory.error("cannot find object " + request.getDelete().getObject().getReference()); 
		}
	}
	
	public ReflectionResponseFactory deleteAll(Message.ReflectionRequest request) throws InvalidMessageException {
		this.session.object_store.clear();
		
		return ReflectionResponseFactory.success();
	}
	
	public ReflectionResponseFactory getProperty(Message.ReflectionRequest request) throws InvalidMessageException {
		Object object = this.session.object_store.get(request.getGetProperty().getObject().getReference());
		
		if(object != null) {
			try {
				Object value = Reflector.getProperty(object, request.getGetProperty().getProperty());
				
				if(Reflector.isPropertyPrimitive(object, request.getGetProperty().getProperty()))
					return ReflectionResponseFactory.primitive(value);
				else {
					if(value != null && this.shouldPutInStore(value))
						this.session.object_store.put(value);
					
					return ReflectionResponseFactory.send(value);
				}
			}
			catch(IllegalAccessException e) {
				return this.handleException(e);
			}
			catch(NoSuchFieldException e) {
				return this.handleExceptionSilently(e);
			}
		}
		else {
			return ReflectionResponseFactory.error("cannot find object " + request.getGetProperty().getObject().getReference()); 
		}
	}
	
	private ReflectionResponseFactory handleException(Throwable tr) {
		return ReflectionResponseFactory.error(tr.toString());
	}
	
	private ReflectionResponseFactory handleExceptionSilently(Throwable tr) {
		return ReflectionResponseFactory.error(tr.toString());
	}
	
	public ReflectionResponseFactory invoke(Message.ReflectionRequest request) throws InvalidMessageException {
		Object object = this.session.object_store.get(request.getInvoke().getObject().getReference());
		
		if(object != null) {
			String method_name = request.getInvoke().getMethod();
			ReflectedType[] arguments = this.parseArguments(request.getInvoke().getArgumentList());
			
			try {
				if(Reflector.isMethodReturnPrimitive(object, method_name, arguments))
					return ReflectionResponseFactory.primitive(Reflector.invoke(object, method_name, arguments));
				else {
					Object result = Reflector.invoke(object, method_name, arguments);
					
					if(result != null && this.shouldPutInStore(result))
						this.session.object_store.put(result);
					
					return ReflectionResponseFactory.send(result);
				}
			}
			catch (IllegalAccessException e) {
				return this.handleException(e);
			}
			catch (IllegalArgumentException e) {
				return this.handleException(e);
			}
			catch (NoSuchMethodException e) {
				return this.handleException(e);
			}
			catch (InvocationTargetException e) {
				return this.handleException(e.getCause());
			}
		}
		else {
			return ReflectionResponseFactory.error("cannot find object " + request.getInvoke().getObject().getReference());
		}
	}
	
	private ReflectedType[] parseArguments(List<Message.Argument> arguments) {
		ReflectedType[] resolved = new ReflectedType[arguments.size()];
		
		for(int i=0; i<arguments.size(); i++)
			resolved[i] = ReflectedType.fromArgument(arguments.get(i), this.session.object_store);
		
		return resolved;
	}
	
	public ReflectionResponseFactory resolve(Message.ReflectionRequest request) throws InvalidMessageException {
		Class<?> klass = Reflector.resolve(request.getResolve().getClassname());
		
		if(klass != null) {
			int ref = this.session.object_store.put(klass);
		
			return ReflectionResponseFactory.object(ref);
		}
		else {
			return ReflectionResponseFactory.error("cannot resolve " + request.getResolve().getClassname());
		}
	}
	
	public ReflectionResponseFactory setProperty(Message.ReflectionRequest request) throws InvalidMessageException {
		Object object = this.session.object_store.get(request.getSetProperty().getObject().getReference());
		
		if(object != null) {
			try {
				Reflector.setProperty(object, request.getSetProperty().getProperty(), ReflectedType.fromArgument(request.getSetProperty().getValue(), this.session.object_store));
				
				return ReflectionResponseFactory.success();
			}
			catch(IllegalArgumentException e) {
				return this.handleException(e);
			}
			catch(IllegalAccessException e) {
				return this.handleException(e);
			}
			catch(NoSuchFieldException e) {
				return this.handleException(e);
			}
		}
		else {
			return ReflectionResponseFactory.error("cannot find object " + request.getSetProperty().getObject().getReference()); 
		}
	}

	private boolean shouldPutInStore(Object obj) {
		return !(obj.getClass().isPrimitive() ||
				obj.getClass() == String.class ||
				obj.getClass().isArray() && obj.getClass().getComponentType().isPrimitive() ||
				obj.getClass().isArray() && obj.getClass() == String.class);
	}

}
