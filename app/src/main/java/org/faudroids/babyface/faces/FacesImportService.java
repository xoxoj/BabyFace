package org.faudroids.babyface.faces;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.faudroids.babyface.R;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.ui.FacesImportActivity;
import org.faudroids.babyface.ui.VideoConversionActivity;
import org.faudroids.babyface.utils.AbstractGoogleApiClientService;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import timber.log.Timber;

/**
 * Handles importing of faces.
 */
public class FacesImportService extends AbstractGoogleApiClientService {

	// constants for status updates
	public static final String
			ACTION_STATUS_UPDATE = FacesImportService.class.getName() + ".ACTION_STATUS_UPDATE",
			EXTRA_STATUS = "EXTRA_STATUS";

	public static final String
			EXTRA_FACES_TO_IMPORT = "EXTRA_FACES_TO_IMPORT", // which faces this service should import
			EXTRA_TARGET_INTENT = "EXTRA_TARGET_INTENT"; // argument for FacesImportActivity


	public static final int NOTIFICATION_ID = 45;

	@Inject private PhotoManager photoManager;
	@Inject private FacesScanner facesScanner;

	@Inject private NotificationManager notificationManager;
	private NotificationCompat.Builder notificationBuilder;

	private Intent activityTargetIntent;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) return START_STICKY;

		final List<FacesScanner.ImportableFace> importableFaces = intent.getParcelableArrayListExtra(EXTRA_FACES_TO_IMPORT);
		final FacesImportStatus status = new FacesImportStatus(importableFaces.size());

		// show progress notification
		notificationBuilder = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.photo_import_title))
				.setSmallIcon(R.drawable.ic_notification)
				.setProgress(100, 0, false)
				.setOngoing(true)
				.setContentIntent(createImportActivityIntent(status));
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		updateStatus(status);

		// start import
		facesScanner.importFaces(importableFaces)
				.compose(new DefaultTransformer<Face>())
				.subscribe(new Subscriber<Face>() {
					@Override
					public void onCompleted() {
						notificationManager.cancel(NOTIFICATION_ID);
						stopSelf();
					}

					@Override
					public void onError(Throwable e) {
						Timber.e(e, "failed to import faces");
						status.setHasError(true);
						updateStatus(status);
					}

					@Override
					public void onNext(Face face) {
						Timber.d("face imported, sending out status update");
						status.onFaceImported();
						updateStatus(status);
					}
				});

		return START_STICKY;
	}


	private void updateStatus(FacesImportStatus status) {
		// setup status update
		Intent updateIntent = new Intent(ACTION_STATUS_UPDATE);
		updateIntent.putExtra(EXTRA_STATUS, Parcels.wrap(status));
		LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

		// send status to activity in case it was not running and did not receive last status
		notificationBuilder.setContentIntent(createImportActivityIntent(status));

		// update notification progress
		int progress = (int) status.getProgress() * 100;
		notificationBuilder.setProgress(100, progress, false);
		if (status.isComplete() && !status.getHasError()) {
			notificationBuilder.setContentText(getString(R.string.success));
		} else if (status.isComplete() && status.getHasError()) {
			notificationBuilder.setContentText(getString(R.string.error));
		} else {
			notificationBuilder.setContentText(getString(R.string.photo_import_status, (status.getImportedFacesCount() + 1), status.getFacesToImportCount()));
		}
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	private PendingIntent createImportActivityIntent(FacesImportStatus status) {
		Intent intent = new Intent(this, VideoConversionActivity.class);
		intent.putExtra(FacesImportActivity.EXTRA_STATUS, Parcels.wrap(status));
		intent.putExtra(FacesImportActivity.EXTRA_TARGET_INTENT, activityTargetIntent);
		return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
