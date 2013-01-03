package com.mwr.droidhg.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.util.Log;

import com.mwr.common.KeyStoreTrustManager;

public class Endpoint extends ConnectorParameters {
	
	/**
	 * An EndpointSerializer provides the logic for converting an Endpoint into various
	 * different formats. An EndpointSerializer defines methods to marshal an Endpoint
	 * into an object form, and to the recover the Endpoint.
	 * 
	 * The EndpointSerializer implementation is passed to the {@link Endpoint#serialize(EndpointSerializer)}
	 * and {@link Endpoint#deserialize(EndpointSerializer, Object)} methods.
	 */
	public interface EndpointSerializer {
		
		public Endpoint deserialize(Object serialized);
		public Object serialize(Endpoint endpoint);
		
	}

	private int id = -1;
	private String name = "Endpoint";
	private String host = "droidhg.local";
	private int port = 31415;
	private boolean ssl = false;
	private String ssl_truststore_password = "mercury";
	private String ssl_truststore_path = "/data/data/com.mwr.droidhg.agent/files/mercury-ca.bks";
	
	public Endpoint() {
		this(-1, "Endpoint", "droidhg.local", 31415);
	}
	
	public Endpoint(String name, String host, int port) {
		this(-1, name, host, port, false);
	}
	
	public Endpoint(String name, String host, int port, boolean ssl) {
		this(-1, name, host, port, ssl);
	}
	
	public Endpoint(String name, String host, int port, boolean ssl, String ssl_truststore_path, String ssl_truststore_password) {
		this(-1, name, host, port, ssl, ssl_truststore_path, ssl_truststore_password);
	}
	
	public Endpoint(int id, String name, String host, int port) {
		this(id, name, host, port, false);
	}
	
	public Endpoint(int id, String name, String host, int port, boolean ssl) {
		this(id, name, host, port, ssl, "/data/data/com.mwr.droidhg.agent/files/mercury-ca.bks", "mercury");
	}
	
	public Endpoint(int id, String name, String host, int port, boolean ssl, String ssl_truststore_path, String ssl_truststore_password) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.port = port;
		this.ssl = ssl;
		this.ssl_truststore_password = ssl_truststore_password;
		this.ssl_truststore_path = ssl_truststore_path;
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
	
	public String getSSLTrustStorePassword() {
		return this.ssl_truststore_password;
	}
	
	public String getSSLTrustStorePath() {
		return this.ssl_truststore_path;
	}
	
	/**
	 * Instantiate a TrustManager that can verify the SSL certificate served by this
	 * Endpoint.
	 */
	public TrustManager getTrustManager() {
		if(this.isSSL()) {
			try {
				return new KeyStoreTrustManager(new FileInputStream(this.getSSLTrustStorePath()), this.getSSLTrustStorePassword().toCharArray());
			}
			catch(Exception e) {
				Log.e("getTrustManager", e.getMessage());
				
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	public boolean isNew() {
		return this.id == -1;
	}
	
	public boolean isSSL() {
		return this.ssl;
	}
	
	public Object serialize(EndpointSerializer serializer) {
		return serializer.serialize(this);
	}
	
	/**
	 * Copy another Endpoint's attributes into this Endpoint and notify any observers,
	 * iff the other Endpoint is different.
	 */
	public void setAttributes(Endpoint endpoint) {
		if(!this.host.equals(endpoint.getHost()) ||
				!this.name.equals(endpoint.getName()) ||
				this.port != endpoint.getPort() ||
				this.ssl != endpoint.isSSL() ||
				!this.ssl_truststore_password.equals(endpoint.ssl_truststore_password) ||
				!this.ssl_truststore_path.equals(endpoint.ssl_truststore_path)) {
			this.host = endpoint.getHost();
			this.name = endpoint.getName();
			this.port = endpoint.getPort();
			this.ssl = endpoint.isSSL();
			this.ssl_truststore_password = endpoint.getSSLTrustStorePassword();
			this.ssl_truststore_path = endpoint.getSSLTrustStorePath();
			
			this.setChanged();
			this.notifyObservers();
		}
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Create a connection string for this endpoint, in the form "hostname:port".
	 */
	public String toConnectionString() {
		return String.format("%s:%d", this.host, this.port);
	}
	
	/**
	 * Convert the Endpoint's hostname into an InetAddress, performing any required
	 * DNS resolution. 
	 */
	public InetAddress toInetAddress() throws UnknownHostException {
		return InetAddress.getByName(this.getHost());
	}
	
	/**
	 * Create a human-readable String representation of this Endpoint, in the form
	 * "name (hostname:port)".
	 */
	public String toString() {
		return String.format("%s (%s:%d)", this.name, this.host, this.port);
	}
	
}
