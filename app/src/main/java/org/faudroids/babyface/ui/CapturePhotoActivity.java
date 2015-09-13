package org.faudroids.babyface.ui;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import org.faudroids.babyface.R;
import org.faudroids.babyface.photo.PhotoManager;

import java.io.IOException;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_capture_photo)
public class CapturePhotoActivity extends AbstractActivity {

	public static final String
			EXTRA_FACE_NAME = "EXTRA_FACE_NAME",
			EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID",
			EXTRA_UPLOAD_PHOTO = "EXTRA_UPLOAD_PHOTO";

	private static final String STATE_PHOTO = "STATE_PHOTO";

	private static final int REQUEST_CAPTURE_PHOTO = 42;

	@InjectView(R.id.txt_disable_landscape) private View disableLandscapeModeView;
	@Inject private PhotoManager photoManager;
	@Inject private NotificationManager notificationManager;

	private String faceName;
	private PhotoManager.PhotoCreationResult photoCreationResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		faceName = intent.getStringExtra(EXTRA_FACE_NAME);

		if (savedInstanceState != null) {
			photoCreationResult = savedInstanceState.getParcelable(STATE_PHOTO);
		}

		// check if notification should be cancelled
		if (intent.hasExtra(EXTRA_NOTIFICATION_ID)) {
			notificationManager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
		}

		// if landscape mode start photo capturing immediately
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE || !photoManager.getForcePhotoLandscapeMode()) {
			startPhotoCapture();
			return;
		}

		// setup option to disable forced landscape mode
		disableLandscapeModeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(CapturePhotoActivity.this)
						.setTitle(R.string.disable_landscape_title)
						.setMessage(R.string.disable_landscape_msg)
						.setPositiveButton(R.string.turn_off, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								photoManager.setForcePhotoLandscapeMode(false);
								startPhotoCapture();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}
		});
	}


	private void startPhotoCapture() {
		try {
			photoCreationResult = photoManager.createPhotoIntent(faceName);
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
						photoManager.requestPhotoSync();
					}
				}
				finish();
		}
	}

}
