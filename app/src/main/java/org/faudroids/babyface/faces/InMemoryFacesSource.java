package org.faudroids.babyface.faces;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;

@Singleton
public class InMemoryFacesSource implements FacesSource {

	private List<Face> faces;

	@Inject
	InMemoryFacesSource() { }

	@Override
	public Observable<List<Face>> loadAll() {
		if (faces == null) return Observable.empty();
		return Observable.just(faces);
	}

	@Override
	public Observable<Void> store(final Face face) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				if (faces == null) faces = new ArrayList<>();
				faces.add(face);
				return Observable.just(null);
			}
		});
	}

	@Override
	public Observable<Void> storeAll(final List<Face> faces) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				InMemoryFacesSource.this.faces = faces;
				return Observable.just(null);
			}
		});
	}

	@Override
	public Observable<Void> remove(final Face face) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				if (faces == null) faces = new ArrayList<>();
				faces.remove(face);
				return Observable.just(null);
			}
		});
	}

	@Override
	public Observable<Void> removeAll() {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				faces = null;
				return Observable.just(null);
			}
		});
	}

}
