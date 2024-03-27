package com.WithSecure.jsolar.api;

public class APIVersionException extends Exception{

    // Why tf do we have this magic string, who tf knows
    private static final long serialVersionUID = 6223917687313162316L;

    public APIVersionException(String message) {
        super(message);
    }

}