package com.armandogomez.specialoffer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FenceMgr {
	private static final String TAG = "FenceMgr";
	private GeofencingClient geofencingClient;
	private PendingIntent geofencePendingIntent;
	private static HashMap<String, FenceData> fenceMap = new HashMap<>();
	private Geocoder geocoder;
	private static final String FENCE_URL = "http://www.christopherhield.com/data/fences.json";
	private static FenceMgr instance;
	private Activity activity;


	public static FenceMgr getInstance(Activity activity) {
		if(instance == null) {
			instance = new FenceMgr(activity);
		}
		return instance;
	}

	FenceMgr(Activity activity) {
		this.activity = activity;
		geofencingClient = LocationServices.getGeofencingClient(activity);

		geofencingClient.removeGeofences(getGeofencePendingIntent())
				.addOnSuccessListener(activity, new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d(TAG, "onSuccess: removeGeofences");
					}
				}).addOnFailureListener(activity, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(TAG, "onFailure: removeGeofences");
					}
				});
		new FenceDataDownloadAsyncTask(activity, this).execute();
	}

	private PendingIntent getGeofencePendingIntent() {
		if(geofencePendingIntent != null) {
			return geofencePendingIntent;
		}

		Intent intent = new Intent(activity, GeofenceBroadcastReceiver.class);

		geofencePendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return geofencePendingIntent;
	}

	static FenceData getFenceData(String id) {
		if(fenceMap.containsKey(id)) {
			return fenceMap.get(id);
		}
		return null;
	}

	void addFences(ArrayList<FenceData> fences) {
		fenceMap.clear();
		for(FenceData fence: fences) {
			if (!fenceMap.containsKey(fence.getId())) {
				fenceMap.put(fence.getId(), fence);
			}

			Geofence geofence = new Geofence.Builder()
					.setRequestId(fence.getId())
					.setCircularRegion(fence.getLat(), fence.getLon(), fence.getRadius())
					.setTransitionTypes(fence.getType())
					.setExpirationDuration(Geofence.NEVER_EXPIRE)
					.build();
			GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
					.addGeofence(geofence)
					.build();
			geofencePendingIntent = getGeofencePendingIntent();

			geofencingClient
					.addGeofences(geofencingRequest, geofencePendingIntent)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: addGeoFences");
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							e.printStackTrace();
							Log.d(TAG, "onFailure: addGeoFences");
						}
					});
		}
	}

}
