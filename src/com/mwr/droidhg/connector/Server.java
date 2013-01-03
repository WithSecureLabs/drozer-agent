package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import android.util.Log;

import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.ServerParameters;

public class Server extends Connector {
	
	private ServerParameters parameters = null;
	private ServerSocket server_socket = null;
	
	public Server(ServerParameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public boolean checkForLiveness() {
		return false;
	}
	
	@Override
	public boolean dieWithLastSession() {
		return true;
	}
	
	@Override
	public boolean mustBind() {
		return false;
	}
	
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
		
		while(this.running) {
			try {
				if(this.connection == null) {
					this.parameters.setStatus(ServerParameters.Status.CONNECTING);
					
					this.server_socket = new ServerSocketFactory().createSocket(this.parameters);
					Socket socket = this.server_socket.accept();
					
					if(socket != null)
						this.createConnection(new SocketTransport(socket));
				}
				else if(this.connection.started && !this.connection.running) {
					Log.i("server", "session was reset");
					
					this.resetConnection();
				}
			}
			catch(CertificateException e) {
				Log.e("server", "unable to load key material");
				
				this.stopServer();
			}
			catch(IOException e) {
				this.resetConnection();
			}
			catch(KeyManagementException e) {
				Log.e("server", "unable to load key material");
				
				this.stopServer();
			}
			catch(KeyStoreException e) {
				Log.e("server", "unable to load key material");
				
				this.stopServer();
			}
			catch(UnrecoverableKeyException e) {
				Log.e("server", "unable to load key material");
				
				this.stopServer();
			}
			
		}
		
		this.parameters.setStatus(ServerParameters.Status.OFFLINE);
	}

	@Override
	public void setStatus(Status status) {
		this.parameters.setStatus(status);
	}
	
	public void stopServer() {
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
