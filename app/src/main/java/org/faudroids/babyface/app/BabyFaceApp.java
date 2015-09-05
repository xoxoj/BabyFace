package org.faudroids.babyface.app;


import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.faudroids.babyface.BuildConfig;
import org.faudroids.babyface.videos.VideosModule;

import io.fabric.sdk.android.Fabric;
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
			Fabric.with(this, new Crashlytics());
			Timber.plant(new CrashReportingTree());
		}
	}

	private static final class CrashReportingTree extends Timber.Tree {

		@Override
		public void e(String msg, Object... args) {
			super.e(msg, args);
			Crashlytics.log(msg);
		}

		@Override
		public void e(Throwable e, String msg, Object... args) {
			super.e(e, msg, args);
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}

		@Override
		public void w(String msg, Object... args) {
			super.w(msg, args);
			Crashlytics.log(msg);
		}

		@Override
		public void w(Throwable e, String msg, Object... args) {
			super.w(e, msg, args);
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}


		@Override
		protected void log(int priority, String tag, String message, Throwable t) {
			// only print warn and error messages
			switch (priority) {
				case Log.WARN:
					if (t == null) Log.w(tag, message);
					else Log.w(tag, message, t);
					break;

				case Log.ERROR:
					if (t == null) Log.e(tag, message);
					else Log.e(tag, message, t);
					break;
			}
		}

	}

}