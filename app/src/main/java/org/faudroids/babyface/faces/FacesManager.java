package org.faudroids.babyface.faces;


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

	@Inject
	FacesManager(DriveFacesSource driveSource, PreferencesFacesSource prefsSource, InMemoryFacesSource memorySource) {
		this.driveSource = driveSource;
		this.prefsSource = prefsSource;
		this.memorySource = memorySource;
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
		return Observable.zip(memorySource.store(face), prefsSource.store(face), driveSource.store(face), new Func3<Void, Void, Void, Void>() {
			@Override
			public Void call(Void aVoid, Void aVoid2, Void aVoid3) {
				return null;
			}
		});
	}

}
