package org.faudroids.babyface.photo;

import android.content.Context;
import android.content.Intent;

import org.faudroids.babyface.faces.Face;

import javax.inject.Inject;

import roboguice.receiver.RoboBroadcastReceiver;
import timber.log.Timber;

/**
 * Receives reminder notifications and shows take photo notification
 */
public class ReminderReceiver extends RoboBroadcastReceiver {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	@Inject private ReminderManager reminderManager;

	@Override
	protected void handleReceive(Context context, Intent intent) {
		Timber.d("reminder!!");
		Face face = intent.getParcelableExtra(EXTRA_FACE);
		reminderManager.onReminderTriggered(face);
	}

}
