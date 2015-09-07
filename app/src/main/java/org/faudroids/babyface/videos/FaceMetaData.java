package org.faudroids.babyface.videos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Info about one face.
 */
public class FaceMetaData {

	private final String faceName;

	@JsonCreator
	public FaceMetaData(@JsonProperty("faceName") String faceName) {
		this.faceName = faceName;
	}

	public String getFaceName() {
		return faceName;
	}
}
