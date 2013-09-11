package com.mwr.dz.services;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.mwr.dz.Agent;
import com.mwr.dz.R;
import com.mwr.dz.models.ServerSettings;
import com.mwr.jdiesel.api.connectors.Connector;
import com.mwr.jdiesel.api.links.Server;

public class ServerService extends ConnectorService {
	
	public static final int MSG_GET_DETAILED_SERVER_STATUS = 21;
	public static final int MSG_GET_SERVER_STATUS = 22;
	public static final int MSG_GET_SSL_FINGERPRINT = 23;
	public static final int MSG_START_SERVER = 24;
	public static final int MSG_STOP_SERVER = 25;
	
	private Server server = null;
	private com.mwr.jdiesel.api.connectors.Server server_parameters = new com.mwr.jdiesel.api.connectors.Server();
	
	public Bundle getDetailedStatus() {
		Bundle data = new Bundle();
		
		data.putBoolean(Connector.CONNECTOR_ENABLED, server_parameters.isEnabled());
		data.putBoolean(com.mwr.jdiesel.api.connectors.Server.SERVER_PASSWORD, server_parameters.hasPassword());
    	data.putBoolean(com.mwr.jdiesel.api.connectors.Server.SERVER_SSL, server_parameters.isSSL());
    	
    	switch(server_parameters.getStatus()) {
    	case ACTIVE:
    		data.putBoolean(Connector.CONNECTOR_CONNECTED, true);
    		data.putBoolean(Connector.CONNECTOR_OPEN_SESSIONS, true);
    		break;
    		
    	case CONNECTING:
    		data.putBoolean(Connector.CONNECTOR_CONNECTED, false);
    		data.putBoolean(Connector.CONNECTOR_OPEN_SESSIONS, false);
    		break;
    		
    	case ONLINE:
    		data.putBoolean(Connector.CONNECTOR_CONNECTED, true);
    		data.putBoolean(Connector.CONNECTOR_OPEN_SESSIONS, false);
    		break;
    		
    	default:
    		data.putBoolean(Connector.CONNECTOR_CONNECTED, false);
    		data.putBoolean(Connector.CONNECTOR_OPEN_SESSIONS, false);
    		break;
    	}
    	
    	return data;
	}
	
	public Bundle getServerFingerprint() {
		Bundle data = new Bundle();

		if(this.server != null)
			data.putString(Connector.CONNECTOR_SSL_FINGERPRINT, this.server.getHostCertificateFingerprint());
		else
			data.putString(Connector.CONNECTOR_SSL_FINGERPRINT, "No running server.");
		
		return data;
	}
	
	public Bundle getStatus() {
		Bundle data = new Bundle();
		
		data.putInt("server", this.server_parameters.getStatus().ordinal());
		
		return data;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case MSG_GET_DETAILED_SERVER_STATUS:
			try {
				Message message = Message.obtain(null, MSG_GET_DETAILED_SERVER_STATUS);
				message.setData(this.getDetailedStatus());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), e.getMessage());
			}
			break;
			
		case MSG_GET_SERVER_STATUS:
			try {
				Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
				message.setData(this.getStatus());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), e.getMessage());
			}
			break;
			
		case MSG_GET_SSL_FINGERPRINT:
			try {
				Message message = Message.obtain(null, MSG_GET_SSL_FINGERPRINT);
				message.setData(this.getServerFingerprint());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), e.getMessage());
			}
			break;
			
		case MSG_START_SERVER:
			try {
				this.startServer();
				
				Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
				message.setData(this.getStatus());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), e.getMessage());
			}
			break;
			
		case MSG_STOP_SERVER:
			try {
				this.stopServer();

				Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
				message.setData(this.getStatus());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), e.getMessage());
			}
			break;
		}	
	
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		int ret_val = super.onStartCommand(intent, flags, startId);
		
		if(intent != null && intent.getCategories() != null && intent.getCategories().contains("com.mwr.dz.START_EMBEDDED")) {
			Agent.getInstance().setContext(this.getApplicationContext());
			this.startServer();
		}
		
		return ret_val;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ServerService.running = true;
		
		this.server_parameters.addObserver(new Observer() {

			@Override
			public void update(Observable arg0, Object arg1) {
				Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
				message.setData(ServerService.this.getStatus());
						
				ServerService.this.sendToAllMessengers(message);
			}
			
		});
		
		this.server_parameters.setStatus(Connector.Status.OFFLINE);
	}
	
	@Override
	public void onDestroy() {
		ServerService.running = false;
	}
	
	public static void startAndBindToService(Context context, ServiceConnection serviceConnection) {
		if(!ServerService.running)
			context.startService(new Intent(context, ServerService.class));

		Intent intent = new Intent(context, ServerService.class);
    	context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void startServer() {
		if(this.server == null) {
			(new ServerSettings()).load(this.server_parameters);
			
			this.server_parameters.enabled = true;
			this.server = new Server(this.server_parameters, Agent.getInstance().getDeviceInfo());
			this.server.setLogger(this.server_parameters.getLogger());
			this.server_parameters.getLogger().addOnLogMessageListener(this);
			
			this.server.start();
			
			Toast.makeText(this, String.format(Locale.ENGLISH, this.getString(R.string.embedded_server_started), this.server_parameters.getPort()), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void stopServer() {
		if(this.server != null) {
			this.server_parameters.enabled = false;
			this.server.stopConnector();
			
			Toast.makeText(this, String.format(Locale.ENGLISH, this.getString(R.string.embedded_server_stopped), this.server_parameters.getPort()), Toast.LENGTH_SHORT).show();
			
			this.server = null;
		}
	}

}
