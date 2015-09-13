package org.faudroids.babyface.photo;


import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import com.google.android.gms.common.ConnectionResult;

import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.utils.DefaultTransformer;

import javax.inject.Inject;

import roboguice.service.RoboService;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Checks for a wifi connection starts syncing local photos to Google drive.
 */
public class PhotoSyncService extends RoboService implements ConnectionListener {

	@Inject private PhotoManager photoManager;
	@Inject private GoogleApiClientManager googleApiClientManager;
	@Inject private ConnectivityManager connectivityManager;
	@Inject private PowerManager powerManager;

	private PowerManager.WakeLock wakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Timber.d("starting upload service");
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PhotoSyncService.class.getSimpleName());
		wakeLock.acquire();
		googleApiClientManager.registerListener(this);
		googleApiClientManager.connectToClient();

		// check for wifi connection
		NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (info.isAvailable() && !info.isConnected()) {
			Timber.d("not uploading photos, missing wifi connection");
			stopSelf();
			return START_STICKY;
		}

		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		googleApiClientManager.disconnectFromClient();
		googleApiClientManager.unregisterListener(this);
		wakeLock.release();
	}


	@Override
	public void onConnected(Bundle bundle) {
		// start photo upload
		photoManager
				.syncToGoogleDrive()
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
	}

	@Override
	public void onConnectionSuspended(int i) { }


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Timber.e("failed to connect to google client, shutting down service");
	}

}
