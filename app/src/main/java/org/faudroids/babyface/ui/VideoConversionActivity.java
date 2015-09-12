package org.faudroids.babyface.ui;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.videos.VideoConversionService;
import org.faudroids.babyface.videos.VideoConversionStatus;
import org.faudroids.babyface.videos.VideoManager;
import org.parceler.Parcels;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Shows the progress of a running video conversion.
 */
@ContentView(R.layout.activity_video_conversion)
public class VideoConversionActivity extends AbstractActivity {

	public static final String EXTRA_STATUS = "EXTRA_STATUS";

	@InjectView(R.id.layout_progress) private View progressLayout;
	@InjectView(R.id.txt_progress) private TextView progressTextView;
	@InjectView(R.id.btn_show_video) private Button showVideoButton;

	@Inject private VideoManager videoManager;
	@Inject private NotificationManager notificationManager;

	private final StatusReceiver statusReceiver = new StatusReceiver();
	private VideoConversionStatus conversionStatus;

	public VideoConversionActivity() {
		super(false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if started with finished video
		conversionStatus = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_STATUS));
		if (conversionStatus != null) onStatusUpdate(conversionStatus);

		showVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri videoUri = Uri.parse("file:///" + conversionStatus.getVideoFile().getAbsolutePath());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(videoUri, "video/mp4");
				startActivity(intent);
				finish();
			}
		});
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		onStatusUpdate((VideoConversionStatus) Parcels.unwrap(intent.getParcelableExtra(EXTRA_STATUS)));
	}


	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, new IntentFilter(VideoConversionService.ACTION_STATUS_UPDATE));
	}


	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
		super.onPause();
	}


	private void onStatusUpdate(VideoConversionStatus conversionStatus) {
		this.conversionStatus = conversionStatus;

		if (conversionStatus.isComplete()) {
			// cancel notification (still running if user did not leave activity)
			notificationManager.cancel(VideoConversionService.NOTIFICATION_ID);

			// update UI
			if (!conversionStatus.isError()) {
				progressLayout.setVisibility(View.GONE);
				showVideoButton.setVisibility(View.VISIBLE);
			} else {
				progressTextView.setText(R.string.video_conversion_error);
			}

		} else {
			int progress = (int) (conversionStatus.getProgress() * 100);
			progressTextView.setText(getString(R.string.video_conversion_progress, progress));
		}
	}


	private class StatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			onStatusUpdate((VideoConversionStatus) Parcels.unwrap(intent.getParcelableExtra(EXTRA_STATUS)));
		}

	}

}
