package org.faudroids.babyface.server.app;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import io.dropwizard.Configuration;

public class BabyFaceConfig extends Configuration {

	private final List<String> googleOAuth2AndroidClientIds;
	private final String googleOAuth2WebClientId;

	@JsonCreator
	public BabyFaceConfig(
			@JsonProperty("googleOAuth2WebClientId") String googleOAuth2WebClientId,
			@JsonProperty("googleOAuth2AndroidClientIds") List<String> googleOAuth2AndroidClientIds) {

		this.googleOAuth2WebClientId = googleOAuth2WebClientId;
		this.googleOAuth2AndroidClientIds = googleOAuth2AndroidClientIds;
	}

	public String getGoogleOAuth2WebClientId() {
		return googleOAuth2WebClientId;
	}

	public List<String> getGoogleOAuth2AndroidClientIds() {
		return googleOAuth2AndroidClientIds;
	}
}
