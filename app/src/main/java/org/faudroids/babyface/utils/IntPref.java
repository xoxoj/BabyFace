package org.faudroids.babyface.utils;


import android.content.SharedPreferences;

class IntPref extends Pref<Integer> {

	public IntPref(SharedPreferences preferences, String key) {
		super(preferences, key);
	}

	public IntPref(SharedPreferences preferences, String key, Integer defaultValue) {
		super(preferences, key, defaultValue);
	}

	@Override
	protected void doSet(Integer value, SharedPreferences.Editor editor) {
		editor.putInt(key, value);
	}

	@Override
	protected Integer doGet() {
		return preferences.getInt(key, 0);
	}

}