package com.mwr.common.tls.trust_managers;

import java.security.cert.CertificateException;

import javax.net.ssl.X509TrustManager;

/**
 * The NaiveTrustManager is naive because it trusts all certificates to identify
 * servers and clients. It has no trusted issuers.
 * 
 * This allows us to trust self-signed certificates.
 * 
 * NB: you'll obviously need some other check to ensure a peer's identity.
 */
public class NaiveTrustManager implements X509TrustManager {
	
	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}
	
}
