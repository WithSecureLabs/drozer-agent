package com.mwr.dz.connector;

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

import com.mwr.dz.Agent;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

public class ServerParameters extends Connector implements OnSharedPreferenceChangeListener {
	
	public static final String SERVER_KEY_PASSWORD = "server:key:password";
	public static final String SERVER_KEYSTORE_PASSWORD = "server:ks:password";
	public static final String SERVER_KEYSTORE_PATH = "server:ks:path";
	public static final String SERVER_PASSWORD = "server:password";
	public static final String SERVER_PORT = "server:port";
	public static final String SERVER_SSL = "server:ssl";

	public interface OnChangeListener {

		public void onChange(ServerParameters parameters);

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

	public ServerParameters() {
		this.setFromPreferences();
	}

	public ServerParameters(int port) {
		this.setPort(port);
	}

	private void clearKeyManagerFactory() {
		this.key_managers = null;
	}
	
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(SERVER_PORT) ||
				key.equals(SERVER_PASSWORD) ||
				key.equals(SERVER_SSL) || 
				key.equals(SERVER_KEYSTORE_PATH) || 
				key.equals(SERVER_KEYSTORE_PASSWORD) || 
				key.equals(SERVER_KEY_PASSWORD))
			this.setFromPreferences();
	}
	
	public void setDetailedStatus(Bundle status) {
		if(this.on_detailed_status_listener != null)
			this.on_detailed_status_listener.onDetailedStatus(status);
	}

	public void setOnChangeListener(OnChangeListener listener) {
		this.on_change_listener = listener;
	}
	
	public void setOnDetailedStatusListener(OnDetailedStatusListener listener) {
		this.on_detailed_status_listener = listener;
	}

	public void setFromPreferences() {
		this.setPort(Integer.parseInt(Agent.getInstance().getSettings().getString(SERVER_PORT, "31415")));
		this.setPassword(Agent.getInstance().getSettings().getString(SERVER_PASSWORD, ""));
		this.setSSL(Agent.getInstance().getSettings().getBoolean(SERVER_SSL, false));

		if(this.isSSL()) {
			this.keystore_path = Agent.getInstance().getSettings().getString(SERVER_KEYSTORE_PATH, "/data/data/com.mwr.dz/files/mercury.bks");
			this.keystore_password = Agent.getInstance().getSettings().getString(SERVER_KEYSTORE_PASSWORD, "mercury").toCharArray();
			this.key_password = Agent.getInstance().getSettings().getString(SERVER_KEY_PASSWORD, "mercury").toCharArray();
		}

		this.clearKeyManagerFactory();

		Agent.getInstance().getSettings().registerOnSharedPreferenceChangeListener(this);
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

	public boolean update(ServerParameters parameters) {
		Editor editor = Agent.getInstance().getSettings().edit();

		editor.remove(SERVER_PORT);
		editor.putString(SERVER_PORT, Integer.valueOf(parameters.getPort()).toString());

		return editor.commit();
	}
	
	@Override
	public boolean verifyPassword(String password) {
		return this.getPassword() == null && (password == null || password.equals("")) || password.equals(this.getPassword());
	}

}
