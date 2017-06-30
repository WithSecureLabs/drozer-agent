package com.mwr.jdiesel.api.handlers;

import com.mwr.jdiesel.api.InvalidMessageException;
import com.mwr.jdiesel.api.Protobuf.Message;

public interface MessageHandler {
	
	public Message handle(Message message) throws InvalidMessageException;
	
}
