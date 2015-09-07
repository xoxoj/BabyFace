package org.faudroids.babyface.utils;


import android.content.SharedPreferences;

class StringPref extends Pref<String> {

	public StringPref(SharedPreferences preferences, String key) {
		super(preferences, key);
	}

	public StringPref(SharedPreferences preferences, String key, String defaultValue) {
		super(preferences, key, defaultValue);
	}

	@Override
	protected void doSet(String value, SharedPreferences.Editor editor) {
		editor.putString(key, value);
	}

	@Override
	protected String doGet() {
		return preferences.getString(key, null);
	}

}