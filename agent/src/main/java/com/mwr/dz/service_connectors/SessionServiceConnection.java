package com.mwr.dz.service_connectors;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;

import com.mwr.dz.services.SessionService;

public class SessionServiceConnection implements ServiceConnection {
	
	private Messenger service = null;
	private boolean bound = false;
	
	public boolean isBound() {
		return this.bound;
	}
	
	public void notifySessionStarted(String sessionId) {
		try {
			this.send(android.os.Message.obtain(null, SessionService.MSG_START_SESSION, sessionId));
		}
		catch(RemoteException e) {}
	}
	
	public void notifySessionStopped(String sessionId) {
		try {
			this.send(android.os.Message.obtain(null, SessionService.MSG_STOP_SESSION, sessionId));
		}
		catch(RemoteException e) {}
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
	
	public void send(android.os.Message msg) throws RemoteException {
		this.service.send(msg);
	}
	
	public void unbind(Context context) {
		if(this.bound) {
			context.unbindService(this);
			this.bound = false;
		}
	}
	
}
