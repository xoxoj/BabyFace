package org.faudroids.babyface.photo;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.utils.Pref;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Starts / stops alarms for notifying users to take photos.
 */
public class ReminderManager {

	private static final String PREFS_NAME = "org.faudroids.babyface.ReminderManager";

	private final Context context;
	private final AlarmManager alarmManager;

	private final Pref<Integer> reminderCounter; // cyclic counter for assigning ids to alarms

	@Inject
	ReminderManager(Context context, AlarmManager alarmManager) {
		this.context = context;
		this.alarmManager = alarmManager;
		this.reminderCounter = Pref.newIntPref(context, PREFS_NAME, "reminder_counter", 0);
	}


	public void addReminder(Face face) {
		Timber.d("adding reminder for " + face.getName());

		// get first trigger timestamp
		long firstReminderTimestamp = System.currentTimeMillis() + face.getReminderPeriodInSeconds() * 1000;
		long interval = face.getReminderPeriodInSeconds() * 1000;
		int reminderId = reminderCounter.get();
		reminderCounter.set(reminderId + 1);

		// store reminder info (not yet persistent!)
		face.setReminderId(reminderId);
		face.setLastReminderTrigger(firstReminderTimestamp);    // Timestamp in future since trigger has not been called
		// Required for restarting reminders.

		// start repeating alarm
		alarmManager.setInexactRepeating(
				AlarmManager.RTC_WAKEUP,
				firstReminderTimestamp,
				interval,
				createPendingIntent(face, reminderId));
	}


	public void removeReminder(Face face) {
		Timber.d("removing reminder for " + face.getName());
		int reminderId = face.getReminderId();
		alarmManager.cancel(createPendingIntent(face, reminderId));
	}


	/**
	 * Call this to track the last trigger time of each reminder
	 */
	public void onReminderTriggered(Face face) {
		// update when this reminder was last triggered (not yet persistent!)
		face.setLastReminderTrigger(System.currentTimeMillis());
	}


	/**
	 * Call this when needing to start all reminders e.g. after device reboot
	 */
	public void restartAllReminders(List<Face> faces) {
		Timber.d("restarting reminders");

		int reminderCount = 0;
		for (Face face : faces) {
			int reminderId = face.getReminderId();
			long lastTrigger = face.getLastReminderTrigger();
			long interval = face.getReminderPeriodInSeconds() * 1000;

			// only if reminder has not triggered before (!) add interval
			if (lastTrigger <= System.currentTimeMillis()) {
				lastTrigger += interval;
			}

			alarmManager.setInexactRepeating(
					AlarmManager.RTC_WAKEUP,
					lastTrigger,
					interval,
					createPendingIntent(face, reminderId));

			++reminderCount;
		}

		Timber.d("restarted " + reminderCount + " reminders");
	}


	private PendingIntent createPendingIntent(Face face, int reminderId) {
		Intent intent = new Intent(context, ReminderReceiver.class);
		intent.putExtra(ReminderReceiver.EXTRA_FACE, face);
		return PendingIntent.getBroadcast(context, reminderId, intent, 0);
	}

}
