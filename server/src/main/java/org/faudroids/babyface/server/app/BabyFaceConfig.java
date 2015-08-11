package org.faudroids.babyface.server.app;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class BabyFaceConfig extends Configuration {

	private final String googleOAuth2WebClientId, googleOAuth2AndroidClientId;

	@JsonCreator
	public BabyFaceConfig(
			@JsonProperty("googleOAuth2WebClientId") String googleOAuth2WebClientId,
			@JsonProperty("googleOAuth2AndroidClientId") String googleOAuth2AndroidClientId) {

		this.googleOAuth2WebClientId = googleOAuth2WebClientId;
		this.googleOAuth2AndroidClientId = googleOAuth2AndroidClientId;
	}

	public String getGoogleOAuth2WebClientId() {
		return googleOAuth2WebClientId;
	}

	public String getGoogleOAuth2AndroidClientId() {
		return googleOAuth2AndroidClientId;
	}
}
