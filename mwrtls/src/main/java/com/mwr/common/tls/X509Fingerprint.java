package com.mwr.common.tls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class X509Fingerprint {
	
	public static final String ALGORITHM = "SHA-1";
	private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',  '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	private X509Certificate certificate;

	public X509Fingerprint(X509Certificate certificate) {
		this.certificate = certificate;
	}
	
	public byte[] getDER() throws CertificateEncodingException {
		return this.certificate.getEncoded();
	}
	
	public byte[] getFingerprint() throws CertificateEncodingException {
		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
			
			digest.update(this.getDER());
	    	return digest.digest();
		}
		catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	private String hexify(byte bytes[]) {
    	StringBuffer buf = new StringBuffer(bytes.length * 2);

        for(int i=0; i<bytes.length; ++i) {
        	buf.append(HEX_DIGITS[(bytes[i] & 0xf0) >> 4]);
            buf.append(HEX_DIGITS[(bytes[i] & 0x0f)]);
            buf.append(':');
        }

        return buf.substring(0, buf.length() - 1).toString();
    }
	
	public String toString() {
		try {
			return this.hexify(this.getFingerprint());
		}
		catch(CertificateEncodingException e) {
			return "was not able to fingerprint certificate";
		}
	}
	
}
