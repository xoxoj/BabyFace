package org.faudroids.babyface.utils;


import android.content.Context;
import android.content.SharedPreferences;

public abstract class Pref<T> {

	protected final SharedPreferences preferences;
	protected final String key;
	protected final T defaultValue;

	public Pref(SharedPreferences preferences, String key) {
		this(preferences, key, null);
	}

	public Pref(SharedPreferences preferences, String key, T defaultValue) {
		this.preferences = preferences;
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public boolean isSet() {
		return preferences.contains(key);
	}

	public final void set(T value) {
		SharedPreferences.Editor editor = preferences.edit();
		doSet(value, editor);
		editor.apply();
	}

	public final void clear() {
		preferences.edit().remove(key).apply();
	}

	protected abstract void doSet(T value, SharedPreferences.Editor editor);

	public final T get() {
		if (!isSet()) return defaultValue;
		else return doGet();
	}

	protected abstract T doGet();


	public static Pref<String> newStringPref(Context context, String prefsName, String key) {
		return new StringPref(getPrefs(context, prefsName), key);
	}

	public static Pref<String> newStringPref(Context context, String prefsName, String key, String defaultValue) {
		return new StringPref(getPrefs(context, prefsName), key, defaultValue);
	}

	public static Pref<Boolean> newBooleanPref(Context context, String prefsName, String key) {
		return new BooleanPref(getPrefs(context, prefsName), key);
	}

	public static Pref<Boolean> newBooleanPref(Context context, String prefsName, String key, Boolean defaultValue) {
		return new BooleanPref(getPrefs(context, prefsName), key, defaultValue);
	}

	private static SharedPreferences getPrefs(Context context, String prefsName) {
		return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
	}
}