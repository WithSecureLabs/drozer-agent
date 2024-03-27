package com.WithSecure.jsolar.api.connectors;

import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

public class Server extends Connector{

    public static final String SERVER_KEY_PASSWORD = "server:key:password";
    public static final String SERVER_KEYSTORE_PASSWORD = "server:ks:password";
    public static final String SERVER_KEYSTORE_PATH = "server:ks:path";
    public static final String SERVER_PASSWORD = "server:password";
    public static final String SERVER_PORT = "server:port";
    public static final String SERVER_SSL = "server:ssl";

    public interface OnChangeListener {
        public void onChange(Server parameters);
    }

    public interface OnDetailedStatusListener {
        public void onDetailedStatus(Bundle status);
    }

    private KeyManager[] keyManagers = null;
    private char[] keyPassword = null;
    private String keyStorePath = null;
    private char[] keyStorePassword = null;
    private OnChangeListener onChangeListener = null;
    private String password = null;
    private int port = 31415;
    private boolean ssl = false;

    private OnDetailedStatusListener onDetailedStatusListener;

    public Server() {}

    //TODO Implement functionality for this
    public Server(int port) {
        this.setPort(port);
    }

    public KeyManager[] getKeyManagers() throws CertificateException, FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (this.keyManagers == null) {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(new FileInputStream(this.keyStorePath), this.keyStorePassword);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, this.keyPassword);
        }

        return this.keyManagers;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public boolean hasPassword() {
        return this.password != null && this.password != "";
    }

    public boolean isSSL() {
        return this.ssl;
    }

    public void resetKeyManagerFactory() {
        this.keyManagers = null;
    }

    public void setDetailedStatus(Bundle status) {
        if(this.onDetailedStatusListener != null) {
            this.onDetailedStatusListener.onDetailedStatus(status);
        }
    }

    public void setKeyPassword(char [] password) {
        this.keyPassword = password;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOnDetailedStatusListener(OnDetailedStatusListener onDetailedStatusListener) {
        this.onDetailedStatusListener = onDetailedStatusListener;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }

    //Allows for blank passwords
    @Override
    public boolean verifyPassword(String password) {
        Log.i("ServerPassword","Guessed password was : "  + password + " Actual password is: " + this.getPassword());
        return this.getPassword() == null && (password == null || password.equals("")) || password.equals(this.getPassword());
    }
}
