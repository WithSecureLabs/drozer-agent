package com.mwr.jdiesel.api.builders;

import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.Protobuf.Message.SystemResponse;

public class SystemResponseFactory {
	
	private SystemResponse.Builder builder = null;
	
	public static SystemResponseFactory deviceList(Message request) {
		return new SystemResponseFactory(SystemResponse.ResponseType.DEVICE_LIST, SystemResponse.ResponseStatus.SUCCESS);
	}
	
	public static SystemResponseFactory pong(Message ping) {
		return new SystemResponseFactory(SystemResponse.ResponseType.PONG, SystemResponse.ResponseStatus.SUCCESS);
	}
	
	public static SystemResponseFactory session(String session_id) {
		return new SystemResponseFactory(SystemResponse.ResponseType.SESSION_ID, SystemResponse.ResponseStatus.SUCCESS).setSessionId(session_id);
	}
	
	private SystemResponseFactory(SystemResponse.ResponseType type, SystemResponse.ResponseStatus status) {
		this.builder = SystemResponse.newBuilder().setType(type).setStatus(status);
	}
	
	public SystemResponseFactory addDevice(String device_id, String manufacturer, String model, String software) {
		this.builder.addDevices(Message.Device.newBuilder()
				.setId(device_id)
				.setManufacturer(manufacturer)
				.setModel(model)
				.setSoftware(software));

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
	
	public SystemResponseFactory setSessionId(String session_id) {
		this.builder.setSessionId(session_id);
		
		return this;
	}
	
}
