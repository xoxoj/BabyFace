package org.faudroids.babyface.faces;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.drive.DriveId;

import org.faudroids.babyface.google.GoogleDriveManager;
import org.roboguice.shaded.goole.common.base.Optional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

/**
 * Keeps track of which people (faces) are being "recorded".
 */
public class FacesManager {

	private static final String DRIVE_FACES_CONFIG_FILE_NAME = "faces_config.json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final GoogleDriveManager googleDriveManager;

	@Inject
	FacesManager(GoogleDriveManager googleDriveManager) {
		this.googleDriveManager = googleDriveManager;
	}


	public Observable<List<Face>> getFaces() {
		return Observable.defer(new Func0<Observable<List<Face>>>() {
			@Override
			public Observable<List<Face>> call() {
				return googleDriveManager.queryForFile(DRIVE_FACES_CONFIG_FILE_NAME)
						.flatMap(new Func1<Optional<DriveId>, Observable<List<Face>>>() {
							@Override
							public Observable<List<Face>> call(Optional<DriveId> driveIdOptional) {
								// if empty return no faces
								if (!driveIdOptional.isPresent()) {
									Timber.d("no drive config file");
									List<Face> faces = new ArrayList<>();
									return Observable.just(faces);
								}

								// read faces config file
								Timber.d("config id " + driveIdOptional.get());
								return readDriveFacesConfig(driveIdOptional.get())
										.flatMap(new Func1<List<Face>, Observable<List<Face>>>() {
											@Override
											public Observable<List<Face>> call(List<Face> faces) {
												return Observable.just(faces);
											}
										});
							}
						});
			}
		});
	}


	public Observable<Void> addFace(final Face face) {
		return Observable
				.zip(getFaces(), googleDriveManager.queryForFile(DRIVE_FACES_CONFIG_FILE_NAME), new Func2<List<Face>, Optional<DriveId>, DriveConfig>() {
					@Override
					public DriveConfig call(List<Face> faces, Optional<DriveId> driveIdOptional) {
						faces.add(face);
						return new DriveConfig(faces, driveIdOptional);
					}
				})
				// store file
				.flatMap(new Func1<DriveConfig, Observable<Void>>() {
					@Override
					public Observable<Void> call(DriveConfig driveConfig) {
						// convert to json
						String json;
						try {
							json = objectMapper.writeValueAsString(driveConfig.faces);
						} catch (JsonProcessingException e) {
							Timber.e(e, "failed to write json");
							return Observable.error(e);
						}

						// check if drive config file exists
						InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes());
						if (driveConfig.driveId.isPresent()) {
							return googleDriveManager.writeFile(driveConfig.driveId.get(), jsonInputStream);
						} else {
							return googleDriveManager.createNewFile(jsonInputStream, DRIVE_FACES_CONFIG_FILE_NAME, "application/json", true);
						}
					}
				});
	}


	public Observable<Void> deleteAllFaces() {
		return googleDriveManager.queryForFile(DRIVE_FACES_CONFIG_FILE_NAME)
				.flatMap(new Func1<Optional<DriveId>, Observable<Void>>() {
					@Override
					public Observable<Void> call(Optional<DriveId> driveIdOptional) {
						if (driveIdOptional.isPresent()) {
							return googleDriveManager.deleteFile(driveIdOptional.get());
						} else {
							return Observable.just(null);
						}
					}
				});
	}


	private Observable<List<Face>> readDriveFacesConfig(DriveId configDriveId) {
		// downloads config file and parses json
		return googleDriveManager.readFile(configDriveId)
				.flatMap(new Func1<InputStream, Observable<List<Face>>>() {
					@Override
					public Observable<List<Face>> call(InputStream stream) {
						try {
							List<Face> faces = objectMapper.readValue(stream, new TypeReference<List<Face>>(){});
							return Observable.just(faces);
						} catch(IOException e) {
							Timber.e(e, "failed to read config file");
							return Observable.error(e);
						}
					}
				});
	}


	private static class DriveConfig {

		private final List<Face> faces;
		private final Optional<DriveId> driveId;

		public DriveConfig(List<Face> faces, Optional<DriveId> driveId) {
			this.faces = faces;
			this.driveId = driveId;
		}

	}

}
