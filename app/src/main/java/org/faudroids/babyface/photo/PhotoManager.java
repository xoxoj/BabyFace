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

	private File tmpImageFile;

	@Inject
	PhotoManager(Context context, GoogleDriveManager googleDriveManager) {
		this.context = context;
		this.googleDriveManager = googleDriveManager;
	}


	public Intent createPhotoIntent(String faceId) throws IOException {
		String timeStamp = PHOTO_DATE_FORMAT.format(new Date());
		String imageFileName = "face_" + faceId + "_" + timeStamp + ".jpg";
		tmpImageFile = new File(getRootStorageDir(), imageFileName);
		Timber.d("storing image as " + tmpImageFile.getAbsolutePath());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpImageFile));
		return intent;
	}


	public File onPhotoResult(Intent data) throws IOException {
		if (tmpImageFile == null) throw new IllegalStateException("tmpImageFile cannot be null");

		// copy image to internal storage
		File internalImageFile = new File(context.getFilesDir(), tmpImageFile.getName());

		InputStream inStream = new FileInputStream(tmpImageFile);
		OutputStream outStream = new FileOutputStream(internalImageFile);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		inStream.close();
		outStream.close();

		if (!tmpImageFile.delete()) Timber.w("failed to delete file " + tmpImageFile.getAbsolutePath());

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
}
