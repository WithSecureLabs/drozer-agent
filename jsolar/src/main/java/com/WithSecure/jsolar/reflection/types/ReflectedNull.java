package com.WithSecure.jsolar.reflection.types;

import com.WithSecure.jsolar.api.Protobuf.Message.Argument;

public class ReflectedNull extends ReflectedType {

	@Override
	public Argument getArgument() {
		return Argument.newBuilder().setType(Argument.ArgumentType.NULL).build();
	}
	
	@Override
	public Object getNative() {
		return null;
	}
	
	@Override
	public Class<?> getType() {
		return null;
	}
	
}
