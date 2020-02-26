package com.armandogomez.specialoffer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class OfferActivity extends AppCompatActivity {
	private FenceData fence;

	private Typeface typeface;
	private TextView offerName;
	private TextView offerAddress;
	private TextView offerWebsite;
	private TextView offerDetails;
	private ImageView offerLogo;
	private ConstraintLayout offerLayout;
	private ImageView offerBarcode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offer);

		String fenceId = getIntent().getStringExtra("FENCE_ID");

		fence = FenceMgr.getFenceData(fenceId);

		typeface = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

		offerName = findViewById(R.id.offerName);
		offerAddress = findViewById(R.id.offerAddress);
		offerWebsite = findViewById(R.id.offerWebsite);
		offerDetails = findViewById(R.id.offerDetails);
		offerLayout = findViewById(R.id.offerLayout);
		offerLogo = findViewById(R.id.offerLogo);
		offerBarcode = findViewById(R.id.offerBarcode);

		setupLayout();
	}

	private void setupLayout() {
		offerName.setTypeface(typeface);
		offerAddress.setTypeface(typeface);
		offerWebsite.setTypeface(typeface);
		offerDetails.setTypeface(typeface);

		offerName.setText(fence.getId());
		offerAddress.setText(fence.getAddress());
		offerWebsite.setText(fence.getWebsite());
		offerDetails.setText(fence.getOffer());
		Linkify.addLinks(offerWebsite, Linkify.WEB_URLS);
		Linkify.addLinks(offerAddress, Linkify.MAP_ADDRESSES);

		int color = ColorUtils.setAlphaComponent(Color.parseColor(fence.getFenceColor()), 60);

		offerLayout.setBackgroundColor(color);
		offerDetails.setBackgroundColor(color);

		new BitmapFactoryAsyncTask(this).execute(fence.getLogo());

		makeQRCode();
	}

	public void setImage(Bitmap bitmap) {
		try {
			offerLogo.setImageBitmap(bitmap);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Bitmap failImage = BitmapFactory.decodeResource(getResources(), R.drawable.brokenimage);
			offerLogo.setImageBitmap(failImage);
		}
	}

	private void makeQRCode() {
		QRCodeWriter writer = new QRCodeWriter();
		try {
			BitMatrix bitMatrix = writer.encode(fence.getCode(), BarcodeFormat.QR_CODE, 176, 176);

			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();

			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			for(int i=0; i < width; i++) {
				for(int j=0; j < height; j++) {
					bitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK: Color.WHITE);
				}
			}

			offerBarcode.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}


}
