package org.faudroids.babyface.ui;


import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import org.faudroids.babyface.R;
import org.faudroids.babyface.photo.PhotoManager;

import java.io.IOException;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import timber.log.Timber;

@ContentView(R.layout.activity_capture_photo)
public class CapturePhotoActivity extends AbstractActivity {

	public static final String
			EXTRA_FACE_ID = "EXTRA_FACE_ID",
			EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID",
			EXTRA_UPLOAD_PHOTO = "EXTRA_UPLOAD_PHOTO";

	private static final String STATE_PHOTO = "STATE_PHOTO";

	private static final int REQUEST_CAPTURE_PHOTO = 42;

	@Inject private PhotoManager photoManager;
	@Inject private NotificationManager notificationManager;

	private String faceId;
	private PhotoManager.PhotoCreationResult photoCreationResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		faceId = intent.getStringExtra(EXTRA_FACE_ID);

		if (savedInstanceState != null) {
			photoCreationResult = savedInstanceState.getParcelable(STATE_PHOTO);
		}

		// check if notification should be cancelled
		if (intent.hasExtra(EXTRA_NOTIFICATION_ID)) {
			notificationManager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
		}

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// immediately forward to actual photo capturing
			startPhotoCapture();
		}
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
	public void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(STATE_PHOTO, photoCreationResult);
		super.onSaveInstanceState(outState);
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

					// upload image if necessary
					if (getIntent().getBooleanExtra(EXTRA_UPLOAD_PHOTO, false)) {
						photoManager.requestPhotoUpload();
					}
				}
				finish();
		}
	}

}
