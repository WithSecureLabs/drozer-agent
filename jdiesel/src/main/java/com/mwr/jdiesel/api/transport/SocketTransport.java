package com.mwr.jdiesel.api.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import com.mwr.common.tls.X509Fingerprint;

import android.util.Log;

public class SocketTransport extends Transport implements SecureTransport {
	
	private static final int SO_TIMEOUT = 5000;	// milliseconds
	
	private InputStream in = null;
	private OutputStream out = null;
	private Socket socket = null;
	
	public SocketTransport(Socket socket) {
		try {
			this.socket = socket;
			
			this.socket.setSoTimeout(SO_TIMEOUT);
			
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();
		}
		catch(IOException e) { 
			Log.e("SocketConnection", "IOException when grabbing streams: " + e.getMessage());
		}
	}

	@Override
	public void close() {
		try {
			this.in.close();
			this.out.close();
			this.socket.close();
		} catch (IOException e) {
			Log.e("SocketConnection", "IOException when closing socket: " + e.getMessage());
		}
	}
	
	@Override
	public String getHostCertificateFingerprint() {
		SSLSession session = ((SSLSocket)this.socket).getSession();
		
		return new X509Fingerprint((X509Certificate)session.getLocalCertificates()[0]).toString();
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return this.in;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return this.out;
	}
	
	@Override
	public String getPeerCertificateFingerprint() {
		try {
			SSLSession session = ((SSLSocket)this.socket).getSession();
		
			return new X509Fingerprint((X509Certificate)session.getPeerCertificates()[0]).toString();
		}
		catch(SSLPeerUnverifiedException e) {
			return "No valid peer certificate";
		}
	}
	
	@Override
	public boolean isLive() {
		return !this.socket.isClosed();
	}

}
