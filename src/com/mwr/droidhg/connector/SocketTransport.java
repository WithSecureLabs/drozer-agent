package com.mwr.droidhg.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

public class SocketTransport extends Transport {
	
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
	protected InputStream getInputStream() throws IOException {
		return this.in;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return this.out;
	}

}
