package org.faudroids.babyface.faces;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

public class PreferencesFacesSource implements FacesSource {

	private static final String
			PREFS_NAME = "org.faudroids.babyface.faces.FacesManager",
			PREFS_KEY_FACES = "faces";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final Context context;

	@Inject
	PreferencesFacesSource(Context context) {
		this.context = context;
	}

	@Override
	public Observable<List<Face>> loadAll() {
		SharedPreferences prefs = getPrefs();
		if (!prefs.contains(PREFS_KEY_FACES)) return Observable.empty();

		try {
			List<Face> faces = objectMapper.readValue(prefs.getString(PREFS_KEY_FACES, null), new TypeReference<List<Face>>(){});
			return Observable.just(faces);
		} catch(IOException e) {
			Timber.e(e, "failed to read faces from prefs");
			return Observable.error(e);
		}
	}

	@Override
	public Observable<Void> store(final Face face) {
		return loadAll().flatMap(new Func1<List<Face>, Observable<Void>>() {
			@Override
			public Observable<Void> call(List<Face> faces) {
				faces.add(face);
				return storeAll(faces);
			}
		});
	}

	@Override
	public Observable<Void> storeAll(final List<Face> faces) {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				try {
					String json = objectMapper.writeValueAsString(faces);
					getPrefs().edit().putString(PREFS_KEY_FACES, json).apply();
					return Observable.just(null);
				} catch(JsonProcessingException e) {
					return Observable.error(e);
				}
			}
		});
	}

	@Override
	public Observable<Void> remove(final Face face) {
		return loadAll().flatMap(new Func1<List<Face>, Observable<Void>>() {
			@Override
			public Observable<Void> call(List<Face> faces) {
				faces.remove(face);
				return storeAll(faces);
			}
		});
	}

	@Override
	public Observable<Void> removeAll() {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				getPrefs().edit().clear().apply();
				return Observable.just(null);
			}
		});
	}

	private SharedPreferences getPrefs() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
