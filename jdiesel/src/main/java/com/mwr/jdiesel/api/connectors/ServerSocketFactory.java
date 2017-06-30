package com.mwr.jdiesel.api.connectors;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;


public class ServerSocketFactory {

	public ServerSocket createSocket(Server server) throws CertificateException, IOException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		if(server.isSSL())
			return this.createSSLSocket(server);
		else
			return new ServerSocket(server.getPort());
	}

	public SSLServerSocket createSSLSocket(Server server) throws CertificateException, IOException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(server.getKeyManagers(), null, null);

			return (SSLServerSocket) context.getServerSocketFactory().createServerSocket(server.getPort());
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException("no such algorithm TLS");
		}
	}
	
}
