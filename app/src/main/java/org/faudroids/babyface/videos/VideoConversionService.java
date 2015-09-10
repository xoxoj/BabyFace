package org.faudroids.babyface.videos;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.ui.VideoConversionActivity;
import org.faudroids.babyface.utils.AbstractGoogleApiClientService;
import org.faudroids.babyface.utils.DefaultTransformer;

import java.io.File;

import javax.inject.Inject;

import rx.functions.Action1;
import timber.log.Timber;

/**
 * Starts the video conversion for one face and shows a progress notification.
 */
public class VideoConversionService extends AbstractGoogleApiClientService {

	public static final String
			ACTION_CONVERSION_COMPLETE = VideoConversionService.class.getName() + ".ACTION_CONVERSION_COMPLETE",
			EXTRA_VIDEO_FILE = "EXTRA_VIDEO_FILE";

	public static final String
			EXTRA_FACE = "EXTRA_FACE";

	public static final int NOTIFICATION_ID = 42;

	@Inject private VideoManager videoManager;
	@Inject private NotificationManager notificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private Face face;

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
		videoManager.createVideo(face)
				.compose(new DefaultTransformer<File>())
				.subscribe(new Action1<File>() {
					@Override
					public void call(File videoFile) {
						Timber.d("create video at " + videoFile.getAbsolutePath());
						sendConversionCompleteUpdate(videoFile);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						// TODO error handling
					}
				});

		return START_STICKY;
	}


	private void sendConversionCompleteUpdate(File videoFile) {
		// send local broadcast with video file
		Intent updateIntent = new Intent(ACTION_CONVERSION_COMPLETE);
		updateIntent.putExtra(EXTRA_VIDEO_FILE, videoFile);
		LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

		// update notification in case target activity did not receive broadcast
		notificationBuilder.setContentIntent(createConversionActivityIntent(videoFile));

		// stop service
		notificationBuilder
				.setProgress(0, 0, false)
				.setOngoing(false)
				.setAutoCancel(true)
				.setContentInfo("Tap for details")
				.setContentTitle("Success");
		stopSelf();
	}


	private PendingIntent createConversionActivityIntent(File videoFile) {
		Intent intent = new Intent(this, VideoConversionActivity.class);
		if (videoFile != null) intent.putExtra(VideoConversionActivity.EXTRA_VIDEO_FILE, videoFile);
		return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
