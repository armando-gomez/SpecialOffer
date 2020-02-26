package com.armandogomez.specialoffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FenceDataDownloadAsyncTask extends AsyncTask<String, Void, String> {
	private Geocoder geocoder;
	private FenceMgr fenceMgr;
	private static final String FENCE_URL = "http://www.christopherhield.com/data/fences.json";

	FenceDataDownloadAsyncTask(Activity activity, FenceMgr fenceMgr) {
		this.fenceMgr = fenceMgr;
		geocoder = new Geocoder(activity);
	}

	@Override
	protected void onPostExecute(String result) {
		if(result == null) {
			return;
		}

		ArrayList<FenceData> fences = new ArrayList<>();
		try {
			JSONObject jObj = new JSONObject(result);
			JSONArray jArr = jObj.getJSONArray("fences");
			for (int i = 0; i < jArr.length(); i++) {
				JSONObject fObj = jArr.getJSONObject(i);
				String id = fObj.getString("id");
				String address = fObj.getString("address");
				float rad = (float) fObj.getDouble("radius");
				int type = fObj.getInt("type");
				String color = fObj.getString("fenceColor");
				String website = fObj.getString("website");
				String offer = fObj.getString("message");
				String logo = fObj.getString("logo");
				String code = fObj.getString("code");

				LatLng ll = getLatLong(address);

				if (ll != null) {
					FenceData fd = new FenceData(id, ll.latitude, ll.longitude, address, rad, type, color, website, offer, logo, code);
					fences.add(fd);
				}
			}
			fenceMgr.addFences(fences);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		HttpURLConnection connection = null;
		BufferedReader reader = null;

		try {
			URL url = new URL(FENCE_URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;

			InputStream stream = connection.getInputStream();

			reader = new BufferedReader(new InputStreamReader(stream));

			StringBuilder buffer = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			return buffer.toString();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private LatLng getLatLong(String address) {
		try {
			List<Address> addressList = geocoder.getFromLocationName(address, 1);
			Address res = addressList.get(0);
			return new LatLng(res.getLatitude(), res.getLongitude());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
