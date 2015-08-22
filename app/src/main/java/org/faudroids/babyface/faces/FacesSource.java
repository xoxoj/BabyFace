package org.faudroids.babyface.faces;

import java.util.List;

import rx.Observable;

/**
 * A source which supports CRUD operations on faces.
 */
public interface FacesSource {

	Observable<List<Face>> loadAll();
	Observable<Void> store(Face face);
	/**
	 * Replace all faces with the supplied ones.
	 */
	Observable<Void> storeAll(List<Face> faces);
	Observable<Void> remove(Face face);
	Observable<Void> removeAll();

}
