package com.mwr.droidhg.api.builders;

import android.provider.Settings;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.api.Protobuf.Message;
import com.mwr.droidhg.api.Protobuf.Message.SystemRequest;

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
	
	public SystemRequestFactory setDevice() {
		this.builder.setDevice(Message.Device.newBuilder()
				.setId(Settings.Secure.getString(Agent.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
				.setManufacturer(android.os.Build.MANUFACTURER)
				.setModel(android.os.Build.MODEL)
				.setSoftware(android.os.Build.VERSION.RELEASE));
		
		return this;
	}
	
}
