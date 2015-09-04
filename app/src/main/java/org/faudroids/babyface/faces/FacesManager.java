package org.faudroids.babyface.faces;


import org.faudroids.babyface.photo.PhotoManager;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func3;

/**
 * Keeps track of which people (faces) are being "recorded".
 */
public class FacesManager {

	private final FacesSource driveSource, prefsSource, memorySource;
	private final PhotoManager photoManager;

	@Inject
	FacesManager(DriveFacesSource driveSource, PreferencesFacesSource prefsSource, InMemoryFacesSource memorySource, PhotoManager photoManager) {
		this.driveSource = driveSource;
		this.prefsSource = prefsSource;
		this.memorySource = memorySource;
		this.photoManager = photoManager;
	}


	public Observable<List<Face>> getFaces() {
		// store data as it is downloaded
		Observable<List<Face>> driveObservable = driveSource.loadAll().flatMap(new Func1<List<Face>, Observable<List<Face>>>() {
			@Override
			public Observable<List<Face>> call(final List<Face> faces) {
				return prefsSource.storeAll(faces).flatMap(new Func1<Void, Observable<List<Face>>>() {
					@Override
					public Observable<List<Face>> call(Void aVoid) {
						return memorySource.storeAll(faces).map(new Func1<Void, List<Face>>() {
							@Override
							public List<Face> call(Void aVoid) {
								return faces;
							}
						});
					}
				});
			}
		});
		Observable<List<Face>> prefsObservable = prefsSource.loadAll().flatMap(new Func1<List<Face>, Observable<List<Face>>>() {
			@Override
			public Observable<List<Face>> call(final List<Face> faces) {
				return memorySource.storeAll(faces).map(new Func1<Void, List<Face>>() {
					@Override
					public List<Face> call(Void aVoid) {
						return faces;
					}
				});
			}
		});
		return Observable
				.concat(memorySource.loadAll(), prefsObservable, driveObservable)
				.first();
	}


	public Observable<Void> addFace(final Face face) {
		// try adding photo dir fist --> on error state is consistent
		return photoManager.addPhotoDir(face)
				.flatMap(new Func1<Void, Observable<Void>>() {
					@Override
					public Observable<Void> call(Void aVoid) {
						return Observable.zip(
								memorySource.store(face),
								prefsSource.store(face),
								driveSource.store(face),
								new Func3<Void, Void, Void, Void>() {
									@Override
									public Void call(Void aVoid, Void aVoid2, Void aVoid3) {
										return null;
									}
								});
					}
				});
	}


	/**
	 * Removes the face and all photos that are associated to it
	 */
	public Observable<Void> deleteFace(final Face face) {
		// try deleting files first --> on error state is consistent (face exists in all sources, photos are present)
		return photoManager.deletePhotoDir(face)
				.flatMap(new Func1<Void, Observable<Void>>() {
					@Override
					public Observable<Void> call(Void aVoid) {
						return Observable.zip(
								memorySource.remove(face),
								prefsSource.remove(face),
								driveSource.remove(face),
								new Func3<Void, Void, Void, Void>() {
									@Override
									public Void call(Void aVoid, Void aVoid2, Void aVoid3) {
										return null;
									}
								});
					}
				});
	}

}
