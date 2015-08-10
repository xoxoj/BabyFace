package org.faudroids.babyface.server.app;


import com.google.inject.Guice;
import com.google.inject.Injector;

import org.faudroids.babyface.server.rest.TestResource;
import org.faudroids.babyface.server.rest.VideoResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class BabyFaceApplication extends Application<BabyFaceConfig> {

	public static void main(String[] args) throws Exception {
		new BabyFaceApplication().run(args);
	}


	@Override
	public void run(BabyFaceConfig configuration, Environment environment) throws Exception {
		Injector injector = Guice.createInjector();

		environment.jersey().register(injector.getInstance(TestResource.class));
		environment.jersey().register(injector.getInstance(VideoResource.class));
	}

}
