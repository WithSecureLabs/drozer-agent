package com.mwr.droidhg.connector;

import com.mwr.cinnibar.api.Protobuf.Message;

public class InvalidMessageException extends Exception {
	
	private Message invalid_message = null;
	private static final long serialVersionUID = -3727783632022708351L;
	
	public InvalidMessageException(String message, Message invalid_message) {
		super(message);
		
		this.invalid_message = invalid_message;
	}
	
	public Message getInvalidMessage() {
		return this.invalid_message;
	}

}
