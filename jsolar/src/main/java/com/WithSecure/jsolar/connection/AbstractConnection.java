package com.WithSecure.jsolar.connection;

import android.util.Log;

import com.WithSecure.jsolar.api.APIVersionException;
import com.WithSecure.jsolar.api.DeviceInfo;
import com.WithSecure.jsolar.api.Frame;
import com.WithSecure.jsolar.api.InvalidMessageException;
import com.WithSecure.jsolar.api.Protobuf.Message;
import com.WithSecure.jsolar.api.UnexpectedMessageException;
import com.WithSecure.jsolar.api.transport.Transport;
import com.WithSecure.jsolar.api.transport.TransportDisconnectedException;

import java.io.IOException;
import java.net.SocketTimeoutException;

public abstract class AbstractConnection extends Thread{

    private static final long LIVENESS_THRESHOLD = 30000L; // Milliseconds

    private AbstractLink connector = null;
    private DeviceInfo device_info;
    private long last_message_at = 0;
    public volatile boolean running = false;
    public volatile boolean started = false;
    private Transport transport = null;

    public AbstractConnection(AbstractLink connector, DeviceInfo device_info, Transport transport) {
        this.connector = connector;
        this.device_info = device_info;
        this.transport = transport;
    }

    /**
     * Attempt to handshake with a Server to bind this device, sharing the device id, manufacturer,
     * model and software version.
     *
     * Note: this is only used if we are operating as a Client (see {@link #mustBind()}).
     */
    protected abstract boolean bindToServer(DeviceInfo device);

    /**
     * Perform a liveness check. If we haven't received a message in the last {@link #LIVENESS_THRESHOLD} milliseconds,
     * then we stop the connection.
     *
     * Note: this is only used if ConnectorcheckForLiveness() is true.
     */
    protected void checkForLiveness() {
        if(this.connector.checkForLiveness() && System.currentTimeMillis() - this.last_message_at > LIVENESS_THRESHOLD || !this.transport.isLive()) {
            Log.i("connection", "connection was reset, no message for " + (System.currentTimeMillis() - this.last_message_at) + "ms");

            this.stopConnection(false);
        }
    }

    protected AbstractLink getConnector() {
        return this.connector;
    }

    protected Transport getTransport() {
        return this.transport;
    }

    private void handleMessage(Message message) {
        try {
            Message response = null;
            Log.i("AbstractConnection","Handling Message" + message.getType());
            switch(message.getType()) {
                case REFLECTION_REQUEST:
                    response = this.handleReflectionRequest(message);
                    break;

                case REFLECTION_RESPONSE:
                    response = this.handleReflectionResponse(message);
                    break;

                case SYSTEM_REQUEST:
                    response = this.handleSystemRequest(message);
                    break;

                case SYSTEM_RESPONSE:
                    response = this.handleSystemResponse(message);
                    break;

                default:
                    throw new UnexpectedMessageException(message.getType());
            }

            if(response != null)
                this.send(response);
        }
        catch(InvalidMessageException e) {
            throw new InvalidMessageException(message);
        }

        this.last_message_at = System.currentTimeMillis();
    }

    protected abstract Message handleReflectionRequest(Message message) throws InvalidMessageException;
    protected abstract Message handleReflectionResponse(Message message) throws InvalidMessageException;
    protected abstract Message handleSystemRequest(Message message) throws InvalidMessageException;
    protected abstract Message handleSystemResponse(Message message) throws InvalidMessageException;

    /**
     * @return true, if there are sessions associated with this Connection
     */
    public boolean hasSessions() {
        return this.connector.hasSessions();
    }

    /**
     * @return true, if the connection must bind to its peer
     */
    protected boolean mustBind() {
        return this.connector.mustBind();
    }

    /**
     * Attempt to receive a Message from the peer.
     *
     * If a full Message is available, it is removed from its headers and returned as a
     * Message object. If no message (or a partial message) is available, null is returned.
     *
     * If something has gone wrong (i.e., an invalid API version, or dropped connection) the
     * Connection will be stopped, and null is returned.
     */
    protected Message receive() {
        try {
            Log.i("AbstractConnection","Recieving MEssage Part 1");
            Frame f = this.transport.receive();
            Log.i("AbstractConnection","Recieving MessagePart 2");
            Log.i("AbstractConnection",f.getPayload().toString());
            return f != null ? f.getPayload() : null;
        }
        catch(SocketTimeoutException e) {
            Log.i("AbstractConnection","Time Out");
            return null;
        }
        catch(APIVersionException e) {
            Log.i("AbstractConnection","API Version error" + e.getMessage());
            this.stopConnection();
            return null;
        }
        catch(IOException e) {
            Log.i("AbstractConnection","IO Exception Error" + e.getMessage());
            this.stopConnection();
            return null;
        }
        catch(TransportDisconnectedException e) {
            Log.i("AbstractConnection","TransportDisconnectedException" + e.getMessage());
            this.stopConnection();

            return null;
        }
    }

    /**
     * Executed by the Java runtime when the Connection thread starts.
     *
     * This manages the entire lifecycle of a connection:
     *
     *   1. Bind to the peer, if necessary.
     *   2. Receive a message from the peer.
     *   3. Interpret and act on the message.
     *      This may send a result.
     *   4. Perform liveness checks.
     *   5. Iff the connection is still alive, return to (2).
     *   6. Unbind from the peer, if necessary.
     */
    public void run() {
        this.running = true;
        this.started = true;
        this.tryAndNotifyAll();

        this.last_message_at = System.currentTimeMillis();

        Message request = null;
        Log.i("Main Loop", "LMAO Lets get YOTED");
        if(!this.bindToServer(this.device_info)) {
            Log.i("Main Loop", "We stopped connection because bind device info thingy");
            this.stopConnection();
        }

        while(this.running) {
            request = this.receive();
            if(request != null) {
                this.handleMessage(request);

                request = null;
            }

            this.checkForLiveness();

            Thread.yield();
        }
        Log.i("Main Loop", "Undbinding because life is cringe");
        this.unbindFromServer(this.device_info);
    }

    /**
     * Deliver a message to the peer.
     */
    protected void send(Message message) {
        try {
            this.transport.send(new Frame(message));
        }
        catch(IOException e) {
            this.stopConnection(false);
        }
    }

    public AbstractSession startSession() {
        return this.startSession(null);
    }

    /**
     * Start a new drozer Session.
     *
     * This is invoked by the system_message_handler, iff it receives a
     * START_SESSION message.
     *
     * We defer to the {@link #connector}, which owns the Session, and return the resultant
     * handle.
     */
    public AbstractSession startSession(String password) {
        return this.connector.startSession(password);
    }

    /**
     * Stops the connection, forcing all active sessions to die.
     */
    public void stopConnection() {
        this.stopConnection(true);
    }

    /**
     * Ask the connection to stop, optionally killing all active sessions.
     */
    public void stopConnection(boolean kill_sessions) {
        this.running = false;
        this.tryAndNotifyAll();

        if(kill_sessions)
            this.stopSessions();
    }

    /**
     * Stop a drozer Session.
     *
     * This is invoked by the system_message_handler, iff it receives a
     * STOP_SESSION message.
     *
     * We defer to the {@link #connector}, which owns the Session.
     */
    public AbstractSession stopSession(String session_id) {
        return this.connector.stopSession(session_id);
    }

    /**
     * Stops all sessions associated with this connection.
     */
    public void stopSessions() {
        this.connector.stopSessions();
    }

    private void tryAndNotifyAll() {
        synchronized(this) {
            try {
                this.notifyAll();
            }
            catch(IllegalMonitorStateException e) {
                Log.e("Connection", "could not notifyAll(), the Connection was not locked");
            }
        }
    }

    /**
     * Attempt to disconnect from the server, indicating that our device id is not longer
     * available.
     */
    protected abstract void unbindFromServer(DeviceInfo device);

}
