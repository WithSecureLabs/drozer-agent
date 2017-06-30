package com.mwr.jdiesel.api.builders;

import com.google.protobuf.MessageOrBuilder;
import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.Protobuf.Message.ReflectionResponse;
import com.mwr.jdiesel.api.Protobuf.Message.ReflectionResponse.ResponseStatus;
import com.mwr.jdiesel.reflection.types.ReflectedType;

public class ReflectionResponseFactory {
	
	private ReflectionResponse.Builder builder = null;
	
	public static ReflectionResponseFactory data(byte[] bytes) {
		return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setData(bytes);
	}
	
	public static ReflectionResponseFactory error(String message) {
		return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.ERROR).setErrorMessage(message);
	}
	
	public static ReflectionResponseFactory nullPointer() {
		return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setResult(Message.Argument.ArgumentType.NULL, null);
	}
	
	public static ReflectionResponseFactory object(int ref) {
		return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setObjectReference(ref);		
	}
	
	public static ReflectionResponseFactory objectArray(Object[] objects) {
		return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setObjects(objects);
	}
	
	public static ReflectionResponseFactory primitive(Object primitive) {
		if(primitive == null) {
			return nullPointer();
		}
		else {
			return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setPrimitive(primitive);
		}
	}
	
	public static ReflectionResponseFactory primitiveArray(Object primitives) {
		return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setPrimitives(primitives);
	}
	
	public static ReflectionResponseFactory send(Object value) {
		if(value == null)
			return nullPointer();
		else if(value.getClass().equals(String.class))
			return string((String)value);
		else if(value.getClass().isArray() && value instanceof String[])
			return primitiveArray((String[])value);
		else if(value.getClass().isArray() && value instanceof Object[])
			return objectArray((Object[])value);
		else if(value.getClass().isArray() && value.getClass().getComponentType() == Byte.TYPE)
			return data((byte[])value);
		else if(value.getClass().isArray())
			return primitiveArray(value);
		//else if(!primitive)
		else
			return object(value.hashCode());
	}
	
	public static ReflectionResponseFactory string(String value) {
		return new ReflectionResponseFactory(ResponseStatus.SUCCESS).setString(value);
	}
	
	public static ReflectionResponseFactory success() {
		return new ReflectionResponseFactory(ResponseStatus.SUCCESS);
	}
	
	private ReflectionResponseFactory(ResponseStatus status) {
		this.builder = ReflectionResponse.newBuilder().setStatus(status);
	}
	
	public ReflectionResponse build() {
		return this.builder.build();
	}

	private Message.Argument buildArgument(String string) {
		return Message.Argument.newBuilder().setType(Message.Argument.ArgumentType.STRING).setString(string).build();
	}
	
	private Message.Argument buildArgument(Message.Argument.ArgumentType type, MessageOrBuilder builder) {
		Message.Argument.Builder argument = Message.Argument.newBuilder().setType(type);
		
		switch(type) {
		case ARRAY:
			argument.setArray((Message.Array.Builder)builder);
			break;
			
		case OBJECT:
			argument.setObject((Message.ObjectReference.Builder)builder);
			break;
			
		case PRIMITIVE:
			argument.setPrimitive((Message.Primitive)builder);
			break;
		
		case DATA:
		case NULL:
		case STRING:
		default:
			break;
		}
		
		return argument.build();
	}
	
	private Message.Primitive buildPrimitive(Message.Primitive.PrimitiveType type, Object value) {
		Message.Primitive.Builder primitive = Message.Primitive.newBuilder().setType(type);
		
		switch(type) {
		case BOOL:
			primitive.setBool((Boolean)value);
			break;
			
		case BYTE:
			primitive.setByte((Byte)value);
			break;
			
		case DOUBLE:
			primitive.setDouble((Double)value);
			break;
			
		case FLOAT:
			primitive.setFloat((Float)value);
			break;
		
		case CHAR:
		case INT:
			primitive.setInt((Integer)value);
			break;
			
		case LONG:
			primitive.setLong((Long)value);
			break;
			
		case SHORT:
			primitive.setShort((Short)value);
			break;
		}
		
		return primitive.build();
	}
	
	public ReflectionResponseFactory isError() {
		this.builder.setStatus(ReflectionResponse.ResponseStatus.ERROR);
		
		return this;
	}
	
	public ReflectionResponseFactory isSuccess() {
		this.builder.setStatus(ReflectionResponse.ResponseStatus.SUCCESS);
		
		return this;
	}
	
	public ReflectionResponseFactory setData(byte[] bytes) {
		this.builder.setResult(ReflectedType.fromNative(bytes).getArgument());
		
		return this;
	}
	
	public ReflectionResponseFactory setErrorMessage(String error_message) {
		this.builder.setErrormessage(error_message);
		
		return this;
	}
	
	public ReflectionResponseFactory setObjectReference(int ref) {
		return this.setResult(Message.Argument.ArgumentType.OBJECT, Message.ObjectReference.newBuilder().setReference(ref));
	}
	
	public ReflectionResponseFactory setObjects(Object[] objects) {
		Message.Array.Builder array_builder = Message.Array.newBuilder().setType(Message.Array.ArrayType.OBJECT);
		
		for(Object object : objects)
			array_builder.addElement(buildArgument(Message.Argument.ArgumentType.OBJECT, Message.ObjectReference.newBuilder().setReference(object.hashCode())));
		
		return this.setResult(Message.Argument.ArgumentType.ARRAY, array_builder);
	}
	
	public ReflectionResponseFactory setPrimitive(Object primitive) {
		if(primitive instanceof Boolean)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.BOOL, primitive));
		else if(primitive instanceof Byte)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.BYTE, primitive));
		else if(primitive instanceof Character)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.CHAR, primitive));
		else if(primitive instanceof Double)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.DOUBLE, primitive));
		else if(primitive instanceof Float)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.FLOAT, primitive));
		else if(primitive instanceof Integer)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.INT, primitive));
		else if(primitive instanceof Long)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.LONG, primitive));
		else if(primitive instanceof Short)
			return this.setResult(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.SHORT, primitive));
		else
			return null;
	}
	
	public ReflectionResponseFactory setPrimitives(Object primitiveArray) {
		Message.Array.Builder array_builder = Message.Array.newBuilder().setType(Message.Array.ArrayType.PRIMITIVE);
		
		if(primitiveArray instanceof boolean[]) {
			for(boolean b : (boolean[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.BOOL, b)));
		}
		else if(primitiveArray instanceof byte[]) {
			for(byte b : (byte[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.BYTE, b)));
		}
		else if(primitiveArray instanceof char[]) {
			for(char c : (char[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.CHAR, c)));
		}
		else if(primitiveArray instanceof double[]) {
			for(double d : (double[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.DOUBLE, d)));
		}
		else if(primitiveArray instanceof float[]) {
			for(float f : (float[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.FLOAT, f)));
		}
		else if(primitiveArray instanceof int[]) {
			for(int i : (int[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.INT, i)));
		}
		else if(primitiveArray instanceof long[]) {
			for(long l : (long[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.LONG, l)));
		}
		else if(primitiveArray instanceof short[]) {
			for(short s : (short[])primitiveArray)
				array_builder.addElement(buildArgument(Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Message.Primitive.PrimitiveType.SHORT, s)));
		}
		else if(primitiveArray instanceof String[]) {
			for(String s : (String[])primitiveArray)
				array_builder.addElement(Message.Argument.newBuilder().setType(Message.Argument.ArgumentType.STRING).setString(s));
		}
		
		return this.setResult(Message.Argument.ArgumentType.ARRAY, array_builder);
	}
	
	public ReflectionResponseFactory setResult(Message.Argument.ArgumentType type, MessageOrBuilder builder) {
		this.builder.setResult(this.buildArgument(type, builder));
		
		return this;
	}
	
	public ReflectionResponseFactory setSessionId(String session_id) {
		this.builder.setSessionId(session_id);
		
		return this;
	}
	
	public ReflectionResponseFactory setString(String string) {
		this.builder.setResult(this.buildArgument(string));
		
		return this;
	}
	
}
