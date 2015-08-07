package org.faudroids.babyface.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.faudroids.babyface.PhotoManager;
import org.faudroids.babyface.R;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

	private static final int
			REQUEST_CAPTURE_IMAGE = 42,
			REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION = 43;

	@InjectView(R.id.btn_camera) private Button cameraButton;
	@InjectView(R.id.btn_photo_count) private Button photoCountButton;

	@Inject private PhotoManager photoManager;
	private GoogleApiClient googleApiClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup google api client
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Drive.API)
				.addScope(Drive.SCOPE_APPFOLDER)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();


		// setup camera button
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (intent.resolveActivity(getPackageManager()) != null) {
					File imageFile;
					try {
						imageFile = photoManager.createImageFile();
						Timber.d("storing image as " + imageFile.getAbsolutePath());
					} catch (IOException ioe) {
						Timber.e(ioe, "failed to create image file");
						return;
					}

					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
					startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
				}
			}
		});

		// setup count photos button
		photoCountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int photoCount = 0;
				for (File file : photoManager.getRootStorageDir().listFiles()) {
					if (file.getName().toLowerCase().endsWith(".jpg")) ++photoCount;
				}
				Toast.makeText(MainActivity.this, "Found " + photoCount + " photos", Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.connect();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CAPTURE_IMAGE:
				if (resultCode != RESULT_OK) return;
				Toast.makeText(this, "Image taking success", Toast.LENGTH_SHORT).show();
				break;

			case REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION:
				if (resultCode != RESULT_OK) return;
				googleApiClient.connect();
				break;
		}
	}


	@Override
	public void onConnected(Bundle bundle) {
		Timber.d("GoogleApiClient connected");
	}


	@Override
	public void onConnectionSuspended(int i) {
		Timber.d("GoogleApiClient connection suspended");
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Timber.d("GoogleApiClient connection failed");
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION);
			} catch (IntentSender.SendIntentException e) {
				Timber.e(e, "failed to resolve connection error");
			}
		} else {
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
		}
	}

}
