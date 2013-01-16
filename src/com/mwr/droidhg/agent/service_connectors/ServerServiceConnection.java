package com.mwr.droidhg.agent.service_connectors;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.mwr.droidhg.agent.services.ServerService;

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
	}
	
	@Override
	public void onServiceDisconnected(ComponentName className) {
		this.service = null;
		this.bound = false;
	}
	
	protected void send(Message msg) throws RemoteException {
		this.service.send(msg);
	}
	
	public void startServer(Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ServerService.MSG_START_SERVER);
		msg.replyTo = replyTo;
		
		this.send(msg);
	}
	
	public void stopServer(Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ServerService.MSG_STOP_SERVER);
		msg.replyTo = replyTo;
		
		this.send(msg);
	}
	
	public void unbind(Context context) {
		if(this.bound) {
			context.unbindService(this);
			this.bound = false;
		}
	}
	
}
