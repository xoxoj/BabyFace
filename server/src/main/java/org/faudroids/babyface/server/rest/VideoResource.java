package org.faudroids.babyface.server.rest;


import com.google.api.services.drive.Drive;

import org.faudroids.babyface.server.auth.User;
import org.faudroids.babyface.server.photo.DriveApiFactory;
import org.faudroids.babyface.server.utils.Log;
import org.faudroids.babyface.server.video.VideoManager;

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
	private final VideoManager videoManager;

	@Inject
	VideoResource(DriveApiFactory driveApiFactory, VideoManager videoManager) {
		this.driveApiFactory = driveApiFactory;
		this.videoManager = videoManager;
	}

	@POST
	public void listPhotos(@Auth User user) throws Exception {
		File targetDirectory = new File("data/" + UUID.randomUUID().toString());
		if (!targetDirectory.mkdirs()) {
			Log.e("failed to create dir " + targetDirectory.getAbsolutePath());
			throw new IOException("error creating output dir");
		}

		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		videoManager.createVideo(drive, targetDirectory);
	}

}
