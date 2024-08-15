package com.WithSecure.dz.services;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.WithSecure.dz.Agent;
import com.WithSecure.dz.models.ForegroundServiceNotification;
import com.WithSecure.jsolar.api.connectors.Connector;
import com.WithSecure.jsolar.api.connectors.Endpoint;
import com.WithSecure.jsolar.logger.LogMessage;
import com.WithSecure.jsolar.logger.Logger;
import com.WithSecure.jsolar.logger.OnLogMessageListener;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.core.content.ContextCompat;

public abstract class ConnectorService extends Service implements OnLogMessageListener<Connector> {

	public static final int MSG_LOG_MESSAGE = 1;
	
	/**
	 * IncomingHandler is used to process all messages received by the
	 * ConnectorService. It stores a reference to the incoming messenger (if
	 * applicable) and hands the message off to the implementation for
	 * processing.
	 */
	private static class IncomingHandler extends Handler {
		
		private final WeakReference<ConnectorService> service;
		
		public IncomingHandler(ConnectorService service) {
			this.service = new WeakReference<ConnectorService>(service);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Bundle data = msg.getData();
			ConnectorService service = this.service.get();
			
			if(data == null || !data.getBoolean("ctrl:no_cache_messenger"))
				service.cacheMessenger(msg.replyTo);
			
			service.handleMessage(msg);
		}
		
	}
	
	private final Messenger messenger = new Messenger(new IncomingHandler(this));
	private final ArrayList<Messenger> messengers = new ArrayList<Messenger>();
	protected static boolean running = false;
	
	/**
	 * Broadcasts a bundle of data, as a log message, to every messenger that has
	 * previously sent us a message.
	 */
	protected void broadcastLogMessageBundle(Bundle data) {
		Message message = Message.obtain(null, ConnectorService.MSG_LOG_MESSAGE);
		message.setData(data);
			
		this.sendToAllMessengers(message);
	}
	
	/**
	 * Stores a reference to a Messenger, if we haven't already stored it.
	 */
	public void cacheMessenger(Messenger messenger) {
		if(!this.messengers.contains(messenger))
			this.messengers.add(messenger);
	}
	
	/**
	 * handleMessage() is handed every message that is passed to this service,
	 * to process and send any replies.
	 * 
	 * Override in an implementation of Connector service.
	 */
	public abstract void handleMessage(Message msg);
	
	@Override
	public IBinder onBind(Intent intent) {
		return this.messenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (ContextCompat.checkSelfPermission(Agent.getContext(), Manifest.permission.FOREGROUND_SERVICE)
				== PackageManager.PERMISSION_GRANTED)
			startForeground(ForegroundServiceNotification.getId(), ForegroundServiceNotification.getNotification());
	}

	@Override
	public void onLogMessage(Logger<Connector> logger, LogMessage message) {
		Bundle data = new Bundle();
		data.putBundle(Connector.CONNECTOR_LOG_MESSAGE, message.toBundle());
		
		Connector connector = logger.getOwner();
		
		if(connector instanceof Endpoint)
			data.putInt(Endpoint.ENDPOINT_ID, ((Endpoint)connector).getId());
		this.broadcastLogMessageBundle(data);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_REDELIVER_INTENT;
	}
	
	/**
	 * Attempt to deliver a Message to all messengers that have previously sent
	 * a message to this service, without specifying not to cache their handle.
	 */
	protected void sendToAllMessengers(Message msg) {
		for(Messenger m : this.messengers) {
			try {
				m.send(msg);
			}
			catch(RemoteException e) {}
		}
	}

	public static void Start(Context ctx, Class<?> c) {
		Intent i = new Intent(ctx, c);

		if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.FOREGROUND_SERVICE)
				== PackageManager.PERMISSION_GRANTED)
			ContextCompat.startForegroundService(ctx, new Intent(ctx, c));
		else
			ctx.startService(i);
	}

}
