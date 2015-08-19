package org.faudroids.babyface.videos;

import android.content.Context;
import android.os.Environment;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

public class VideoManager {

	private static final DateFormat VIDEO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	private final Context context;
	private final VideoService videoService;
	private final IOUtils ioUtils;

	@Inject
	VideoManager(Context context, VideoService videoService, IOUtils ioUtils) {
		this.context = context;
		this.videoService = videoService;
		this.ioUtils = ioUtils;
	}


	public Observable<File> downloadVideo(final Face face, VideoConversionStatus status) {
		return videoService.getVideo(status.getVideoId())
				.flatMap(new Func1<Response, Observable<File>>() {
					@Override
					public Observable<File> call(Response response) {
						// create dir file
						File rootDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), context.getString(R.string.app_name));
						if (!rootDir.exists()) {
							if (!rootDir.mkdirs()) {
								Timber.e("failed to create dir " + rootDir.getAbsolutePath());
								return Observable.error(new IOException("error creating dir"));
							}
						}

						// create video file
						final String videoFileName = face.getName() + VIDEO_DATE_FORMAT.format(new Date()) + ".mp4";
						File videoFile = new File(rootDir, videoFileName);

						// download video
						try {
							ioUtils.copyStream(response.getBody().in(), new FileOutputStream(videoFile));
						} catch (IOException ioe) {
							return Observable.error(ioe);
						}

						return Observable.just(videoFile);
					}
				});
	}

}
