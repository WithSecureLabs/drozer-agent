package com.mwr.droidhg.agent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.mwr.common.logging.LogMessage;
import com.mwr.common.logging.Logger;
import com.mwr.common.logging.OnLogMessageListener;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public abstract class ConnectorService extends Service implements Logger {

	public static final int MSG_LOG_MESSAGE = 5;
	
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
	private OnLogMessageListener on_log_message_listener = null;
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
	
	@Override
	/**
	 * Required by Logger interface. Not used.
	 */
	public List<LogMessage> getLogMessages() {
		return null;
	}
	
	/**
	 * handleMessage() is handed every message that is passed to this service,
	 * to process and send any replies.
	 * 
	 * Override in an implementation of Connector service.
	 */
	public abstract void handleMessage(Message msg);
	
	@Override
	/**
	 * Broadcast a log message to all messengers that have previously sent a
	 * message to this service.
	 */
	public void log(LogMessage msg) {
		Bundle data = new Bundle();
		data.putBundle("message", msg.toBundle());
		
		this.broadcastLogMessageBundle(data);
	}
	
	@Override
	/**
	 * Broadcast a log message to all messengers that have previously sent a
	 * message to this service.
	 */
	public void log(Logger logger, LogMessage msg) {
		if(this.on_log_message_listener != null)
			this.on_log_message_listener.onLogMessage(logger, msg);
		
		this.log(msg);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return this.messenger.getBinder();
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
	
	@Override
	/**
	 * Set an OnLogMessageListener, that will be handed every message logged
	 * by this ConnectorService.
	 */
	public void setOnLogMessageListener(OnLogMessageListener listener) {
		this.on_log_message_listener = listener;
	}

}
