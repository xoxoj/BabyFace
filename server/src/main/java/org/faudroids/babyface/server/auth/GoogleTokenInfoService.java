package org.faudroids.babyface.server.auth;


import retrofit.http.GET;
import retrofit.http.Query;

public interface GoogleTokenInfoService {

	@GET("/oauth2/v1/tokeninfo")
	GoogleTokenInfo getTokenInfo(@Query("access_token") String accessToken);

}
