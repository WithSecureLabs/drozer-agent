package com.mwr.jdiesel.api.connectors;

import com.mwr.jdiesel.api.DeviceInfo;
import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.builders.MessageFactory;
import com.mwr.jdiesel.api.builders.SystemRequestFactory;
import com.mwr.jdiesel.api.handlers.MessageHandler;
import com.mwr.jdiesel.api.handlers.SystemMessageHandler;
import com.mwr.jdiesel.api.links.Link;
import com.mwr.jdiesel.api.sessions.Session;
import com.mwr.jdiesel.api.transport.SecureTransport;
import com.mwr.jdiesel.api.transport.Transport;
import com.mwr.jdiesel.connection.AbstractConnection;
import com.mwr.jdiesel.connection.AbstractLink;
import com.mwr.jdiesel.connection.SecureConnection;
import com.mwr.jdiesel.logger.LogMessage;

/**
 * A Connection is created by a Connector when a live transport connection is available.
 * 
 * The Connection polls the connector for new Messages, and routes them to the appropriate
 * handler.
 */
public class Connection extends AbstractConnection implements SecureConnection {
	
	private MessageHandler system_message_handler;
	
	public Connection(AbstractLink connector, DeviceInfo device_info, Transport transport) {
		super(connector, device_info, transport);
		
		this.system_message_handler = new SystemMessageHandler(this, device_info);
	}
	
	@Override
	/**
	 * Attempt to handshake with a Server to bind this device, sharing the device id, manufacturer,
	 * model and software version.
	 * 
	 * Note: this is only used if we are operating as a Client (see {@link #mustBind()}).
	 */
	protected boolean bindToServer(DeviceInfo device) {
		if(this.mustBind()) {
			this.log(LogMessage.DEBUG, "Sending BIND_DEVICE to drozer server...");
			
			this.send(new MessageFactory(SystemRequestFactory.bind().setDevice(
					device.getAndroidID(),
					device.getManufacturer(),
					device.getModel(),
					device.getSoftware())).setId(1).build());
			
			Message message = this.receive();
			
			if(message != null && 
					message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
					message.hasSystemResponse() &&
					message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
					message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.BOUND) {
				this.getConnector().setStatus(Connector.Status.ONLINE);
			
				return true;
			}
				
			return false;
		}
		else
			return true;
	}
	
	@Override
	protected Link getConnector() {
		return (Link)super.getConnector();
	}
	
	@Override
	/**
	 * Calculates the fingerprint of the host's SSL Certificate.
	 */
	public String getHostCertificateFingerprint() {
		return ((SecureTransport)this.getTransport()).getHostCertificateFingerprint();
	}

	@Override
	/**
	 * Calculates the fingerprint of the peer's SSL Certificate.
	 */
	public String getPeerCertificateFingerprint() {
		return ((SecureTransport)this.getTransport()).getPeerCertificateFingerprint();
	}
	
	@Override
	protected Message handleReflectionRequest(Message message) {
        Session session = this.getConnector().getSession(message.getReflectionRequest().getSessionId());
        
        if(session != null)
        	session.deliverMessage(message);
        
        return null;
	}
	
	@Override
	protected Message handleReflectionResponse(Message message) {
		return null;
	}
	
	@Override
	protected Message handleSystemRequest(Message message) {
		return this.system_message_handler.handle(message);
	}
	
	@Override
	protected Message handleSystemResponse(Message message) {
		return null;
	}
	
	/**
	 * Send a log message, with a custom log level.
	 */
	public void log(int level, String message) {
		this.getConnector().log(level, message);
	}
	
	@Override
	/**
	 * Attempt to disconnect from the server, indicating that our device id is not longer
	 * available.
	 */
	protected void unbindFromServer(DeviceInfo device) {
		if(this.mustBind()) {
			this.log(LogMessage.DEBUG, "Sending UNBIND_DEVICE to drozer server...");
			
			this.send(new MessageFactory(SystemRequestFactory.unbind().setDevice(
					device.getAndroidID(),
					device.getManufacturer(),
					device.getModel(),
					device.getSoftware())).setId(1).build());
			
			Message message = this.receive();
			
			if(message != null && 
					message.getType() == Message.MessageType.SYSTEM_RESPONSE &&
					message.hasSystemResponse() &&
					message.getSystemResponse().getStatus() == Message.SystemResponse.ResponseStatus.SUCCESS &&
					message.getSystemResponse().getType() == Message.SystemResponse.ResponseType.UNBOUND) {
				this.getConnector().setStatus(Connector.Status.OFFLINE);
			}
		}
	}

}
