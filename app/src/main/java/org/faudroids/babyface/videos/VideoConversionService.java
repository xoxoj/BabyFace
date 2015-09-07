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
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.ui.VideoConversionActivity;
import org.faudroids.babyface.utils.DefaultTransformer;

import javax.inject.Inject;

import roboguice.service.RoboService;
import rx.functions.Action1;

/**
 * Starts the video conversion for one face and shows a progress notification.
 */
public class VideoConversionService extends RoboService {

	public static final String
			ACTION_STATUS_UPDATE = "org.faudroids.babyface.ACTION_STATUS_UDPATE",
			EXTRA_STATUS = "EXTRA_STATUS";

	public static final String
			EXTRA_FACE = "EXTRA_FACE";

	public static final int NOTIFICATION_ID = 42;

	@Inject private VideoService videoService;
	@Inject private GoogleApiClientManager googleApiClientManager;

	private Face face;

	@Inject private NotificationManager notificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private final Handler handler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		googleApiClientManager.connectToClient();
	}


	@Override
	public void onDestroy() {
		googleApiClientManager.disconnectFromClient();
		super.onDestroy();
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

		videoService.createVideo(new FaceMetaData(face.getName()))
				.compose(new DefaultTransformer<VideoConversionStatus>())
				.subscribe(new Action1<VideoConversionStatus>() {
					@Override
					public void call(VideoConversionStatus status) {
						updateStatus(status);
					}
				});

		return START_STICKY;
	}


	private void updateStatus(final VideoConversionStatus status) {
		// setup status update
		Intent updateIntent = new Intent(ACTION_STATUS_UPDATE);
		updateIntent.putExtra(EXTRA_STATUS, status);
		LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

		// send status to activity in case it was not running and did not receive last status
		notificationBuilder.setContentIntent(createConversionActivityIntent(status));

		// stop service if necessary
		if (status.isComplete()) {
			notificationBuilder
					.setProgress(0, 0, false)
					.setOngoing(false)
					.setAutoCancel(true)
					.setContentInfo("Tap for details");
			if (!status.getIsConversionSuccessful()) {
				notificationBuilder.setContentTitle("Error");
			} else {
				notificationBuilder.setContentTitle("Success");
			}
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
			stopSelf();
			return;
		}

		// update notification progress
		int progress = (int) (status.getProgress() * 100);
		notificationBuilder
				.setProgress(100, progress, false)
				.setContentText(progress + " % complete");
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				videoService.getStatus(status.getVideoId())
						.compose(new DefaultTransformer<VideoConversionStatus>())
						.subscribe(new Action1<VideoConversionStatus>() {
							@Override
							public void call(VideoConversionStatus status) {
								updateStatus(status);
							}
						});
			}
		}, 1000);
	}


	private PendingIntent createConversionActivityIntent(VideoConversionStatus status) {
		Intent intent = new Intent(this, VideoConversionActivity.class);
		intent.putExtra(VideoConversionActivity.EXTRA_FACE, face);
		if (status != null) intent.putExtra(VideoConversionActivity.EXTRA_STATUS, status);
		return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
