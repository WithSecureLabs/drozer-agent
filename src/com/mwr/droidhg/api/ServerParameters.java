package com.mwr.droidhg.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.X509KeyManager;

import com.mwr.common.tls.X509Fingerprint;
import com.mwr.droidhg.Agent;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class ServerParameters extends ConnectorParameters implements
		OnSharedPreferenceChangeListener {

	public interface OnChangeListener {

		public void onChange(ServerParameters parameters);

	}

	private KeyManagerFactory key_manager_factory = null;
	private char[] key_password = null;
	private String keystore_path = null;
	private char[] keystore_password = null;
	private OnChangeListener on_change_listener = null;
	private int port = 31415;
	private boolean ssl = false;

	public ServerParameters() {
		this.setFromPreferences();
	}

	public ServerParameters(int port) {
		this.setPort(port);
	}

	private void clearKeyManagerFactory() {
		this.key_manager_factory = null;
	}
	
	public String getCertificateFingerprint() {
		return new X509Fingerprint(((X509KeyManager)this.getKeyManagerFactory().getKeyManagers()[0]).getCertificateChain("mercury")[0]).toString();
	}

	/**
	 * Create a KeyManagerFactory based on the key material provided through the
	 * configuration.
	 * 
	 * This factory is cached, so if the key material is updated
	 * clearKeyManagerFactory() must be called to cause it to take effect.
	 */
	private KeyManagerFactory getKeyManagerFactory() {
		if (this.key_manager_factory == null) {
			try {

				KeyStore key_store = KeyStore.getInstance("BKS");
				key_store.load(new FileInputStream(this.keystore_path),
						this.keystore_password);
				this.key_manager_factory = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				this.key_manager_factory.init(key_store, this.key_password);
			} catch (CertificateException e) {
				Log.e("getKeyManagerFactory", e.getMessage());
			} catch (IOException e) {
				Log.e("getKeyManagerFactory", e.getMessage());
			} catch (KeyStoreException e) {
				Log.e("getKeyManagerFactory", e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				Log.e("getKeyManagerFactory", e.getMessage());
			} catch (UnrecoverableKeyException e) {
				Log.e("getKeyManagerFactory", e.getMessage());
			}
		}

		return this.key_manager_factory;
	}

	public int getPort() {
		return this.port;
	}

	public boolean isSSL() {
		return this.ssl;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("server_port") ||
				key.equals("server_ssl") || 
				key.equals("ssl_keystore_path") || 
				key.equals("ssl_keystore_password") || 
				key.equals("ssl_key_password"))
			this.setFromPreferences();
	}

	public void setPort(int port) {
		this.port = port;

		if (this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public void setOnChangeListener(OnChangeListener listener) {
		this.on_change_listener = listener;
	}

	public void setFromPreferences() {
		this.setPort(Integer.parseInt(Agent.getSettings().getString(
				"server_port", "31415")));
		this.setSSL(Agent.getSettings().getBoolean("server_ssl", false));

		if (this.isSSL()) {
			this.keystore_path = Agent.getSettings().getString(
					"ssl_keystore_path",
					"/data/data/com.mwr.droidhg.agent/files/mercury.bks");
			this.keystore_password = Agent.getSettings()
					.getString("ssl_keystore_password", "mercury")
					.toCharArray();
			this.key_password = Agent.getSettings()
					.getString("ssl_key_password", "mercury").toCharArray();
		}

		this.clearKeyManagerFactory();

		Agent.getSettings().registerOnSharedPreferenceChangeListener(this);
	}

	public void setSSL(boolean ssl) {
		this.ssl = ssl;

		if (this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}

	public ServerSocket toServerSocket() throws IOException {
		if (this.ssl)
			return this.toSSLServerSocket();
		else
			return new ServerSocket(this.port);
	}

	public SSLServerSocket toSSLServerSocket() throws IOException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(this.getKeyManagerFactory().getKeyManagers(), null,
					null);

			return (SSLServerSocket) context.getServerSocketFactory()
					.createServerSocket(this.port);
		} catch (KeyManagementException e) {
			Log.e("toSSLServerSocket", e.getMessage());
			return null;
		} catch (NoSuchAlgorithmException e) {
			Log.e("toSSLServerSocket", e.getMessage());
			return null;
		} catch (NullPointerException e) {
			Log.e("toSSLServerSocket",
					"Null Pointer: most likely the KeyManagerFactory was not intialised properly");
			return null;
		}
	}

	public boolean update(ServerParameters parameters) {
		Editor editor = Agent.getSettings().edit();

		editor.remove("server_port");
		editor.putString("server_port", Integer.valueOf(parameters.getPort())
				.toString());

		return editor.commit();
	}

}
