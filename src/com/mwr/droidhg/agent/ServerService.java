package com.mwr.droidhg.agent;

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

import com.mwr.droidhg.api.ServerParameters;
import com.mwr.droidhg.connector.Server;

public class ServerService extends ConnectorService {
	
	public static final int MSG_GET_SERVER_STATUS = 10;
	public static final int MSG_START_SERVER = 11;
	public static final int MSG_STOP_SERVER = 12;
	public static final int MSG_GET_DETAILED_STATUS = 13;
	public static final int MSG_GET_SERVER_DETAILED_STATUS = 15;
	public static final int MSG_GET_SSL_FINGERPRINT = 16;
	
	private Server server = null;
	private ServerParameters server_parameters = new ServerParameters();
	
	public Bundle getDetailedStatus() {
		Bundle data = new Bundle();
		
		data.putBoolean("server:enabled", server_parameters.isEnabled());
		data.putBoolean("server:password_enabled", server_parameters.hasPassword());
    	data.putBoolean("server:ssl_enabled", server_parameters.isSSL());
    	
    	switch(server_parameters.getStatus()) {
    	case ACTIVE:
    		data.putBoolean("server:connected", true);
    		data.putBoolean("server:sessions", true);
    		break;
    		
    	case CONNECTING:
    		data.putBoolean("server:connected", false);
    		data.putBoolean("server:sessions", false);
    		break;
    		
    	case ONLINE:
    		data.putBoolean("server:connected", true);
    		data.putBoolean("server:sessions", false);
    		break;
    		
    	default:
    		data.putBoolean("endpoint:connected", false);
    		data.putBoolean("endpoint:sessions", false);
    		break;
    	}
    	
    	return data;
	}
	
	public Bundle getServerFingerprint() {
		Bundle data = new Bundle();

		if(this.server != null)
			data.putString("certificate:fingerprint", this.server.getHostCertificateFingerprint());
		else
			data.putString("certificate:fingerprint", "error");
		
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
		case MSG_GET_SERVER_DETAILED_STATUS:
			try {
				Message message = Message.obtain(null, MSG_GET_SERVER_DETAILED_STATUS);
				message.setData(this.getDetailedStatus());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
			}
			break;
			
		case MSG_GET_SERVER_STATUS:
			try {
				Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
				message.setData(this.getStatus());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
			}
			break;
			
		case MSG_GET_SSL_FINGERPRINT:
			try {
				Message message = Message.obtain(null, MSG_GET_SSL_FINGERPRINT);
				message.setData(this.getServerFingerprint());
				
				msg.replyTo.send(message);
			}
			catch(RemoteException e) {
				Log.e(this.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
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
				Log.e(this.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
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
				Log.e(this.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
			}
			break;
		}
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
		
		this.server_parameters.setStatus(ServerParameters.Status.OFFLINE);
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
			this.server_parameters.setFromPreferences();
			
			this.server_parameters.enabled = true;
			this.server = new Server(this.server_parameters);
			this.server.setLogger(this);
			
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
