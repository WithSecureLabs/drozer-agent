package com.mwr.jdiesel.api.transport;

public interface SecureTransport {

	public abstract String getHostCertificateFingerprint();
	public abstract String getPeerCertificateFingerprint();
	
}
