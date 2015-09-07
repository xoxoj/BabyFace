package org.faudroids.babyface.photo;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.faudroids.babyface.faces.Face;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Starts / stops alarms for notifying users to take photos.
 */
public class ReminderManager {

	private static final String PREFS_NAME = "org.faudroids.babyface.ReminderManager";
	private static final String
			KEY_REMINDER_COUNTER = "REMINDER_COUNTER",
			PROPERTY_LAST_TRIGGER = "LAST_TRIGGER",
			PROPERTY_REMINDER_ID = "REMINDER_ID",
			PROPERTY_FACE = "FACE";

	private static final ObjectMapper mapper = new ObjectMapper();

	private final Context context;
	private final AlarmManager alarmManager;

	private int reminderCounter; // cyclic counter for assigning ids to alarms

	@Inject
	ReminderManager(Context context, AlarmManager alarmManager) {
		this.context = context;
		this.alarmManager = alarmManager;
		this.reminderCounter = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_REMINDER_COUNTER, 0);
	}


	public void addReminder(Face face) {
		Timber.d("adding reminder for " + face.getName());
		long firstReminderTimestamp = System.currentTimeMillis() + face.getReminderPeriodInSeconds() * 1000;
		long interval = face.getReminderPeriodInSeconds() * 1000;
		int reminderId = addReminderToPrefs(face, firstReminderTimestamp);

		alarmManager.setInexactRepeating(
				AlarmManager.RTC_WAKEUP,
				firstReminderTimestamp,
				interval,
				createPendingIntent(face, reminderId));
	}


	public void removeReminder(Face face) {
		Timber.d("removing reminder for " + face.getName());
		int reminderId = removeReminderFromPrefs(face);
		alarmManager.cancel(createPendingIntent(face, reminderId));
	}


	/**
	 * Call this to track the last trigger time of each reminder
	 */
	public void onReminderTriggered(Face face) {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putLong(toKey(face, PROPERTY_LAST_TRIGGER), System.currentTimeMillis());
		editor.apply();
	}


	/**
	 * Call this when needing to start all reminders e.g. after device reboot
	 */
	public void restartAllReminders() {
		Timber.d("restarting reminders");
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		int reminderCount = 0;
		for (String key : prefs.getAll().keySet()) {
			if (!key.endsWith(PROPERTY_REMINDER_ID)) continue;

			try {
				String faceId = key.substring(0, key.indexOf("." + PROPERTY_REMINDER_ID));
				int reminderId = prefs.getInt(toKey(faceId, PROPERTY_REMINDER_ID), -1);
				long lastTrigger = prefs.getLong(toKey(faceId, PROPERTY_LAST_TRIGGER), -1);
				Face face = mapper.readValue(prefs.getString(toKey(faceId, PROPERTY_FACE), null), Face.class);
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

			} catch (IOException e) {
				Timber.e(e, "failed to deserialize face");
			}
		}
		Timber.d("restarted " + reminderCount + " reminders");
	}


	/**
	 * @return the id for this reminder
	 */
	private int addReminderToPrefs(Face face, long firstReminderTimestamp) {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

		int reminderId = reminderCounter;
		++reminderCounter;
		editor.putInt(toKey(face, PROPERTY_REMINDER_ID), reminderId);
		editor.putLong(toKey(face, PROPERTY_LAST_TRIGGER), firstReminderTimestamp);
		try {
			editor.putString(toKey(face, PROPERTY_FACE), mapper.writeValueAsString(face));
		} catch (JsonProcessingException e) {
			Timber.e(e, "failed to serialize face");
		}
		editor.putInt(KEY_REMINDER_COUNTER, reminderCounter);
		editor.apply();

		return reminderId;
	}


	/**
	 * @return the id for this reminder
	 */
	private int removeReminderFromPrefs(Face face) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor  = prefs.edit();

		int reminderId  = prefs.getInt(toKey(face, PROPERTY_REMINDER_ID), -1);
		editor.remove(toKey(face, PROPERTY_LAST_TRIGGER));
		editor.remove(toKey(face, PROPERTY_FACE));
		editor.remove(toKey(face, PROPERTY_REMINDER_ID));
		editor.apply();

		return reminderId;
	}


	private PendingIntent createPendingIntent(Face face, int reminderId) {
		Intent intent = new Intent(context, ReminderReceiver.class);
		intent.putExtra(ReminderReceiver.EXTRA_FACE, face);
		return PendingIntent.getBroadcast(context, reminderId, intent, 0);
	}


	private String toKey(Face face, String property) {
		return toKey(face.getName(), property);
	}


	private String toKey(String faceId, String property) {
		return faceId + "." + property;
	}
}
