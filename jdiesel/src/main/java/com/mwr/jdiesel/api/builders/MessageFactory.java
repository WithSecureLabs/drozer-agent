package com.mwr.jdiesel.api.builders;

import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.Protobuf.Message.ReflectionRequest;
import com.mwr.jdiesel.api.Protobuf.Message.ReflectionResponse;
import com.mwr.jdiesel.api.Protobuf.Message.SystemRequest;
import com.mwr.jdiesel.api.Protobuf.Message.SystemResponse;

public class MessageFactory {
	
	Message.Builder builder = null;
	
	public MessageFactory(ReflectionRequest reflection_request) {
		this.builder = Message.newBuilder();

		this.builder.setType(Message.MessageType.REFLECTION_REQUEST);
		this.builder.setReflectionRequest(reflection_request);
	}
	
	public MessageFactory(ReflectionResponse reflection_response) {
		this.builder = Message.newBuilder();

		this.builder.setType(Message.MessageType.REFLECTION_RESPONSE);
		this.builder.setReflectionResponse(reflection_response);
	}
	
	public MessageFactory(ReflectionResponseFactory reflection_response) {
		this(reflection_response.build());
	}
	
	public MessageFactory(SystemRequest system_request) {
		this.builder = Message.newBuilder();

		this.builder.setType(Message.MessageType.SYSTEM_REQUEST);
		this.builder.setSystemRequest(system_request);
	}
	
	public MessageFactory(SystemRequestFactory system_request) {
		this(system_request.build());
	}
	
	public MessageFactory(SystemResponse system_response) {
		this.builder = Message.newBuilder();

		this.builder.setType(Message.MessageType.SYSTEM_RESPONSE);
		this.builder.setSystemResponse(system_response);
	}
	
	public MessageFactory(SystemResponseFactory system_response) {
		this(system_response.build());
	}
	
	public Message build() {
		return this.builder.build();
	}
	
	public MessageFactory inReplyTo(Message message) {
		this.builder.setId(message.getId());
		
		return this;
	}
	
	public MessageFactory setId(int id) {
		this.builder.setId(id);
		
		return this;
	}
	
}
