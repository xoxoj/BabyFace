package org.faudroids.babyface.server.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenInfo {

	private final String issuedTo;
	private final String audience;
	private final String scope;
	private final long expiresIn;
	private final String accessType;

	@JsonCreator
	public GoogleTokenInfo(
			@JsonProperty("issued_to") String issuedTo,
			@JsonProperty("audience") String audience,
			@JsonProperty("scope") String scope,
			@JsonProperty("expires_in") long expiresIn,
			@JsonProperty("access_type") String accessType) {

		this.issuedTo = issuedTo;
		this.audience = audience;
		this.scope = scope;
		this.expiresIn = expiresIn;
		this.accessType = accessType;
	}

	public String getIssuedTo() {
		return issuedTo;
	}

	public String getAudience() {
		return audience;
	}

	public String getScope() {
		return scope;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public String getAccessType() {
		return accessType;
	}

}
