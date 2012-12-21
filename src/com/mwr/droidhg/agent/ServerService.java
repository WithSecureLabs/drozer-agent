package com.mwr.droidhg.agent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.mwr.droidhg.api.ServerParameters;
import com.mwr.droidhg.connector.Server;

public class ServerService extends Service {
	
	public static final int MSG_GET_SERVER_STATUS = 10;
	public static final int MSG_START_SERVER = 11;
	public static final int MSG_STOP_SERVER = 12;
	
	private final Messenger messenger = new Messenger(new IncomingHandler(this));
	private final ArrayList<Messenger> messengers = new ArrayList<Messenger>();
	private static boolean running = false;
	private Server server = null;
	private ServerParameters server_parameters = new ServerParameters();
	
	static class IncomingHandler extends Handler {
		
		private final WeakReference<ServerService> service;
		
		public IncomingHandler(ServerService service) {
			this.service = new WeakReference<ServerService>(service);
		}
		
		@Override
		public void handleMessage(Message msg) {
			ServerService service = this.service.get();
			
			if(!service.messengers.contains(msg.replyTo))
				service.messengers.add(msg.replyTo);
			
			switch(msg.what) {
			case MSG_GET_SERVER_STATUS:
				try {
					Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
					message.setData(service.getStatus());
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			case MSG_START_SERVER:
				try {
					service.startServer();
					
					Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
					message.setData(service.getStatus());
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			case MSG_STOP_SERVER:
				try {
					service.stopServer();

					Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
					message.setData(service.getStatus());
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_server_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
		
	}
	
	public Bundle getStatus() {
		Bundle data = new Bundle();
		
		data.putInt("server", this.server_parameters.getStatus().ordinal());
		
		return data;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(getString(R.string.log_tag_server_service), "received bind request");
		
		return this.messenger.getBinder();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ServerService.running = true;
		
		Log.i(getString(R.string.log_tag_server_service), "starting service");
		
		this.server_parameters.addObserver(new Observer() {

			@Override
			public void update(Observable arg0, Object arg1) {
				for(Messenger m : ServerService.this.messengers)
					try {
						Message message = Message.obtain(null, MSG_GET_SERVER_STATUS);
						message.setData(ServerService.this.getStatus());
						
						m.send(message);
					}
					catch (RemoteException e) {
						Log.e(getString(R.string.log_tag_server_service), "failed to send updated server status");
					}
			}
			
		});
		
		this.server_parameters.setStatus(ServerParameters.Status.OFFLINE);
	}
	
	@Override
	public void onDestroy() {
		ServerService.running = false;
		
		Log.i(getString(R.string.log_tag_server_service), "stopping service");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_REDELIVER_INTENT;
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
			
			this.server.start();
			
			Toast.makeText(this, "Started server on port " + Integer.valueOf(this.server_parameters.getPort()).toString(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void stopServer() {
		if(this.server != null) {
			this.server_parameters.enabled = false;
			this.server.stopServer();
			
			this.server = null;
		}
	}

}
