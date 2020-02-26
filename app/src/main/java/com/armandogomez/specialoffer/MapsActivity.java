package com.armandogomez.specialoffer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
	private static final int LOCATION_REQUEST = 111;
	private static final String TAG = "MapsActivity";

	private GoogleMap mMap;
	private Geocoder geocoder;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private List<PatternItem> pattern = Collections.<PatternItem>singletonList(new Dot());

	private ArrayList<LatLng> latLngHistory = new ArrayList<>();
	private Location currLocation;
	private Polyline llHistoryPolyline;
	private boolean zooming = false;
	private Marker carMarker;
	private TextView addressText;
	private CheckBox geofencesCheck;
	private CheckBox addressCheck;
	private boolean showAddress;
	private boolean showGeofences;
	private ArrayList<Circle> circles = new ArrayList<>();
	private float oldZoom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		addressText = findViewById(R.id.addressText);
		geocoder = new Geocoder(this);

		geofencesCheck = findViewById(R.id.geofencesCheckbox);
		addressCheck = findViewById(R.id.addressesCheckbox);
		showGeofences = geofencesCheck.isChecked();
		showAddress = addressCheck.isChecked();

		geofencesCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					showGeofences = true;
					drawFences();
				} else {
					showGeofences = false;
					eraseFences();
				}
			}
		});

		addressCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					showAddress = true;
					setAddressText();
				} else {
					showAddress = false;
					setAddressText();
				}
			}
		});

		initMap();
	}

	private void initMap() {
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		if(mapFragment != null) {
			mapFragment.getMapAsync(this);
		}
	}

	private void drawFences() {
		if(showGeofences) {
			HashMap<String, FenceData> fenceMap = FenceMgr.getFenceMap();
			for(FenceData fence: fenceMap.values()) {
				drawFence(fence);
			}
		} else {
			eraseFences();
		}

	}

	private void drawFence(FenceData fence) {
		int line = Color.parseColor(fence.getFenceColor());
		int fill = ColorUtils.setAlphaComponent(line, 85);

		LatLng latLng = new LatLng(fence.getLat(), fence.getLon());
		Circle c = mMap.addCircle(new CircleOptions()
				.center(latLng)
				.radius(fence.getRadius())
				.strokePattern(pattern)
				.strokeColor(line)
				.fillColor(fill));
		circles.add(c);
	}

	private void eraseFences() {
		for(Circle c: circles) {
			c.remove();
		}
		circles.clear();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(locationManager != null && locationListener != null) {
			locationManager.removeUpdates(locationListener);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(checkPermission() && locationManager != null && locationListener != null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 15, locationListener);
		}
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		// Add a marker in Sydney and move the camera
		mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
		mMap.getUiSettings().setRotateGesturesEnabled(false);
		mMap.setBuildingsEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.getUiSettings().setCompassEnabled(true);

		setupLocationListener();
		setupZoomListener();
	}

	private void setupLocationListener() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocListener(this);

		if(checkPermission() && locationManager != null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 15, locationListener);
		}
	}

	private void setupZoomListener() {
		mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
			@Override
			public void onCameraIdle() {
				if(zooming) {
					Log.d(TAG, "onCameraIdle: DONE ZOOMING " + mMap.getCameraPosition().zoom);
					zooming = false;
					oldZoom = mMap.getCameraPosition().zoom;
				}
			}
		});

		mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
			@Override
			public void onCameraMove() {
				Log.d(TAG, "onCameraMove: ZOOMING " + mMap.getCameraPosition().zoom);
				zooming = true;
			}
		});
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
						setupLocationListener();
					} else {
						createPermissionDeniedAlert();
					}
				}
			}
		}
	}

	private void setAddressText() {
		if(showAddress) {
			LatLng latLng = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
			try {
				List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
				Address address = addresses.get(0);
				addressText.setText(address.getAddressLine(0));
			} catch (IOException e) {
				e.printStackTrace();
				addressText.setText("");
			}
		} else {
			addressText.setText("");
		}
	}

	public void updateLocation(Location location) {
		currLocation = location;
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		latLngHistory.add(latLng);

		setAddressText();
		drawFences();

		if(llHistoryPolyline != null) {
			llHistoryPolyline.remove();
		}

		if(latLngHistory.size() == 1) {
			mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
			zooming = true;
			return;
		}

		if(latLngHistory.size() > 1) {
			PolylineOptions polylineOptions = new PolylineOptions();

			for(LatLng l: latLngHistory) {
				polylineOptions.add(l);
			}
			llHistoryPolyline = mMap.addPolyline(polylineOptions);
			llHistoryPolyline.setEndCap(new RoundCap());
			llHistoryPolyline.setWidth(8);
			llHistoryPolyline.setColor(Color.BLUE);

			float r = getRadius();
			if(r > 0) {
				Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.car);
				Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
				BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

				MarkerOptions options = new MarkerOptions();
				options.position(latLng);
				options.icon(iconBitmap);
				options.rotation(location.getBearing());

				if(carMarker != null) {
					carMarker.remove();
				}

				carMarker = mMap.addMarker(options);
			}
		}

		if(!zooming) {
			mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		}
	}

	private float getRadius() {
		float z = mMap.getCameraPosition().zoom;
		return 15f * z - 145f;
	}

	private void createPermissionDeniedAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
		builder.setTitle("Error with Permission");
		builder.setMessage("Cannot run application without location permissions");
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public GoogleMap getmMap() {
		return mMap;
	}
}
