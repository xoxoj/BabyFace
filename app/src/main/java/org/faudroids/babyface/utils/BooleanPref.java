package org.faudroids.babyface.utils;


import android.content.SharedPreferences;

class BooleanPref extends Pref<Boolean> {

	public BooleanPref(SharedPreferences preferences, String key) {
		super(preferences, key);
	}

	public BooleanPref(SharedPreferences preferences, String key, Boolean defaultValue) {
		super(preferences, key, defaultValue);
	}

	@Override
	protected void doSet(Boolean value, SharedPreferences.Editor editor) {
		editor.putBoolean(key, value);
	}

	@Override
	protected Boolean doGet() {
		return preferences.getBoolean(key, false);
	}

}