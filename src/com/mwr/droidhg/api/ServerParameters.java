package com.mwr.droidhg.api;

import java.io.IOException;
import java.net.ServerSocket;

import com.mwr.droidhg.Agent;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class ServerParameters extends ConnectorParameters implements OnSharedPreferenceChangeListener {
	
	public interface OnChangeListener {
		
		public void onChange(ServerParameters parameters);
		
	}
	
	private OnChangeListener on_change_listener = null;
	private int port = 31415;
	
	public ServerParameters() {
		this.setPortFromPreferences();
	}
	
	public ServerParameters(int port) {
		this.setPort(port);
	}
	
	public int getPort() {
		return this.port;
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
		return new ServerSocket(this.port);
	}

	public boolean update(ServerParameters parameters) {
		Editor editor = Agent.getSettings().edit();
		
		editor.remove("server_port");
		editor.putString("server_port", Integer.valueOf(parameters.getPort()).toString());
		
		return editor.commit();
	}
	
}
