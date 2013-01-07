package com.mwr.droidhg;

import java.math.BigInteger;
import java.security.SecureRandom;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.agent.ClientService;
import com.mwr.droidhg.agent.EndpointManager;
import com.mwr.droidhg.agent.R;
import com.mwr.droidhg.agent.ServerService;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.api.ServerParameters;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

public class Agent {
	
	public static class ClientServiceConnection implements ServiceConnection {
    	
    	private Messenger service = null;
    	private boolean bound = false;
    	
    	public boolean isBound() {
    		return this.bound;
    	}
    	
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		this.service = new Messenger(service);
    		this.bound = true;

        	Agent.updateEndpointStatuses();
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName className) {
    		this.service = null;
    		this.bound = false;
    	}
    	
    	public void send(Message msg) throws RemoteException {
    		this.service.send(msg);
    	}
    	
    	public void unbind(Context context) {
    		if(this.bound) {
    			context.unbindService(this);
    			this.bound = false;
    		}
    	}
    	
    }
	
	public static class IncomingHandler extends Handler {
		
		//private final WeakReference<Context> context;
		
		public IncomingHandler(Context context) {
			//this.context = new WeakReference<Context>(context);
		}
		
		@Override
		public void handleMessage(Message msg) {
			//Context context = this.context.get();
			Bundle data = msg.getData();
			
			switch(msg.what) {
			case ClientService.MSG_GET_ENDPOINT_DETAILED_STATUS:
				getEndpointManager().get(data.getInt("endpoint:id")).setDetailedStatus(data);
				break;
				
			case ClientService.MSG_GET_ENDPOINTS_STATUS:
				for(Endpoint e : getEndpointManager().all())
					if(data.containsKey("endpoint-" + e.getId()))
						e.setStatus(Endpoint.Status.values()[data.getInt("endpoint-" + e.getId())]);
				break;
			
			case ServerService.MSG_GET_SERVER_DETAILED_STATUS:
				getServerParameters().setDetailedStatus(data);
				break;
				
			case ServerService.MSG_GET_SERVER_STATUS:
				getServerParameters().setStatus(ServerParameters.Status.values()[data.getInt("server")]);
				break;
				
			case ClientService.MSG_LOG_MESSAGE:
				getEndpointManager().get(data.getInt("endpoint:id")).log(LogMessage.fromBundle(data.getBundle("message")));
				break;
				
			case ServerService.MSG_LOG_MESSAGE:
				getServerParameters().log(LogMessage.fromBundle(data.getBundle("message")));
				
			default:
				super.handleMessage(msg);
			}
		}
		
	}
	
	public static class ServerServiceConnection implements ServiceConnection {
    	
    	private Messenger service = null;
    	private boolean bound = false;
    	
    	public boolean isBound() {
    		return this.bound;
    	}
    	
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		this.service = new Messenger(service);
    		this.bound = true;

        	Agent.updateServerStatus();
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName className) {
    		this.service = null;
    		this.bound = false;
    	}
    	
    	public void send(Message msg) throws RemoteException {
    		this.service.send(msg);
    	}
    	
    	public void unbind(Context context) {
    		if(this.bound) {
    			context.unbindService(this);
    			this.bound = false;
    		}
    	}
    	
    }
	
	private static Context context = null;
	
	private static ClientServiceConnection client_service_connection = null;
	private static EndpointManager endpoint_manager = null;
	private static Messenger messenger = null;
	private static ServerParameters server_parameters = null;
	private static ServerServiceConnection server_service_connection = null;
	private static String uid = null;
	
	public static void bindServices() {
		ClientService.startAndBindToService(context, client_service_connection);
		ServerService.startAndBindToService(context, server_service_connection);
	}
	
	public static ClientServiceConnection getClientService() {
		return client_service_connection;
	}
	
	public static Context getContext() {
		return context;
	}
	
	public static EndpointManager getEndpointManager() {
		return endpoint_manager;
	}
	
	public static void getEndpointDetailedStatus(Endpoint endpoint) {
		Bundle data = new Bundle();
		data.putInt("endpoint_id", endpoint.getId());
		
		try {
			Message message = Message.obtain(null, ClientService.MSG_GET_ENDPOINT_DETAILED_STATUS);
			message.setData(data);
			
			Agent.sendToClientService(message);
		}
		catch(RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to get endpoint detailed status " + endpoint.getId());
		}
	}
	
	public static Messenger getMessenger() {
		return messenger;
	}
	
	public static void getServerDetailedStatus() {
		try {
			Message message = Message.obtain(null, ServerService.MSG_GET_SERVER_DETAILED_STATUS);
			
			Agent.sendToServerService(message);
		}
		catch(RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to get server detailed status");
		}
	}
	
	public static ServerParameters getServerParameters() {
		return server_parameters;
	}
	
	public static ServerServiceConnection getServerService() {
		return server_service_connection;
	}
	
	public static SharedPreferences getSettings() {
		return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
	}
	
	public static String getUID() {
		if(uid == null)
			uid = Settings.Secure.getString(Agent.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
		
		// sometimes, a device will not have an ANDROID_ID, particularly if we are
		// in lower API versions; in that case we generate one at random
		if(uid == null)
			uid = new BigInteger(64, new SecureRandom()).toString(32);
		
		return uid;
	}
	
	public static void sendToClientService(Message msg) throws RemoteException {
		msg.replyTo = Agent.getMessenger();
		
		client_service_connection.send(msg);
	}
	
	public static void sendToServerService(Message msg) throws RemoteException {
		msg.replyTo = Agent.getMessenger();
		
		server_service_connection.send(msg);
	}
	
	public static void setContext(Context context) {
		Agent.context = context.getApplicationContext();
		
		if(client_service_connection == null)
			client_service_connection = new ClientServiceConnection();
		if(endpoint_manager == null)
			endpoint_manager = new EndpointManager(context);
		if(messenger == null)
			messenger = new Messenger(new IncomingHandler(context));
		if(server_parameters == null)
			server_parameters = new ServerParameters();
		if(server_service_connection == null)
			server_service_connection = new ServerServiceConnection();
	}
	
	public static void startEndpoint(Endpoint endpoint) {
		Bundle data = new Bundle();
		
		data.putInt("endpoint_id", endpoint.getId());
		
		try {
			endpoint.enabled = true;
			endpoint.setStatus(Endpoint.Status.UPDATING);
			
			Message message = Message.obtain(null, ClientService.MSG_START_ENDPOINT);
			message.setData(data);
			
			Agent.sendToClientService(message);
		}
		catch(RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to start endpoint " + endpoint.getId());
			
			endpoint.setStatus(Endpoint.Status.OFFLINE);
		}
	}
	
	public static void startServer() {
		try {
			server_parameters.enabled = true;
			server_parameters.setStatus(ServerParameters.Status.UPDATING);
			
			Agent.sendToServerService(Message.obtain(null, ServerService.MSG_START_SERVER));
		}
		catch(RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to start adb server");
			
			server_parameters.setStatus(ServerParameters.Status.OFFLINE);
		}
	}
	
	public static void stopEndpoint(Endpoint endpoint) {
		Bundle data = new Bundle();
		
		data.putInt("endpoint_id", endpoint.getId());
		
		try {
			endpoint.enabled = false;
			endpoint.setStatus(Endpoint.Status.UPDATING);
			
			Message message = Message.obtain(null, ClientService.MSG_STOP_ENDPOINT);
			message.setData(data);
			
			Agent.sendToClientService(message);
		}
		catch (RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to stop endpoint " + endpoint.getId());
			
			endpoint.setStatus(Endpoint.Status.OFFLINE);
		}
	}
	
	public static void stopServer() {
		try {
			server_parameters.enabled = false;
			server_parameters.setStatus(ServerParameters.Status.UPDATING);
			
			Agent.sendToServerService(Message.obtain(null, ServerService.MSG_STOP_SERVER));
		}
		catch(RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to stop adb server");
			
			server_parameters.setStatus(ServerParameters.Status.OFFLINE);
		}
	}
	
	public static void unbindServices() {
		client_service_connection.unbind(context);
		server_service_connection.unbind(context);
	}
	
	public static void updateEndpointStatuses() {
		try {
			for(Endpoint e : getEndpointManager().all())
				e.setStatus(Endpoint.Status.UPDATING);
			
			Agent.sendToClientService(Message.obtain(null, ClientService.MSG_GET_ENDPOINTS_STATUS));
		}
		catch (RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to update endpoint statuses");
		}
	}
	
	public static void updateServerStatus() {
		try {
			server_parameters.setStatus(ServerParameters.Status.UPDATING);
			
			Agent.sendToServerService(Message.obtain(null, ServerService.MSG_GET_SERVER_STATUS));
		}
		catch (RemoteException e) {
			Log.e(context.getString(R.string.log_tag_agent), "failed to update server status");
		}
	}

}
