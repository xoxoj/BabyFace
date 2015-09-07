package org.faudroids.babyface.photo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.ui.CapturePhotoActivity;

import javax.inject.Inject;

import roboguice.receiver.RoboBroadcastReceiver;
import timber.log.Timber;

/**
 * Receives reminder notifications and shows take photo notification
 */
public class ReminderReceiver extends RoboBroadcastReceiver {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	@Inject private ReminderManager reminderManager;
	@Inject private NotificationManager notificationManager;

	@Override
	protected void handleReceive(Context context, Intent intent) {
		Face face = intent.getParcelableExtra(EXTRA_FACE);
		Timber.d("triggered reminder for face " + face.getName());
		reminderManager.onReminderTriggered(face);
		final int notificationId = faceToNotificationId(face);

		// build the photo taking intent
		Intent photoIntent = new Intent(context, CapturePhotoActivity.class);
		photoIntent.putExtra(CapturePhotoActivity.EXTRA_FACE_NAME, face.getName());
		photoIntent.putExtra(CapturePhotoActivity.EXTRA_NOTIFICATION_ID, notificationId);
		photoIntent.putExtra(CapturePhotoActivity.EXTRA_UPLOAD_PHOTO, true);
		PendingIntent photoPendingIntent = PendingIntent.getActivity(context, 0, photoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// show notification
		String msg = context.getString(R.string.time_to_take_photo, face.getName());
		Notification notification = new NotificationCompat.Builder(context)
				.setAutoCancel(true)
				.setContentTitle(context.getString(R.string.photo_time))
				.setContentText(msg)
				.setSmallIcon(R.drawable.ic_notification)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_photo_camera_white_24dp, context.getString(R.string.take_photo), photoPendingIntent).build())
				.build();

		notificationManager.notify(faceToNotificationId(face), notification);
	}


	private int faceToNotificationId(Face face) {
		return face.getName().hashCode();
	}

}
