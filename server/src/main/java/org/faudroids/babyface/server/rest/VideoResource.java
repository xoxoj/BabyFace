package org.faudroids.babyface.server.rest;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.api.services.drive.Drive;
import com.google.common.base.Optional;

import org.faudroids.babyface.server.auth.User;
import org.faudroids.babyface.server.photo.DriveApiFactory;
import org.faudroids.babyface.server.video.VideoConversionStatus;
import org.faudroids.babyface.server.video.VideoManager;

import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.auth.Auth;

@Path("/video")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VideoResource {

	private final DriveApiFactory driveApiFactory;
	private final VideoManager videoManager;

	@Inject
	VideoResource(DriveApiFactory driveApiFactory, VideoManager videoManager) {
		this.driveApiFactory = driveApiFactory;
		this.videoManager = videoManager;
	}

	@POST
	public Status listPhotos(@Auth User user) throws Exception {
		Drive drive = driveApiFactory.createDriveApi(user.getToken());
		VideoConversionStatus status = videoManager.createVideo(drive);
		return new Status(status);
	}

	@GET
	@Path("/{videoId}/status")
	public Status getStatus(@Auth User user, @PathParam("videoId") String videoId) {
		// TODO check user access rights
		VideoConversionStatus status = assertValidVideo(videoId);
		return new Status(status);
	}

	@GET
	@Path("/{videoId}/data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getVideoData(@Auth User user, @PathParam("videoId") String videoId) {
		// TODO check user access rights

		// check params
		VideoConversionStatus status = assertValidVideo(videoId);
		if (!status.isComplete()) throw RestUtils.createJsonFormattedException("video not yet converted", 404);
		if (!status.isConversionSuccessful()) throw RestUtils.createJsonFormattedException("video conversion error", 404);

		// return video
		File videoFile = status.getVideoFile();
		return Response
				.ok(videoFile, MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"out.mp4\"")
				.build();
	}


	private VideoConversionStatus assertValidVideo(String videoId) {
		Optional<VideoConversionStatus> statusOptional = videoManager.getStatusForVideo(videoId);
		if (!statusOptional.isPresent()) throw RestUtils.createNotFoundException();
		return statusOptional.get();
	}



	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Status {

		private final String videoId;
		private final boolean isComplete;
		private final Boolean isConversionSuccessful;

		public Status(VideoConversionStatus status) {
			this.videoId = status.getVideoId();
			this.isComplete = status.isComplete();
			if (!isComplete) this.isConversionSuccessful = null;
			else this.isConversionSuccessful = status.isConversionSuccessful();
		}

		public String getVideoId() {
			return videoId;
		}

		public boolean isComplete() {
			return isComplete;
		}

		public Boolean isConversionSuccessful() {
			return isConversionSuccessful;
		}

	}

}
