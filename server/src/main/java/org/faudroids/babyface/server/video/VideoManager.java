package org.faudroids.babyface.server.video;


import com.google.api.services.drive.Drive;
import com.google.common.base.Optional;

import org.faudroids.babyface.server.photo.PhotoDownloadManager;
import org.faudroids.babyface.server.utils.Log;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class VideoManager {

	private static final int
			IMAGE_LENGTH_IN_SECONDS = 2,
			FRAMERATE = 30;

	private static final int
			CONVERSION_THREAD_COUNT = 10;


	private final PhotoDownloadManager photoDownloadManager;
	private final ExecutorService threadPool;
	private final Map<String, VideoConversionStatus> videoConversionStatusMap;


	@Inject
	VideoManager(PhotoDownloadManager photoDownloadManager) {
		this.photoDownloadManager = photoDownloadManager;
		this.threadPool = Executors.newFixedThreadPool(CONVERSION_THREAD_COUNT);
		this.videoConversionStatusMap = new HashMap<>();
	}


	public VideoConversionStatus createVideo(Drive drive) {
		// setup target directory
		final String videoId = UUID.randomUUID().toString();
		final File targetDirectory = new File("data/" + videoId);
		if (!targetDirectory.mkdirs()) {
			Log.e("failed to create dir " + targetDirectory.getAbsolutePath());
			throw new IllegalStateException("error creating output dir");
		}

		// setup initial conversion status
		VideoConversionStatus status = new VideoConversionStatus(videoId);
		videoConversionStatusMap.put(videoId, status);

		// start async conversion
		threadPool.execute(new VideoCreationTask(drive, targetDirectory, status));

		return status;
	}


	public Optional<VideoConversionStatus> getStatusForVideo(String videoId) {
		if (!videoConversionStatusMap.containsKey(videoId)) return Optional.absent();
		return Optional.of(videoConversionStatusMap.get(videoId));
	}


	private class VideoCreationTask implements Runnable {

		private final Drive drive;
		private final File targetDirectory;
		private final VideoConversionStatus status;

		public VideoCreationTask(Drive drive, File targetDirectory, VideoConversionStatus status) {
			this.drive = drive;
			this.targetDirectory = targetDirectory;
			this.status = status;
		}

		@Override
		public void run() {
			try {
				// download photos
				List<File> photoFiles = photoDownloadManager.downloadAllPhotos(drive, targetDirectory);

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

				// execute video conversion
				String photoFileNameTemplate = targetDirectory.getAbsolutePath() + "/img%03d.jpg";
				File logFile = new File(targetDirectory, "logs");
				File videoFile = new File(targetDirectory, "out.mp4");
				boolean success = new FFmpegCommand(photoFileNameTemplate, IMAGE_LENGTH_IN_SECONDS, FRAMERATE, videoFile.getAbsolutePath()).execute(logFile);

				// update status
				status.setStatus(true, videoFile, success);

			} catch (Exception e) {
				Log.e("failed to create video", e);
				status.setStatus(true, null, false);
			}
		}

	}

}
