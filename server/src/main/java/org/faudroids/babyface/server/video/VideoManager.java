package org.faudroids.babyface.server.video;


import com.google.api.services.drive.Drive;

import org.faudroids.babyface.server.photo.PhotoDownloadManager;
import org.faudroids.babyface.server.utils.Log;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class VideoManager {

	private static final int
			IMAGE_LENGTH_IN_SECONDS = 2,
			FRAMERATE = 30;

	private final PhotoDownloadManager photoDownloadManager;

	@Inject
	VideoManager(PhotoDownloadManager photoDownloadManager) {
		this.photoDownloadManager = photoDownloadManager;
	}


	public File createVideo(Drive drive, File targetDirectory) throws Exception {
		// download photos
		List<File> photoFiles = photoDownloadManager.downloadAllPhotos(drive, targetDirectory);

		// rename photos to img0000.jpg
		Collections.sort(photoFiles);
		int idx = 0;
		for (File oldFile : photoFiles) {
			++idx;
			File newFile = new File(oldFile.getParent(), "img" + String.format("%03d", idx) + ".jpg");
			boolean success = oldFile.renameTo(newFile);
			if (!success) Log.e("failed to rename file " + oldFile.getName() + " to " + newFile.getName());
		}

		// execute video conversion
		String photoFileNameTemplate = targetDirectory.getAbsolutePath() + "/img%03d.jpg";
		File videoFile = new File(targetDirectory, "out.mp4");
		File logFile = new File(targetDirectory, "logs");
		new FFmpegCommand(photoFileNameTemplate, IMAGE_LENGTH_IN_SECONDS, FRAMERATE, videoFile.getAbsolutePath()).execute(logFile);
		return videoFile;
	}

}
