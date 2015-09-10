package org.faudroids.babyface.videos;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.ui.VideoConversionActivity;
import org.faudroids.babyface.utils.AbstractGoogleApiClientService;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.parceler.Parcels;

import java.io.File;

import javax.inject.Inject;

import rx.functions.Action1;
import timber.log.Timber;

/**
 * Starts the video conversion for one face and shows a progress notification.
 */
public class VideoConversionService extends AbstractGoogleApiClientService {

	public static final String
			ACTION_STATUS_UPDATE = VideoConversionService.class.getName() + ".ACTION_STATUS_UPDATE",
			EXTRA_STATUS = "EXTRA_STATUS";

	public static final String
			EXTRA_FACE = "EXTRA_FACE";

	public static final int NOTIFICATION_ID = 42;

	private static final int UPDATE_INTERVAL = 1000; // ms

	@Inject private VideoManager videoManager;
	@Inject private NotificationManager notificationManager;
	private NotificationCompat.Builder notificationBuilder;

	private final Handler handler = new Handler();
	private Face face;

	private VideoManager.VideoConversion runningConversion;
	private VideoConversionStatus lastStatus;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) return START_STICKY;
		this.face = intent.getParcelableExtra(EXTRA_FACE);

		// show progress notification
		notificationBuilder = new NotificationCompat.Builder(this)
				.setContentTitle("Creating movie")
				.setContentText("0 % complete")
				.setSmallIcon(R.drawable.ic_notification)
				.setProgress(100, 0, false)
				.setOngoing(true)
				.setContentIntent(createConversionActivityIntent(null));
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

		// start conversion
		runningConversion = videoManager.createVideo(face);
		runningConversion.toObservable()
				.compose(new DefaultTransformer<File>())
				.subscribe(new Action1<File>() {
					@Override
					public void call(File videoFile) {
						Timber.d("created video at " + videoFile.getAbsolutePath());
						sendStatusUpdate(new VideoConversionStatus(1, videoFile, false));
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						sendStatusUpdate(new VideoConversionStatus(1, null, true));
					}
				});

		// send first status update
		sendStatusUpdate(new VideoConversionStatus(0, null, false));

		return START_STICKY;
	}


	private void sendStatusUpdate(final VideoConversionStatus status) {
		this.lastStatus = status;

		// send local broadcast with video file
		Intent updateIntent = new Intent(ACTION_STATUS_UPDATE);
		updateIntent.putExtra(EXTRA_STATUS, Parcels.wrap(status));
		LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

		// update notification in case target activity did not receive broadcast
		notificationBuilder.setContentIntent(createConversionActivityIntent(status));

		if (status.isComplete()) {
			// stop service
			notificationBuilder
					.setProgress(0, 0, false)
					.setOngoing(false)
					.setAutoCancel(true)
					.setContentInfo("Tap for details");
			stopSelf();

			if (!status.isError()) notificationBuilder.setContentText("Success");
			else notificationBuilder.setContentText("Error");

		} else {
			int progress = (int) (status.getProgress() * 100);
			notificationBuilder
					.setProgress(100, progress, false)
					.setContentText(progress + " % complete");

			// schedule another update
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (lastStatus == null || lastStatus.isComplete()) return;
					final float progress = videoManager.getVideoConversionProgress(runningConversion);
					sendStatusUpdate(new VideoConversionStatus(progress, status.getVideoFile(), false));
				}
			}, UPDATE_INTERVAL);
		}

		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	private PendingIntent createConversionActivityIntent(VideoConversionStatus status) {
		Intent intent = new Intent(this, VideoConversionActivity.class);
		intent.putExtra(VideoConversionActivity.EXTRA_STATUS, Parcels.wrap(status));
		return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
