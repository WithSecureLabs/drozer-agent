package com.mwr.droidhg.agent;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.RemoteViews;

import com.mwr.common.NotifyingService;

public class SessionService extends NotifyingService {
	
	public static final int MSG_START_SESSION = 1;
	public static final int MSG_STOP_SESSION = 2;
	
	private final Messenger messenger = new Messenger(new IncomingHandler(this));
	private static boolean running = false;
	private HashSet<String> sessions = new HashSet<String>();
	
	static class IncomingHandler extends Handler {
		
		private final WeakReference<SessionService> service;
		
		public IncomingHandler(SessionService service) {
			this.service = new WeakReference<SessionService>(service);
		}
		
		@Override
		public void handleMessage(Message msg) {
			SessionService service = this.service.get();
			String session_id = (String)msg.obj;
			
			switch(msg.what) {
			case MSG_START_SESSION:
				service.add(session_id);
				
				service.updateNotification();
				break;
				
			case MSG_STOP_SESSION:
				service.remove(session_id);
				
				service.updateNotification();
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
		
	}
	
	public void add(String session_id) {
		this.sessions.add(session_id);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(getString(R.string.log_tag_session_service), "received bind request");
		
		return this.messenger.getBinder();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		SessionService.running = true;
		
		Log.i(getString(R.string.log_tag_session_service), "starting service");
	}
	
	@Override
	public void onDestroy() {
		SessionService.running = false;
		
		Log.i(getString(R.string.log_tag_session_service), "stopping service");
	}

	@Override
	protected void onCreateNotification(RemoteViews view) {
		view.setImageViewResource(R.id.cs_notification_icon, R.drawable.ic_launcher);
		view.setTextViewText(R.id.cs_notification_ticker, getString(R.string.session_connected));		
	}
	
	public void remove(String session_id) {
		this.sessions.remove(session_id);
	}
	
	public static void startAndBindToService(Context context, ServiceConnection serviceConnection) {
		if(!SessionService.running)
			context.startService(new Intent(context, SessionService.class));

		Intent intent = new Intent(context, SessionService.class);
    	context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void updateNotification() {
		if(!this.sessions.isEmpty()) {
			this.showNotification(R.layout.notification_session, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
		}
		else {
			this.hideNotification(R.layout.notification_session);
		}
	}

}
