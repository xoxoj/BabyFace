package org.faudroids.babyface.server.photo;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.faudroids.babyface.server.utils.Log;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

public class PhotoDownloadManager {

	private static final int DOWNLOAD_THREAD_COUNT = 10;

	@Inject
	PhotoDownloadManager() { }


	public List<java.io.File> downloadAllPhotos(final Drive drive, final java.io.File targetDirectory) throws Exception {
		// get files to download
		List<File> photoFiles = drive.files().list().execute().getItems();

		// setup threads + tasks
		final ExecutorService threadPool = Executors.newFixedThreadPool(DOWNLOAD_THREAD_COUNT);
		List<DownloadTask> tasks = new ArrayList<>();
		for (File photoFile : photoFiles) {
			tasks.add(new DownloadTask(drive, targetDirectory, photoFile));
		}

		// start download and block
		List<Future<java.io.File>> downloadedFileFutures = threadPool.invokeAll(tasks);
		List<java.io.File> downloadedFiles = new ArrayList<>();
		for (Future<java.io.File> future : downloadedFileFutures) {
			downloadedFiles.add(future.get());
		}
		return downloadedFiles;
	}


	private static class DownloadTask implements Callable<java.io.File> {

		private final Drive drive;
		private final java.io.File targetDirectory;
		private final File photoFile;

		public DownloadTask(Drive drive, java.io.File targetDirectory, File photoFile) {
			this.drive = drive;
			this.targetDirectory = targetDirectory;
			this.photoFile = photoFile;
		}

		@Override
		public java.io.File call() throws Exception {
			Log.i("downloading on thread " + Thread.currentThread().getId());
			java.io.File targetFile = new java.io.File(targetDirectory, photoFile.getOriginalFilename());
			OutputStream outputStream = new FileOutputStream(targetFile);
			Drive.Files.Get downloadRequest = drive.files().get(photoFile.getId());
			downloadRequest.executeMediaAndDownloadTo(outputStream);
			return targetFile;
		}
	}
}
