package org.faudroids.babyface.videos;

import android.content.Context;
import android.os.Environment;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.utils.IOUtils;
import org.faudroids.babyface.utils.Pref;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import timber.log.Timber;

public class VideoManager {

	private static final DateFormat VIDEO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final String VIDEO_FILENAME_REGEX = "(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)_(\\d\\d)-(\\d\\d)-(\\d\\d)\\.mp4";
	private static final String TMP_VIDEO_DIR = "/video";

	private static final int
			IMAGE_LENGTH_IN_SECONDS = 1,
			FRAMERATE = 25;

	private final Context context;
	private final PhotoManager photoManager;
	private final IOUtils ioUtils;
	private final Pref<Boolean> firstSetup;
	private final FFmpeg fFmpeg;


	@Inject
	VideoManager(Context context, PhotoManager photoManager, IOUtils ioUtils) {
		this.context = context;
		this.photoManager = photoManager;
		this.ioUtils = ioUtils;
		this.firstSetup = Pref.newBooleanPref(context, VideoManager.class.getName() + ".FIRST_SETUP", "first_setup", true);
		this.fFmpeg = FFmpeg.getInstance(context);
	}


	/**
	 * One time setup of the FFmpeg library.
	 */
	public Observable<Void> setupFFmpeg() {
		if (!firstSetup.get()) return Observable.just(null);
		firstSetup.set(false);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(final Subscriber<? super Void> subscriber) {
				LoadBinaryResponseHandler responseHandler = new LoadBinaryResponseHandler() {
					@Override
					public void onFailure() {
						if (subscriber.isUnsubscribed()) return;
						subscriber.onError(new IllegalStateException("failed to load FFmpeg binaries"));
					}

					@Override
					public void onSuccess() {
						if (subscriber.isUnsubscribed()) return;
						subscriber.onNext(null);
						subscriber.onCompleted();
					}
				};

				try {
					fFmpeg.loadBinary(responseHandler);
				} catch (FFmpegNotSupportedException e) {
					subscriber.onError(e);
				}
			}
		});
	}


	public Observable<File> createVideo(final Face face) {
		return photoManager.getPhotosForFace(face)
				.flatMap(new Func1<List<File>, Observable<File>>() {
					@Override
					public Observable<File> call(List<File> photoFiles) {
						// rename photos to img0000.jpg
						Collections.sort(photoFiles);
						int idx = 0;
						for (File oldFile : photoFiles) {
							++idx;
							File newFile = new File(getTmpVideoDir(), "img" + String.format("%03d", idx) + ".jpg");
							boolean success = oldFile.renameTo(newFile);
							if (!success) Timber.e("failed to rename file " + oldFile.getName() + " to " + newFile.getName());
							else Timber.d("written file " + newFile.getAbsolutePath());
						}

						// create conversion command
						final String photoFileNameTemplate = getTmpVideoDir().getAbsolutePath() + "/img%03d.jpg";
						final String videoFileName = VIDEO_DATE_FORMAT.format(new Date()) + ".mp4";
						final File videoFile = new File(getExternalFaceVideoDir(face), videoFileName);

						final String command = String.format(
								"-framerate 1/%d -i %s -c:v libx264 -r %d -pix_fmt yuv420p %s",
								IMAGE_LENGTH_IN_SECONDS,
								photoFileNameTemplate,
								FRAMERATE,
								videoFile.getAbsolutePath());

						Timber.d("executing ffmpeg: " + command);

						// start command
						return Observable.create(new Observable.OnSubscribe<File>() {
							@Override
							public void call(final Subscriber<? super File> subscriber) {
								try {
									fFmpeg.execute(command, new FFmpegExecuteResponseHandler() {
										@Override
										public void onSuccess(String s) {
											Timber.d("video conversion success: " + s);
											if (subscriber.isUnsubscribed()) return;
											subscriber.onNext(videoFile);
											subscriber.onCompleted();
										}

										@Override
										public void onProgress(String s) {
											Timber.d("video conversion progress: " + s);
										}

										@Override
										public void onFailure(String s) {
											Timber.d("video conversion error: " + s);
											subscriber.onError(new IllegalStateException(s));
										}

										@Override
										public void onStart() {
											Timber.d("video conversion started");
										}

										@Override
										public void onFinish() {
											Timber.d("video conversion finished");
										}
									});
								} catch (FFmpegCommandAlreadyRunningException e) {
									Timber.e(e, "failed to start video conversion");
									if (subscriber.isUnsubscribed()) return;
									subscriber.onError(e);
								}
							}
						});
					}
				})
				.finallyDo(new Action0() {
					@Override
					public void call() {
						// cleanup
						ioUtils.delete(getTmpVideoDir());
					}
				});
	}


	public List<VideoInfo> getVideosForFace(final Face face) {
		List<VideoInfo> result = new ArrayList<>();
		File faceDir = getExternalFaceVideoDir(face);
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
					Integer.valueOf(matcher.group(2)) - 1,
					Integer.valueOf(matcher.group(3)),
					Integer.valueOf(matcher.group(4)),
					Integer.valueOf(matcher.group(5)),
					Integer.valueOf(matcher.group(6)));


			result.add(new VideoInfo(face, videoFile, calendar.getTime()));
		}

		return result;
	}


	private File getExternalFaceVideoDir(Face face) {
		File rootDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), context.getString(R.string.app_name));
		File faceDir = new File(rootDir, face.getPhotoFolderName());
		return ioUtils.assertDir(faceDir);
	}


	private File getTmpVideoDir() {
		return ioUtils.assertDir(new File(context.getFilesDir(), TMP_VIDEO_DIR));
	}

}
