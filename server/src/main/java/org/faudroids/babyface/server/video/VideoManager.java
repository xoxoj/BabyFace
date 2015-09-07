package org.faudroids.babyface.server.video;


import com.google.api.services.drive.Drive;
import com.google.common.base.Optional;

import org.faudroids.babyface.server.photo.PhotoDownloadCommand;
import org.faudroids.babyface.server.photo.PhotoResizeManager;
import org.faudroids.babyface.server.utils.Log;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class VideoManager {

	private static final int
			IMAGE_LENGTH_IN_SECONDS = 1,
			FRAMERATE = 25;

	private static final int
			CONVERSION_THREAD_COUNT = 10;


	private final PhotoResizeManager photoResizeManager;
	private final ExecutorService threadPool;
	private final Map<String, VideoConversionStatus> videoConversionStatusMap;


	@Inject
	VideoManager(PhotoResizeManager photoResizeManager) {
		this.photoResizeManager = photoResizeManager;
		this.threadPool = Executors.newFixedThreadPool(CONVERSION_THREAD_COUNT);
		this.videoConversionStatusMap = new HashMap<>();
	}


	public VideoConversionStatus createVideo(Drive drive, String faceId) {
		// setup target directory
		final String videoId = UUID.randomUUID().toString();
		final File targetDirectory = new File("data/" + faceId + "/" + videoId);
		if (!targetDirectory.mkdirs()) {
			Log.e("failed to create dir " + targetDirectory.getAbsolutePath());
			throw new IllegalStateException("error creating output dir");
		}

		// setup initial conversion status
		VideoConversionStatus status = new VideoConversionStatus(videoId);
		videoConversionStatusMap.put(videoId, status);

		// start async conversion
		threadPool.execute(new VideoCreationTask(drive, faceId, targetDirectory, status));

		return status;
	}


	public Optional<VideoConversionStatus> getStatusForVideo(String videoId) {
		if (!videoConversionStatusMap.containsKey(videoId)) return Optional.absent();
		return Optional.of(videoConversionStatusMap.get(videoId));
	}


	private class VideoCreationTask implements Runnable {

		private final Drive drive;
		private final String faceId;
		private final File targetDirectory;
		private final VideoConversionStatus status;
		private final ScheduledExecutorService executorService;

		public VideoCreationTask(Drive drive, String faceId, File targetDirectory, VideoConversionStatus status) {
			this.drive = drive;
			this.faceId = faceId;
			this.targetDirectory = targetDirectory;
			this.status = status;
			this.executorService = Executors.newSingleThreadScheduledExecutor();
		}

		@Override
		public void run() {
			try {
				// create download command
				final PhotoDownloadCommand downloadCommand = new PhotoDownloadCommand.Builder(drive, faceId, targetDirectory).build();

				// start reading download progress
				ScheduledFuture<?> downloadProgressFuture = executorService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						synchronized (status) {
							status.setDownloadProgress(downloadCommand.getProgress());
						}
					}
				}, 0, 1, TimeUnit.SECONDS);

				// start download
				final List<File> photoFiles = downloadCommand.execute();
				downloadProgressFuture.cancel(true);
				status.setDownloadProgress(1);

				// rename photos to img0000.jpg
				Collections.sort(photoFiles);
				int idx = 0;
				for (File oldFile : photoFiles) {
					++idx;
					File newFile = new File(oldFile.getParent(), "img" + String.format("%03d", idx) + ".jpg");
					boolean success = oldFile.renameTo(newFile);
					if (!success)
						Log.e("failed to rename file " + oldFile.getName() + " to " + newFile.getName());
				}

				// create conversion command
				String photoFileNameTemplate = "img%03d.jpg";
				String videoFileName = "out.mp4";
				final FFmpegCommand conversionCommand = new FFmpegCommand.Builder(targetDirectory, photoFileNameTemplate)
						.setOutputFileName(videoFileName)
						.setFramerate(FRAMERATE)
						.setImageDuration(IMAGE_LENGTH_IN_SECONDS)
						.build();

				// start reading conversion progress
				ScheduledFuture<?> conversionProgressFuture = executorService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						float conversionProgress = conversionCommand.getProgress(photoFiles.size());
						synchronized (status) {
							status.setConversionProgress(conversionProgress);
						}
					}
				}, 0, 1, TimeUnit.SECONDS);

				// run conversion
				boolean success = conversionCommand.execute();
				conversionProgressFuture.cancel(true);
				status.setConversionProgress(1);

				// cleanup + update status
				synchronized (status) {
					status.setStatus(true, new File(targetDirectory, videoFileName), success);
				}
				executorService.shutdown();

			} catch (Exception e) {
				Log.e("failed to create video", e);
				status.setStatus(true, null, false);
			}
		}



	}

}
