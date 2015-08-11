package org.faudroids.babyface.server.auth;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import retrofit.RetrofitError;

public class GoogleOAuth2Authenticator implements Authenticator<String, User> {

	private final String googleOAuth2WebClientId, googleOAuth2AndroidClientId;
	private final GoogleTokenInfoService tokenInfoService;

	public GoogleOAuth2Authenticator(
			GoogleTokenInfoService tokenInfoService,
			String googleOAuth2WebClientId,
			String googleOAuth2AndroidClientId) {

		this.tokenInfoService = tokenInfoService;
		this.googleOAuth2WebClientId = googleOAuth2WebClientId;
		this.googleOAuth2AndroidClientId = googleOAuth2AndroidClientId;
	}


	@Override
	public Optional<User> authenticate(String accessToken) throws AuthenticationException {
		try {
			GoogleTokenInfo info = tokenInfoService.getTokenInfo(accessToken);
			if (!info.getIssuedTo().equals(googleOAuth2AndroidClientId)) {
				return Optional.absent();
			}

			// TODO this should check the web client id
			if (!info.getAudience().equals(googleOAuth2AndroidClientId)) {
				return Optional.absent();
			}

			return Optional.of(new User(accessToken));

		} catch (RetrofitError e) {
			return Optional.absent();
		}
	}

}
