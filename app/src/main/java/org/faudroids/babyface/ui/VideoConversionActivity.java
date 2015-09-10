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
import org.faudroids.babyface.videos.VideoManager;

import java.io.File;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Shows the progress of a running video conversion.
 */
@ContentView(R.layout.activity_video_conversion)
public class VideoConversionActivity extends AbstractActivity {

	public static final String EXTRA_VIDEO_FILE = "EXTRA_VIDEO_FILE";

	@InjectView(R.id.txt_progress) private TextView progressView;
	@InjectView(R.id.btn_show_video) private Button showVideoButton;

	@Inject private VideoManager videoManager;
	@Inject private NotificationManager notificationManager;

	private final StatusReceiver statusReceiver = new StatusReceiver();
	private File videoFile = null;

	public VideoConversionActivity() {
		super(false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if started with finished video
		videoFile = (File) getIntent().getSerializableExtra(EXTRA_VIDEO_FILE);
		if (videoFile != null) onConversionComplete(videoFile);

		showVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri videoUri = Uri.parse("file:///" + videoFile.getAbsolutePath());
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
		onConversionComplete((File) intent.getSerializableExtra(EXTRA_VIDEO_FILE));
	}


	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, new IntentFilter(VideoConversionService.ACTION_CONVERSION_COMPLETE));
	}


	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
		super.onPause();
	}


	private void onConversionComplete(File videoFile) {
		this.videoFile = videoFile;
		showVideoButton.setVisibility(View.VISIBLE);
		progressView.setVisibility(View.GONE);
		notificationManager.cancel(VideoConversionService.NOTIFICATION_ID);
	}


	private class StatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			onConversionComplete((File) intent.getSerializableExtra(VideoConversionService.EXTRA_VIDEO_FILE));
		}

	}

}
