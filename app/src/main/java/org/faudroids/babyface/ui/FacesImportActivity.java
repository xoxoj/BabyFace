package org.faudroids.babyface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.FacesImportService;
import org.faudroids.babyface.faces.FacesImportStatus;
import org.parceler.Parcels;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Shows the face importing progress.
 */
@ContentView(R.layout.activity_import_faces)
public class FacesImportActivity extends AbstractActivity {

	public static final String
			EXTRA_STATUS = "EXTRA_STATUS",
			EXTRA_TARGET_INTENT = "EXTRA_TARGET_INTENT"; // which activity to launch once import is complete. Can be null!

	private final StatusReceiver statusReceiver = new StatusReceiver();

	@InjectView(R.id.txt_msg) private TextView msgTextView;
	@InjectView(R.id.layout_progress) private View progressLayout;
	@InjectView(R.id.txt_progress) private TextView progressTextView;
	@InjectView(R.id.btn_done) private Button doneButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent targetIntent = getIntent().getParcelableExtra(EXTRA_TARGET_INTENT);
		final FacesImportStatus status = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_STATUS));
		if (status != null) onStatusUpdate(status);

		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (targetIntent != null) startActivity(targetIntent);
				finish();
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		onStatusUpdate((FacesImportStatus) Parcels.unwrap(intent.getParcelableExtra(FacesImportService.EXTRA_STATUS)));
	}


	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, new IntentFilter(FacesImportService.ACTION_STATUS_UPDATE));
	}


	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
		super.onPause();
	}


	private void onStatusUpdate(FacesImportStatus status) {
		if (!status.isComplete()) {
			progressTextView.setText(getString(R.string.photo_import_status, (status.getImportedFacesCount() + 1), status.getFacesToImportCount()));
		} else {
			progressLayout.setVisibility(View.GONE);
			doneButton.setVisibility(View.VISIBLE);

			if (!status.getHasError()) msgTextView.setText(R.string.photo_import_msg_done);
			else msgTextView.setText(R.string.photo_import_msg_error);
		}
	}


	private class StatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			onStatusUpdate((FacesImportStatus) Parcels.unwrap(intent.getParcelableExtra(FacesImportService.EXTRA_STATUS)));
		}

	}

}
