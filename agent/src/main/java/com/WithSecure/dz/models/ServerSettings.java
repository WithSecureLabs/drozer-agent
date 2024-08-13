package com.WithSecure.dz.models;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.WithSecure.dz.Agent;
import com.WithSecure.jsolar.api.connectors.Server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ServerSettings implements OnSharedPreferenceChangeListener {
	
	private Server server;
	
	private char[] getKeyPassword() {
		return this.getSettings().getString(Server.SERVER_KEY_PASSWORD, "drozer").toCharArray();
	}
	
	private char[] getKeyStorePassword() {
		return this.getSettings().getString(Server.SERVER_KEYSTORE_PASSWORD, "drozer").toCharArray();
	}
	
	private String getKeyStorePath() {
		return this.getSettings().getString(Server.SERVER_KEYSTORE_PATH, "/data/data/com.withsecure.dz/files/drozer.bks");
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
		this.server = server;
		
		server.setPort(this.getPort());
		server.setPassword(this.getPassword());
		server.setSSL(this.isSSL());

		if(this.isSSL()) {
			server.setSSL(this.isSSL());
			server.setKeyStorePath(this.getKeyStorePath());
			server.setKeyStorePassword(this.getKeyStorePassword());
			server.setKeyPassword(this.getKeyPassword());
		}

		server.resetKeyManagerFactory();

		this.getSettings().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(Server.SERVER_PORT))
			this.server.setPort(this.getPort());
		if(key.equals(Server.SERVER_PASSWORD))
			this.server.setPassword(this.getPassword());
		
		if(key.equals(Server.SERVER_SSL)) {
			this.server.setSSL(this.isSSL());
			server.setKeyStorePath(this.getKeyStorePath());
			server.setKeyStorePassword(this.getKeyStorePassword());
			server.setKeyPassword(this.getKeyPassword());
		}
		
		if(key.equals(Server.SERVER_KEYSTORE_PATH))
			server.setKeyStorePath(this.getKeyStorePath());
		if(key.equals(Server.SERVER_KEYSTORE_PASSWORD))
			server.setKeyStorePassword(this.getKeyStorePassword());
		if(key.equals(Server.SERVER_KEY_PASSWORD))
			server.setKeyPassword(this.getKeyPassword());
	}
	
	public boolean save(Server server) {
		SharedPreferences.Editor editor = Agent.getInstance().getSettings().edit();

		editor.remove(Server.SERVER_PORT);
		editor.putString(Server.SERVER_PORT, Integer.valueOf(this.getPort()).toString());

		return editor.commit();
	}

	public static String interfacesAsString() {
        try {
			List<String> ipAddrs = new ArrayList<>();

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface i = interfaces.nextElement();

				if (!i.isUp()) {
					continue;
				}

				Enumeration<InetAddress> ips = i.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ip = ips.nextElement();

					if (ip.isLinkLocalAddress() || ip.isLoopbackAddress()) {
						continue;
					}

					ipAddrs.add(ip.getHostAddress());
				}
			}

			return String.join(", ", ipAddrs);
        } catch (Exception ignored) { }
		return "could not retrieve interfaces";
    }

}
