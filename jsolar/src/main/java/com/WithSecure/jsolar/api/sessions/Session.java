package com.WithSecure.jsolar.api.sessions;

import android.os.Looper;

import com.WithSecure.jsolar.api.InvalidMessageException;
import com.WithSecure.jsolar.api.Protobuf;
import com.WithSecure.jsolar.api.handlers.MessageHandler;
import com.WithSecure.jsolar.api.handlers.ReflectionMessageHandler;
import com.WithSecure.jsolar.api.links.Link;
import com.WithSecure.jsolar.connection.AbstractSession;
import com.WithSecure.jsolar.reflection.ObjectStore;

public class Session extends AbstractSession {

    private Link connector = null;
    public ObjectStore object_store = new ObjectStore();
    private MessageHandler reflection_message_handler = new ReflectionMessageHandler(this);

    public Session(Link connector) {
        super();

        this.connector = connector;
    }

    protected Session(String session_id) {
        super(session_id);
    }

    public static Session nullSession() {
        return new Session("null");
    }

    @Override
    protected Protobuf.Message handleMessage(Protobuf.Message message) throws InvalidMessageException {
        return this.reflection_message_handler.handle(message);
    }

    @Override
    public void run(){
        Looper.prepare();

        super.run();
    }

    @Override
    public void send(Protobuf.Message message) {
        this.connector.send(message);
    }

}
