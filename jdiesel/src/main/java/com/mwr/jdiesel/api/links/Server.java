package com.mwr.jdiesel.api.links;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import com.mwr.jdiesel.api.DeviceInfo;
import com.mwr.jdiesel.api.connectors.ServerSocketFactory;
import com.mwr.jdiesel.api.connectors.Connector.Status;
import com.mwr.jdiesel.api.transport.SocketTransport;
import com.mwr.jdiesel.connection.SecureConnection;
import com.mwr.jdiesel.logger.LogMessage;

public class Server extends Link {
	
	private ServerSocket server_socket = null;
	
	public Server(com.mwr.jdiesel.api.connectors.Server parameters, DeviceInfo device_info) {
		super(parameters, device_info);
	}
	
	@Override
	public boolean checkForLiveness() { return false; }
	
	@Override
	public boolean dieWithLastSession() { return true; }

	public String getHostCertificateFingerprint() {
		return ((SecureConnection)this.connection).getHostCertificateFingerprint();
	}
	
	public String getPeerCertificateFingerprint() {
		return ((SecureConnection)this.connection).getPeerCertificateFingerprint();
	}
	
	@Override
	public boolean mustBind() { return false; }
	
	@Override
	public void resetConnection() {
		this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.CONNECTING);
		
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
		
		this.log(LogMessage.INFO, "Starting Server...");
		while(this.running) {
			try {
				if(this.connection == null) {
					this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.CONNECTING);
					
					this.log(LogMessage.INFO, "Attempting to bind to port " + ((com.mwr.jdiesel.api.connectors.Server)this.parameters).getPort() + "...");
					this.server_socket = new ServerSocketFactory().createSocket((com.mwr.jdiesel.api.connectors.Server)this.parameters);
					
					this.log(LogMessage.INFO, "Waiting for connections...");
					Socket socket = this.server_socket.accept();
					
					if(socket != null) {
						this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.ONLINE);
						
						this.log(LogMessage.INFO, "Accepted connection...");
						
						this.log(LogMessage.INFO, "Starting drozer thread...");
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
			System.out.println("error: " + e.toString());	
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
		
		this.log(LogMessage.INFO, "Stopped.");
		this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.OFFLINE);
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
