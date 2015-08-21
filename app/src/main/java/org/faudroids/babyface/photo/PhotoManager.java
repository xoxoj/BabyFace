package org.faudroids.babyface.photo;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.google.android.gms.drive.DriveId;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.GoogleDriveManager;
import org.faudroids.babyface.utils.IOUtils;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * 1. Take photo and store in temp internal file
 * 2. Copy temp photo file to /faceId/uploads (indicates which photos have to be uploaded) if user accepts photo
 * 3. Upload to google drive + on success move photo to /faceId locally
 *
 */
public class PhotoManager {

	private static final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private static final String INTERNAL_UPLOADS_DIR = "uploads";

	private final Context context;
	private final GoogleDriveManager googleDriveManager;
	private final IOUtils ioUtils;


	@Inject
	PhotoManager(Context context, GoogleDriveManager googleDriveManager, IOUtils ioUtils) {
		this.context = context;
		this.googleDriveManager = googleDriveManager;
		this.ioUtils = ioUtils;
	}


	public PhotoCreationResult createPhotoIntent(String faceId) throws IOException {
		File tmpPhotoFile = getTmpPhotoFile();
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpPhotoFile));
		return new PhotoCreationResult(takePictureIntent, faceId, tmpPhotoFile);
	}


	public void onPhotoResult(PhotoCreationResult photoCreationResult) throws IOException {
		// copy image to internal storage
		final String faceId = photoCreationResult.faceId;
		final String photoFileName = faceId + "_" + PHOTO_DATE_FORMAT.format(new Date()) + ".jpg";

		File internalImageFile = new File(getUploadsDir(faceId), photoFileName);
		File tmpPhotoFile = photoCreationResult.getTmpPhotoFile();
		ioUtils.copyStream(new FileInputStream(tmpPhotoFile), new FileOutputStream(internalImageFile));

		// delete public file
		if (!tmpPhotoFile.delete()) Timber.w("failed to delete file " + tmpPhotoFile.getAbsolutePath());
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
												ioUtils.copyStream(new FileInputStream(container.photoFile), new FileOutputStream(newPhotoFile));
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
	 * Starts uploading photos to Google drive if WiFi is connected
	 */
	public void requestPhotoUpload() {
		context.startService(new Intent(context, PhotoUploadService.class));
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


	private File getTmpPhotoFile() {
		File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
		if (!photoDir.exists()) {
			if (photoDir.mkdirs()) Timber.e("failed to make dir " + photoDir.getAbsolutePath());
		}
		return new File(photoDir, "tmp.jpg");
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


	public static class PhotoCreationResult implements Parcelable {

		private final Intent photoCaptureIntent;
		private final String faceId;
		private final File tmpPhotoFile;

		private PhotoCreationResult(Intent photoCaptureIntent, String faceId, File tmpPhotoFile) {
			this.photoCaptureIntent = photoCaptureIntent;
			this.faceId = faceId;
			this.tmpPhotoFile = tmpPhotoFile;
		}

		public Intent getPhotoCaptureIntent() {
			return photoCaptureIntent;
		}

		public String getFaceId() {
			return faceId;
		}

		private File getTmpPhotoFile() {
			return tmpPhotoFile;
		}

		protected PhotoCreationResult(Parcel in) {
			photoCaptureIntent = (Intent) in.readValue(Intent.class.getClassLoader());
			faceId = in.readString();
			tmpPhotoFile = (File) in.readSerializable();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeValue(photoCaptureIntent);
			dest.writeString(faceId);
			dest.writeSerializable(tmpPhotoFile);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<PhotoCreationResult> CREATOR = new Parcelable.Creator<PhotoCreationResult>() {
			@Override
			public PhotoCreationResult createFromParcel(Parcel in) {
				return new PhotoCreationResult(in);
			}

			@Override
			public PhotoCreationResult[] newArray(int size) {
				return new PhotoCreationResult[size];
			}
		};
	}
}
