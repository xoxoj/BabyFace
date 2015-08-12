package org.faudroids.babyface.server.rest;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.Lists;

import org.faudroids.babyface.server.auth.User;
import org.faudroids.babyface.server.photo.DriveApiFactory;
import org.faudroids.babyface.server.photo.PhotoDownloadCommand;
import org.faudroids.babyface.server.photo.PhotoResizeManager;
import org.faudroids.babyface.server.utils.Log;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import io.dropwizard.auth.Auth;

@Path("/photos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PhotoResource {

	private final DriveApiFactory driveApiFactory;
	private final PhotoResizeManager photoResizeManager;

	@Inject
	PhotoResource(DriveApiFactory driveApiFactory, PhotoResizeManager photoResizeManager) {
		this.driveApiFactory = driveApiFactory;
		this.photoResizeManager = photoResizeManager;
	}

	@GET
	public List<File> getPhotos(@Auth User user) throws IOException {
		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		return drive.files().list().execute().getItems();
	}

	@GET
	@Produces("image/jpeg")
	@Path("/{photoId}")
	public Response getPhoto(@Auth User user, @PathParam("photoId") String photoId) throws IOException {
		Log.i("photoId is " + photoId);
		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		URI downloadUri = UriBuilder
				.fromUri(drive.files().get(photoId).execute().getDownloadUrl())
				.queryParam("access_token", user.getToken())
				.build();
		Log.i("download url " + downloadUri.toString());
		return Response.seeOther(downloadUri).build();
	}

	@DELETE
	public void deletePhotos(@Auth User user) throws IOException {
		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		for (File photoFile : drive.files().list().execute().getItems()) {
			Log.i("deleting file " + photoFile.getId());
			drive.files().delete(photoFile.getId()).execute();
		}
	}

	@POST
	@Path("/{photoId}/resize")
	public void resizePhoto(@Auth User user, @PathParam("photoId") String photoId) throws Exception {
		// download photo
		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		File photoDriveFile = drive.files().get(photoId).execute();
		List<java.io.File> photoFiles = new PhotoDownloadCommand.Builder(drive, new java.io.File("data/")).setPhotoFileToDownload(photoDriveFile).build().execute();

		// resize photo
		photoResizeManager.resizeAndCropPhotos(Lists.newArrayList(photoFiles.get(0)));
	}

}
