package org.faudroids.babyface.photo;


import android.content.Intent;
import android.os.IBinder;

import org.faudroids.babyface.utils.DefaultTransformer;

import javax.inject.Inject;

import roboguice.service.RoboService;
import rx.functions.Action1;
import timber.log.Timber;

public class PhotoUploadService extends RoboService {

	@Inject private PhotoManager photoManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Timber.d("starting upload service");
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
