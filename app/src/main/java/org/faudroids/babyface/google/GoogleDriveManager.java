package org.faudroids.babyface.google;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import timber.log.Timber;

/**
 * Helper methods for interacting with the Google Drive API.
 */
public class GoogleDriveManager {

	private final GoogleApiClientManager googleApiClientManager;

	@Inject
	public GoogleDriveManager(GoogleApiClientManager googleApiClientManager) {
		this.googleApiClientManager = googleApiClientManager;
	}


	public Observable<Status> createNewFile(
			final InputStream inputStream,
			final String fileName,
			final String mimeType) {

		return Observable.defer(new Func0<Observable<Status>>() {
			@Override
			public Observable<Status> call() {
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
					OutputStream driveOutputStream = driveContentsResult.getDriveContents().getOutputStream();
					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						driveOutputStream.write(buffer, 0, bytesRead);
					}

				} catch (IOException e) {
					Timber.e(e, "failed to read image");
					return Observable.error(e);
				}

				// create drive file
				MetadataChangeSet metadatachangeset = new MetadataChangeSet.Builder()
						.setTitle(fileName)
						.setMimeType(mimeType)
						.build();
				DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi
						.getAppFolder(apiClient)
						.createFile(apiClient, metadatachangeset, driveContentsResult.getDriveContents())
						.await();
				status = driveFileResult.getStatus();
				if (!status.isSuccess()) {
					return Observable.error(new Exception(status.getStatusMessage()));
				}

				return Observable.just(status);
			}
		});
	}
}
