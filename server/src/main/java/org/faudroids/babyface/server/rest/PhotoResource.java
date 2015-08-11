package org.faudroids.babyface.server.rest;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.faudroids.babyface.server.auth.User;
import org.faudroids.babyface.server.photo.DriveApiFactory;
import org.faudroids.babyface.server.utils.Log;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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

	@Inject
	PhotoResource(DriveApiFactory driveApiFactory) {
		this.driveApiFactory = driveApiFactory;
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
			drive.files().delete(photoFile.getId()).execute();
		}
	}

}
