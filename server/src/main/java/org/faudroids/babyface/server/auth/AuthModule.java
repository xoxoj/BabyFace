package org.faudroids.babyface.server.auth;

import com.google.inject.AbstractModule;

import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

public class AuthModule extends AbstractModule {

	@Override
	protected void configure() {
		RestAdapter adapter = new RestAdapter.Builder()
				.setEndpoint("https://www.googleapis.com/")
				.setConverter(new JacksonConverter())
				.build();

		bind(GoogleTokenInfoService.class).toInstance(adapter.create(GoogleTokenInfoService.class));
	}

}
