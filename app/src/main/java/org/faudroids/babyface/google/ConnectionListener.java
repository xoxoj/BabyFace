package org.faudroids.babyface.google;


import com.google.android.gms.common.api.GoogleApiClient;

public interface ConnectionListener extends
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

}
