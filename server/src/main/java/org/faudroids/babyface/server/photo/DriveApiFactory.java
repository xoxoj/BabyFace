package org.faudroids.babyface.server.photo;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import javax.inject.Inject;

public class DriveApiFactory {

	@Inject
	DriveApiFactory() { }


	public Drive createDriveApi(String accessToken) {
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
		return new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
				.setApplicationName("BabyFace")
				.build();
	}
}
