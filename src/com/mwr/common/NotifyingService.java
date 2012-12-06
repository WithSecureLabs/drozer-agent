package com.mwr.common;

import com.mwr.droidhg.agent.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.widget.RemoteViews;

public abstract class NotifyingService extends Service {
	
	private NotificationManager notification_manager = null;
	
	protected void hideNotification(int view_id) {
		this.notification_manager.cancel(this.toString(), view_id);
	}
	
	@Override
	public void onCreate() {
		this.notification_manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	protected abstract void onCreateNotification(RemoteViews view);

	protected void showNotification(int view_id, PendingIntent intent) {
		Notification notification = new Notification();
		
		RemoteViews contentView = new RemoteViews(this.getPackageName(), view_id);
		
		this.onCreateNotification(contentView);
		
		notification.icon = R.drawable.ic_notification;
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.contentIntent = intent;
		notification.contentView = contentView;
		
		this.notification_manager.notify(this.toString(), view_id, notification);
	}

}
