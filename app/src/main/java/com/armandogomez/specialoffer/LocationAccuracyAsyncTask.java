package com.armandogomez.specialoffer;

import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationAccuracyAsyncTask extends AsyncTask<String, Void, Void> {
	private static final String TAG = "LocationAccAsyncTask";
	private static final int ACCURACY_REQUEST = 222;
	private SplashActivity splashActivity;

	LocationAccuracyAsyncTask(SplashActivity sa) {
		splashActivity = sa;
	}

	@Override
	protected Void doInBackground(String... string) {
		checkLocationAccuracy();
		return null;
	}

	private void checkLocationAccuracy() {
		Log.d(TAG, "checkLocationAccuracy: ");
		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
		SettingsClient settingsClient = LocationServices.getSettingsClient(splashActivity);

		Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

		task.addOnSuccessListener(splashActivity, new OnSuccessListener<LocationSettingsResponse>() {
			@Override
			public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
				Log.d(TAG, "onSuccess: High Accuracy Already Present");
				splashActivity.successLocationAccuracyCheck();
			}
		});

		task.addOnFailureListener(splashActivity, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				if(e instanceof ResolvableApiException) {
					// Location settings are not satisfied, but this can be fixed
					// by showing the user a dialog.
					try {
						// The next line will cause a pop-up to display asking you if it is ok to turn
						// on google's location services if they are not already running)
						//
						// Show the dialog by calling startResolutionForResult(),
						// and check the result in onActivityResult().
						ResolvableApiException resolvable = (ResolvableApiException) e;
						resolvable.startResolutionForResult(splashActivity, ACCURACY_REQUEST);
					} catch (IntentSender.SendIntentException sendEx) {
						sendEx.printStackTrace();
					}
				}
			}
		});
	}
}
