package com.mwr.jdiesel.reflection.types;

import java.util.Arrays;
import java.util.Iterator;

import com.mwr.jdiesel.api.Protobuf.Message.Argument;
import com.mwr.jdiesel.api.Protobuf.Message.Array;
import com.mwr.jdiesel.reflection.ObjectStore;

public class ReflectedArray extends ReflectedType implements Iterable<ReflectedType> {
	
	private Array.ArrayType type;
	private ReflectedType[] elements;
	
	public ReflectedArray(Array.ArrayType type, ReflectedType[] elements) {
		this.type = type;
		this.elements = elements;
	}
	
	public ReflectedArray(Array array, ObjectStore object_store) {
		this.type = array.getType();
		this.elements = new ReflectedType[array.getElementCount()];
		
		for(int i=0; i<array.getElementCount(); i++)
			this.elements[i] = ReflectedType.fromArgument(array.getElement(i), object_store);
	}
	
	public static ReflectedArray fromNative(Object[] elements) {
		ReflectedType[] reflected_elements = new ReflectedType[elements.length];
		
		for(int i=0; i<elements.length; i++)
			reflected_elements[i] = ReflectedType.fromNative(elements[i]);
		
		Array.ArrayType type = Array.ArrayType.OBJECT;
		
		if(reflected_elements[0].getType().isArray())
			type = Array.ArrayType.ARRAY;
		if(reflected_elements[0].getNative() instanceof String)
			type = Array.ArrayType.STRING;
		if(reflected_elements[0].getType().isPrimitive())
			type = Array.ArrayType.PRIMITIVE;
		
		return new ReflectedArray(type, reflected_elements);
	}

	@Override
	public Argument getArgument() {
		throw new RuntimeException("not implemented yet");
		//return Argument.newBuilder().setType(Argument.ArgumentType.ARRAY).setObject(ObjectReference.newBuilder().setReference(this.reference)).build();
	}
	
	public ReflectedType[] getElements() {
		return this.elements;
	}
	
	@Override
	public Object getNative() {
		if(this.type == Array.ArrayType.PRIMITIVE) {
			switch(((ReflectedPrimitive)this.elements[0]).getPrimitiveType()) {
			case BOOL:
				Boolean[] boolean_array = new Boolean[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					boolean_array[i] = (Boolean)this.elements[i].getNative();
				
				return boolean_array;
				
			case BYTE:
				byte[] byte_array = new byte[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					byte_array[i] = ((Byte)this.elements[i].getNative()).byteValue();
				
				return byte_array;
				
			case CHAR:
				Character[] char_array = new Character[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					char_array[i] = (Character)this.elements[i].getNative();
				
				return char_array;
				
			case DOUBLE:
				Double[] double_array = new Double[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					double_array[i] = (Double)this.elements[i].getNative();
				
				return double_array;
				
			case FLOAT:
				Float[] float_array = new Float[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					float_array[i] = (Float)this.elements[i].getNative();
				
				return float_array;
				
			case INT:
				Integer[] int_array = new Integer[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					int_array[i] = (Integer)this.elements[i].getNative();
				
				return int_array;
				
			case LONG:
				Long[] long_array = new Long[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					long_array[i] = (Long)this.elements[i].getNative();
				
				return long_array;
				
			case SHORT:
				Short[] short_array = new Short[this.elements.length];
				
				for(int i=0; i<this.elements.length; i++)
					short_array[i] = (Short)this.elements[i].getNative();
				
				return short_array;
				
			default:
				return null;
			}
		}
		else if(this.type == Array.ArrayType.STRING) {
			String[] natives = new String[this.elements.length];
			
			for(int i=0; i<this.elements.length; i++)
				natives[i] = (String)this.elements[i].getNative();
			
			return natives;
		}
		else {
			Object[] natives = new Object[this.elements.length];
			
			for(int i=0; i<this.elements.length; i++)
				natives[i] = this.elements[i].getNative();
			
			return natives;
		}
	}
	
	@Override
	public Class<?> getType() {
		switch(this.type) {
		case ARRAY:			return Object[][].class;
		case OBJECT:		return Object[].class;
		case PRIMITIVE:		return ((ReflectedPrimitive)this.elements[0]).getArrayType();
		case STRING:		return String[].class;
		default:			return Object[].class;
		}
	}

	@Override
	public Iterator<ReflectedType> iterator() {
		return Arrays.asList(this.elements).iterator();
	}
	
	public String toString() {
		return "reflected array";
	}
	
}
