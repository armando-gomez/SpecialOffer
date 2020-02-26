package com.armandogomez.specialoffer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

//	private FusedLocationProviderClient fusedLocationProviderClient;

	private static final int LOCATION_REQUEST = 111;

	private View progressOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		progressOverlay = findViewById(R.id.progressOverlay);
		progressOverlay.setVisibility(View.VISIBLE);
		if(checkPermission()) {
			setup();
		}
	}

	private void setup() {
		new LocationAccuracyAsyncTask(this).execute();
	}

	public void successLocationAccuracyCheck() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FenceMgr.getInstance(SplashActivity.this);
			}
		}).start();

		Intent intent = new Intent(this, MapsActivity.class);
		startActivity(intent);
		finish();
	}

	private boolean checkPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(this,
					new String[]{
							Manifest.permission.ACCESS_FINE_LOCATION
					}, LOCATION_REQUEST);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == LOCATION_REQUEST) {
			for (int i = 0; i < permissions.length; i++) {
				if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
					if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
						setup();
					} else {
						createPermissionDeniedAlert();
					}
				}
			}
		}
	}

	private void createPermissionDeniedAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
		builder.setTitle("Error with Permission");
		builder.setMessage("Cannot run application without location permissions");
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
