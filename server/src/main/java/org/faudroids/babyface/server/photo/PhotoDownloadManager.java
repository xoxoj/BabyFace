package org.faudroids.babyface.server.photo;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PhotoDownloadManager {

	@Inject
	PhotoDownloadManager() { }


	public List<String> getAllPhotoNames(Drive drive) throws IOException {
		List<String> photoNames = new ArrayList<>();
		for (File photoFile : drive.files().list().execute().getItems()) {
			photoNames.add(photoFile.getOriginalFilename());
		}
		return photoNames;
	}
}
