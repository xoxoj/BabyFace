package org.faudroids.babyface.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.faudroids.babyface.R;
import org.faudroids.babyface.auth.AuthManager;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleDriveManager;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.faudroids.babyface.videos.VideoManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Handles first time user login + importing faces from Google Drive.
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends AbstractActivity implements ConnectionListener {

	private static final int REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION = 42;
	private static final int ANIM_LENGTH = 500;

	@InjectView(R.id.layout_title) private View titleView;
	@InjectView(R.id.img_teddy) private View teddyView;
	@InjectView(R.id.layout_sign_in) private View loginView;

	@Inject private AuthManager authManager;
	@Inject private GoogleDriveManager driveManager;
	@Inject private FacesManager facesManager;
	@Inject private FacesImportViewHandler importViewHandler;
	@Inject private VideoManager videoManager;

	private boolean loginClicked = false;
	private boolean connectionSuccess = false;
	private ConnectionResult connectionResult = null;

	private Animation teddyAnim;
	private AnimationSet titleAnim, btnAnim;

	public LoginActivity() {
		super(false, true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (authManager.isSignedIn()) {
			startMainActivity(false);
		}

		// setup login click
		loginView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginClicked = true;
				if (connectionSuccess) login();
				else if (connectionResult != null) resolveConnectionError(connectionResult);
			}
		});

		// setup animations
		teddyAnim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		teddyAnim.setInterpolator(new OvershootInterpolator());
		teddyAnim.setDuration(ANIM_LENGTH);
		teddyAnim.setStartOffset(500);

		titleAnim = new AnimationSet(true);
		titleAnim.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_SELF, 0));
		titleAnim.addAnimation(new AlphaAnimation(0, 1));
		titleAnim.setDuration(ANIM_LENGTH);
		titleAnim.setStartOffset(500);
		titleAnim.setInterpolator(new DecelerateInterpolator());

		btnAnim = new AnimationSet(true);
		btnAnim.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_SELF, 0));
		btnAnim.addAnimation(new AlphaAnimation(0, 1));
		btnAnim.setDuration(ANIM_LENGTH);
		btnAnim.setStartOffset(500);
		btnAnim.setInterpolator(new DecelerateInterpolator());

		// start animations (only if not resuming activity)
		if (savedInstanceState == null) {
			teddyView.startAnimation(teddyAnim);
			titleView.startAnimation(titleAnim);
			loginView.startAnimation(btnAnim);
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
		if (loginClicked) login();
		else connectionSuccess = true;
	}


	@Override
	public void onConnectionSuspended(int i) { }


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (loginClicked) resolveConnectionError(connectionResult);
		else this.connectionResult = connectionResult;
	}


	private void login() {
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
										startMainActivity(true);
										break;
								}
							}

							@Override
							public void onError(Throwable throwable) {
								new AlertDialog.Builder(LoginActivity.this)
										.setMessage(R.string.error)
										.setTitle(R.string.photo_import_msg_error)
										.setPositiveButton(android.R.string.ok, null)
										.show();
							}
						}, new Intent(LoginActivity.this, MainDrawerActivity.class));
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to create root dir");
						new AlertDialog.Builder(LoginActivity.this)
								.setTitle(R.string.error_setup_drive_title)
								.setMessage(R.string.error_delete_face_msg)
								.setPositiveButton(android.R.string.ok, null)
								.show();
					}
				});
	}


	private void resolveConnectionError(ConnectionResult connectionResult) {
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


	private void startMainActivity(final boolean showExitAnimation) {
		int startDelay = showExitAnimation ? ANIM_LENGTH + 200 : 0;
		titleView.postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(LoginActivity.this, MainDrawerActivity.class));
				finish();
			}
		}, startDelay);
		if (!showExitAnimation) return;

		Animation[] anims = new Animation[] { titleAnim, btnAnim };
		for (Animation anim : anims) {
			anim.reset();
			anim.setInterpolator(new ReverseInterpolator(new DecelerateInterpolator()));
			anim.setFillAfter(true);
		}
		titleView.startAnimation(titleAnim);
		loginView.startAnimation(btnAnim);
	}

}
