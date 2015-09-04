package org.faudroids.babyface.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.PluralsRes;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.photo.ReminderUnit;
import org.faudroids.babyface.utils.DefaultTransformer;

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

	public static final String EXTRA_FACE = "EXTRA_FACE";
	public static final int RESULT_FACE_DELETED = 42;

	@InjectView(R.id.txt_name) private TextView nameTextView;
	@InjectView(R.id.txt_reminder_period) private TextView reminderTextView;
	@InjectView(R.id.btn_delete) private Button deleteButton;

	@Inject private FacesManager facesManager;

	public FaceSettingsActivity() {
		super(false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Face face = getIntent().getParcelableExtra(EXTRA_FACE);
		nameTextView.setText(face.getName());
		reminderTextView.setText(prettyPrintDuration(face.getReminderPeriodInSeconds()));
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
												Timber.e(throwable, "failed to delete face with id " + face.getId());
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
