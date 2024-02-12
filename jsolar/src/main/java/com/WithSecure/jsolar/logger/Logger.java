package com.WithSecure.jsolar.logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// Custom logger class copied from jdiesel, why it's custom I dont know but will update here if I figure it out
// ideally this just gets replaced with log4j or some other mature logging solution
public class Logger<T> {

    private List<LogMessage> log_messages = new ArrayList<>();
    private Set<OnLogMessageListener<T>> on_log_message_listeners = new HashSet<OnLogMessageListener<T>>();
    private T owner;

    public Logger(T owner) { this.owner = owner;}

    public void addOnLogMessageListener(OnLogMessageListener<T> listener) {
        this.on_log_message_listeners.add(listener);
    }

    public List<LogMessage> getLogMessages() { return this.log_messages; }

    public T getOwner() { return this.owner; }

    public void log(int level, String message) {this.log(new LogMessage(level, message));}

    public void log(LogMessage message) {
        this.log_messages.add(message);

        for(OnLogMessageListener<T> listener : this.on_log_message_listeners) {
            listener.onLogMessage(this,message);
        }
    }

    public void removeOnLogMessageListener(OnLogMessageListener<T> listener) {
        this.on_log_message_listeners.remove(listener);
    }

}
