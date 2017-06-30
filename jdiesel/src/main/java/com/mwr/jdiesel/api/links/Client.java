package com.mwr.jdiesel.api.links;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;

import com.mwr.jdiesel.api.DeviceInfo;
import com.mwr.jdiesel.api.connectors.Endpoint;
import com.mwr.jdiesel.api.connectors.EndpointSocketFactory;
import com.mwr.jdiesel.api.connectors.Connector.Status;
import com.mwr.jdiesel.api.transport.SocketTransport;
import com.mwr.jdiesel.connection.SecureConnection;
import com.mwr.jdiesel.logger.LogMessage;

public class Client extends Link {
	
	public static final int RESET_TIMEOUT = 5000;
	
	public Client(Endpoint endpoint, DeviceInfo device_info) {
		super(endpoint, device_info);
	}

	public String getHostCertificateFingerprint() {
		return ((SecureConnection)this.connection).getHostCertificateFingerprint();
	}
	
	public String getPeerCertificateFingerprint() {
		return ((SecureConnection)this.connection).getPeerCertificateFingerprint();
	}
	
	@Override
	public void resetConnection() {
		this.parameters.setStatus(Endpoint.Status.CONNECTING);
		
		try {
			Thread.sleep(RESET_TIMEOUT);
		}
		catch(InterruptedException e) {}
		
		super.resetConnection();
	}
	
	@Override
	public void run() {
		Endpoint endpoint = (Endpoint)this.parameters;
		
		this.log(LogMessage.INFO, "Starting...");
		this.running = true;
		
		while(this.running) {
			try {
				if(this.connection == null) {
					this.parameters.setStatus(Endpoint.Status.CONNECTING);
					
					this.log(LogMessage.INFO, "Attempting connection to " + endpoint.toConnectionString() + "...");
					Socket socket = new EndpointSocketFactory().createSocket(endpoint);
					
					if(socket != null) {
						this.log(LogMessage.INFO, "Socket connected.");
						
						this.log(LogMessage.INFO, "Attempting to start drozer thread...");
						this.createConnection(new SocketTransport(socket));
					}
				}
				else {
					synchronized(this.connection) {
						try {
							this.connection.wait();
						}
						catch(InterruptedException e) {}
						catch(IllegalMonitorStateException e){}
					}
					
					if(this.connection.started && !this.connection.running) {
						this.log(LogMessage.INFO, "Connection was reset.");
						
						this.resetConnection();
					}
				}
			}
			catch(UnknownHostException e) {
				this.log(LogMessage.ERROR, "Unknown Host: " + endpoint.getHost());
				
				this.stopConnector();
			}
			catch(IOException e) {
				this.log(LogMessage.ERROR, "IO Error. Resetting connection.");
				this.log(LogMessage.DEBUG, e.getMessage());
				
				this.resetConnection();
			}
			catch(KeyManagementException e) {
				this.log(LogMessage.ERROR, "Error loading key material for SSL.");
				
				this.stopConnector();
			}
		}
		
		this.log(LogMessage.INFO, "Stopped.");
	}

	@Override
	public void setStatus(Status status) {
		this.parameters.setStatus(status);
	}

}
