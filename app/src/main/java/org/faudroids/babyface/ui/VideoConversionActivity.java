package org.faudroids.babyface.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.faudroids.babyface.videos.VideoConversionStatus;
import org.faudroids.babyface.videos.VideoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import retrofit.client.Response;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;


@ContentView(R.layout.activity_video_conversion)
public class VideoConversionActivity extends AbstractActivity {

	private static final String STATE_STATUS = "STATE_STATUS";

	@InjectView(R.id.btn_start_conversion) private Button startConversionButton;
	@InjectView(R.id.btn_show_video) private Button showVideoButton;
	@InjectView(R.id.btn_reset) private Button resetButton;
	@InjectView(R.id.txt_status) private TextView statusTextView;

	@Inject private VideoService videoService;
	private VideoConversionStatus status;
	private StatusUpdateTask statusUpdateTask;

	@Inject private GoogleApiClientManager googleApiClientManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup conversion start
		startConversionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				videoService.createVideo()
						.compose(new DefaultTransformer<VideoConversionStatus>())
						.subscribe(new Action1<VideoConversionStatus>() {
							@Override
							public void call(VideoConversionStatus status) {
								VideoConversionActivity.this.status = status;
								updateStatus();
							}
						});
			}
		});

		// setup show video
		showVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(VideoConversionActivity.this, "Stub", Toast.LENGTH_SHORT).show();
			}
		});

		// setup view video button
		showVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				videoService.getVideo(status.getVideoId())
						.flatMap(new Func1<Response, Observable<File>>() {
							@Override
							public Observable<File> call(Response response) {
								// create dir file
								File rootDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), getString(R.string.app_name));
								if (!rootDir.exists()) {
									if (!rootDir.mkdirs()) {
										Timber.e("failed to create dir " + rootDir.getAbsolutePath());
										return Observable.error(new IOException("error creating dir"));
									}
								}

								// create video file
								File videoFile = new File(rootDir, "out.mp4");
								videoFile.deleteOnExit();

								// download video
								OutputStream outStream = null;
								try {
									outStream = new FileOutputStream(videoFile);
									InputStream inStream = response.getBody().in();
									byte[] buffer = new byte[1024];
									int bytesRead;
									while ((bytesRead = inStream.read(buffer)) != -1) {
										outStream.write(buffer, 0, bytesRead);
									}
								} catch (IOException ioe) {
									return Observable.error(ioe);
								} finally {
									if (outStream != null) {
										try {
											outStream.close();
										} catch (IOException e) {
											Timber.e(e, "failed to close stream");
										}
									}
								}

								return Observable.just(videoFile);
							}
						})
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

		// setup reset
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				status = null;
				updateStatus();
			}
		});

		// restore state
		if (savedInstanceState != null) {
			status = savedInstanceState.getParcelable(STATE_STATUS);
		}

		// set status in ui
		updateStatus();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(STATE_STATUS, status);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStart() {
		googleApiClientManager.connectToClient();
		super.onStart();
	}


	@Override
	public void onPause() {
		stopStatusUpdates();
		super.onPause();
	}


	@Override
	public void onResume() {
		super.onResume();
		updateStatus();
	}


	@Override
	public void onStop() {
		googleApiClientManager.disconnectFromClient();
		super.onStop();
	}


	private void updateStatus() {
		// "reset" state
		if (status == null) {
			statusTextView.setText("");
			startConversionButton.setEnabled(true);
			showVideoButton.setEnabled(false);
			stopStatusUpdates();
			return;
		}

		// conversion running or finished
		startConversionButton.setEnabled(false);

		// conversion running
		if (!status.isComplete()) {
			statusTextView.setText("converting ...");
			showVideoButton.setEnabled(false);
			if (statusUpdateTask == null) {
				statusUpdateTask = new StatusUpdateTask();
				statusTextView.postDelayed(statusUpdateTask, 1000);
			}
			return;
		}

		// stop status updates (conversion has finished)
		stopStatusUpdates();

		// finish successful
		if (status.getIsConversionSuccessful()) {
			statusTextView.setText("done");
			showVideoButton.setEnabled(true);
			return;
		}

		// finish error
		statusTextView.setText("conversion error");
		showVideoButton.setEnabled(false);
	}


	private void stopStatusUpdates() {
		if (statusUpdateTask != null) {
			statusUpdateTask.stop();
			statusUpdateTask = null;
		}
	}


	private class StatusUpdateTask implements Runnable {

		private boolean isRunning = true;

		@Override
		public void run() {
			if (status == null) return;
			videoService.getStatus(status.getVideoId())
					.compose(new DefaultTransformer<VideoConversionStatus>())
					.subscribe(new Action1<VideoConversionStatus>() {
						@Override
						public void call(VideoConversionStatus status) {
							if (!isRunning) return;
							VideoConversionActivity.this.status = status;
							updateStatus();
							statusTextView.postDelayed(StatusUpdateTask.this, 1000);
						}
					});
		}

		public void stop() {
			isRunning = false;
		}

	}

}
