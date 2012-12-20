package com.mwr.droidhg.api;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import com.mwr.droidhg.Agent;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class ServerParameters extends ConnectorParameters implements OnSharedPreferenceChangeListener {
	
	public interface OnChangeListener {
		
		public void onChange(ServerParameters parameters);
		
	}
	
	private OnChangeListener on_change_listener = null;
	private int port = 31415;
	private boolean ssl = true;
	
	public ServerParameters() {
		this.setPortFromPreferences();
	}
	
	public ServerParameters(int port) {
		this.setPort(port);
	}
	
	public int getPort() {
		return this.port;
	}
	
	public boolean isSSL() {
		return this.ssl;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("server_port"))
			this.setPortFromPreferences();
	}
	
	public void setPort(int port) {
		this.port = port;
		
		if(this.on_change_listener != null)
			this.on_change_listener.onChange(this);
	}
	
	public void setOnChangeListener(OnChangeListener listener) {
		this.on_change_listener = listener;
	}
	
	public void setPortFromPreferences() {
		this.setPort(Integer.parseInt(Agent.getSettings().getString("server_port", "31415")));
		
		Agent.getSettings().registerOnSharedPreferenceChangeListener(this);
	}
	
	public ServerSocket toServerSocket() throws IOException {
		if(this.ssl)
			return this.toSSLServerSocket();
		else
			return new ServerSocket(this.port);
	}
	
	public SSLServerSocket toSSLServerSocket() throws IOException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(Agent.getKeyManagerFactory().getKeyManagers(), null, null);
			
			return (SSLServerSocket)context.getServerSocketFactory().createServerSocket(this.port);
		}
		catch(KeyManagementException e) {
			Log.e("toSSLServerSocket", e.getMessage());
			return null;
		}
		catch(NoSuchAlgorithmException e) {
			Log.e("toSSLServerSocket", e.getMessage());
			return null;
		}
		catch(NullPointerException e) {
			Log.e("toSSLServerSocket", "Null Pointer: most likely the KeyManagerFactory was not intialised properly");
			return null;
		}
	}

	public boolean update(ServerParameters parameters) {
		Editor editor = Agent.getSettings().edit();
		
		editor.remove("server_port");
		editor.putString("server_port", Integer.valueOf(parameters.getPort()).toString());
		
		return editor.commit();
	}
	
}
