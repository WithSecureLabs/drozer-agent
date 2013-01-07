package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;

import android.util.Log;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Protobuf.Message;
import com.mwr.droidhg.api.builders.MessageFactory;
import com.mwr.droidhg.api.builders.SystemRequestFactory;
import com.mwr.droidhg.api.Handler;
import com.mwr.droidhg.api.SystemMessageHandler;

/**
 * A Connection is created by a Connector when a live transport connection is available.
 * 
 * The Connection polls the connector for new Messages, and routes them to the appropriate
 * handler.
 */
public class Connection extends Thread {
	
	private static final long LIVENESS_THRESHOLD = 30000L; // milliseconds
	
	private Connector connector = null;
	private long last_message_at = 0;
	public volatile boolean running = false;
	public volatile boolean started = false;
	private Handler system_message_handler = new SystemMessageHandler(this);
	private Transport transport = null;
	
	public Connection(Connector connector, Transport transport) {
		this.connector = connector;
		this.transport = transport;
	}
	
	/**
	 * Attempt to handshake with a Server to bind this device, sharing the device id, manufacturer,
	 * model and software version.
	 * 
	 * Note: this is only used if we are operating as a Client (see {@link #mustBind()}).
	 */
	private boolean bindToServer() {
		if(this.mustBind()) {
			this.log(LogMessage.DEBUG, "Sending BIND_DEVICE to Mercury server...");
			
			this.send(new MessageFactory(SystemRequestFactory.bind().setDevice()).setId(1).build());
			
	//		while(true) {
				Message message = this.receive();
				
				if(message != null && 
						message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
						message.hasSystemResponse() &&
						message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
						message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.BOUND) {
					this.connector.setStatus(Status.ONLINE);
				
					return true;
				}
				// TODO: reintroduce the loop and add a timeout - we may not get a response in the first
				// socket timeout, but if we don't get a response fairly sharpish, it isn't coming
	//		}
				
			return false;
		}
		else
			return true;
	}
	
	/**
	 * Perform a liveness check. If we haven't received a message in the last {@link #LIVENESS_THRESHOLD} milliseconds,
	 * then we stop the connection.
	 * 
	 * Note: this is only used if {@link Connector#checkForLiveness()} is true.
	 */
	private void checkForLiveness() {
		if(this.connector.checkForLiveness() && System.currentTimeMillis() - this.last_message_at > LIVENESS_THRESHOLD || !this.transport.isLive()) {
			Log.i("connection", "connection was reset, no message for " + (System.currentTimeMillis() - this.last_message_at) + "ms");
			
			this.stopConnection(false);
		}
	}
	
	/**
	 * Gets all Sessions associated with this Connection.
	 */
	public Collection<Session> getSessions() {
		return this.connector.getSessions();
	}
	
	/**
	 * handleMessage is invoked by the main {@link #run()} loop, each time a Message has
	 * been received from the peer. It inspects the message, and dispatches it to the
	 * appropriate handler:
	 * 
	 *   - all SYSTEM_REQUEST messages are dispatched to {@link #system_message_handler};
	 *   - all REFLECTION_REQUEST messages are dispatched to the appropriate Session;
	 *   - other messages are discarded.
	 * 
	 * If the handler returns a response message, it is delivered to the peer automatically.
	 */
	private void handleMessage(Message message) {
		try {
			Message response = null;
			
			switch(message.getType()) {
			case REFLECTION_REQUEST:
				Session session = this.connector.getSession(message.getReflectionRequest().getSessionId());
				
				if(session != null) {
					session.deliverMessage(message);
				}
				else {
					Log.e("connection", "got message for session " + message.getReflectionRequest().getSessionId() + ", which does not exist");
				}
				break;
				
			case SYSTEM_REQUEST:
				response = this.system_message_handler.handle(message);
				break;

			case REFLECTION_RESPONSE:
			case SYSTEM_RESPONSE:
			default:
				Log.e("connection", "got unexpected message type: " + message.getType().toString());
				break;
			}
			
			if(response != null)
				this.send(response);
		}
		catch(InvalidMessageException e) {
			Log.e("connection", "dropped invalid request: " + e.getMessage());
		}
		
		this.last_message_at = System.currentTimeMillis();
	}
	
	/**
	 * @return true, if there are sessions associated with this Connection
	 */
	public boolean hasSessions() {
		return this.connector.hasSessions();
	}
	
	/**
	 * Send a log message.
	 */
	public void log(String message) {
		this.connector.log(message);
	}
	
	public void log(int level, String message) {
		this.connector.log(level, message);
	}
	
	/**
	 * @return true, if the connection must bind to its peer
	 */
	private boolean mustBind() {
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
	public Message receive() {
		try {
			Frame f = this.transport.receive();
			
			return f != null ? f.getPayload() : null;
		}
		catch(SocketTimeoutException e) {
			return null;
		}
		catch(APIVersionException e) {
			this.log(LogMessage.ERROR, "unexpected API version");
			
			this.stopConnection();
			
			return null;
		}
		catch(IOException e) {
			this.log(LogMessage.ERROR, "IO Error.");
			
			this.stopConnection();
			
			return null;
		}
		catch(TransportDisconnectedException e) {
			this.log(LogMessage.ERROR, "the Transport was dropped, whilst trying to read a frame");
			
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
		
		if(!this.bindToServer())
			this.stopConnection();

		this.log("Mercury thread started...");
		while(this.running) {
			request = this.receive();
			
			if(request != null) {
				this.handleMessage(request);
			
				request = null;
			}
			
			this.checkForLiveness();
			
			Thread.yield();
		}
		
		this.unbindFromServer();
	}
	
	/**
	 * Deliver a message to the peer.
	 */
	public void send(Message message) {
		try {
			this.transport.send(new Frame(message));
		}
		catch(IOException e) {
			this.log(LogMessage.ERROR, "IO Error");
			
			this.stopConnection(false);
		}
	}
	
	public Session startSession() {
		return this.startSession(null);
	}
	
	/**
	 * Start a new Mercury Session.
	 * 
	 * This is invoked by the {@link #system_message_handler}, iff it receives a
	 * START_SESSION message.
	 * 
	 * We defer to the {@link #connector}, which owns the Session, and return the resultant
	 * handle.
	 */
	public Session startSession(String password) {
		this.log("Attempting to start session...");
		
		Session session = this.connector.startSession(password);
		
		if(session != null)
			this.log("Started session: " + session.getSessionId());
		else
			this.log(LogMessage.ERROR, "Failed to start session. Maybe wrong password?");
			
		return session;
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
	 * Stop a Mercury Session.
	 * 
	 * This is invoked by the {@link #system_message_handler}, iff it receives a
	 * STOP_SESSION message.
	 * 
	 * We defer to the {@link #connector}, which owns the Session.
	 */
	public Session stopSession(String session_id) {
		this.log("Finishing session " + session_id + ".");
		
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
	private void unbindFromServer() {
		if(this.mustBind()) {
			this.log(LogMessage.DEBUG, "Sending UNBIND_DEVICE to Mercury server...");
			
			this.send(new MessageFactory(SystemRequestFactory.unbind().setDevice()).setId(1).build());
			
	//		while(true) {
				Message message = this.receive();
				
				if(message != null && 
						message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
						message.hasSystemResponse() &&
						message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
						message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.UNBOUND) {
					this.connector.setStatus(Status.OFFLINE);
				}
	//		}
	//			TODO: loop, and timeouts
		}
	}

}
