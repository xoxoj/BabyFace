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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.utils.DefaultTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import timber.log.Timber;


@ContentView(R.layout.activity_main)
public class MainActivity extends AbstractActivity implements ConnectionListener {

	private static final int
			REQUEST_CAPTURE_IMAGE = 42,
			REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION = 43;

	@InjectView(R.id.btn_camera) private Button cameraButton;
	@InjectView(R.id.btn_photo_count) private Button photoCountButton;

	@Inject private PhotoManager photoManager;
	private File imageFile;

	@Inject private GoogleApiClientManager googleApiClientManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup camera button
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (intent.resolveActivity(getPackageManager()) != null) {
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
		Timber.d("onStart");
		googleApiClientManager.connectToClient();
		super.onStart();
	}


	@Override
	public void onStop() {
		Timber.d("onStop");
		googleApiClientManager.disconnectFromClient();
		super.onStop();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION:
				googleApiClientManager.getGoogleApiClient().connect();
				break;

			case REQUEST_CAPTURE_IMAGE:
				if (resultCode != RESULT_OK) return;
				Timber.d("image taking success");

				// start image uploading
				subscriptions.add(Observable
						.defer(new Func0<Observable<Object>>() {
							@Override
							public Observable<Object> call() {
								GoogleApiClient googleApiClient = googleApiClientManager.getGoogleApiClient();

								// create new drive content
								DriveApi.DriveContentsResult driveContentsResult = Drive.DriveApi
										.newDriveContents(googleApiClient)
										.await();
								Status status = driveContentsResult.getStatus();
								if (!status.isSuccess()) {
									Timber.e("failed to create new drive contents (" + status.getStatusMessage() + ")");
									return Observable.error(new Exception(status.getStatusMessage()));
								}

								// copy image to drive contents
								Timber.d("about to copy " + imageFile.length() + " bytes");
								try {
									OutputStream driveOutputStream = driveContentsResult.getDriveContents().getOutputStream();
									InputStream photoInputStream = new FileInputStream(imageFile);
									byte[] buffer = new byte[1024];
									int bytesRead;
									while ((bytesRead = photoInputStream.read(buffer)) != -1) {
										driveOutputStream.write(buffer, 0, bytesRead);
									}
									Timber.d("done copying stream");

								} catch (IOException e) {
									Timber.e(e, "failed to read image");
									return Observable.error(e);
								}

								// create drive file
								MetadataChangeSet metadatachangeset = new MetadataChangeSet.Builder()
										.setTitle(imageFile.getName())
										.setMimeType("image/jpeg")
										.build();
								DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi
										.getAppFolder(googleApiClient)
										.createFile(googleApiClient, metadatachangeset, driveContentsResult.getDriveContents())
										.await();
								status = driveFileResult.getStatus();
								if (!status.isSuccess()) {
									return Observable.error(new Exception(status.getStatusMessage()));
								}

								return Observable.just(null);
							}
						})
						.compose(new DefaultTransformer<>())
						.subscribe(new Action1<Object>() {
							@Override
							public void call(Object nothing) {
								Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
							}
						}));
				break;
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		// nothing to do for now
	}

	@Override
	public void onConnectionSuspended(int i) {
		// nothing to do for now
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// missing play services etc.
		if (!connectionResult.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), MainActivity.this, 0).show();
			return;
		}

		// try resolving error
		try {
			connectionResult.startResolutionForResult(MainActivity.this, REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION);
		} catch (IntentSender.SendIntentException e) {
			Timber.e(e, "failed to resolve connection error");
		}
	}

}
