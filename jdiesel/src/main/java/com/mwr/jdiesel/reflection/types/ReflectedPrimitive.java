package com.mwr.jdiesel.reflection.types;

import com.mwr.jdiesel.api.Protobuf.Message.Argument;
import com.mwr.jdiesel.api.Protobuf.Message.Primitive;

public class ReflectedPrimitive extends ReflectedType {
	
	private Primitive.PrimitiveType type;
	private Object value;
	
	public ReflectedPrimitive(Boolean b) {
		this.type = Primitive.PrimitiveType.BOOL;
		this.value = b;
	}
	
	public ReflectedPrimitive(Byte b) {
		this.type = Primitive.PrimitiveType.BYTE;
		this.value = b;
	}
	
	public ReflectedPrimitive(Character c) {
		this.type = Primitive.PrimitiveType.CHAR;
		this.value = c;
	}
	
	public ReflectedPrimitive(Double d) {
		this.type = Primitive.PrimitiveType.DOUBLE;
		this.value = d;
	}
	
	public ReflectedPrimitive(Float f) {
		this.type = Primitive.PrimitiveType.FLOAT;
		this.value = f;
	}
	
	public ReflectedPrimitive(Integer i) {
		this.type = Primitive.PrimitiveType.INT;
		this.value = i;
	}
	
	public ReflectedPrimitive(Long l) {
		this.type = Primitive.PrimitiveType.LONG;
		this.value = l;
	}
	
	public ReflectedPrimitive(Short s) {
		this.type = Primitive.PrimitiveType.SHORT;
		this.value = s;
	}
	
	public ReflectedPrimitive(Primitive primitive) {
		this.type = primitive.getType();
		
		switch(this.type) {
		case BOOL:
			this.value = primitive.getBool();
			break;
			
		case BYTE:
			this.value = new Byte(Integer.valueOf(primitive.getByte()).toString());
			break;
			
		case CHAR:
			this.value = Character.valueOf((char)primitive.getChar());
			break;
			
		case DOUBLE:
			this.value = primitive.getDouble();
			break;
			
		case FLOAT:
			this.value = primitive.getFloat();
			break;
			
		case INT:
			this.value = primitive.getInt();
			break;
			
		case LONG:
			this.value = primitive.getLong();
			break;
			
		case SHORT:
			this.value = Short.valueOf((short)primitive.getShort());
			break;
		}
	}

	@Override
	public Argument getArgument() {
		return Argument.newBuilder().setType(Argument.ArgumentType.PRIMITIVE).setPrimitive(this.getPrimitive()).build();
	}
	
	public Class<?> getArrayType() {
		switch(this.type) {
		case BOOL:		return Boolean[].class;
		case BYTE:		return Byte[].class;
		case CHAR:		return Character[].class;
		case DOUBLE:	return Double[].class;
		case FLOAT:		return Float[].class;
		case INT:		return Integer[].class;
		case LONG:		return Long[].class;
		case SHORT:		return Short[].class;
		default:		return null;
		}
	}
	
	@Override
	public Object getNative() {
		return this.getWrapperClass().cast(this.value);
	}
	
	private Primitive getPrimitive() {
		Primitive.Builder primitive = Primitive.newBuilder().setType(this.type);
		
		switch(this.type) {
		case BOOL:		return primitive.setBool((Boolean)this.value).build();
		case BYTE:		return primitive.setByte((Byte)this.value).build();
		case CHAR:		return primitive.setChar((Character)this.value).build();
		case DOUBLE:	return primitive.setDouble((Double)this.value).build();
		case FLOAT:		return primitive.setFloat((Float)this.value).build();
		case INT:		return primitive.setInt((Integer)this.value).build();
		case LONG:		return primitive.setLong((Long)this.value).build();
		case SHORT:		return primitive.setShort((Short)this.value).build();
		default:		return null;
		}
	}
	
	public Primitive.PrimitiveType getPrimitiveType() {
		return this.type;
	}
	
	@Override
	public Class<?> getType() {
		switch(this.type) {
		case BOOL:		return Boolean.TYPE;
		case BYTE:		return Byte.TYPE;
		case CHAR:		return Character.TYPE;
		case DOUBLE:	return Double.TYPE;
		case FLOAT:		return Float.TYPE;
		case INT:		return Integer.TYPE;
		case LONG:		return Long.TYPE;
		case SHORT:		return Short.TYPE;
		default:		return null;
		}
	}

	public Class<?> getWrapperClass() {
		switch(this.type) {
		case BOOL:		return Boolean.class;
		case BYTE:		return Byte.class;
		case CHAR:		return Character.class;
		case DOUBLE:	return Double.class;
		case FLOAT:		return Float.class;
		case INT:		return Integer.class;
		case LONG:		return Long.class;
		case SHORT:		return Short.class;
		default:		return null;
		}
	}

}
