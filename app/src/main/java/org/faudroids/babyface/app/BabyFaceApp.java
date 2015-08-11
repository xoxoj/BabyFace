package org.faudroids.babyface.app;


import android.app.Application;

import org.faudroids.babyface.BuildConfig;
import org.faudroids.babyface.videos.VideosModule;

import roboguice.RoboGuice;
import timber.log.Timber;

public class BabyFaceApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// setup DI
		RoboGuice.getOrCreateBaseApplicationInjector(
				this,
				RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this),
				new VideosModule());

		// setup logging
		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			throw new UnsupportedOperationException("production logging not configured!");
			// Fabric.with(this, new Crashlytics());
			// Timber.plant(new CrashReportingTree());
		}
	}

	/*
	private static final class CrashReportingTree extends Timber.Tree {

		@Override
		public void e(String msg, Object... args) {
			Crashlytics.log(msg);
		}

		@Override
		public void e(Throwable e, String msg, Object... args) {
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}

		@Override
		public void w(String msg, Object... args) {
			Crashlytics.log(msg);
		}

		@Override
		public void w(Throwable e, String msg, Object... args) {
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}


		@Override
		protected void log(int priority, String tag, String message, Throwable t) {
			// nothing to do here
		}

	}
	*/

}