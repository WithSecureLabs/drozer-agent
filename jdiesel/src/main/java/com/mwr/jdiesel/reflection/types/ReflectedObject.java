package com.mwr.jdiesel.reflection.types;

import com.mwr.jdiesel.api.Protobuf.Message.Argument;
import com.mwr.jdiesel.api.Protobuf.Message.ObjectReference;
import com.mwr.jdiesel.reflection.ObjectStore;

public class ReflectedObject extends ReflectedType {
	
	private Object object;
	private int reference;
	
	public ReflectedObject(Object object) {
		this.object = object;
		this.reference = object.hashCode();
	}
	
	public ReflectedObject(ObjectReference object, ObjectStore object_store) {
		this.reference = object.getReference();
		this.object = object_store.get(this.reference);
	}

	@Override
	public Argument getArgument() {
		return Argument.newBuilder().setType(Argument.ArgumentType.OBJECT).setObject(ObjectReference.newBuilder().setReference(this.reference)).build();
	}
	
	public int getReference() {
		return this.reference;
	}
	
	@Override
	public Object getNative() {
		return this.object;
	}
	
	@Override
	public Class<?> getType() {
		return this.object.getClass();
	}

}
