package org.faudroids.babyface.photo;

import android.content.Context;
import android.content.Intent;

import org.faudroids.babyface.faces.FacesManager;

import javax.inject.Inject;

import roboguice.receiver.RoboBroadcastReceiver;

/**
 * Restarts all reminders when the device has been rebooted.
 */
public class RestartRemindersReceiver extends RoboBroadcastReceiver {

	@Inject private FacesManager facesManager;
	@Inject private ReminderManager reminderManager;

	@Override
	protected void handleReceive(Context context, Intent intent) {
		if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) return;
		reminderManager.restartAllReminders(facesManager.getFaces());
	}

}
