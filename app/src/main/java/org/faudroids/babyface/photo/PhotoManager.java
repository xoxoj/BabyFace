package org.faudroids.babyface.photo;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.android.gms.drive.DriveId;

import org.faudroids.babyface.google.GoogleDriveManager;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;


/**
 * Responsible for taking, storing and uploading photos.
 *
 * The general workflow is as follows:
 *
 * 1. Take photo (regular camera via Intent) and store in public directory (accessible by all apps!).
 * 2. Copy to internal storage under /faceId/uploads (indicates which photos have to be uploaded)
 * 3. Upload to google drive + on success move photo to /faceId locally
 *
 */
public class PhotoManager {

	private static final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private static final String
			PUBLIC_ROOT_DIR = "BabyFace",
			INTERNAL_UPLOADS_DIR = "uploads";

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


	public void onPhotoResult(PhotoCreationResult photoCreationResult) throws IOException {
		// copy image to internal storage
		File uploadsDir = getUploadsDir(photoCreationResult.faceId);
		File internalImageFile = new File(uploadsDir, photoCreationResult.tmpImageFile.getName());
		copyStream(new FileInputStream(photoCreationResult.tmpImageFile), new FileOutputStream(internalImageFile));

		// delete public file
		if (!photoCreationResult.tmpImageFile.delete()) Timber.w("failed to delete file " + photoCreationResult.tmpImageFile.getAbsolutePath());
	}


	/**
	 * Uploads all photos to google drive that have not been uploaded
	 */
	public Observable<Void> uploadAllPhotos() {
		// get all photos that have not been uploaded
		List<File> filesToUpload = new ArrayList<>();
		for (File faceDir : context.getFilesDir().listFiles()) {
			if (!faceDir.isDirectory()) continue;
			File uploadsDir = new File(faceDir, INTERNAL_UPLOADS_DIR);
			if (!uploadsDir.exists()) continue;
			Collections.addAll(filesToUpload, uploadsDir.listFiles());
		}
		Timber.d("uploading " + filesToUpload.size() + " photos");

		// upload + move photos internally
		return Observable.from(filesToUpload)
				// find + create face folder if necessary
				.flatMap(new Func1<File, Observable<PhotoUploadContainer>>() {
					@Override
					public Observable<PhotoUploadContainer> call(final File photoFile) {
						final String faceId = photoFile.getParentFile().getParentFile().getName();
						return googleDriveManager.query(faceId, false)
								.flatMap(new Func1<Optional<DriveId>, Observable<PhotoUploadContainer>>() {
									@Override
									public Observable<PhotoUploadContainer> call(Optional<DriveId> driveIdOptional) {
										if (driveIdOptional.isPresent()) return Observable.just(new PhotoUploadContainer(driveIdOptional.get(),  photoFile));

										// create folder
										return googleDriveManager
												.createNewFolder(faceId)
												.map(new Func1<DriveId, PhotoUploadContainer>() {
													@Override
													public PhotoUploadContainer call(DriveId driveId) {
														return new PhotoUploadContainer(driveId, photoFile);
													}
												});
									}
								});
					}
				})
				.flatMap(new Func1<PhotoUploadContainer, Observable<Void>>() {
					@Override
					public Observable<Void> call(final PhotoUploadContainer container) {
						Timber.d("uploading " + container.photoFile.getAbsolutePath());
						try {
							return googleDriveManager
									.createNewFile(Optional.of(container.driveId), new FileInputStream(container.photoFile), container.photoFile.getName(), "image/jpeg", false)
									.flatMap(new Func1<Void, Observable<Void>>() {
										@Override
										public Observable<Void> call(Void nothing) {
											// move photo to regular face dir (and remove from uploads dir)
											try {
												File newPhotoFile = new File(container.photoFile.getParentFile().getParentFile(), container.photoFile.getName());
												copyStream(new FileInputStream(container.photoFile), new FileOutputStream(newPhotoFile));
												if (!container.photoFile.delete())
													Timber.d("failed to remove " + container.photoFile.getAbsolutePath());
											} catch (IOException e) {
												return Observable.error(e);
											}
											return Observable.just(null);
										}
									});
						} catch (FileNotFoundException e) {
							Timber.e(e, "failed to upload file");
							return Observable.error(e);
						}
					}
				})
				.toList()
				.map(new Func1<List<Void>, Void>() {
					@Override
					public Void call(List<Void> voids) {
						return null;
					}
				});
	}


	public Optional<File> getRecentPhoto(String faceId) {
		File uploadsDir = getUploadsDir(faceId);
		List<File> files = Lists.newArrayList(uploadsDir.listFiles());
		File faceDir = getFaceDir(faceId);
		for (File photoFile : faceDir.listFiles()) {
			if (!photoFile.isDirectory()) files.add(photoFile);
		}

		if (files.isEmpty()) return Optional.absent();

		// sort and get last (newest) photo file
		Collections.sort(files);
		return Optional.of(files.get(files.size() - 1));
	}


	/**
	 * Returns the internal (!) root directory for one face.
	 */
	private File getFaceDir(String faceId) {
		File faceDir = new File(context.getFilesDir(), faceId);
		if (!faceDir.exists() && !faceDir.mkdirs()) {
			Timber.e("failed to create dir " + faceDir.getAbsolutePath());
		}
		return faceDir;
	}


	/**
	 * Returns the internal (!) uploads directory for one face.
	 */
	private File getUploadsDir(String faceId) {
		File uploadsDir = new File(getFaceDir(faceId), INTERNAL_UPLOADS_DIR);
		if (!uploadsDir.exists() && !uploadsDir.mkdirs()) {
			Timber.e("failed to create dir " + uploadsDir.getAbsolutePath());
		}
		return uploadsDir;
	}


	/**
	 * Returns the public (!) root directory for this app.
	 */
	private File getRootStorageDir() {
		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PUBLIC_ROOT_DIR);
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdirs();
			if (!success) Timber.e("failed to create dir " + storageDir.getAbsolutePath());
		}
		return storageDir;
	}


	private void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		} finally {
			inStream.close();
			outStream.close();
		}
	}


	/**
	 * Helper class for grouping values during rx chains.
	 */
	private static class PhotoUploadContainer {
		private final DriveId driveId;
		private final File photoFile;
		public PhotoUploadContainer(DriveId driveId, File photoFile) {
			this.driveId = driveId;
			this.photoFile = photoFile;
		}
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
