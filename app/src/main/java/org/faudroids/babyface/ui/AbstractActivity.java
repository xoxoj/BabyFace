package org.faudroids.babyface.ui;


import org.faudroids.babyface.google.GoogleApiClientManager;

import javax.inject.Inject;

import roboguice.activity.RoboActivity;
import rx.subscriptions.CompositeSubscription;

public class AbstractActivity extends RoboActivity {

	protected CompositeSubscription subscriptions = new CompositeSubscription();
	@Inject protected GoogleApiClientManager googleApiClientManager;

	@Override
	public void onStart() {
		googleApiClientManager.connectToClient();
		super.onStart();
	}

	@Override
	public void onStop() {
		subscriptions.unsubscribe();
		subscriptions = new CompositeSubscription();
		googleApiClientManager.disconnectFromClient();
		super.onStop();
	}

}
