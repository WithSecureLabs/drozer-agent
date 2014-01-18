package com.mwr.dz.service_connectors;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.mwr.dz.Agent;
import com.mwr.dz.services.ClientService;
import com.mwr.jdiesel.api.connectors.Endpoint;

public class ClientServiceConnection implements ServiceConnection {
	
	private Messenger service = null;
	private boolean bound = false;
	
	public boolean isBound() {
		return this.bound;
	}
	
	public void getDetailedEndpointStatus(int id, Messenger replyTo) throws RemoteException {
		Bundle data = new Bundle();
		
		data.putInt(Endpoint.ENDPOINT_ID, id);
		
		Message msg = Message.obtain(null, ClientService.MSG_GET_DETAILED_ENDPOINT_STATUS);
		msg.replyTo = Agent.getInstance().getMessenger();
		msg.setData(data);
		
		this.send(msg);
	}
	
	public void getEndpointStatuses(Messenger replyTo) throws RemoteException {
		Message msg = Message.obtain(null, ClientService.MSG_GET_ENDPOINTS_STATUS);
		msg.replyTo = replyTo;
		
		this.send(msg);
	}
	
	public void getPeerFingerprint(int id, Messenger replyTo) throws RemoteException {
		Bundle data = new Bundle();
		data.putBoolean("ctrl:no_cache_messenger", true);
		data.putInt(Endpoint.ENDPOINT_ID, id);
		
		Message msg = Message.obtain(null, ClientService.MSG_GET_SSL_FINGERPRINT);
		msg.replyTo = replyTo;
		msg.setData(data);
		
		this.send(msg);
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		this.service = new Messenger(service);
		this.bound = true;

		if(Agent.getInstance().getSettings().getBoolean("restore_after_crash", true)){
			for(Endpoint e : Agent.getInstance().getEndpointManager().all()){
				if(e.isActive()){
					try {
						Log.d("ClientService", "Resuming connection to endpoint...");
						Agent.getInstance().getClientService().startEndpoint(e, Agent.getInstance().getMessenger());
					} catch (RemoteException re) {
						re.printStackTrace();
					}
				}
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
	
	public void startEndpoint(Endpoint endpoint, Messenger replyTo) throws RemoteException {
		Bundle data = new Bundle();
		data.putInt(Endpoint.ENDPOINT_ID, endpoint.getId());
		
		Message msg = Message.obtain(null, ClientService.MSG_START_ENDPOINT);
		msg.replyTo = replyTo;
		msg.setData(data);
		
		this.send(msg);
		
		endpoint.enabled = true;
		endpoint.notifyObservers();
	}
	
	public void stopEndpoint(Endpoint endpoint, Messenger replyTo) throws RemoteException {
		Bundle data = new Bundle();
		
		data.putInt(Endpoint.ENDPOINT_ID, endpoint.getId());
		
		Message msg = Message.obtain(null, ClientService.MSG_STOP_ENDPOINT);
		msg.replyTo = replyTo;
		msg.setData(data);
		
		this.send(msg);
		
		endpoint.enabled = false;
		endpoint.notifyObservers();
	}
	
	public void unbind(Context context) {
		if(this.bound) {
			context.unbindService(this);
			this.bound = false;
		}
	}
	
}
