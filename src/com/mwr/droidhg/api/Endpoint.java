package com.mwr.droidhg.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Endpoint extends ConnectorParameters {
	
	public interface EndpointSerializer {
		
		public Endpoint deserialize(Object serialized);
		public Object serialize(Endpoint endpoint);
		
	}

	private int id = -1;
	private String name = "Endpoint";
	private String host = "droidhg.local";
	private int port = 31415;
	
	public Endpoint() {
		this(-1, "Endpoint", "droidhg.local", 31415);
	}
	
	public Endpoint(String name, String host, int port) {
		this(-1, name, host, port);
	}
	
	public Endpoint(int id, String name, String host, int port) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.port = port;
	}
	
	public static Endpoint deserialize(EndpointSerializer serializer, Object serialized) {
		return serializer.deserialize(serialized);
	}

	public String getHost() {
		return this.host;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public int getPort() {
		return this.port;
	}
	
	public boolean isNew() {
		return this.id == -1;
	}
	
	public Object serialize(EndpointSerializer serializer) {
		return serializer.serialize(this);
	}
	
	public void setAttributes(Endpoint endpoint) {
		if(!this.host.equals(endpoint.getHost()) ||
				!this.name.equals(endpoint.getName()) ||
				this.port != endpoint.getPort()) {
			this.host = endpoint.getHost();
			this.name = endpoint.getName();
			this.port = endpoint.getPort();
			
			this.setChanged();
			this.notifyObservers();
		}
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String toConnectionString() {
		return String.format("%s:%d", this.host, this.port);
	}
	
	public InetAddress toInetAddress() throws UnknownHostException {
		return InetAddress.getByName(this.getHost());
	}

	public Socket toSocket() throws IOException {
		return new Socket(this.toInetAddress(), this.getPort());
	}
	
	public String toString() {
		return String.format("%s (%s:%d)", this.name, this.host, this.port);
	}
	
}
