package org.faudroids.babyface.server.photo;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PhotoDownloadManager {

	@Inject
	PhotoDownloadManager() { }


	public List<java.io.File> downloadAllPhotos(Drive drive, java.io.File targetDirectory) throws IOException {
		List<java.io.File> downloadedFiles = new ArrayList<>();
		for (File photoFile : drive.files().list().execute().getItems()) {
			if (photoFile.getExplicitlyTrashed()) continue;
			downloadedFiles.add(downloadPhoto(drive, targetDirectory, photoFile));
		}
		return downloadedFiles;
	}


	private java.io.File downloadPhoto(Drive drive, java.io.File targetDirectory, File photoFile) throws IOException {
		// drive.files().delete(photoFile.getId());
		java.io.File targetFile = new java.io.File(targetDirectory, photoFile.getOriginalFilename());
		OutputStream outputStream = new FileOutputStream(targetFile);
		Drive.Files.Get downloadRequest = drive.files().get(photoFile.getId());
		downloadRequest.executeMediaAndDownloadTo(outputStream);
		return targetFile;
	}
}
