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
	
	public interface EndpointSerializer {
		
		public Endpoint deserialize(Object serialized);
		public Object serialize(Endpoint endpoint);
		
	}

	private int id = -1;
	private String name = "Endpoint";
	private String host = "droidhg.local";
	private int port = 31415;
	private boolean ssl = true;
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
	
	private TrustManager getTrustManager() {
		try {
			return new KeyStoreTrustManager(new FileInputStream(this.getSSLTrustStorePath()), this.getSSLTrustStorePassword().toCharArray());
		}
		catch(Exception e) {
			Log.e("getTrustManager", e.getMessage());
			
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
	
	public String toConnectionString() {
		return String.format("%s:%d", this.host, this.port);
	}
	
	public InetAddress toInetAddress() throws UnknownHostException {
		return InetAddress.getByName(this.getHost());
	}

	public Socket toSocket() throws IOException {
		if(this.ssl)
			return this.toSSLSocket();
		else
			return new Socket(this.toInetAddress(), this.getPort());
	}
	
	public Socket toSSLSocket() {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(new KeyManager[0], new TrustManager[] { this.getTrustManager() }, new SecureRandom());
			
			return ((SSLSocketFactory)context.getSocketFactory()).createSocket(this.toInetAddress(), this.getPort());
		}
		catch(IOException e) {
			Log.e("toSSLSocket", e.getMessage());
			return null;
		}
		catch(KeyManagementException e) {
			Log.e("toSSLSocket", e.getMessage());
			return null;
		}
		catch (NoSuchAlgorithmException e) {
			Log.e("toSSLSocket", e.getMessage());
			return null;
		}
	}
	
	public String toString() {
		return String.format("%s (%s:%d)", this.name, this.host, this.port);
	}
	
}
