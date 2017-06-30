package com.mwr.jdiesel.api.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mwr.jdiesel.api.APIVersionException;
import com.mwr.jdiesel.api.Frame;

public abstract class Transport {
	
	public abstract void close();
	protected abstract InputStream getInputStream() throws IOException;
	protected abstract OutputStream getOutputStream() throws IOException;
	public abstract boolean isLive();
	
	public Frame receive() throws APIVersionException, IOException, TransportDisconnectedException {
		if(this.getInputStream() != null)
			return Frame.readFrom(this.getInputStream());
		else
			throw new TransportDisconnectedException();
	}
	
	public void send(Frame frame) throws IOException {
		this.getOutputStream().write(frame.toByteArray());
	}
	
}
