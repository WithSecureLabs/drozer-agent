package com.WithSecure.jsolar.reflection.types;

import com.WithSecure.jsolar.api.Protobuf.Message.Argument;

public class ReflectedString extends ReflectedType {
	
	private String string;

	public ReflectedString(String string) {
		this.string = string;
	}

	@Override
	public Argument getArgument() {
		return Argument.newBuilder().setType(Argument.ArgumentType.STRING).setString(this.string).build();
	}
	
	@Override
	public Object getNative() {
		return this.string;
	}
	
	@Override
	public Class<?> getType() {
		return String.class;
	}
	
}
