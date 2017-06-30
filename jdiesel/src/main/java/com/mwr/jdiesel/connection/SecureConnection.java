package com.mwr.jdiesel.connection;

public interface SecureConnection {

	public String getHostCertificateFingerprint();
	public String getPeerCertificateFingerprint();
	
}
