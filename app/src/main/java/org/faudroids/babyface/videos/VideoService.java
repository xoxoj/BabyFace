package org.faudroids.babyface.videos;


import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Streaming;
import rx.Observable;

public interface VideoService {

	@POST("/video")
	@Headers("Content-type: application/json")
	Observable<VideoConversionStatus> createVideo();

	@GET("/video/{videoId}/status")
	@Headers("Content-type: application/json")
	Observable<VideoConversionStatus> getStatus(@Path("videoId") String videoId);

	@GET("/video/{videoId}/data")
	@Streaming
	Observable<Response> getVideo(@Path("videoId") String videoId);

}
