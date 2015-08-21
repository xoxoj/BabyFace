package org.faudroids.babyface.ui;


import android.content.Intent;
import android.os.Bundle;

import org.faudroids.babyface.R;
import org.faudroids.babyface.photo.PhotoManager;

import java.io.IOException;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import timber.log.Timber;

@ContentView(R.layout.activity_capture_photo)
public class CapturePhotoActivity extends AbstractActivity {

	public static final String EXTRA_FACE_ID = "EXTRA_FACE_ID";

	private static final int REQUEST_CAPTURE_PHOTO = 42;

	@Inject private PhotoManager photoManager;
	private String faceId;
	private PhotoManager.PhotoCreationResult photoCreationResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		faceId = getIntent().getStringExtra(EXTRA_FACE_ID);

		// immediately forward to actual photo capturing
		startPhotoCapture();
	}


	private void startPhotoCapture() {
		try {
			photoCreationResult = photoManager.createPhotoIntent(faceId);
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
				if (resultCode == RESULT_OK) {
					setResult(RESULT_OK);
					try {
						photoManager.onPhotoResult(photoCreationResult);
					} catch (IOException e) {
						Timber.e(e, "failed to make video");
					}
				}
				finish();
		}
	}

}
