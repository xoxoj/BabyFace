package org.faudroids.babyface.server.auth;

import com.google.common.base.Optional;

import java.util.List;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import retrofit.RetrofitError;

public class GoogleOAuth2Authenticator implements Authenticator<String, User> {

	private final String googleOAuth2WebClientId;
	private final List<String> googleOAuth2AndroidClientIds;
	private final GoogleTokenInfoService tokenInfoService;

	public GoogleOAuth2Authenticator(
			GoogleTokenInfoService tokenInfoService,
			String googleOAuth2WebClientId,
			List<String> googleOAuth2AndroidClientIds) {

		this.tokenInfoService = tokenInfoService;
		this.googleOAuth2WebClientId = googleOAuth2WebClientId;
		this.googleOAuth2AndroidClientIds = googleOAuth2AndroidClientIds;
	}


	@Override
	public Optional<User> authenticate(String accessToken) throws AuthenticationException {
		try {
			GoogleTokenInfo info = tokenInfoService.getTokenInfo(accessToken);
			if (!googleOAuth2AndroidClientIds.contains(info.getIssuedTo())) {
				return Optional.absent();
			}

			// TODO this should check the web client id
			if (!googleOAuth2AndroidClientIds.contains(info.getIssuedTo())) {
				return Optional.absent();
			}

			return Optional.of(new User(accessToken));

		} catch (RetrofitError e) {
			return Optional.absent();
		}
	}

}
