package com.mwr.jdiesel.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.mwr.jdiesel.api.Protobuf.Message;

public class Frame {

	private static final int SIZE_SIZE = 4;		// bytes
	private static final int VERSION_SIZE = 4;	// bytes
	private static final int HEADER_SIZE = VERSION_SIZE + SIZE_SIZE;
	
	private static final int VERSION = 2;
	
	private int version;
	private Message payload;
	private int size;
	
	public Frame(Message payload) {
		this.version = VERSION;
		this.setPayload(payload);
	}
	
	public Frame(int version, Message payload) {
		this.version = version;
		this.setPayload(payload);
	}
	
	public Message getPayload() {
		return this.payload;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public static Frame readFrom(InputStream in) throws IOException, APIVersionException {
		byte[] bytes = new byte[HEADER_SIZE];
		int length = in.read(bytes);
		
		if(length == -1)
			throw new IOException("invalid input stream");
		else if(length != HEADER_SIZE)
			return null;
		
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		
		int version = buf.getInt();
		int size = buf.getInt();
		
		if(version != VERSION)
			throw new APIVersionException("expected version " + VERSION + ", got " + version);
		
		byte[] message = new byte[size];
		
		int bytes_read = 0;
		while(bytes_read < size)
			bytes_read += in.read(message, bytes_read, size - bytes_read);
		
		return new Frame(version, Message.parseFrom(message));
	}
	
	private void setPayload(Message payload) {
		this.payload = payload;
		this.size = payload.toByteArray().length;
	}
	
	public byte[] toByteArray() {
		byte[] bytes = new byte[HEADER_SIZE + this.getSize()];
		
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		
		buf.putInt(this.getVersion());
		buf.putInt(this.getSize());
		buf.put(this.payload.toByteArray());
		
		return bytes;
	}
	
}
