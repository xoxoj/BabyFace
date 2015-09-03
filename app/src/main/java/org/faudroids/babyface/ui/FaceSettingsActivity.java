package org.faudroids.babyface.ui;


import android.os.Bundle;
import android.support.annotation.PluralsRes;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.photo.ReminderUnit;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

/**
 * Shows settings for one face
 */
@ContentView(R.layout.activity_face_settings)
public class FaceSettingsActivity extends AbstractActivity {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	@InjectView(R.id.txt_name) private TextView nameTextView;
	@InjectView(R.id.txt_reminder_period) private TextView reminderTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Face face = getIntent().getParcelableExtra(EXTRA_FACE);
		nameTextView.setText(face.getName());
		reminderTextView.setText(prettyPrintDuration(face.getReminderPeriodInSeconds()));
	}


	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
	}


	private String prettyPrintDuration(long durationInSeconds) {
		if (durationInSeconds / ReminderUnit.MONTH > 0) {
			return prettyPrintDuration(durationInSeconds, ReminderUnit.MONTH, R.plurals.months);
		} else if (durationInSeconds / ReminderUnit.WEEK> 0) {
			return prettyPrintDuration(durationInSeconds, ReminderUnit.WEEK, R.plurals.weeks);
		} else if (durationInSeconds / ReminderUnit.DAY > 0) {
			return prettyPrintDuration(durationInSeconds, ReminderUnit.DAY, R.plurals.days);
		} else if (durationInSeconds / ReminderUnit.HOUR > 0) {
			return prettyPrintDuration(durationInSeconds, ReminderUnit.HOUR, R.plurals.hours);
		}
		Timber.e("failed to pretty print duration " + durationInSeconds + " seconds");
		return "unknown";
	}


	private String prettyPrintDuration(long durationInSeconds, int durationUnit, @PluralsRes int pluralsRes) {
		int amount = (int) (durationInSeconds / durationUnit);
		return amount + " " + getResources().getQuantityString(pluralsRes, amount);
	}

}
