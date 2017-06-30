package com.mwr.common.tls.trust_managers;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.net.ssl.X509TrustManager;

/**
 * The KeyStoreTrustManager verifies that the given X509Certificate is signed by
 * a public key contained in a KeyStore of trusted CA certificates.
 * 
 * This does not perform any other checks, such as CRL or CN.
 */
public class KeyStoreTrustManager implements X509TrustManager {

	private KeyStore key_store = null;
	
	public KeyStoreTrustManager(InputStream stream, char[] password) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
		this.key_store = KeyStore.getInstance("BKS");
		this.key_store.load(stream, password);
	}
	
	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
		throw new CertificateException("client verification not supported");
	}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
		try {
			Enumeration<String> aliases = this.key_store.aliases();
			
			while(aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				
				if(this.key_store.isCertificateEntry(alias)) {
					try {
						chain[0].verify(this.key_store.getCertificate(alias).getPublicKey());
						
						return;
					}
					catch(InvalidKeyException e) {}
					catch(NoSuchAlgorithmException e) {}
					catch(NoSuchProviderException e) {}
					catch(NullPointerException e) {}
					catch(SignatureException e) {}
				}
			}
		}
		catch(KeyStoreException e) {}
			
		throw new CertificateException("ssl certificate is not trusted");
	}

	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}
