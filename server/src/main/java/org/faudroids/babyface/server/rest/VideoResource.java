package org.faudroids.babyface.server.rest;


import com.google.api.services.drive.Drive;

import org.faudroids.babyface.server.photo.DriveApiFactory;
import org.faudroids.babyface.server.photo.PhotoDownloadManager;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
	@Path("/new")
	public void listPhotos(Token token) throws IOException {
		Drive drive = driveApiFactory.createDriveApi(token.getToken());
		for (String name : photoDownloadManager.getAllPhotoNames(drive)) {
			System.out.println(name);
		}
	}

}
