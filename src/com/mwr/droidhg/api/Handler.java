package com.mwr.droidhg.api;

import com.mwr.cinnibar.api.Protobuf.Message;
import com.mwr.droidhg.connector.InvalidMessageException;

public interface Handler {
	
	public Message handle(Message message) throws InvalidMessageException;
	
}
