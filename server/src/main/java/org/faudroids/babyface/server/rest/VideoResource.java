package org.faudroids.babyface.server.rest;


import com.google.api.services.drive.Drive;

import org.faudroids.babyface.server.auth.User;
import org.faudroids.babyface.server.photo.DriveApiFactory;
import org.faudroids.babyface.server.photo.PhotoDownloadManager;
import org.faudroids.babyface.server.utils.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.dropwizard.auth.Auth;

@Path("/video")
public class VideoResource {

	private final DriveApiFactory driveApiFactory;
	private final PhotoDownloadManager photoDownloadManager;

	@Inject
	VideoResource(DriveApiFactory driveApiFactory, PhotoDownloadManager photoDownloadManager) {
		this.driveApiFactory = driveApiFactory;
		this.photoDownloadManager = photoDownloadManager;
	}

	@POST
	public void listPhotos(@Auth User user) throws IOException {
		File targetDirectory = new File(UUID.randomUUID().toString());
		if (!targetDirectory.mkdir()) {
			Log.e("failed to create dir " + targetDirectory.getAbsolutePath());
			throw new IOException("error creating output dir");
		}

		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		photoDownloadManager.downloadAllPhotos(drive, targetDirectory);
	}

}
