package com.mwr.jdiesel.api;

public class DeviceInfo {
	
	private String android_id;
	private String manufacturer;
	private String model;
	private String software;
	
	public DeviceInfo(String android_id, String manufacturer, String model, String software) {
		this.android_id = android_id;
		this.manufacturer = manufacturer;
		this.model = model;
		this.software = software;
	}
	
	public String getAndroidID() {
		return this.android_id;
	}
	
	public String getManufacturer() {
		return this.manufacturer;
	}
	
	public String getModel() {
		return this.model;
	}
	
	public String getSoftware() {
		return this.software;
	}

}
