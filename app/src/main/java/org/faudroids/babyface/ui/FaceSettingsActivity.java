package org.faudroids.babyface.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.PluralsRes;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.photo.ReminderPeriod;
import org.faudroids.babyface.photo.ReminderUnit;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.parceler.Parcels;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Shows settings for one face
 */
@ContentView(R.layout.activity_face_settings)
public class FaceSettingsActivity extends AbstractActivity {

	public static final String EXTRA_FACE_NAME = "EXTRA_FACE_NAME"; // ensures that the newest face details are loaded
	public static final int RESULT_FACE_DELETED = 42;
	private static final int REQUEST_EDIT_REMINDER_SETTINGS = 42;

	@InjectView(R.id.txt_name) private TextView nameTextView;
	@InjectView(R.id.layout_reminder) private View reminderLayout;
	@InjectView(R.id.txt_reminder_period) private TextView reminderTextView;
	@InjectView(R.id.btn_delete) private Button deleteButton;

	@Inject private FacesManager facesManager;
	private Face face;

	public FaceSettingsActivity() {
		super(false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		face = facesManager.findFaceByName(getIntent().getStringExtra(EXTRA_FACE_NAME));
		nameTextView.setText(face.getName());

		reminderTextView.setText(prettyPrintDuration(face.getReminderPeriod()));
		reminderLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent settingsIntent = new Intent(FaceSettingsActivity.this, ReminderSettingsActivity.class);
				settingsIntent.putExtra(ReminderSettingsActivity.EXTRA_FACE, Parcels.wrap(face));
				startActivityForResult(settingsIntent, REQUEST_EDIT_REMINDER_SETTINGS);
			}
		});

		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(FaceSettingsActivity.this)
						.setTitle(getString(R.string.delete_title, face.getName()))
						.setMessage(getString(R.string.delete_message, face.getName()))
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								showProgressBar();
								facesManager.deleteFace(face)
										.compose(new DefaultTransformer<Void>())
										.subscribe(new Action1<Void>() {
											@Override
											public void call(Void aVoid) {
												hideProgressBar();
												setResult(RESULT_FACE_DELETED);
												finish();
											}
										}, new Action1<Throwable>() {
											@Override
											public void call(Throwable throwable) {
												// TODO error handling
												Timber.e(throwable, "failed to delete face with name " + face.getName());
											}
										});
							}
						})
						.show();
			}
		});
	}


	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_EDIT_REMINDER_SETTINGS:
				face = facesManager.findFaceByName(face.getName());
				reminderTextView.setText(prettyPrintDuration(face.getReminderPeriod()));
		}
	}


	private String prettyPrintDuration(ReminderPeriod period) {
		final long unit = period.getUnitInSeconds();
		final int amount = period.getAmount();

		if (amount == 0) {
			return getString(R.string.never);

		} else if (unit == ReminderUnit.MONTH) {
			return prettyPrintDuration(amount, R.plurals.months);

		} else if (unit == ReminderUnit.WEEK) {
			return prettyPrintDuration(amount, R.plurals.weeks);

		} else if (unit == ReminderUnit.DAY) {
			return prettyPrintDuration(amount, R.plurals.days);

		} else if (unit == ReminderUnit.HOUR) {
			return prettyPrintDuration(amount, R.plurals.hours);
		}

		Timber.e("failed to pretty print period " + period);
		return "unknown";
	}


	private String prettyPrintDuration(int amount, @PluralsRes int pluralsRes) {
		return amount + " " + getResources().getQuantityString(pluralsRes, amount);
	}

}
