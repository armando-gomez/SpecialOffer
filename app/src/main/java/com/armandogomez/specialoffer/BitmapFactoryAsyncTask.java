package com.armandogomez.specialoffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

public class BitmapFactoryAsyncTask extends AsyncTask<String, Void, Bitmap> {

	private OfferActivity offerActivity;

	BitmapFactoryAsyncTask(OfferActivity offerActivity) {
		this.offerActivity = offerActivity;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		offerActivity.setImage(bitmap);
	}

	@Override
	protected Bitmap doInBackground(String... strings) {
		try {
			InputStream is = (InputStream) new URL(strings[0]).getContent();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			is.close();
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return BitmapFactory.decodeResource(
					offerActivity.getResources(), R.drawable.brokenimage);
		}
	}
}
