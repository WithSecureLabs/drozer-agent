package com.WithSecure.jsolar.logger;

public interface OnLogMessageListener<T> {
    public void onLogMessage(Logger<T> tLogger, LogMessage message);
}
