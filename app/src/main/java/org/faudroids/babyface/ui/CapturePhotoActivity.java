package org.faudroids.babyface.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.photo.PhotoUploadService;

import java.io.IOException;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_capture_photo)
public class CapturePhotoActivity extends AbstractActivity {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	private static final int REQUEST_CAPTURE_PHOTO = 42;

	@InjectView(R.id.img_photo) private ImageView photoView;
	@InjectView(R.id.btn_cancel) private View cancelBtn;
	@InjectView(R.id.btn_confirm) private View confirmBtn;
	@InjectView(R.id.btn_redo) private View redoBtn;

	@Inject private PhotoManager photoManager;
	private Face face;
	private PhotoManager.PhotoCreationResult photoCreationResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		face = getIntent().getParcelableExtra(EXTRA_FACE);

		// hide status bar
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

		// immediately forward to actual photo capturing
		startPhotoCapture();

		// setup buttons
		confirmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					photoManager.onPhotoResult(photoCreationResult);
				} catch (IOException e) {
					Timber.e(e, "failed to store image");
				}
				startService(new Intent(CapturePhotoActivity.this, PhotoUploadService.class));
				setResult(RESULT_OK);
				finish();
			}
		});
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		redoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startPhotoCapture();
			}
		});
	}


	private void startPhotoCapture() {
		try {
			photoCreationResult = photoManager.createPhotoIntent(face.getId());
			startActivityForResult(photoCreationResult.getPhotoCaptureIntent(), REQUEST_CAPTURE_PHOTO);

		} catch (IOException e) {
			Timber.e(e, "failed to start camera");
			// TODO
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CAPTURE_PHOTO:
				if (resultCode != RESULT_OK) {
					finish();
					return;
				}

				Picasso.with(this).load(photoCreationResult.getTmpPhotoFile()).into(photoView);
		}
	}

}
