package com.mwr.dz.service_connectors;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.mwr.dz.Agent;
import com.mwr.dz.services.ServerService;
import com.mwr.jdiesel.api.connectors.Server;

public class ServerServiceConnection implements ServiceConnection {
	
	private Messenger service = null;
	private boolean bound = false;
	
	public void getDetailedServerStatus(Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ServerService.MSG_GET_DETAILED_SERVER_STATUS);
		msg.replyTo = replyTo;
		
		this.send(msg);
	}
	
	public void getHostFingerprint(Messenger replyTo) throws RemoteException {
		Bundle data = new Bundle();
		data.putBoolean("ctrl:no_cache_messenger", true);
		
		Message msg = Message.obtain(null, ServerService.MSG_GET_SSL_FINGERPRINT);
		msg.replyTo = replyTo;
		msg.setData(data);
		
		this.send(msg);
	}
	
	public void getServerStatus(Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ServerService.MSG_GET_SERVER_STATUS);
		msg.replyTo = replyTo;
		
		this.send(msg);
	}
	
	public boolean isBound() {
		return this.bound;
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		this.service = new Messenger(service);
		this.bound = true;		
		if(Agent.getInstance().getSettings().getBoolean("localServerEnabled", false) && Agent.getInstance().getSettings().getBoolean("restore_after_crash", true)){
			try {
				ServerServiceConnection ssc = Agent.getInstance().getServerService();
				Server server = Agent.getInstance().getServerParameters();
				Messenger messenger = Agent.getInstance().getMessenger();
				
				ssc.startServer(server, messenger);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onServiceDisconnected(ComponentName className) {
		this.service = null;
		this.bound = false;
	}
	
	protected void send(Message msg) throws RemoteException {
		this.service.send(msg);
	}
	
	public void startServer(Server server, Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ServerService.MSG_START_SERVER);
		msg.replyTo = replyTo;
		
		this.send(msg);
		
		Editor edit = Agent.getInstance().getSettings().edit();
		edit.putBoolean("localServerEnabled", true);
		edit.commit();
		
		server.enabled = true;
		server.notifyObservers();
	}
	
	public void stopServer(Server server, Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ServerService.MSG_STOP_SERVER);
		msg.replyTo = replyTo;
		
		this.send(msg);
		
		Editor edit = Agent.getInstance().getSettings().edit();
		edit.putBoolean("localServerEnabled", false);
		edit.commit();
		
		server.enabled = false;
		server.notifyObservers();
	}
	
	public void unbind(Context context) {
		if(this.bound) {
			context.unbindService(this);
			this.bound = false;
		}
	}
	
}
