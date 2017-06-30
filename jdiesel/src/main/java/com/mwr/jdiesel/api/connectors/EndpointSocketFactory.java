package com.mwr.jdiesel.api.connectors;

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


/**
 * The EndpointSocketFactory builds Socket objects from a given Endpoint.
 * 
 * If the given Endpoint has SSL enabled, an SSLSocket will be returned that has
 * been initialised with the Endpoint's TrustManager.
 */
public class EndpointSocketFactory {

	public Socket createSocket(Endpoint endpoint) throws IOException, KeyManagementException, UnknownHostException {
		if(endpoint.isSSL())
			return this.createSSLSocket(endpoint.toInetAddress(), endpoint.getPort(), endpoint.getTrustManager());
		else
			return this.createSocket(endpoint.toInetAddress(), endpoint.getPort());
	}
	
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return new Socket(host, port);
	}
		
	public Socket createSSLSocket(InetAddress host, int port, TrustManager trust_manager) throws IOException, KeyManagementException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(new KeyManager[0], new TrustManager[] { trust_manager }, new SecureRandom());
				
			return ((SSLSocketFactory)context.getSocketFactory()).createSocket(host, port);
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException("no such algorithm TLS");
		}
	}
	
}
