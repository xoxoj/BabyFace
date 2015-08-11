package org.faudroids.babyface.server.app;


import com.google.inject.Guice;
import com.google.inject.Injector;

import org.faudroids.babyface.server.auth.AuthModule;
import org.faudroids.babyface.server.auth.GoogleOAuth2Authenticator;
import org.faudroids.babyface.server.auth.GoogleTokenInfoService;
import org.faudroids.babyface.server.auth.User;
import org.faudroids.babyface.server.rest.TestResource;
import org.faudroids.babyface.server.rest.VideoResource;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.setup.Environment;

public class BabyFaceApplication extends Application<BabyFaceConfig> {

	public static void main(String[] args) throws Exception {
		new BabyFaceApplication().run(args);
	}


	@Override
	public void run(BabyFaceConfig configuration, Environment environment) throws Exception {
		Injector injector = Guice.createInjector(new AuthModule());

		// setup resources
		environment.jersey().register(injector.getInstance(TestResource.class));
		environment.jersey().register(injector.getInstance(VideoResource.class));

		// setup google oauth
		environment.jersey().register(AuthFactory.binder(new OAuthFactory<User>(
				new GoogleOAuth2Authenticator(
						injector.getInstance(GoogleTokenInfoService.class),
						configuration.getGoogleOAuth2WebClientId(),
						configuration.getGoogleOAuth2AndroidClientId()),
				"secret",
				User.class)));
	}

}
