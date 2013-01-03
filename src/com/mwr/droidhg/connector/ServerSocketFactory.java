package com.mwr.droidhg.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import com.mwr.droidhg.api.ServerParameters;

public class ServerSocketFactory {

	public ServerSocket createSocket(ServerParameters server) throws CertificateException, IOException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		if(server.isSSL())
			return this.createSSLSocket(server);
		else
			return new ServerSocket(server.getPort());
	}

	public SSLServerSocket createSSLSocket(ServerParameters server) throws CertificateException, IOException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
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
