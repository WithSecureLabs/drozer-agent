package com.mwr.jdiesel.reflection.types;

import com.google.protobuf.ByteString;
import com.mwr.jdiesel.api.Protobuf.Message;

public class ReflectedBinary extends ReflectedType {
	
	private ByteString bytes;
	
	public ReflectedBinary(ByteString bytes) {
		this.bytes = bytes;
	}
	
	public static ReflectedBinary fromNative(byte[] bytes) {
		return new ReflectedBinary(ByteString.copyFrom(bytes));
	}

	@Override
	public Message.Argument getArgument() {
		return Message.Argument.newBuilder()
				.setType(Message.Argument.ArgumentType.DATA)
				.setData(this.bytes).build();
	}

	@Override
	public Object getNative() {
		return this.bytes.toByteArray();
	}

	@Override
	public Class<?> getType() {
		return byte[].class;
	}
	
	public int size() {
		return this.bytes.size();
	}

}
