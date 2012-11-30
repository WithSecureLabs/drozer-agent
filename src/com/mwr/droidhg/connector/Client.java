package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Endpoint;

public class Client extends Connector {
	
	private Endpoint endpoint = null;
	
	public Client(Endpoint endpoint) {
		super();
		
		this.endpoint = endpoint;
	}
	
	public void resetConnection() {
		this.endpoint.setStatus(Endpoint.Status.CONNECTING);
		
		try {
			Thread.sleep(1000);
		}
		catch(InterruptedException e) {}
		
		super.resetConnection();
	}
	
	public void run() {
		this.running = true;
		
		while(this.running) {
			try {
				if(this.connection == null) {
					this.endpoint.setStatus(Endpoint.Status.CONNECTING);
					
					Socket socket = this.endpoint.toSocket();
					
					this.createConnection(new SocketTransport(socket));
				}
				else if(this.connection.started && !this.connection.running) {
					Log.i("client " + this.endpoint.getId(), "session was reset");
					
					this.resetConnection();
				}
			}
			catch(UnknownHostException e) {
				Log.e("client " + this.endpoint.getId(), "unknown host: " + this.endpoint.getHost());
				
				this.stopClient();
			}
			catch(IOException e) {
				this.resetConnection();
			}
		}
	}

	@Override
	public void setStatus(Status status) {
		this.endpoint.setStatus(status);
	}
	
	public void stopClient() {
		super.stopConnector();
	}

}
