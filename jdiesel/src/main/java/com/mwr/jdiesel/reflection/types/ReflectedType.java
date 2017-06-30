package com.mwr.jdiesel.reflection.types;

import com.mwr.jdiesel.api.Protobuf.Message.Argument;
import com.mwr.jdiesel.reflection.ObjectStore;

public abstract class ReflectedType {
	
	public static ReflectedType fromArgument(Argument argument, ObjectStore object_store) {
		switch(argument.getType()) {
		case ARRAY:		return new ReflectedArray(argument.getArray(), object_store);
		case DATA:		return new ReflectedBinary(argument.getData());
		case OBJECT:	return new ReflectedObject(argument.getObject(), object_store);
		case PRIMITIVE:	return new ReflectedPrimitive(argument.getPrimitive());
		case STRING:	return new ReflectedString(argument.getString());
		case NULL:		return new ReflectedNull();
		default:		return null;
		}
	}
	
	public static ReflectedType fromNative(Object object) {
		if(object == null)
			return new ReflectedNull();
		else if(object instanceof Boolean)
			return new ReflectedPrimitive((Boolean)object);
		else if(object instanceof Byte)
			return new ReflectedPrimitive((Byte)object);
		else if(object instanceof Character)
			return new ReflectedPrimitive((Character)object);
		else if(object instanceof Double)
			return new ReflectedPrimitive((Double)object);
		else if(object instanceof Float)
			return new ReflectedPrimitive((Float)object);
		else if(object instanceof Integer)
			return new ReflectedPrimitive((Integer)object);
		else if(object instanceof Long)
			return new ReflectedPrimitive((Long)object);
		else if(object instanceof Short)
			return new ReflectedPrimitive((Short)object);
		else if(object instanceof String)
			return new ReflectedString((String)object);
		else if(object.getClass().isArray() && object.getClass().getComponentType() == Byte.TYPE)
			return ReflectedBinary.fromNative((byte[])object);
		else if(object.getClass().isArray())
			return ReflectedArray.fromNative((Object[])object);
		else
			return new ReflectedObject(object);
	}

	public abstract Argument getArgument();
	public abstract Object getNative();
	public abstract Class<?> getType();

}
