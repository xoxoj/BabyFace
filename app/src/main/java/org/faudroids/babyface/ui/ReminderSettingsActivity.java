package org.faudroids.babyface.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.photo.ReminderManager;
import org.faudroids.babyface.photo.ReminderPeriod;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Shows and and allows editing the reminder settings for one face.
 */
@ContentView(R.layout.activity_reminder_settings)
public class ReminderSettingsActivity extends AbstractActivity {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	@Inject private FacesManager facesManager;
	@Inject private ReminderManager reminderManager;
	private ReminderPeriodViewHandler reminderViewHandler;

	@InjectView(R.id.btn_done) ImageButton doneButton;

	@Override
	public  void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Face face = getIntent().getParcelableExtra(EXTRA_FACE);

		reminderViewHandler = new ReminderPeriodViewHandler(((ViewGroup) findViewById(android.R.id.content)).getChildAt(0));
		reminderViewHandler.setReminderPeriod(face.getReminderPeriod());

		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReminderPeriod newPeriod = reminderViewHandler.getReminderPeriod();
				face.setReminderPeriod(newPeriod);
				facesManager.updateFace(face);

				reminderManager.removeReminder(face);
				reminderManager.addReminder(face);

				finish();
			}
		});
	}

}
