package com.mwr.jdiesel.api.connectors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import android.os.Bundle;

public class Server extends Connector {
	
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

	private KeyManager[] key_managers = null;
	private char[] key_password = null;
	private String keystore_path = null;
	private char[] keystore_password = null;
	private OnChangeListener on_change_listener = null;
	private String password = null;
	private int port = 31415;
	private boolean ssl = false;
	
	private OnDetailedStatusListener on_detailed_status_listener;

	public Server() {}

//	public Server(int port) {
//		this.setPort(port);
//	}
	
	public KeyManager[] getKeyManagers() throws CertificateException, FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		if (this.key_managers == null) {
			KeyStore key_store = KeyStore.getInstance("BKS");
			key_store.load(new FileInputStream(this.keystore_path), this.keystore_password);
			
			KeyManagerFactory key_manager_factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			key_manager_factory.init(key_store, this.key_password);
			
			this.key_managers = key_manager_factory.getKeyManagers();
		}

		return this.key_managers;
	}
	
	public String getPassword() {
		return this.password;
	}

	public int getPort() {
		return this.port;
	}
	
	public boolean hasPassword() {
		return this.password != null && this.password != "";
	}

	public boolean isSSL() {
		return this.ssl;
	}

	public void resetKeyManagerFactory() {
		this.key_managers = null;
	}
	
	public void setDetailedStatus(Bundle status) {
		if(this.on_detailed_status_listener != null)
			this.on_detailed_status_listener.onDetailedStatus(status);
	}
	
	public void setKeyPassword(char[] password) {
		this.key_password = password;
	}
	
	public void setKeyStorePassword(char[] password) {
		this.keystore_password = password;
	}
	
	public void setKeyStorePath(String path) {
		this.keystore_path = path;
	}

	public void setOnChangeListener(OnChangeListener listener) {
		this.on_change_listener = listener;
	}
	
	public void setOnDetailedStatusListener(OnDetailedStatusListener listener) {
		this.on_detailed_status_listener = listener;
	}

	public void setPassword(String password) {
		this.password = password;

		if(this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public void setPort(int port) {
		this.port = port;

		if(this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public void setSSL(boolean ssl) {
		this.ssl = ssl;

		if(this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}
	
	@Override
	public boolean verifyPassword(String password) {
		return this.getPassword() == null && (password == null || password.equals("")) || password.equals(this.getPassword());
	}

}
