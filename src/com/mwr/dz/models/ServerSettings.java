package com.mwr.dz.models;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.mwr.dz.Agent;
import com.mwr.jdiesel.api.connectors.Server;

public class ServerSettings implements OnSharedPreferenceChangeListener {
	
	private char[] getKeyPassword() {
		return this.getSettings().getString(Server.SERVER_KEY_PASSWORD, "mercury").toCharArray();
	}
	
	private char[] getKeyStorePassword() {
		return this.getSettings().getString(Server.SERVER_KEYSTORE_PASSWORD, "mercury").toCharArray();
	}
	
	private String getKeyStorePath() {
		return this.getSettings().getString(Server.SERVER_KEYSTORE_PATH, "/data/data/com.mwr.dz/files/mercury.bks");
	}
	
	private String getPassword() {
		return this.getSettings().getString(Server.SERVER_PASSWORD, "");
	}
	
	private int getPort() {
		return Integer.parseInt(this.getSettings().getString(Server.SERVER_PORT, "31415"));
	}
	
	private SharedPreferences getSettings() {
		return Agent.getInstance().getSettings();
	}
	
	private boolean isSSL() {
		return this.getSettings().getBoolean(Server.SERVER_SSL, false);
	}
	
	public void load(Server server) {
		server.setPort(this.getPort());
		server.setPassword(this.getPassword());
		server.setSSL(this.isSSL());

		if(this.isSSL()) {
			server.setKeyStorePath(this.getKeyStorePath());
			server.setKeyStorePassword(this.getKeyStorePassword());
			server.setKeyPassword(this.getKeyPassword());
		}

		server.resetKeyManagerFactory();

		this.getSettings().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//		if(key.equals(Server.SERVER_PORT) ||
//				key.equals(Server.SERVER_PASSWORD) ||
//				key.equals(Server.SERVER_SSL) || 
//				key.equals(Server.SERVER_KEYSTORE_PATH) || 
//				key.equals(Server.SERVER_KEYSTORE_PASSWORD) || 
//				key.equals(Server.SERVER_KEY_PASSWORD))
	}
	
	public boolean save(Server server) {
		SharedPreferences.Editor editor = Agent.getInstance().getSettings().edit();

		editor.remove(Server.SERVER_PORT);
		editor.putString(Server.SERVER_PORT, Integer.valueOf(this.getPort()).toString());

		return editor.commit();
	}

}
