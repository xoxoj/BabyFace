package org.faudroids.babyface.ui;


import roboguice.activity.RoboActivity;
import rx.subscriptions.CompositeSubscription;

public class AbstractActivity extends RoboActivity {

	protected CompositeSubscription subscriptions = new CompositeSubscription();

	@Override
	public void onStop() {
		subscriptions.unsubscribe();
		subscriptions = new CompositeSubscription();
		super.onStop();
	}

}
