package org.faudroids.babyface.server.photo;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.faudroids.babyface.server.utils.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

public class PhotoDownloadManager {

	private static final int DOWNLOAD_THREAD_COUNT = 10;

	@Inject
	PhotoDownloadManager() { }


	/**
	 * Downloads all photos from Google Drive belonging to one user asynchronously.
	 * @return the list of downloaded photos
	 */
	public List<java.io.File> downloadAllPhotos(final Drive drive, final java.io.File targetDirectory) throws Exception {
		// get files to download
		List<File> photoFiles = drive.files().list().execute().getItems();
		Log.i("downloading " + photoFiles.size() + " photos");

		// setup threads + tasks
		final ExecutorService threadPool = Executors.newFixedThreadPool(DOWNLOAD_THREAD_COUNT);
		List<DownloadTask> tasks = new ArrayList<>();
		for (File photoFile : photoFiles) {
			tasks.add(new DownloadTask(drive, targetDirectory, photoFile));
		}

		// start download
		List<Future<java.io.File>> fileFutureList = threadPool.invokeAll(tasks);

		// map drive files to future
		Map<File, Future<java.io.File>> fileFutureMap = new HashMap<>();
		Iterator<Future<java.io.File>> futureIterator = fileFutureList.iterator();
		Iterator<File> fileIterator = photoFiles.iterator();
		while (futureIterator.hasNext()) {
			fileFutureMap.put(fileIterator.next(), futureIterator.next());
		}

		// wait for download completion and collect results
		List<java.io.File> downloadedFiles = new ArrayList<>();
		for (Map.Entry<File, Future<java.io.File>> entry : fileFutureMap.entrySet()) {
			try {
				downloadedFiles.add(entry.getValue().get());
			} catch (ExecutionException e) {
				Log.e("failed to download photo " + entry.getKey().getOriginalFilename());
			}
		}
		return downloadedFiles;
	}


	/**
	 * Downloads one photo from Google Drive synchronously.
	 * @return the downloaded file
	 */
	public java.io.File downloadPhoto(final Drive drive, final java.io.File targetDirectory, File photoFile) throws IOException {
		return new DownloadTask(drive, targetDirectory, photoFile).call();
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
		public java.io.File call() throws IOException {
			Log.i("downloading " + photoFile.getOriginalFilename());
			java.io.File targetFile = new java.io.File(targetDirectory, photoFile.getOriginalFilename());
			OutputStream outputStream = new FileOutputStream(targetFile);
			Drive.Files.Get downloadRequest = drive.files().get(photoFile.getId());
			downloadRequest.executeMediaAndDownloadTo(outputStream);
			return targetFile;
		}
	}
}
