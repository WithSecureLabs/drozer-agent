package com.mwr.jdiesel.api.builders;

import com.mwr.jdiesel.api.Protobuf.Message;
import com.mwr.jdiesel.api.Protobuf.Message.SystemRequest;

public class SystemRequestFactory {
	
	private SystemRequest.Builder builder = null;
	
	public static SystemRequestFactory bind() {
		return new SystemRequestFactory(SystemRequest.RequestType.BIND_DEVICE);
	}
	
	public static SystemRequestFactory unbind() {
		return new SystemRequestFactory(SystemRequest.RequestType.UNBIND_DEVICE);
	}
	
	private SystemRequestFactory(SystemRequest.RequestType type) {
		this.builder = SystemRequest.newBuilder().setType(type);
	}
	
	public SystemRequest build() {
		return this.builder.build();
	}
	
	public SystemRequestFactory setDevice(String device_id, String manufacturer, String model, String software) {
		this.builder.setDevice(Message.Device.newBuilder()
				.setId(device_id)
				.setManufacturer(manufacturer)
				.setModel(model)
				.setSoftware(software));
		
		return this;
	}
	
}
