package org.faudroids.babyface.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleDriveManager;
import org.faudroids.babyface.photo.PhotoManager;

import java.io.File;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import timber.log.Timber;


@ContentView(R.layout.activity_main)
public class MainActivity extends AbstractActivity implements ConnectionListener {

	private static final int
			REQUEST_CAPTURE_IMAGE = 42,
			REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION = 43;

	/*
	@InjectView(R.id.btn_camera) private Button cameraButton;
	@InjectView(R.id.btn_photo_count) private Button photoCountButton;
	@InjectView(R.id.btn_get_token) private Button getTokenButton;
	@InjectView(R.id.btn_video_conversion) private Button videoConversionButton;
	*/

	@Inject private PhotoManager photoManager;
	private File imageFile;

	@Inject private GoogleDriveManager googleDriveManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup oauth2 token example button
		/*
		getTokenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Observable
						.defer(new Func0<Observable<String>>() {
							@Override
							public Observable<String> call() {
								try {
									// String scope = "oauth2:server:client_id:"
									/*
									String scope = "audience:server:client_id:"
											+ getString(R.string.google_web_oauth_client_id);
											// + ":api_scope:" + Drive.SCOPE_APPFOLDER.toString();
											*/
		/*
									String scope = "oauth2:" + Drive.SCOPE_APPFOLDER.toString();
									Timber.d("scope is " + scope);
									String token = GoogleAuthUtil.getToken(
											MainActivity.this,
											Plus.AccountApi.getAccountName(googleApiClientManager.getGoogleApiClient()),
											scope);
									return Observable.just(token);
								} catch (Exception e) {
									return Observable.error(e);
								}
							}
						})
						.compose(new DefaultTransformer<String>())
						.subscribe(new Action1<String>() {
							@Override
							public void call(String token) {
								Timber.d("token is " + token);
							}
						});
			}
		});
		*/

		/*
		// setup forwarding to conversion activity
		videoConversionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, OldVideoConversionActivity.class));
			}
		});
		*/
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION:
				googleApiClientManager.getGoogleApiClient().connect();
				break;

			case REQUEST_CAPTURE_IMAGE:
				Timber.d("foo = " + data.getStringExtra("bar"));
				if (resultCode != RESULT_OK) return;
				Timber.d("image taking success");

				/*
				// start image uploading
				try {
					subscriptions.add(googleDriveManager.createNewFile(new FileInputStream(imageFile), imageFile.getName(), "image/jpeg", false)
							.compose(new DefaultTransformer<Void>())
							.subscribe(new Action1<Void>() {
								@Override
								public void call(Void nothing) {
									Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
									Timber.d("photo saving success");
								}
							}));
				} catch (FileNotFoundException e) {
					Timber.e(e, "failed to read file");
					Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
				}
				*/
				break;
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		googleApiClientManager.registerListener(this);
	}


	@Override
	public void onStop() {
		super.onStop();
		googleApiClientManager.unregisterListener(this);
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
