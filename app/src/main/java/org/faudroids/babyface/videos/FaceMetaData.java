package org.faudroids.babyface.videos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Info about one face.
 */
public class FaceMetaData {

	private final String faceId;

	@JsonCreator
	public FaceMetaData(@JsonProperty("faceId") String faceId) {
		this.faceId = faceId;
	}

	public String getFaceId() {
		return faceId;
	}
}
