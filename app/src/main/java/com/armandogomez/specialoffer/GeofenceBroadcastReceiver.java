package com.armandogomez.specialoffer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "GeofenceBroadcastRec";
	private static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel";
	private NotificationManager notificationManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

		if(geofencingEvent.hasError()) {
			Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
			return;
		}

		int geofenceTransition = geofencingEvent.getGeofenceTransition();

		if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
				geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
			List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

			for(Geofence geofence: triggeringGeofences) {
				FenceData fenceData = FenceMgr.getFenceData(geofence.getRequestId());

				sendNotification(context, geofenceTransition, fenceData);
			}
		}


	}

	public void sendNotification(Context context, int geofenceTransition, FenceData fenceData) {
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
				&& notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
			String name = context.getString(R.string.app_name);
			NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
					name, NotificationManager.IMPORTANCE_DEFAULT);

			notificationManager.createNotificationChannel(channel);
		}

		Intent resultIntent = new Intent(context, OfferActivity.class);
		resultIntent.putExtra("FENCE_ID", fenceData.getId());

		PendingIntent pendingIntent = PendingIntent.getActivity(context, getUniqueId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// You could build a pending intent here to open an activity when the
		// notification is tapped. Not doing that here though.
		Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
				.setSmallIcon(R.drawable.fence_notif2)
				.setContentTitle("Notification from '" + fenceData.getId() + "' Geofence")
				.setSubText(fenceData.getId()) // small text at top left
				.setContentText(fenceData.getAddress()) // Detail info
				.setVibrate(new long[] {1, 1, 1})
				.setAutoCancel(true)
				.setLights(0xff0000ff, 300, 1000) // blue color
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setContentIntent(pendingIntent)
				.build();

		notificationManager.notify(getUniqueId(), notification);
	}

	private static int getUniqueId() {
		return (int) (System.currentTimeMillis() % 100000);
	}


}
