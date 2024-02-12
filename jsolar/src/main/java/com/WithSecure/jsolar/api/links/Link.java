package com.WithSecure.jsolar.api.links;

import android.util.Log;

import com.WithSecure.jsolar.api.DeviceInfo;
import com.WithSecure.jsolar.api.connectors.Connection;
import com.WithSecure.jsolar.api.connectors.Connector;
import com.WithSecure.jsolar.api.sessions.Session;
import com.WithSecure.jsolar.api.sessions.SessionCollection;
import com.WithSecure.jsolar.api.transport.Transport;
import com.WithSecure.jsolar.connection.AbstractLink;
import com.WithSecure.jsolar.logger.LogMessage;
import com.WithSecure.jsolar.logger.Logger;

public abstract class Link extends AbstractLink {

    protected Connector parameters = null;
    private DeviceInfo device_info;

    private Logger logger = null;

    public Link(Connector parameters, DeviceInfo device_info) {
        this.parameters = parameters;
        this.device_info = device_info;

        this.setSessionCollection(new SessionCollection(this));
    }

    public abstract void setStatus(Connector.Status status);

    @Override
    protected void createConnection(Transport transport) {
        if(transport.isLive()) {
            this.connection = new Connection(this, this.device_info, transport);
            this.connection.start();
        }
    }

    @Override
    public Session getSession(String session_id) {
        return (Session)super.getSession(session_id);
    }

    public void log(int level, String message) {
        if(this.logger != null)
            this.logger.log(level, message);
        else
            Log.i("link", message);
    }

    public void log(LogMessage message) {
        if(this.logger != null)
            this.logger.log(message);
        else
            Log.i("link", message.getMessage());
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Session startSession(String password) {
        Log.i("Link","Got password: " + password);
        Boolean verifyPass = this.parameters.verifyPassword(password);
        Log.i("Link","Password match: " + verifyPass);
        if(verifyPass)
            return (Session)this.createSession();
        else
            return null;
    }

}
