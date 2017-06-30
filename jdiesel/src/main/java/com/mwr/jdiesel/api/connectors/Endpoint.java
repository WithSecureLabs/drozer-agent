package com.mwr.jdiesel.api.connectors;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import javax.net.ssl.TrustManager;

import android.os.Bundle;
import android.util.Log;

import com.mwr.common.tls.trust_managers.KeyStoreTrustManager;

public class Endpoint extends Connector {
	
	public static final String ENDPOINT_DELETED = "endpoint:deleted";
	public static final String ENDPOINT_HOST = "endpoint:host";
	public static final String ENDPOINT_ID = "endpoint:id";
	public static final String ENDPOINT_NAME = "endpoint:name";
	public static final String ENDPOINT_PASSWORD = "endpoint:password";
	public static final String ENDPOINT_PORT = "endpoint:port";
	public static final String ENDPOINT_SSL = "endpoint:ssl";
	public static final String ENDPOINT_TRUSTSTORE_PASSWORD = "endpoint:ts:password";
	public static final String ENDPOINT_TRUSTSTORE_PATH = "endpoint:ts:path";
	
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
	
	/**
	 * An OnDetailedStatusListener will receive a notification whenever an Endpoint's
	 * detailed status information is updated.
	 */
	public interface OnDetailedStatusListener {
		
		public void onDetailedStatus(Bundle status);
		
	}

	private int id = -1;
	private String name = "Endpoint";
	private String host = "drozer.local";
	private String password = "";
	private int port = 31415;
	private boolean ssl = false;
	private String ssl_truststore_password = "drozer";
	private String ssl_truststore_path = "/data/data/com.mwr.dz/files/ca.bks";
	private boolean active = false;
	
	private OnDetailedStatusListener on_detailed_status_listener;
	
	public Endpoint() {
		this(-1, "Endpoint", "drozer.local", 31415);
	}
	
	public Endpoint(String name, String host, int port, boolean ssl, String ssl_truststore_path, String ssl_truststore_password, String password) {
		this(-1, name, host, port, ssl, ssl_truststore_path, ssl_truststore_password, password, false);
	}
	
	public Endpoint(int id, String name, String host, int port) {
		this(id, name, host, port, false, "/data/data/com.mwr.dz/files/ca.bks", "drozer", "", false);
	}
	
	public Endpoint(int id, String name, String host, int port, boolean ssl, String ssl_truststore_path, String ssl_truststore_password, String password, boolean active) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.password = password;
		this.port = port;
		this.ssl = ssl;
		this.ssl_truststore_password = ssl_truststore_password;
		this.ssl_truststore_path = ssl_truststore_path;
		this.active = active;
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
	
	public String getPassword() {
		return this.password;
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
	
	public boolean isActive(){
		return this.active;
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
	
	public boolean hasPassword() {
		return !(this.getPassword() == null || this.getPassword().equals(""));
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
				!this.password.equals(endpoint.getPassword()) ||
				this.port != endpoint.getPort() ||
				this.ssl != endpoint.isSSL() ||
				!this.ssl_truststore_password.equals(endpoint.ssl_truststore_password) ||
				!this.ssl_truststore_path.equals(endpoint.ssl_truststore_path) ||
				this.active != endpoint.isActive()
				) {
			this.host = endpoint.getHost();
			this.name = endpoint.getName();
			this.password = endpoint.getPassword();
			this.port = endpoint.getPort();
			this.ssl = endpoint.isSSL();
			this.ssl_truststore_password = endpoint.getSSLTrustStorePassword();
			this.ssl_truststore_path = endpoint.getSSLTrustStorePath();
			this.active = endpoint.isActive();
			
			this.setChanged();
			this.notifyObservers();
		}
	}
	
	public void setDetailedStatus(Bundle status) {
		if(this.on_detailed_status_listener != null)
			this.on_detailed_status_listener.onDetailedStatus(status);
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setOnDetailedStatusListener(OnDetailedStatusListener listener) {
		this.on_detailed_status_listener = listener;
	}
	
	/**
	 * Create a connection string for this endpoint, in the form "hostname:port".
	 */
	public String toConnectionString() {
		return String.format(Locale.ENGLISH, "%s:%d", this.host, this.port);
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
		return String.format(Locale.ENGLISH, "%s (%s:%d)", this.name, this.host, this.port);
	}
	
	@Override
	public boolean verifyPassword(String password) {
		return this.getPassword() == null && (password == null || password.equals("")) || password.equals(this.getPassword());
	}
	
}
