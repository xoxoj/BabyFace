package org.faudroids.babyface.faces;


import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.utils.Pref;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Keeps track of which people (faces) are being "recorded".
 */
public class FacesManager {

	private static final String PREFS_NAME = "org.faudroids.babyface.faces.FacesManager";

	private static final ObjectMapper mapper = new ObjectMapper();

	private final PhotoManager photoManager;
	private final Pref<String> facesPref;

	@Inject
	FacesManager(Context context, PhotoManager photoManager) {
		this.photoManager = photoManager;
		this.facesPref = Pref.newStringPref(context, PREFS_NAME, "faces", null);
	}


	public List<Face> getFaces() {
		return loadFaces();
	}


	/**
	 * Stores the face and inits it's photo directory
	 */
	public Observable<Void> addFaceAndInitPhotos(final Face face) {
		return photoManager
				.addPhotoDir(face)
				.flatMap(new Func1<Void, Observable<Void>>() {
					@Override
					public Observable<Void> call(Void aVoid) {
						addFace(face);
						return Observable.just(null);
					}
				});
	}


	/**
	 * Stores the face without any further initialization.
	 */
	public void addFace(final Face face) {
		List<Face> faces = loadFaces();
		faces.add(face);
		storeFaces(faces);
	}


	/**
	 * Removes the face and all photos that are associated with it
	 */
	public Observable<Void> deleteFace(final Face face) {
		// try deleting files first --> on error state is consistent (face still exists)
		return photoManager.deletePhotoDir(face)
				.flatMap(new Func1<Void, Observable<Void>>() {
					@Override
					public Observable<Void> call(Void aVoid) {
						List<Face> faces = loadFaces();
						faces.remove(face);
						storeFaces(faces);
						return Observable.just(null);
					}
				});
	}


	public void updateFace(final Face face) {
		List<Face> faces = loadFaces();
		Iterator<Face> faceIterator = faces.iterator();
		while (faceIterator.hasNext()) {
			if (faceIterator.next().getName().equals(face.getName())) {
				faceIterator.remove();
				break;
			}
		}
		faces.add(face);
		storeFaces(faces);
	}


	private void storeFaces(List<Face> faces) {
		try {
			String facesJson = mapper.writeValueAsString(faces);
			facesPref.set(facesJson);
		} catch (Exception e) {
			Timber.e(e, "failed to serialize faces");
			throw new IllegalStateException(e);
		}
	}


	private List<Face> loadFaces() {
		if (!facesPref.isSet()) return Lists.newArrayList();
		String facesJson = facesPref.get();
		try {
			return mapper.readValue(facesJson, new TypeReference<List<Face>>() { });
		} catch (Exception e) {
			Timber.e(e, "failed to deserialize faces");
			throw new IllegalStateException(e);
		}
	}

}
