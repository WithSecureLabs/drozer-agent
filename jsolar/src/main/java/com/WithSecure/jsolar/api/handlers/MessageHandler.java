package com.WithSecure.jsolar.api.handlers;

import com.WithSecure.jsolar.api.InvalidMessageException;
import com.WithSecure.jsolar.api.Protobuf;

public interface MessageHandler {
    public Protobuf.Message handle(Protobuf.Message message) throws InvalidMessageException;
}
