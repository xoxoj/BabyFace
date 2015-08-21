package org.faudroids.babyface.ui;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.GoogleApiClientManager;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import rx.subscriptions.CompositeSubscription;

public class AbstractActivity extends RoboActionBarActivity {

	private final boolean setupToolbar;
	protected Toolbar toolbar;

	protected CompositeSubscription subscriptions = new CompositeSubscription();
	@Inject protected GoogleApiClientManager googleApiClientManager;

	protected AbstractActivity() {
		this(false);
	}

	protected AbstractActivity(boolean setupToolbar) {
		this.setupToolbar = setupToolbar;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (setupToolbar) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
	}

	@Override
	public void onStart() {
		googleApiClientManager.connectToClient();
		super.onStart();
	}

	@Override
	public void onStop() {
        System.out.println("ABSTRACT ACTVITY ON STOP");
		subscriptions.unsubscribe();
		subscriptions = new CompositeSubscription();
		googleApiClientManager.disconnectFromClient();
		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
