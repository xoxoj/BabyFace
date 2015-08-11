package org.faudroids.babyface.videos;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.GoogleApiClientManager;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

public class VideosModule extends AbstractModule {

	@Override
	protected void configure() {
		// nothing to do for now
	}

	@Provides
	@Inject
	public VideoService provideVideoService(final Context context, final GoogleApiClientManager clientManager) {
		RestAdapter adapter = new RestAdapter.Builder()
				.setEndpoint(context.getString(R.string.video_service_base_url))
				.setConverter(new JacksonConverter())
				.setRequestInterceptor(new RequestInterceptor() {
					@Override
					public void intercept(RequestFacade request) {
						try {
							String accessToken = GoogleAuthUtil.getToken(
									context,
									Plus.AccountApi.getAccountName(clientManager.getGoogleApiClient()),
									"oauth2:" + Drive.SCOPE_APPFOLDER.toString());
							request.addHeader("Authorization", "Bearer " + accessToken);
						} catch (IOException | GoogleAuthException e) {
							throw new RuntimeException(e);
						}
					}
				})
				.build();
		return adapter.create(VideoService.class);
	}

}
