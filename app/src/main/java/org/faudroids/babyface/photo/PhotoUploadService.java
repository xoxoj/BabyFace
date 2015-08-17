package org.faudroids.babyface.photo;


import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import org.faudroids.babyface.utils.DefaultTransformer;

import javax.inject.Inject;

import roboguice.service.RoboService;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Checks for a wifi connection starts uploading all (!) photos
 * if present.
 */
public class PhotoUploadService extends RoboService {

	@Inject private PhotoManager photoManager;
	@Inject private ConnectivityManager connectivityManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Timber.d("starting upload service");

		// check for wifi connection
		NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (info.isAvailable() && !info.isConnected()) {
			Timber.d("not uploading photos, missing wifi connection");
			stopSelf();
			return START_STICKY;
		}

		// start photo upload
		photoManager
				.uploadAllPhotos()
				.compose(new DefaultTransformer<Void>())
				.subscribe(new Action1<Void>() {
					@Override
					public void call(Void aVoid) {
						Timber.d("shutting down upload service");
						stopSelf();
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "upload failed");
						stopSelf();
					}
				});

		return START_STICKY;
	}

}
