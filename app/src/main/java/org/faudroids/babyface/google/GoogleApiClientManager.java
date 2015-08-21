package org.faudroids.babyface.google;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class GoogleApiClientManager {

	private final GoogleApiClient googleApiClient;
	private final CompositeConnectionListener connectionListener;
	private int connectionCount = 0;

	@Inject
	GoogleApiClientManager(Context context) {
		this.connectionListener = new CompositeConnectionListener();
		this.googleApiClient = new GoogleApiClient.Builder(context)
				.addApi(Drive.API)
				.addApi(Plus.API)
				.addScope(new Scope(Scopes.PROFILE))
				.addScope(Drive.SCOPE_APPFOLDER)
				.addConnectionCallbacks(connectionListener)
				.addOnConnectionFailedListener(connectionListener)
				.build();
	}


	public void connectToClient() {
		if (connectionCount == 0) googleApiClient.connect();
		++connectionCount;
	}


	public void disconnectFromClient() {
		--connectionCount;
		if (connectionCount == 0) googleApiClient.disconnect();
	}


	public void registerListener(ConnectionListener listener) {
		this.connectionListener.register(listener);
	}


	public void unregisterListener(ConnectionListener listener) {
		this.connectionListener.unregister(listener);
	}


	public GoogleApiClient getGoogleApiClient() {
		return googleApiClient;
	}


	private class CompositeConnectionListener implements ConnectionListener {

		private final List<ConnectionListener> listeners = new ArrayList<>();
		private ConnectionResult lastConnectionResult = null;

		@Override
		public void onConnected(Bundle bundle) {
			Timber.d("google api client connected");
			this.lastConnectionResult = null;
			for (ConnectionListener listener : listeners) listener.onConnected(bundle);
		}

		@Override
		public void onConnectionSuspended(int i) {
			Timber.d("google api client connection suspended");
			this.lastConnectionResult = null;
			for (ConnectionListener listener : listeners) listener.onConnectionSuspended(i);
		}

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			Timber.d("google api client connection error");
			this.lastConnectionResult = connectionResult;
			for (ConnectionListener listener : listeners) listener.onConnectionFailed(connectionResult);
		}

		public void register(ConnectionListener listener) {
			listeners.add(listener);
			if (googleApiClient.isConnected()) listener.onConnected(null);
			else if (lastConnectionResult != null) listener.onConnectionFailed(lastConnectionResult);
		}

		public void unregister(ConnectionListener listener) {
			listeners.remove(listener);
		}

	}

}
