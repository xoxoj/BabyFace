package org.faudroids.babyface.photo;


import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import timber.log.Timber;

public class PhotoManager {

	private static final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static final String ROOT_DIR_NAME = "BabyFace";

	@Inject
	PhotoManager() {
		// nothing to do for now
	}


	public File createImageFile() throws IOException {
		String timeStamp = PHOTO_DATE_FORMAT.format(new Date());
		String imageFileName = "BABY_" + timeStamp + "_";
		return File.createTempFile(imageFileName, ".jpg", getRootStorageDir());
	}


	public File getRootStorageDir() {
		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ROOT_DIR_NAME);
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdirs();
			if (!success) Timber.e("failed to create dir " + storageDir.getAbsolutePath());
		}
		return storageDir;
	}
}
