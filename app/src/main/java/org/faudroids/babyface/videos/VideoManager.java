package org.faudroids.babyface.videos;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

public class VideoManager {

	private static final DateFormat VIDEO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final String VIDEO_FILENAME_REGEX = "(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)_(\\d\\d)-(\\d\\d)-(\\d\\d)\\.mp4";

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
						File faceDir = getFaceDir(face);
						if (!faceDir.exists()) {
							if (!faceDir.mkdirs()) {
								Timber.e("failed to create dir " + faceDir.getAbsolutePath());
								return Observable.error(new IOException("error creating dir"));
							}
						}

						// create video file
						final String videoFileName = VIDEO_DATE_FORMAT.format(new Date()) + ".mp4";
						File videoFile = new File(faceDir, videoFileName);

						// download video
						try {
							ioUtils.copyStream(response.getBody().in(), new FileOutputStream(videoFile));
						} catch (IOException ioe) {
							return Observable.error(ioe);
						}

						// tell media manager about new video
						MediaScannerConnection.scanFile(context, new String[] { videoFile.getAbsolutePath() }, null, null);

						return Observable.just(videoFile);
					}
				});
	}


	public List<VideoInfo> getVideosForFace(final Face face) {
		List<VideoInfo> result = new ArrayList<>();
		File faceDir = getFaceDir(face);
		if (!faceDir.exists()) return result;

		Pattern fileNamePattern = Pattern.compile(VIDEO_FILENAME_REGEX);
		for (File videoFile : faceDir.listFiles()) {
			String fileName = videoFile.getName();
			if (!fileName.matches(VIDEO_FILENAME_REGEX)) continue;

			// parse file name
			Matcher matcher = fileNamePattern.matcher(fileName);
			matcher.find();
			Calendar calendar = Calendar.getInstance();
			calendar.set(
					Integer.valueOf(matcher.group(1)),
					Integer.valueOf(matcher.group(2)) + 1,
					Integer.valueOf(matcher.group(3)),
					Integer.valueOf(matcher.group(4)),
					Integer.valueOf(matcher.group(5)),
					Integer.valueOf(matcher.group(6)));


			result.add(new VideoInfo(face, videoFile, calendar.getTime()));
		}

		return result;
	}


	private File getFaceDir(Face face) {
		File rootDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), context.getString(R.string.app_name));
		return new File(rootDir, face.getPhotoFolderName());
	}

}
