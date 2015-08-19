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
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.faudroids.babyface.videos.VideoConversionService;
import org.faudroids.babyface.videos.VideoConversionStatus;
import org.faudroids.babyface.videos.VideoManager;

import java.io.File;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Shows the progress of a running video conversion.
 */
@ContentView(R.layout.activity_video_conversion)
public class VideoConversionActivity extends AbstractActivity {

	public static final String
			EXTRA_FACE = "EXTRA_FACE",
			EXTRA_STATUS = "EXTRA_STATUS";

	@InjectView(R.id.txt_progress) private TextView progressView;
	@InjectView(R.id.btn_show_video) private Button showVideoButton;

	@Inject private VideoManager videoManager;
	@Inject private NotificationManager notificationManager;

	private final StatusReceiver statusReceiver = new StatusReceiver();
	private VideoConversionStatus lastStatus = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Face face = getIntent().getParcelableExtra(EXTRA_FACE);

		// if started with a status object update UI
		VideoConversionStatus status = getIntent().getParcelableExtra(EXTRA_STATUS);
		if (status != null) onStatusUpdate(status);

		showVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				videoManager.downloadVideo(face, lastStatus)
						.compose(new DefaultTransformer<File>())
						.subscribe(new Action1<File>() {
							@Override
							public void call(File videoFile) {
								Timber.d("written video to " + videoFile.getAbsolutePath());
								Uri videoUri = Uri.parse("file:///" + videoFile.getAbsolutePath());
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setDataAndType(videoUri, "video/mp4");
								startActivity(intent);
							}
						});
			}
		});
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		onStatusUpdate((VideoConversionStatus) intent.getParcelableExtra(EXTRA_STATUS));
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


	private void onStatusUpdate(VideoConversionStatus status) {
		this.lastStatus = status;

		// update progress view
		progressView.setText((int) (lastStatus.getProgress() * 100) + " % complete");

		if (lastStatus.isComplete()) {
			// cancel notification (still running if user did not leave activity)
			notificationManager.cancel(VideoConversionService.NOTIFICATION_ID);

			// update UI
			if (lastStatus.getIsConversionSuccessful()) {
				showVideoButton.setVisibility(View.VISIBLE);
				progressView.setVisibility(View.GONE);
			} else {
				progressView.setText("Sorry, but there was an error converting the video");
			}
		}
	}


	private class StatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			onStatusUpdate((VideoConversionStatus ) intent.getParcelableExtra(VideoConversionService.EXTRA_STATUS));
		}

	}

}
