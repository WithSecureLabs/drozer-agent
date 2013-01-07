package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;

import android.util.Log;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Endpoint;

public class Client extends Connector {
	
	public static final int RESET_TIMEOUT = 5000;
	
	public Client(Endpoint endpoint) {
		super(endpoint);
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
		
		this.log("Starting...");
		this.running = true;
		
		while(this.running) {
			try {
				if(this.connection == null) {
					this.parameters.setStatus(Endpoint.Status.CONNECTING);
					
					this.log("Attempting connection to " + endpoint.toConnectionString() + "...");
					Socket socket = new EndpointSocketFactory().createSocket(endpoint);
					
					if(socket != null) {
						this.log("Socket connected.");
						
						this.log("Attempting to start Mercury thread...");
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
						this.log("Connection was reset.");
						
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
				
				this.resetConnection();
			}
			catch(KeyManagementException e) {
				this.log(LogMessage.ERROR, "Error loading key material for SSL.");
				
				this.stopConnector();
			}
		}
		
		this.log("Stopped.");
	}

	@Override
	public void setStatus(Status status) {
		this.parameters.setStatus(status);
	}

}
