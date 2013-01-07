package com.mwr.droidhg.agent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
import android.util.SparseArray;

import com.mwr.common.logging.LogMessage;
import com.mwr.common.logging.Logger;
import com.mwr.common.logging.OnLogMessageListener;
import com.mwr.droidhg.Agent;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.connector.Client;

public class ClientService extends Service implements Logger {
	
	public static final int MSG_GET_ENDPOINTS_STATUS = 1;
	public static final int MSG_START_ENDPOINT = 2;
	public static final int MSG_STOP_ENDPOINT = 3;
	public static final int MSG_GET_ENDPOINT_DETAILED_STATUS = 4;
	public static final int MSG_LOG_MESSAGE = 5;
	
	private SparseArray<Client> clients = new SparseArray<Client>();
	private final EndpointManager endpoint_manager = new EndpointManager(this);
	private final Messenger messenger = new Messenger(new IncomingHandler(this));
	private static boolean running = false;
	public ArrayList<Messenger> messengers = new ArrayList<Messenger>();
	
	static class IncomingHandler extends Handler {
		
		private final WeakReference<ClientService> service;
		
		public IncomingHandler(ClientService service) {
			this.service = new WeakReference<ClientService>(service);
		}
		
		@Override
		public void handleMessage(Message msg) {
			ClientService service = this.service.get();
			
			if(!service.messengers.contains(msg.replyTo))
				service.messengers.add(msg.replyTo);
			
			switch(msg.what) {
			case MSG_GET_ENDPOINT_DETAILED_STATUS:
				try {
					Message message = Message.obtain(null, MSG_GET_ENDPOINT_DETAILED_STATUS);
					message.setData(service.getEndpointDetailedStatus(msg.getData().getInt("endpoint_id")));
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_client_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			case MSG_GET_ENDPOINTS_STATUS:
				try {
					Message message = Message.obtain(null, MSG_GET_ENDPOINTS_STATUS);
					message.setData(service.getEndpointsStatus());
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_client_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			case MSG_START_ENDPOINT:
				try {
					service.startEndpoint(msg.getData().getInt("endpoint_id"));
					
					Message message = Message.obtain(null, MSG_GET_ENDPOINTS_STATUS);
					message.setData(service.getEndpointsStatus());
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_client_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			case MSG_STOP_ENDPOINT:
				try {
					service.stopEndpoint(msg.getData().getInt("endpoint_id"));
					
					Message message = Message.obtain(null, MSG_GET_ENDPOINTS_STATUS);
					message.setData(service.getEndpointsStatus());
					
					msg.replyTo.send(message);
				}
				catch(RemoteException e) {
					Log.e(service.getString(R.string.log_tag_client_service), "exception replying to a Message: " + e.getMessage());
				}
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
		
	}
	
	public Bundle getEndpointDetailedStatus(int endpoint_id) {
		Bundle data = new Bundle();
		Endpoint endpoint = this.endpoint_manager.get(endpoint_id);
		
		data.putInt("endpoint:id", endpoint.getId());
		data.putBoolean("endpoint:enabled", endpoint.isEnabled());
		data.putBoolean("endpoint:password_enabled", endpoint.hasPassword());
    	data.putBoolean("endpoint:ssl_enabled", endpoint.isSSL());
    	
    	switch(endpoint.getStatus()) {
    	case ACTIVE:
    		data.putBoolean("endpoint:connected", true);
    		data.putBoolean("endpoint:sessions", true);
    		break;
    		
    	case CONNECTING:
    		data.putBoolean("endpoint:connected", false);
    		data.putBoolean("endpoint:sessions", false);
    		break;
    		
    	case ONLINE:
    		data.putBoolean("endpoint:connected", true);
    		data.putBoolean("endpoint:sessions", false);
    		break;
    		
    	default:
    		data.putBoolean("endpoint:connected", false);
    		data.putBoolean("endpoint:sessions", false);
    		break;
    	}
    	
    	return data;
	}
	
	public Bundle getEndpointsStatus() {
		Bundle data = new Bundle();
		
		for(Endpoint e : this.endpoint_manager.all())
			data.putInt("endpoint-" + e.getId(), e.getStatus().ordinal());
		
		return data;
	}
	
	@Override
	public List<LogMessage> getLogMessages() {
		return null;
	}
	
	@Override
	public void log(LogMessage msg) {
	}
	
	@Override
	public void log(Logger logger, LogMessage msg) {
		Bundle data = new Bundle();
		data.putInt("endpoint:id", ((Endpoint)logger).getId());
		data.putBundle("message", msg.toBundle());
		
		for(Messenger m : this.messengers) {
			try {
				Message message = Message.obtain(null, MSG_LOG_MESSAGE);
				message.setData(data);
				
				m.send(message);
			}
			catch(RemoteException e) {
				Log.e(getString(R.string.log_tag_client_service), "failed to send log message");
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(getString(R.string.log_tag_client_service), "received bind request");
		
		return this.messenger.getBinder();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Agent.setContext(this);
		ClientService.running = true;
		
		Log.i(getString(R.string.log_tag_client_service), "starting service");
		
		this.endpoint_manager.setOnEndpointStatusChangeListener(new EndpointManager.OnEndpointStatusChangeListener() {
			
			@Override
			public void onEndpointStopped(Endpoint endpoint) {}
			
			@Override
			public void onEndpointStarted(Endpoint endpoint) {}
			
			@Override
			public void onEndpointStatusChanged(Endpoint endpoint) {
				for(Messenger m : ClientService.this.messengers)
					try {
						Message message = Message.obtain(null, MSG_GET_ENDPOINTS_STATUS);
						message.setData(ClientService.this.getEndpointsStatus());
						
						m.send(message);
					}
					catch(RemoteException e) {
						Log.e(getString(R.string.log_tag_client_service), "failed to send updated endpoint status");
					}
			}
			
		});
		
		for(Endpoint e : this.endpoint_manager.all())
			e.setStatus(Endpoint.Status.OFFLINE);
	}
	
	@Override
	public void onDestroy() {
		ClientService.running = false;
		
		Log.i(getString(R.string.log_tag_client_service), "stopping service");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void setOnLogMessageListener(OnLogMessageListener listener) {
		throw new RuntimeException();
	}
	
	public static void startAndBindToService(Context context, ServiceConnection serviceConnection) {
		if(!ClientService.running)
			context.startService(new Intent(context, ClientService.class));
		
		Intent intent = new Intent(context, ClientService.class);
    	context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void startEndpoint(int id) {
		if(this.clients.get(id) == null) {
			Endpoint endpoint = this.endpoint_manager.get(id, true);
			
			Client client = new Client(endpoint);
			client.setLogger(this);
			
			this.clients.put(id, client);
			
			endpoint.enabled = true;
			client.start();
		}
	}
	
	public void stopEndpoint(int id) {
		Endpoint endpoint = this.endpoint_manager.get(id);
		Client client = this.clients.get(id);
		
		if(client != null) {
			client.stopConnector();
			endpoint.enabled = false;
			
			this.clients.remove(id);
		}
	}

}
