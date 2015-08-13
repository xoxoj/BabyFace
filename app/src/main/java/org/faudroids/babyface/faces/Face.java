package org.faudroids.babyface.faces;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.roboguice.shaded.goole.common.base.Optional;

import java.io.File;

/**
 * Information about one face (person).
 */
public class Face {

	private final String id;
	private final String name;
	@JsonIgnore
	private final Optional<File> mostRecentPhotoFile;

	public Face(String id, String name, Optional<File> mostRecentPhotoFile) {
		this.id = id;
		this.name = name;
		this.mostRecentPhotoFile = mostRecentPhotoFile;
	}

	@JsonCreator
	public Face(
			@JsonProperty("id") String id,
			@JsonProperty("name") String name,
			@JsonProperty("mostRecentPhotoFileName") String mostRecentPhotoFileName) {

		this.id = id;
		this.name = name;
		if (mostRecentPhotoFileName == null) this.mostRecentPhotoFile = Optional.absent();
		else this.mostRecentPhotoFile = Optional.of(new File(mostRecentPhotoFileName));
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public Optional<File> getMostRecentPhotoFile() {
		return mostRecentPhotoFile;
	}

	public String getMostRecentPhotoFileName() {
		if (mostRecentPhotoFile.isPresent()) return mostRecentPhotoFile.get().getAbsolutePath();
		return null;
	}

	@Override
	public String toString() {
		String s = "[id = " + id + ", name = " + name + ", mostRecentPhotoFileName = ";
		if (mostRecentPhotoFile.isPresent()) s += mostRecentPhotoFile.get().getAbsolutePath();
		else s += "null";
		return s + "]";
	}

}
