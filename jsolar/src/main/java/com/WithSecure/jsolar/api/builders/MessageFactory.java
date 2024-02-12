package com.WithSecure.jsolar.api.builders;

import com.WithSecure.jsolar.api.Protobuf.Message;
import com.WithSecure.jsolar.api.Protobuf.Message.*;

public class MessageFactory {

    Message.Builder builder = null;

    public MessageFactory(ReflectionRequest reflectionRequest) {
        this.builder = Message.newBuilder();

        this.builder.setType(Message.MessageType.REFLECTION_REQUEST);
        this.builder.setReflectionRequest(reflectionRequest);
    }

    public MessageFactory(ReflectionResponse reflectionResponse) {
        this.builder = Message.newBuilder();

        this.builder.setType(Message.MessageType.REFLECTION_RESPONSE);
        this.builder.setReflectionResponse(reflectionResponse);
    }

    public MessageFactory(ReflectionResponseFactory responseFactory) {
        this(responseFactory.build());
    }

    public MessageFactory(SystemRequest systemRequest) {
        this.builder = Message.newBuilder();

        this.builder.setType(MessageType.SYSTEM_REQUEST);
        this.builder.setSystemRequest(systemRequest);
    }

    public MessageFactory(SystemRequestFactory requestFactory) {
        this(requestFactory.build());
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

