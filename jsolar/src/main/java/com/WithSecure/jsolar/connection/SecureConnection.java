package com.WithSecure.jsolar.connection;

public interface SecureConnection {

    public String getHostCertificateFingerprint();
    public String getPeerCertificateFingerprint();

}