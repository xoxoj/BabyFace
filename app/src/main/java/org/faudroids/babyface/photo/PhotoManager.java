package org.faudroids.babyface.photo;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.google.GoogleDriveManager;
import org.faudroids.babyface.utils.IOUtils;
import org.faudroids.babyface.utils.MultiValueMap;
import org.faudroids.babyface.utils.Pref;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;


/**
 * Responsible for taking, storing and uploading photos.
 *
 * The general workflow is as follows:
 *
 * 1. Take photo and store in temp internal file
 * 2. Copy temp photo file to /faceName/uploads (indicates which photos have to be uploaded) if user accepts photo
 * 3. Upload to google drive + on success move photo to /faceName locally
 *
 */
public class PhotoManager {

	private static final String PREFS_NAME = "org.faudroids.babyface.photo.PhotoManager";

	private static final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final String PHOTO_FILE_NAME_REGEX = "(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)_(\\d\\d)-(\\d\\d)-(\\d\\d)\\.jpg";

	private static final float PHOTO_SCALE = 1.5f; // how much larger the raw photos should be compared to the processed output

	private static final String
			INTERNAL_UPLOADS_DIR = "uploads",
			INTERNAL_DELETE_DIR = "deleted";

	private final Context context;
	private final PhotoProcessor photoProcessor;
	private final GoogleDriveManager googleDriveManager;
	private final GoogleApiClientManager googleApiClientManager;
	private final IOUtils ioUtils;
	private final Pref<Boolean> forcePhotoLandscapePref;


	@Inject
	PhotoManager(Context context, PhotoProcessor photoProcessor, GoogleDriveManager googleDriveManager, GoogleApiClientManager googleApiClientManager, IOUtils ioUtils) {
		this.context = context;
		this.photoProcessor = photoProcessor;
		this.googleDriveManager = googleDriveManager;
		this.googleApiClientManager = googleApiClientManager;
		this.ioUtils = ioUtils;
		this.forcePhotoLandscapePref = Pref.newBooleanPref(context, PREFS_NAME, "force_photo_landscape", true);
	}


	public boolean getForcePhotoLandscapeMode() {
		return forcePhotoLandscapePref.get();
	}


	public void setForcePhotoLandscapeMode(boolean force) {
		forcePhotoLandscapePref.set(force);
	}


	public PhotoCreationResult createPhotoIntent(String faceName) throws IOException {
		File tmpPhotoFile = getTmpPhotoFile();
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpPhotoFile));
		return new PhotoCreationResult(takePictureIntent, faceName, tmpPhotoFile);
	}


	public void onPhotoResult(PhotoCreationResult photoCreationResult) throws IOException {
		final String faceName = photoCreationResult.faceName;
		final File tmpPhotoFile = photoCreationResult.getTmpPhotoFile();
		final Bitmap originalImage = assertCorrectPhotoRotation(photoCreationResult);

		// scale image down to reduce memory usage
		final float xScale = originalImage.getWidth() / ((float) PhotoProcessor.OUTPUT_WIDTH);
		final float yScale = originalImage.getHeight() / ((float) PhotoProcessor.OUTPUT_HEIGHT);
		final float scale = Math.min(xScale, yScale);
		Bitmap scaledImage;
		if (scale > PHOTO_SCALE) {
			final float scaledWidth = originalImage.getWidth() * PHOTO_SCALE / scale;
			final float scaledHeight = originalImage.getHeight() * PHOTO_SCALE / scale;
			scaledImage = Bitmap.createScaledBitmap(originalImage, (int) scaledWidth, (int) scaledHeight, false);
		} else {
			scaledImage = originalImage;
		}


		// copy image to internal storage
		final String photoFileName = PHOTO_DATE_FORMAT.format(new Date()) + ".jpg";
		File internalImageFile = new File(getFaceUploadsDir(faceName), photoFileName);
		scaledImage.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(internalImageFile));

		// delete public file
		if (!tmpPhotoFile.delete()) Timber.w("failed to delete file " + tmpPhotoFile.getAbsolutePath());
	}


	public Observable<Void> addPhotoDir(Face face) {
		return googleDriveManager.createNewFolder(face.getPhotoFolderName())
				.map(new Func1<DriveId, Void>() {
					@Override
					public Void call(DriveId driveId) {
						return null;
					}
				});
	}


	public Observable<Void> deletePhotoDir(Face face) {
		return googleDriveManager.deleteFolder(face.getPhotoFolderName());
	}


	public void deletePhoto(PhotoInfo photo) {
		File markedFile = new File(getFaceDeletedDir(photo.getFace()), photo.getPhotoFile().getName());
		try {
			ioUtils.copyStream(new FileInputStream(photo.getPhotoFile()), new FileOutputStream(markedFile));
			ioUtils.delete(photo.getPhotoFile());
		} catch (IOException e) {
			Timber.e(e, "failed to delete photo" + photo.getPhotoFile().getAbsolutePath());
		}
	}


	/**
	 * Syncs all photos to Google Drive (+ deletes those which have been marked as deleted).
	 */
	public Observable<Void> syncToGoogleDrive() {
		final MultiValueMap<String, File> filesToUpload = new MultiValueMap<>(); // face name --> photo file
		final MultiValueMap<String, File> filesToDelete = new MultiValueMap<>(); // face name --> photo file

		for (File faceDir : context.getFilesDir().listFiles()) {
			if (!faceDir.isDirectory()) continue;

			// get all photos that have not been uploaded
			File uploadsDir = new File(faceDir, INTERNAL_UPLOADS_DIR);
			if (!uploadsDir.exists()) continue;
			for (File photoFile : uploadsDir.listFiles()) {
				if (!isPhotoFileName(photoFile.getName())) continue;
				filesToUpload.put(faceDir.getName(), photoFile);
			}

			// get all photos that have not been deleted
			File deletedDir = new File(faceDir, INTERNAL_DELETE_DIR);
			if (!deletedDir.exists()) continue;
			for (File photoFile : deletedDir.listFiles()) {
				if (!isPhotoFileName(photoFile.getName())) continue;
				filesToDelete.put(faceDir.getName(), photoFile);
			}

		}

		Timber.d("uploading " + filesToUpload.size() + " photos");
		Timber.d("deleting " + filesToDelete.size() + " photos");

		Set<String> faceNames = new HashSet<>();
		faceNames.addAll(filesToUpload.keySet());
		faceNames.addAll(filesToDelete.keySet());

		return Observable.from(faceNames)
				// find + create face folder
				.flatMap(new Func1<String, Observable<SyncContainer>>() {
					@Override
					public Observable<SyncContainer> call(final String faceName) {
						return googleDriveManager.query(faceName, false)
								.flatMap(new Func1<Optional<DriveId>, Observable<SyncContainer>>() {
									@Override
									public Observable<SyncContainer> call(Optional<DriveId> folderId) {
										if (folderId.isPresent()) return Observable.just(new SyncContainer(folderId.get(), faceName));
										return googleDriveManager.createNewFolder(faceName).map(new Func1<DriveId, SyncContainer>() {
											@Override
											public SyncContainer call(DriveId driveId) {
												return new SyncContainer(driveId, faceName);
											}
										});
									}
								});

					}
				})
				// add + remove photos
				.flatMap(new Func1<SyncContainer, Observable<Void>>() {
					@Override
					public Observable<Void> call(final SyncContainer syncContainer) {
						return Observable.zip(
								// add photos
								Observable.from(filesToUpload.get(syncContainer.faceName))
										.flatMap(new Func1<File, Observable<Void>>() {
											@Override
											public Observable<Void> call(final File fileToUpload) {
												Timber.d("uploading " + fileToUpload.getAbsolutePath());
												try {
													return googleDriveManager
															.createNewFile(Optional.of(syncContainer.folderDriveId), new FileInputStream(fileToUpload), fileToUpload.getName(), "image/jpeg", false)
															.flatMap(new Func1<Void, Observable<Void>>() {
																@Override
																public Observable<Void> call(Void nothing) {
																	// move photo to regular face dir (and remove from uploads dir)
																	try {
																		File newPhotoFile = new File(fileToUpload.getParentFile().getParentFile(), fileToUpload.getName());
																		ioUtils.copyStream(new FileInputStream(fileToUpload), new FileOutputStream(newPhotoFile));
																		ioUtils.delete(fileToUpload);
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
										}).toList(),
								// remove photos
								Observable.defer(new Func0<Observable<List<Void>>>() {
									@Override
									public Observable<List<Void>> call() {
										GoogleApiClient client = googleApiClientManager.getGoogleApiClient();
										MetadataBuffer metadata = Drive.DriveApi.getFolder(client, syncContainer.folderDriveId).listChildren(client).await().getMetadataBuffer();

										// find drive id for files to delete
										Map<String, DriveId> driveFiles = new HashMap<>();
										for (Metadata fileMetadata : metadata) {
											driveFiles.put(fileMetadata.getTitle(), fileMetadata.getDriveId());
										}
										metadata.release();
										List<DriveId> driveIdsToDelete = new ArrayList<>();

										for (File photoFile : filesToDelete.get(syncContainer.faceName)) {
											Timber.d("deleting " + photoFile.getAbsolutePath());
											String fileName = photoFile.getName();
											if (driveFiles.containsKey(fileName)) {
												driveIdsToDelete.add(driveFiles.get(fileName));
											}
										}

										// actually delete files
										return Observable.from(driveIdsToDelete)
												.flatMap(new Func1<DriveId, Observable<Void>>() {
													@Override
													public Observable<Void> call(DriveId driveId) {
														return googleDriveManager.deleteFile(driveId);
													}
												})
												.toList()
												.map(new Func1<List<Void>, List<Void>>() {
													@Override
													public List<Void> call(List<Void> voids) {
														for (File photoFile : filesToDelete.get(syncContainer.faceName)) {
															ioUtils.delete(photoFile);
														}
														return null;
													}
												});
									}
								}),
								new Func2<List<Void>, List<Void>, Void>() {
									@Override
									public Void call(List<Void> nothing, List<Void> evenMoreNothing) {
										return null;
									}
								});
					}

				});
	}


	public Optional<File> getRecentPhoto(Face face) {
		return getRecentPhoto(face.getName());
	}


	public Optional<File> getRecentPhoto(String faceName) {
		File uploadsDir = getFaceUploadsDir(faceName);
		List<File> files = Lists.newArrayList(uploadsDir.listFiles());
		File faceDir = getFaceDir(faceName);
		for (File photoFile : faceDir.listFiles()) {
			if (!photoFile.isDirectory()) files.add(photoFile);
		}

		if (files.isEmpty()) return Optional.absent();

		// sort and get last (newest) photo file
		Collections.sort(files);
		return Optional.of(files.get(files.size() - 1));
	}


	/**
	 * @return all photos (store locally in internal storage) that belong to this one face.
	 */
	public List<PhotoInfo> getPhotosForFace(final Face face) {
		List<File> photoFiles = Lists.newArrayList();
		for (File file : getFaceDir(face.getName()).listFiles()) {
			photoFiles.add(file);
		}
		for (File file : getFaceUploadsDir(face.getName()).listFiles()) {
			photoFiles.add(file);
		}

		List<PhotoInfo> result = Lists.newArrayList();
		Pattern fileNamePattern = Pattern.compile(PHOTO_FILE_NAME_REGEX);
		for (File photoFile : photoFiles) {
			String name = photoFile.getName();
			if (!isPhotoFileName(name)) continue;

			// parse file name
			Matcher matcher = fileNamePattern.matcher(name);
			matcher.find();
			Calendar calendar = Calendar.getInstance();
			calendar.set(
					Integer.valueOf(matcher.group(1)),
					Integer.valueOf(matcher.group(2)) - 1,
					Integer.valueOf(matcher.group(3)),
					Integer.valueOf(matcher.group(4)),
					Integer.valueOf(matcher.group(5)),
					Integer.valueOf(matcher.group(6)));
			result.add(new PhotoInfo(face, photoFile, calendar.getTime()));
		}

		Collections.sort(result);
		return result;
	}


	/**
	 * Downloads photos belong to one particular faces and stores them locally ("import").
	 *
	 * @param photos list of photo file names mapped to their drive id
	 */
	public Observable<Void> downloadPhotos(final Face face, final List<ImportablePhoto> photos) {
		return Observable.from(photos)
				.flatMap(new Func1<ImportablePhoto, Observable<Void>>() {
					@Override
					public Observable<Void> call(final ImportablePhoto photo) {
						return googleDriveManager.readFile(photo.getDriveId())
								.flatMap(new Func1<InputStream, Observable<Void>>() {
									@Override
									public Observable<Void> call(InputStream driveStream) {
										try {
											final File localPhotoFile = new File(getFaceDir(face.getName()), photo.getTitle());
											ioUtils.copyStream(driveStream, new FileOutputStream(localPhotoFile));
											return Observable.just(null);
										} catch (IOException e) {
											Timber.e(e, "failed to download file " + photo.getTitle() + " from drive");
											return Observable.error(e);
										}
									}
								});
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


	/**
	 * Starts uploading photos to Google drive if WiFi is connected
	 */
	public void requestPhotoSync() {
		context.startService(new Intent(context, PhotoSyncService.class));
	}


	public boolean isPhotoFileName(String photoFileName) {
		return photoFileName.matches(PHOTO_FILE_NAME_REGEX);
	}


	/**
	 * Returns the internal (!) root directory for one face.
	 */
	private File getFaceDir(String faceName) {
		File faceDir = new File(context.getFilesDir(), faceName);
		if (!faceDir.exists() && !faceDir.mkdirs()) {
			Timber.e("failed to create dir " + faceDir.getAbsolutePath());
		}
		return faceDir;
	}


	/**
	 * Returns the internal (!) uploads directory for one face.
	 */
	private File getFaceUploadsDir(String faceName) {
		return ioUtils.assertDir(new File(getFaceDir(faceName), INTERNAL_UPLOADS_DIR));
	}


	/**
	 * Returns the internal (!) directory where photos are stored that are marked for deletion.
	 */
	private File getFaceDeletedDir(Face face) {
		return ioUtils.assertDir(new File(getFaceDir(face.getName()), INTERNAL_DELETE_DIR));
	}


	private File getTmpPhotoFile() {
		File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
		if (!photoDir.exists()) {
			if (photoDir.mkdirs()) Timber.e("failed to make dir " + photoDir.getAbsolutePath());
		}
		return new File(photoDir, "tmp.jpg");
	}


	/**
	 * Checks that the photo is has been properly rotated (zero degrees afterwards).
	 */
	private Bitmap assertCorrectPhotoRotation(PhotoCreationResult result) throws IOException {
		final Bitmap input = BitmapFactory.decodeFile(result.getTmpPhotoFile().getAbsolutePath());

		final int rotation = getRotationForPhoto(result);
		if (rotation == 0) return input;

		// rotate image
		Matrix rotateMatrix = new Matrix();
		rotateMatrix.postRotate(rotation);
		if (rotation == 180) return Bitmap.createBitmap(input,  0, 0, input.getWidth(), input.getHeight(), rotateMatrix, true);

		Matrix centerRotateMatrix = new Matrix();
		centerRotateMatrix.postTranslate(-input.getWidth() / 2, -input.getHeight() / 2);
		centerRotateMatrix.postRotate(rotation);
		centerRotateMatrix.postTranslate(input.getHeight() / 2, input.getWidth() / 2);
		final Bitmap output = Bitmap.createBitmap(input.getHeight(), input.getWidth(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(output);
		canvas.drawBitmap(input, centerRotateMatrix, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
		return output;
	}


	private int getRotationForPhoto(PhotoCreationResult result) {
		// courtesy to http://stackoverflow.com/a/8864367
		int rotation = -1;
		final long fileSize = result.getTmpPhotoFile().length();
		Cursor mediaCursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE },
				MediaStore.MediaColumns.DATE_ADDED + ">=?",
				new String[]{String.valueOf(result.getPhotoCaptureTime() /1000 - 1)}, MediaStore.MediaColumns.DATE_ADDED + " desc");

		// try getting orientation from media provider (image should be there though, this is a known Android bug on some devices!)
		if (mediaCursor != null && mediaCursor.getCount() !=0 ) {
			while(mediaCursor.moveToNext()){
				long size = mediaCursor.getLong(1);
				//Extra check to make sure that we are getting the orientation from the proper file
				if( size == fileSize){
					rotation = mediaCursor.getInt(0);
					break;
				}
			}
			mediaCursor.close();
		}

		// try reading regular exif tags (not present on all devices)
		if(rotation == -1) {
			try {
				ExifInterface exif = new ExifInterface(result.getTmpPhotoFile().getAbsolutePath());
				int rotationConstant = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				switch(rotationConstant) {
					case ExifInterface.ORIENTATION_ROTATE_180:
						return 180;
					case ExifInterface.ORIENTATION_ROTATE_270:
						return 270;
					case ExifInterface.ORIENTATION_ROTATE_90:
						return 90;
				}
			} catch (IOException e) {
				Timber.e(e, "failed to read exif info for image " + result.getTmpPhotoFile().getAbsolutePath());
			}
		}
		return 0;
	}


	/**
	 * Helper class for grouping values during rx chains.
	 */
	private static class SyncContainer {
		private final DriveId folderDriveId;
		private final String faceName;
		public SyncContainer(DriveId folderDriveId, String faceName) {
			this.folderDriveId = folderDriveId;
			this.faceName = faceName;
		}
	}


	public static class PhotoCreationResult implements Parcelable {

		private final Intent photoCaptureIntent;
		private final String faceName;
		private final File tmpPhotoFile;
		private final long photoCaptureTime;

		private PhotoCreationResult(Intent photoCaptureIntent, String faceName, File tmpPhotoFile) {
			this.photoCaptureIntent = photoCaptureIntent;
			this.faceName = faceName;
			this.tmpPhotoFile = tmpPhotoFile;
			this.photoCaptureTime = System.currentTimeMillis();
		}

		public Intent getPhotoCaptureIntent() {
			return photoCaptureIntent;
		}

		public String getFaceName() {
			return faceName;
		}

		private File getTmpPhotoFile() {
			return tmpPhotoFile;
		}

		private long getPhotoCaptureTime() {
			return photoCaptureTime;
		}

		protected PhotoCreationResult(Parcel in) {
			photoCaptureIntent = (Intent) in.readValue(Intent.class.getClassLoader());
			faceName = in.readString();
			tmpPhotoFile = (File) in.readSerializable();
			photoCaptureTime = in.readLong();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeValue(photoCaptureIntent);
			dest.writeString(faceName);
			dest.writeSerializable(tmpPhotoFile);
			dest.writeLong(photoCaptureTime);
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


	public static class ImportablePhoto implements Parcelable {

		private final String title;
		private final DriveId driveId;

		public ImportablePhoto(String title, DriveId driveId) {
			this.title = title;
			this.driveId = driveId;
		}

		public String getTitle() {
			return title;
		}

		public DriveId getDriveId() {
			return driveId;
		}

		protected ImportablePhoto(Parcel in) {
			title = in.readString();
			driveId = (DriveId) in.readValue(DriveId.class.getClassLoader());
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(title);
			dest.writeValue(driveId);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<ImportablePhoto> CREATOR = new Parcelable.Creator<ImportablePhoto>() {
			@Override
			public ImportablePhoto createFromParcel(Parcel in) {
				return new ImportablePhoto(in);
			}

			@Override
			public ImportablePhoto[] newArray(int size) {
				return new ImportablePhoto[size];
			}
		};
	}

}
