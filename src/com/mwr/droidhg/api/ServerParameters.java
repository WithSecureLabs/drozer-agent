package com.mwr.droidhg.api;

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

import com.mwr.droidhg.Agent;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

public class ServerParameters extends ConnectorParameters implements OnSharedPreferenceChangeListener {

	public interface OnChangeListener {

		public void onChange(ServerParameters parameters);

	}
	
	public interface OnDetailedStatusListener {
		
		public void onDetailedStatus(Bundle status);
		
	}

	private KeyManagerFactory key_manager_factory = null;
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
		this.key_manager_factory = null;
	}
	/**
	 * Create a KeyManagerFactory based on the key material provided through the
	 * configuration.
	 * 
	 * This factory is cached, so if the key material is updated
	 * clearKeyManagerFactory() must be called to cause it to take effect.
	 */
	private KeyManagerFactory getKeyManagerFactory() throws CertificateException, FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		if (this.key_manager_factory == null) {
			KeyStore key_store = KeyStore.getInstance("BKS");
			key_store.load(new FileInputStream(this.keystore_path), this.keystore_password);
			this.key_manager_factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			this.key_manager_factory.init(key_store, this.key_password);
		}

		return this.key_manager_factory;
	}
	
	public KeyManager[] getKeyManagers() throws CertificateException, FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		return this.getKeyManagerFactory().getKeyManagers();
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
		if(key.equals("server_port") ||
				key.equals("server_password") ||
				key.equals("server_ssl") || 
				key.equals("ssl_keystore_path") || 
				key.equals("ssl_keystore_password") || 
				key.equals("ssl_key_password"))
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
		this.setPort(Integer.parseInt(Agent.getInstance().getSettings().getString("server_port", "31415")));
		this.setPassword(Agent.getInstance().getSettings().getString("server_password", ""));
		this.setSSL(Agent.getInstance().getSettings().getBoolean("server_ssl", false));

		if(this.isSSL()) {
			this.keystore_path = Agent.getInstance().getSettings().getString("ssl_keystore_path", "/data/data/com.mwr.droidhg.agent/files/mercury.bks");
			this.keystore_password = Agent.getInstance().getSettings().getString("ssl_keystore_password", "mercury").toCharArray();
			this.key_password = Agent.getInstance().getSettings().getString("ssl_key_password", "mercury").toCharArray();
		}

		this.clearKeyManagerFactory();

		Agent.getInstance().getSettings().registerOnSharedPreferenceChangeListener(this);
	}

	public void setPassword(String password) {
		this.password = password;

		if (this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public void setPort(int port) {
		this.port = port;

		if (this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public void setSSL(boolean ssl) {
		this.ssl = ssl;

		if (this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public boolean update(ServerParameters parameters) {
		Editor editor = Agent.getInstance().getSettings().edit();

		editor.remove("server_port");
		editor.putString("server_port", Integer.valueOf(parameters.getPort()).toString());

		return editor.commit();
	}
	
	@Override
	public boolean verifyPassword(String password) {
		return this.getPassword() == null && (password == null || password.equals("")) || password.equals(this.getPassword());
	}

}
