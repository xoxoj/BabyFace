package org.faudroids.babyface.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;

import org.faudroids.babyface.R;
import org.faudroids.babyface.auth.AuthManager;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleDriveManager;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.faudroids.babyface.videos.VideoManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Handles first time user login + importing faces from Google Drive.
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends AbstractActivity implements ConnectionListener {

	private static final int REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION = 42;

	@Inject private AuthManager authManager;
	@Inject private GoogleDriveManager driveManager;
	@Inject private FacesManager facesManager;
	@Inject private FacesImportViewHandler importViewHandler;
	@Inject private VideoManager videoManager;


	public LoginActivity() {
		super(false, true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (authManager.isSignedIn()) {
			startMainActivity();
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
		authManager.signIn(googleApiClientManager.getGoogleApiClient());

		// first time setup
		driveManager.assertAppRootFolderExists()
				.flatMap(new Func1<Void, Observable<Void>>() {
					@Override
					public Observable<Void> call(Void aVoid) {
						return videoManager.setupFFmpeg();
					}
				})
				.compose(new DefaultTransformer<Void>())
				.subscribe(new Action1<Void>() {
					@Override
					public void call(Void aVoid) {
						importViewHandler.showImportDialog(LoginActivity.this, new FacesImportViewHandler.ImportListener() {
							@Override
							public void onSuccess(FacesImportViewHandler.ImportStatus status) {
								switch (status) {
									case IMPORT_STARTED:
										finish();
										break;

									case IMPORT_ABORTED:
									case NOTHING_TO_IMPORT:
										startMainActivity();
										break;
								}
							}

							@Override
							public void onError(Throwable throwable) {
								// TODO error handling
							}
						}, new Intent(LoginActivity.this, MainDrawerActivity.class));
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to create root dir");
						// TODO error handling
					}
				});
	}


	@Override
	public void onConnectionSuspended(int i) { }


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// missing play services etc.
		if (!connectionResult.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), LoginActivity.this, 0).show();
			return;
		}

		// try resolving error
		try {
			connectionResult.startResolutionForResult(LoginActivity.this, REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION);
		} catch (IntentSender.SendIntentException e) {
			Timber.e(e, "failed to resolve connection error");
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION:
				googleApiClientManager.getGoogleApiClient().connect();
				break;
		}
	}


	private void startMainActivity() {
		startActivity(new Intent(LoginActivity.this, MainDrawerActivity.class));
		finish();
	}


	private void printGoogleAuthToken() {
		Observable
				.defer(new Func0<Observable<String>>() {
					@Override
					public Observable<String> call() {
						try {
							String scope = "oauth2:" + Drive.SCOPE_APPFOLDER.toString();
							Timber.d("scope is " + scope);
							String token = GoogleAuthUtil.getToken(
									LoginActivity.this,
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
						Timber.d("google auth token: " + token);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to get token");
					}
				});

	}
}
