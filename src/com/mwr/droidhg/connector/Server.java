package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.ServerParameters;

public class Server extends Connector {
	
	private ServerSocket server_socket = null;
	
	public Server(ServerParameters parameters) {
		super(parameters);
	}
	
	@Override
	public boolean checkForLiveness() { return false; }
	
	@Override
	public boolean dieWithLastSession() { return true; }

	public String getHostCertificateFingerprint() {
		return this.connection.getHostCertificateFingerprint();
	}
	
	public String getPeerCertificateFingerprint() {
		return this.connection.getPeerCertificateFingerprint();
	}
	
	@Override
	public boolean mustBind() { return false; }
	
	@Override
	public void resetConnection() {
		this.parameters.setStatus(ServerParameters.Status.CONNECTING);
		
		Thread.yield();
		
		if(this.server_socket != null) {
			try {
				this.server_socket.close();
				
				this.server_socket = null;
			}
			catch(IOException e) {}
		}
		
		super.resetConnection();
	}
	
	@Override
	public void run() {
		this.running = true;
		
		this.log("Starting Server...");
		while(this.running) {
			try {
				if(this.connection == null) {
					this.parameters.setStatus(ServerParameters.Status.CONNECTING);
					
					this.log("Attempting to bind to port " + ((ServerParameters)this.parameters).getPort() + "...");
					this.server_socket = new ServerSocketFactory().createSocket((ServerParameters)this.parameters);
					
					this.log("Waiting for connections...");
					Socket socket = this.server_socket.accept();
					
					if(socket != null) {
						this.parameters.setStatus(ServerParameters.Status.ONLINE);
						
						this.log("Accepted connection...");
						
						this.log("Starting Mercury thread...");
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
					// block until connection == null or connection.started && !connection.running
							
						if(this.connection.started && !this.connection.running) {
							this.log(LogMessage.WARN, "Connection was reset.");
							
							this.resetConnection();
						}
				}
			}
			catch(CertificateException e) {
				this.log(LogMessage.ERROR, "Error loading key material for SSL.");
				
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
			catch(KeyStoreException e) {
				this.log(LogMessage.ERROR, "Error loading key material for SSL.");
				
				this.stopConnector();
			}
			catch(UnrecoverableKeyException e) {
				this.log(LogMessage.ERROR, "Error loading key material for SSL.");
				
				this.stopConnector();
			}
			
		}
		
		this.log("Stopped.");
		this.parameters.setStatus(ServerParameters.Status.OFFLINE);
	}

	@Override
	public void setStatus(Status status) {
		this.parameters.setStatus(status);
	}
	
	public void stopConnector() {
		super.stopConnector();
		
		try {
			if(this.server_socket != null) {
				this.server_socket.close();
				this.server_socket = null;
			}
		}
		catch(IOException e) {}
	}
	
}
