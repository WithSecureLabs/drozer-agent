package com.WithSecure.jsolar.api.handlers;

import android.util.Log;

import com.WithSecure.jsolar.api.DeviceInfo;
import com.WithSecure.jsolar.api.InvalidMessageException;
import com.WithSecure.jsolar.api.builders.MessageFactory;
import com.WithSecure.jsolar.api.builders.SystemResponseFactory;
import com.WithSecure.jsolar.api.connectors.Connection;
import com.WithSecure.jsolar.api.Protobuf.Message;
import com.WithSecure.jsolar.api.sessions.Session;

public class SystemMessageHandler implements MessageHandler{

    private Connection connection = null;
    private DeviceInfo device_info;

    public SystemMessageHandler(Connection connection, DeviceInfo device_info) {
        this.connection = connection;
        this.device_info = device_info;
    }

    @Override
    public Message handle(Message message) throws InvalidMessageException {
        Log.i("System Message Handler","Handling Message: " + message.getType());
        if(message.getType() != Message.MessageType.SYSTEM_REQUEST)
            throw new InvalidMessageException(message);
        if(!message.hasSystemRequest())
            throw new InvalidMessageException(message);

        Log.i("System Message Handler","Recieved Message: " + message);
        switch (message.getSystemRequest().getType()) {
            case LIST_DEVICES:
                return this.handleListDevices(message);
            case LIST_SESSIONS:
                return this.handleListSessions(message);
            case PING:
                return this.handlePing(message);
            case START_SESSION:
                Log.i("System Message Handler","Starting Sessions: " + message);
                return this.startSession(message);
            case STOP_SESSION:
                return this.stopSession(message);

            default:
                throw new InvalidMessageException(message);
        }
    }

    protected Message handleListDevices(Message message) throws InvalidMessageException {
        MessageFactory factory = new MessageFactory(SystemResponseFactory.deviceList(message).addDevice(
                this.device_info.getAndroid_id(),
                this.device_info.getManufacturer(),
                this.device_info.getModel(),
                this.device_info.getSoftware()));

        factory.inReplyTo(message);
        return factory.build();
    }

    protected Message handleListSessions(Message message) throws InvalidMessageException {
        SystemResponseFactory response = SystemResponseFactory.sessionList(message);

        MessageFactory factory = new MessageFactory(response);
        factory.inReplyTo(message);

        return factory.build();
    }

    protected Message handlePing(Message message) throws InvalidMessageException {
        MessageFactory factory = new MessageFactory(SystemResponseFactory.pong(message));

        factory.inReplyTo(message);

        return factory.build();
    }

    protected Message startSession(Message message) throws InvalidMessageException {
        Log.i("SystemMessageHandler","Starting these sessions lets go");
        Session session = (Session)this.connection.startSession(message.getSystemRequest().getPassword());
        if(session != null) {
            MessageFactory factory = new MessageFactory(SystemResponseFactory.session(session.getSessionId()));

            factory.inReplyTo(message);

            return factory.build();
        }
        else {
            MessageFactory factory = new MessageFactory(SystemResponseFactory.session(Session.nullSession().getSessionId()).isError());

            factory.inReplyTo(message);

            return factory.build();
        }
    }

    protected Message stopSession(Message message) throws InvalidMessageException {
        Session session = (Session)this.connection.stopSession(message.getSystemRequest().getSessionId());

        if(session != null) {
            MessageFactory factory = new MessageFactory(SystemResponseFactory.session(session.getSessionId()));

            factory.inReplyTo(message);

            return factory.build();
        }
        else {
            MessageFactory factory = new MessageFactory(SystemResponseFactory.session(Session.nullSession().getSessionId()).isError());

            factory.inReplyTo(message);

            return factory.build();
        }
    }
}
