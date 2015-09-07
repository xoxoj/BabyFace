package org.faudroids.babyface.faces;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.google.GoogleDriveManager;
import org.faudroids.babyface.photo.PhotoManager;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import timber.log.Timber;

/**
 * Scans Google Drive for face folders which
 */
public class FaceScanner {

	private final FacesManager facesManager;
	private final GoogleApiClientManager googleApiClientManager;
	private final GoogleDriveManager driveManager;
	private final PhotoManager photoManager;

	@Inject
	FaceScanner(FacesManager facesManager, GoogleApiClientManager googleApiClientManager, GoogleDriveManager driveManager, PhotoManager photoManager) {
		this.facesManager = facesManager;
		this.googleApiClientManager = googleApiClientManager;
		this.driveManager = driveManager;
		this.photoManager = photoManager;
	}


	public Observable<List<ImportableFace>> scanGoogleDriveForFaces() {
		return Observable.defer(new Func0<Observable<List<ImportableFace>>>() {
			@Override
			public Observable<List<ImportableFace>> call() {
				GoogleApiClient client = googleApiClientManager.getGoogleApiClient();
				List<ImportableFace> result = Lists.newArrayList();

				// get exiting faces
				List<Face> existingFaces = facesManager.getFaces();
				Set<String> existingFaceFolders = new HashSet<>();
				for (Face existingFace : existingFaces) existingFaceFolders.add(existingFace.getPhotoFolderName());

				// search for folders in app root folder
				DriveFolder rootFolder = driveManager.getAppRootFolder();
				MetadataBuffer queryResult = rootFolder
						.queryChildren(client, new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder")).build())
						.await()
						.getMetadataBuffer();

				// search for images in those folders
				Timber.d("found " + queryResult.getCount() + " potential face dirs");
				for (Metadata folderMetadata : queryResult) {
					// if face is already "imported" then skip it
					if (existingFaceFolders.contains(folderMetadata.getTitle())) continue;

					int imageCount = getImageCountInFaceDir(folderMetadata.getDriveId());
					if (imageCount > 0) {
						result.add(new ImportableFace(folderMetadata.getTitle(), imageCount));
					}
				}
				queryResult.release();

				return Observable.just(result);
			}
		});
	}


	private int getImageCountInFaceDir(DriveId faceFolderId) {
		GoogleApiClient client = googleApiClientManager.getGoogleApiClient();

		// TODO pagination has been deprecated, but there is no info if there is a different pagination mechanism now
		DriveApi.MetadataBufferResult queryResult = Drive.DriveApi.getFolder(client, faceFolderId).listChildren(client).await();
		queryResult.getMetadataBuffer().getNextPageToken();
		int imageCount = 0;
		for (Metadata file : queryResult.getMetadataBuffer()) {
			if (photoManager.isFaceFileName(file.getTitle())) {
				++imageCount;
			}
		}
		queryResult.getMetadataBuffer().release();
		return imageCount;
	}


	public static class ImportableFace {

		private final String faceName;
		private final int imageCount;

		public ImportableFace(String faceName, int imageCount) {
			this.faceName = faceName;
			this.imageCount = imageCount;
		}

		public String getFaceName() {
			return faceName;
		}

		public int getImageCount() {
			return imageCount;
		}

	}

}
