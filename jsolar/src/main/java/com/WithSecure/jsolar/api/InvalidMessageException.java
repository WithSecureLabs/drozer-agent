package com.WithSecure.jsolar.api;

import com.WithSecure.jsolar.api.Protobuf.Message;

import java.util.Locale;

public class InvalidMessageException extends RuntimeException{

    private Message invalid_message = null;
    // I dont even know...
    private static final long serialVersionUID = -3727783632022708351L;

    public InvalidMessageException(Message invalid_message) {
        this.invalid_message = invalid_message;
    }

    public Message getInvalid_message() {return this.invalid_message;}
    public String toString() {
        return String.format(Locale.ENGLISH, "Invalid message: %s", this.invalid_message.toString());
    }
}
