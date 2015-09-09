package org.faudroids.babyface.utils;

import org.faudroids.babyface.google.GoogleApiClientManager;

import javax.inject.Inject;

import roboguice.service.RoboService;

public abstract class AbstractGoogleApiClientService extends RoboService {

	@Inject protected GoogleApiClientManager googleApiClientManager;

	@Override
	public void onCreate() {
		super.onCreate();
		googleApiClientManager.connectToClient();
	}


	@Override
	public void onDestroy() {
		googleApiClientManager.disconnectFromClient();
		super.onDestroy();
	}

}
