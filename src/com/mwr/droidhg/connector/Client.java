package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;

import android.util.Log;

import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Endpoint;

public class Client extends Connector {
	
	public Client(Endpoint endpoint) {
		super(endpoint);
	}
	
	@Override
	public void resetConnection() {
		this.parameters.setStatus(Endpoint.Status.CONNECTING);
		
		Thread.yield();
		
		super.resetConnection();
	}
	
	@Override
	public void run() {
		Endpoint endpoint = (Endpoint)this.parameters;
		
		this.running = true;
		
		while(this.running) {
			try {
				if(this.connection == null) {
					this.parameters.setStatus(Endpoint.Status.CONNECTING);
					
					Socket socket = new EndpointSocketFactory().createSocket(endpoint);
					
					this.createConnection(new SocketTransport(socket));
				}
				else if(this.connection.started && !this.connection.running) {
					Log.i("client " + endpoint.getId(), "session was reset");
					
					this.resetConnection();
				}
			}
			catch(UnknownHostException e) {
				Log.e("client " + endpoint.getId(), "unknown host: " + endpoint.getHost());
				
				this.stopConnector();
			}
			catch(IOException e) {
				this.resetConnection();
			}
			catch(KeyManagementException e) {
				Log.e("client " + endpoint.getId(), "failed to load trust store");
				
				this.stopConnector();
			}
		}
	}

	@Override
	public void setStatus(Status status) {
		this.parameters.setStatus(status);
	}

}
