package com.WithSecure.dz.models;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.WithSecure.dz.R;
import com.WithSecure.dz.activities.MainActivity;

public class ForegroundServiceNotification {
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private Notification notification;
    private static ForegroundServiceNotification instance = null;

    private  ForegroundServiceNotification(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for foreground service");

            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            Intent notificationIntent = new Intent(ctx, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            notification = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setContentTitle("Drozer")
                    .setContentText("Drozer is running in background")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .build();
        }
    }

    public static void Init(Context ctx) {
        if (instance == null) {
            instance = new ForegroundServiceNotification(ctx);
        }
    }

    public static Notification getNotification() {
        return instance.notification;
    }

    public static int getId() {
        return NOTIFICATION_ID;
    }
}
