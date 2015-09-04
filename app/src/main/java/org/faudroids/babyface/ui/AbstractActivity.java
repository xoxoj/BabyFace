package org.faudroids.babyface.ui;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.GoogleApiClientManager;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import rx.subscriptions.CompositeSubscription;

public class AbstractActivity extends RoboActionBarActivity {

	private final boolean setupToolbar, setupProgressBar;

	protected Toolbar toolbar;
	protected CircleProgressBar progressBar;

	protected CompositeSubscription subscriptions = new CompositeSubscription();
	@Inject protected GoogleApiClientManager googleApiClientManager;

	protected AbstractActivity() {
		this(false, false);
	}

	protected AbstractActivity(boolean setupToolbar, boolean setupProgressBar) {
		this.setupToolbar = setupToolbar;
		this.setupProgressBar = setupProgressBar;
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
		if (setupProgressBar) {
			progressBar = (CircleProgressBar) findViewById(R.id.progressbar);
		}
	}

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showProgressBar() {
		if (!setupProgressBar) throw new IllegalStateException("no progress bar");
		progressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		if (!setupProgressBar) throw new IllegalStateException("no progress bar");
		progressBar.setVisibility(View.GONE);
	}

}
