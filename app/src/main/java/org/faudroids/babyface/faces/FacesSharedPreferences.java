package org.faudroids.babyface.faces;


import android.content.SharedPreferences;

import org.roboguice.shaded.goole.common.base.Optional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacesSharedPreferences {

	private static final String
			PROPERTY_NAME = "name",
			PROPERTY_MOST_RECENT_PHOTO_FILE_NAME = "mostRecentPhotoFileName";

	private final SharedPreferences preferences;

	public FacesSharedPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	public List<Face> getAll() {
		Map<String, Face> faces = new HashMap<>();
		Map<String, ?> data = preferences.getAll();
		for (String key : data.keySet()) {
			// check if already read
			String id = key.substring(0, key.indexOf("."));
			if (faces.containsKey(id)) continue;

			// read face
			String name = (String) data.get(toKey(id, PROPERTY_NAME));
			String fileName = (String) data.get(toKey(id, PROPERTY_MOST_RECENT_PHOTO_FILE_NAME));
			Optional<File> fileOptional = (fileName == null) ? Optional.<File>absent() : Optional.of(new File(fileName));
			faces.put(id, new Face(id, name, fileOptional));
		}
		return new ArrayList<>(faces.values());
	}

	public void add(Face face) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(toKey(face.getId(), PROPERTY_NAME), face.getName());
		if (face.getMostRecentPhotoFile().isPresent()) {
			editor.putString(
					toKey(face.getId(), PROPERTY_MOST_RECENT_PHOTO_FILE_NAME),
					face.getMostRecentPhotoFile().get().getAbsolutePath());
		}
		editor.apply();
	}


	private String toKey(String id, String property) {
		return id + "." + property;
	}

}
