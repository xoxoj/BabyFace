package org.faudroids.babyface.photo;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.faudroids.babyface.google.GoogleDriveManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

public class PhotoManager {

	private static final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static final String ROOT_DIR_NAME = "BabyFace";

	private final Context context;
	private final GoogleDriveManager googleDriveManager;


	@Inject
	PhotoManager(Context context, GoogleDriveManager googleDriveManager) {
		this.context = context;
		this.googleDriveManager = googleDriveManager;
	}


	public PhotoCreationResult createPhotoIntent(String faceId) throws IOException {
		String timeStamp = PHOTO_DATE_FORMAT.format(new Date());
		String imageFileName = "face_" + faceId + "_" + timeStamp + ".jpg";

		File tmpImageFile = new File(getRootStorageDir(), imageFileName);
		Timber.d("storing image as " + tmpImageFile.getAbsolutePath());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpImageFile));

		return new PhotoCreationResult(intent, faceId, tmpImageFile);
	}


	public File onPhotoResult(PhotoCreationResult photoCreationResult) throws IOException {
		// copy image to internal storage
		File faceDir = new File(context.getFilesDir(), photoCreationResult.faceId);
		if (!faceDir.exists() && !faceDir.mkdirs()) {
			Timber.e("failed to create dir " + faceDir.getAbsolutePath());
		}

		File internalImageFile = new File(faceDir, photoCreationResult.tmpImageFile.getName());

		InputStream inStream = new FileInputStream(photoCreationResult.tmpImageFile);
		OutputStream outStream = new FileOutputStream(internalImageFile);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		inStream.close();
		outStream.close();

		if (!photoCreationResult.tmpImageFile.delete()) Timber.w("failed to delete file " + photoCreationResult.tmpImageFile.getAbsolutePath());

		return internalImageFile;
	}


	public Observable<Void> uploadPhoto(File photoFile) throws IOException {
		return googleDriveManager.createNewFile(new FileInputStream(photoFile), photoFile.getName(), "image/jpeg", false);
	}


	private File getRootStorageDir() {
		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ROOT_DIR_NAME);
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdirs();
			if (!success) Timber.e("failed to create dir " + storageDir.getAbsolutePath());
		}
		return storageDir;
	}


	public static class PhotoCreationResult {

		private final Intent photoCaptureIntent;
		private final String faceId;
		private final File tmpImageFile;

		private PhotoCreationResult(Intent photoCaptureIntent, String faceId, File tmpImageFile) {
			this.photoCaptureIntent = photoCaptureIntent;
			this.faceId = faceId;
			this.tmpImageFile = tmpImageFile;
		}

		public Intent getPhotoCaptureIntent() {
			return photoCaptureIntent;
		}

	}
}
