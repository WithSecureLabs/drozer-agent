package com.mwr.droidhg.api.builders;

import android.provider.Settings;

import com.mwr.droidhg.Agent;
import com.mwr.cinnibar.api.Protobuf.Message;
import com.mwr.cinnibar.api.Protobuf.Message.SystemResponse;
import com.mwr.droidhg.connector.Session;

public class SystemResponseFactory {
	
	private SystemResponse.Builder builder = null;
	
	public SystemResponseFactory addSession(Session session) {
		this.builder.addSessions(Message.Session.newBuilder()
				.setId(session.getSessionId())
				.setDeviceId(Settings.Secure.getString(Agent.getInstance().getMercuryContext().getContentResolver(), Settings.Secure.ANDROID_ID)));
		
		return this;
	}
	
	public static SystemResponseFactory deviceList(Message request) {
		return new SystemResponseFactory(SystemResponse.ResponseType.DEVICE_LIST, SystemResponse.ResponseStatus.SUCCESS).addDevice();
	}
	
	public static SystemResponseFactory pong(Message ping) {
		return new SystemResponseFactory(SystemResponse.ResponseType.PONG, SystemResponse.ResponseStatus.SUCCESS);
	}
	
	public static SystemResponseFactory session(Session session) {
		return new SystemResponseFactory(SystemResponse.ResponseType.SESSION_ID, SystemResponse.ResponseStatus.SUCCESS).setSession(session);
	}
	
	private SystemResponseFactory(SystemResponse.ResponseType type, SystemResponse.ResponseStatus status) {
		this.builder = SystemResponse.newBuilder().setType(type).setStatus(status);
	}
	
	public SystemResponseFactory addDevice() {
		this.builder.addDevices(Message.Device.newBuilder()
				.setId(Agent.getInstance().getUID())
				.setManufacturer(android.os.Build.MANUFACTURER)
				.setModel(android.os.Build.MODEL)
				.setSoftware(android.os.Build.VERSION.RELEASE));

		return this;
	}
	
	public SystemResponse build() {
		return this.builder.build();
	}
	
	public SystemResponseFactory isError() {
		this.builder.setStatus(SystemResponse.ResponseStatus.ERROR);
		
		return this;
	}
	
	public SystemResponseFactory isSuccess() {
		this.builder.setStatus(SystemResponse.ResponseStatus.SUCCESS);
		
		return this;
	}
	
	public static SystemResponseFactory sessionList(Message request) {
		return new SystemResponseFactory(SystemResponse.ResponseType.SESSION_LIST, SystemResponse.ResponseStatus.SUCCESS);//addDevice();
	}
	
	public SystemResponseFactory setSession(Session session) {
		this.builder.setSessionId(session.getSessionId());
		
		return this;
	}
	
}
