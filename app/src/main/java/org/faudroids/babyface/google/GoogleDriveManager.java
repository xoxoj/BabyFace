package org.faudroids.babyface.google;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.faudroids.babyface.R;
import org.faudroids.babyface.utils.Pref;
import org.roboguice.shaded.goole.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Helper methods for interacting with the Google Drive API.
 */
public class GoogleDriveManager {

	private static final String PREFS_NAME = "org.faudroids.babyface";

	private final GoogleApiClientManager googleApiClientManager;
	private final String appRootFolderName;
	private final Pref<String> appRootFolderIdPref;

	@Inject
	public GoogleDriveManager(Context context, GoogleApiClientManager googleApiClientManager) {
		this.googleApiClientManager = googleApiClientManager;
		this.appRootFolderName = context.getString(R.string.app_name);
		this.appRootFolderIdPref = Pref.newStringPref(context, PREFS_NAME, "rootFolderId", null);
	}


	public Observable<DriveId> createNewFolder(final String folderName) {
		return Observable.defer(new Func0<Observable<DriveId>>() {
			@Override
			public Observable<DriveId> call() {
				DriveFolder folder = createNewFolder(getAppRootFolder(), folderName);
				return Observable.just(folder.getDriveId());
			}
		});
	}


	public Observable<Void> deleteFolder(final String folderName) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				GoogleApiClient client = googleApiClientManager.getGoogleApiClient();

				// find folder drive id
				DriveFolder appFolder = getAppRootFolder();
				MetadataBuffer queryResult = appFolder.queryChildren(
						client,
						new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, folderName)).build())
						.await().getMetadataBuffer();
				if (queryResult.getCount() == 0) throw new IllegalArgumentException("no folder with name " + folderName);
				DriveId folderId = queryResult.get(0).getDriveId();

				// delete folder
				DriveFile folder = Drive.DriveApi.getFile(client, folderId);
				folder.delete(client).await();
				return Observable.just(null);
			}
		});
	}


	public Observable<Void> createNewFile(
			final Optional<DriveId> folderId,
			final InputStream inputStream,
			final String fileName,
			final String mimeType,
			final boolean pinned) {

		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				GoogleApiClient apiClient = googleApiClientManager.getGoogleApiClient();

				// create new drive content
				DriveApi.DriveContentsResult driveContentsResult = Drive.DriveApi
						.newDriveContents(apiClient)
						.await();
				Status status = driveContentsResult.getStatus();
				if (!status.isSuccess()) {
					Timber.e("failed to create new drive contents (" + status.getStatusMessage() + ")");
					return Observable.error(new Exception(status.getStatusMessage()));
				}

				// copy image to drive contents
				try {
					copyStream(inputStream, driveContentsResult.getDriveContents().getOutputStream());
				} catch (IOException e) {
					Timber.e(e, "failed to read image");
					return Observable.error(e);
				}

				// find target folder
				DriveFolder targetFolder;
				if (folderId.isPresent()) targetFolder = Drive.DriveApi.getFolder(apiClient, folderId.get());
				else targetFolder = getAppRootFolder();

				// create drive file
				MetadataChangeSet metadatachangeset = new MetadataChangeSet.Builder()
						.setTitle(fileName)
						.setMimeType(mimeType)
						.setPinned(pinned)
						.build();

				DriveFolder.DriveFileResult driveFileResult = targetFolder
						.createFile(apiClient, metadatachangeset, driveContentsResult.getDriveContents())
						.await();
				status = driveFileResult.getStatus();
				if (!status.isSuccess()) {
					return Observable.error(new Exception(status.getStatusMessage()));
				}

				return Observable.just(null);
			}
		});
	}


	public Observable<Void> writeFile(final DriveId driveId, final InputStream inputStream) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				// OutputStream outputStream = Drive.DriveApi.getFile(googleApiClientManager.getGoogleApiClient(), driveId)
				DriveContents driveContents = Drive.DriveApi.getFile(googleApiClientManager.getGoogleApiClient(), driveId)
						.open(googleApiClientManager.getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null)
						.await()
						.getDriveContents();

				// copy file to drive contents
				try {
					copyStream(inputStream, driveContents.getOutputStream());
				} catch (IOException e) {
					Timber.e(e, "failed to read image");
					return Observable.error(e);
				}

				// commit changes
				driveContents.commit(googleApiClientManager.getGoogleApiClient(), new MetadataChangeSet.Builder().build()).await();
				return Observable.just(null);
			}
		});
	}


	public Observable<InputStream> readFile(final DriveId driveId) {
		return Observable.defer(new Func0<Observable<InputStream>>() {
			@Override
			public Observable<InputStream> call() {
				return Observable.just(Drive.DriveApi
						.getFile(googleApiClientManager.getGoogleApiClient(), driveId)
						.open(googleApiClientManager.getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
						.await()
						.getDriveContents()
						.getInputStream());
			}
		});
	}


	public Observable<Optional<DriveId>> query(final String fileName, final boolean tryReadingFile) {
		final GoogleApiClient googleApiClient = googleApiClientManager.getGoogleApiClient();
		return Observable
				.defer(new Func0<Observable<Optional<DriveId>>>() {
					@Override
					public Observable<Optional<DriveId>> call() {
						DriveApi.MetadataBufferResult queryResult = getAppRootFolder()
								.queryChildren(
										googleApiClientManager.getGoogleApiClient(),
										new Query.Builder()
												.addFilter(Filters.eq(SearchableField.TITLE, fileName))
												.addFilter(Filters.eq(SearchableField.TRASHED, false))
												.build())
								.await();
						MetadataBuffer buffer = queryResult.getMetadataBuffer();
						Optional<DriveId> result;
						if (buffer.getCount() > 0) {
							result = Optional.of(buffer.get(0).getDriveId());
						} else {
							result = Optional.absent();
						}
						return Observable.just(result);
					}
				})
				.flatMap(new Func1<Optional<DriveId>, Observable<Optional<DriveId>>>() {
					@Override
					public Observable<Optional<DriveId>> call(Optional<DriveId> driveIdOptional) {
						if (!tryReadingFile || !driveIdOptional.isPresent()) return Observable.just(driveIdOptional);

						// HACK: query sometimes returns files which are not (!) present
						// Hence try downloading file and check for errors
						DriveApi.DriveContentsResult result = Drive.DriveApi
								.getFile(googleApiClient, driveIdOptional.get())
								.open(googleApiClient, DriveFile.MODE_READ_ONLY, null)
								.await();
						if (!result.getStatus().isSuccess()) {
							return Observable.just(Optional.<DriveId>absent());
						}
						return Observable.just(driveIdOptional);
					}
				});
	}


	public Observable<Void> deleteFile(final DriveId driveId) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				Drive.DriveApi
						.getFile(googleApiClientManager.getGoogleApiClient(), driveId)
						.delete(googleApiClientManager.getGoogleApiClient()).await();
				return Observable.just(null);
			}
		});
	}


	/**
	 * Creates the root folder for this app in Google Drive (/<app name>).
	 */
	public Observable<Void> assertAppRootFolderExists() {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				// query for app (!) root folder
				GoogleApiClient client = googleApiClientManager.getGoogleApiClient();
				DriveApi.MetadataBufferResult queryResult = Drive.DriveApi
						.getRootFolder(client)
						.queryChildren(client, new Query.Builder()
								.addFilter(Filters.eq(SearchableField.TITLE, appRootFolderName))
								.build())
						.await();

				DriveId appRootId;

				// search for folder that is not trashed
				Metadata folderMetaData = null;
				for (Metadata metaData : queryResult.getMetadataBuffer()) {
					if (!metaData.isTrashed() && !metaData.isExplicitlyTrashed()) {
						Timber.d(metaData.isTrashed() + " " + metaData.isExplicitlyTrashed());
						Timber.d(metaData.getTitle() + " " + metaData.getCreatedDate());
						folderMetaData = metaData;
						break;
					}
				}

				if (folderMetaData == null) {
					// create new folder
					Timber.d("root folder not found, creating new one");
					appRootId = createNewFolder(Drive.DriveApi.getRootFolder(client), appRootFolderName).getDriveId();

				} else {
					// get id
					Timber.d("found existing app root folder");
					Timber.d("see " + folderMetaData.getWebViewLink());
					appRootId = folderMetaData.getDriveId();
				}

				// store id
				appRootFolderIdPref.set(appRootId.encodeToString());
				return Observable.just(null);
			}
		});
	}


	/**
	 * @return the root folder for this app. All application data should be stored there
	 */
	public DriveFolder getAppRootFolder() {
		DriveId folderId = DriveId.decodeFromString(appRootFolderIdPref.get());
		return Drive.DriveApi.getFolder(googleApiClientManager.getGoogleApiClient(), folderId);
	}


	private DriveFolder createNewFolder(DriveFolder parentFolder, String folderName) {
		GoogleApiClient client = googleApiClientManager.getGoogleApiClient();
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(folderName).build();
		return parentFolder.createFolder(client, changeSet).await().getDriveFolder();
	}


	private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
	}
}
