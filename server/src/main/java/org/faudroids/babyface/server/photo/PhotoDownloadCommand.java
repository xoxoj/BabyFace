package org.faudroids.babyface.server.photo;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

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
import java.util.concurrent.atomic.AtomicInteger;

public class PhotoDownloadCommand {

	private static final int DOWNLOAD_THREAD_COUNT = 10;

	private final Drive drive;
	private final java.io.File targetDirectory;
	private final List<File> photoFilesToDownload = new ArrayList<>();

	final AtomicInteger downloadedPhotos = new AtomicInteger(0);
	final ExecutorService threadPool = Executors.newFixedThreadPool(DOWNLOAD_THREAD_COUNT);

	private PhotoDownloadCommand(Drive drive, java.io.File targetDirectory, Optional<File> photoFile) {
		this.drive = drive;
		this.targetDirectory = targetDirectory;
		if (photoFile.isPresent()) photoFilesToDownload.add(photoFile.get());
	}


	/**
	 * Downloads one or all photos from Google Drive belonging to one user synchronously.
	 * @return the list of downloaded photos
	 */
	public List<java.io.File> execute() throws Exception {
		// check if single file should be downloaded
		if (!photoFilesToDownload.isEmpty()) {
			return Lists.newArrayList(new DownloadTask(drive, targetDirectory, photoFilesToDownload.get(0), downloadedPhotos).call());
		}

		// get all files to download
		photoFilesToDownload.addAll(drive.files().list().setMaxResults(100).execute().getItems());
		Log.i("downloading " + photoFilesToDownload.size() + " photos");

		// setup threads + tasks
		List<DownloadTask> tasks = new ArrayList<>();
		for (File photoFile : photoFilesToDownload) {
			tasks.add(new DownloadTask(drive, targetDirectory, photoFile, downloadedPhotos));
		}

		// start download
		List<Future<java.io.File>> fileFutureList = threadPool.invokeAll(tasks);

		// map drive files to future
		Map<File, Future<java.io.File>> fileFutureMap = new HashMap<>();
		Iterator<Future<java.io.File>> futureIterator = fileFutureList.iterator();
		Iterator<File> fileIterator = photoFilesToDownload.iterator();
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
	 * @return the progress as a percentage (0% - 100%)
	 */
	public float getProgress() {
		if (photoFilesToDownload.isEmpty()) return 0;
		return downloadedPhotos.get() / ((float) photoFilesToDownload.size());
	}


	public static class Builder {

		private final Drive drive;
		private final java.io.File targetDirectory;
		private Optional<File> photoFileToDownload = Optional.absent();

		public Builder(Drive drive, java.io.File targetDirectory) {
			this.drive = drive;
			this.targetDirectory = targetDirectory;
		}

		public Builder setPhotoFileToDownload(File photoFileToDownload) {
			this.photoFileToDownload = Optional.fromNullable(photoFileToDownload);
			return this;
		}

		public PhotoDownloadCommand build() {
			return new PhotoDownloadCommand(drive, targetDirectory, photoFileToDownload);
		}

	}

	private static class DownloadTask implements Callable<java.io.File> {

		private final Drive drive;
		private final java.io.File targetDirectory;
		private final File photoFile;
		private final AtomicInteger downloadedPhotos;

		public DownloadTask(Drive drive, java.io.File targetDirectory, File photoFile, AtomicInteger downloadedPhotos) {
			this.drive = drive;
			this.targetDirectory = targetDirectory;
			this.photoFile = photoFile;
			this.downloadedPhotos = downloadedPhotos;
		}

		@Override
		public java.io.File call() throws IOException {
			Log.i("downloading " + photoFile.getOriginalFilename());
			java.io.File targetFile = new java.io.File(targetDirectory, photoFile.getOriginalFilename());
			OutputStream outputStream = new FileOutputStream(targetFile);
			Drive.Files.Get downloadRequest = drive.files().get(photoFile.getId());
			downloadRequest.executeMediaAndDownloadTo(outputStream);
			downloadedPhotos.incrementAndGet();
			return targetFile;
		}
	}
}
