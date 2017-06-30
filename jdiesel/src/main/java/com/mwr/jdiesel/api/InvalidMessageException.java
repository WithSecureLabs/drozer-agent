package com.mwr.jdiesel.api;

import java.util.Locale;

import com.mwr.jdiesel.api.Protobuf.Message;

public class InvalidMessageException extends RuntimeException {
	
	private Message invalid_message = null;
	private static final long serialVersionUID = -3727783632022708351L;
	
	public InvalidMessageException(Message invalid_message) {
		this.invalid_message = invalid_message;
	}
	
	public Message getInvalidMessage() {
		return this.invalid_message;
	}
	
	public String toString() {
		return String.format(Locale.ENGLISH, "Invalid message: %s", this.invalid_message.toString());
	}

}
