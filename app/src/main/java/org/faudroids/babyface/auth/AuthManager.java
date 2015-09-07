package org.faudroids.babyface.auth;


import android.content.Context;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.faudroids.babyface.utils.Pref;

import javax.inject.Inject;

public class AuthManager {

	private static final String PREFS_NAME = "org.faudroids.babyface.AuthManager";
	private static final String
			KEY_NAME = "KEY_NAME",
			KEY_IMAGE_URL = "KEY_IMAGE_URL",
			KEY_EMAIL = "KEY_EMAIL";

	private final Pref<String> namePref, emailPref, imageUrlPref;

	@Inject
	AuthManager(Context context) {
		this.namePref = Pref.newStringPref(context, PREFS_NAME, KEY_NAME, null);
		this.emailPref = Pref.newStringPref(context, PREFS_NAME, KEY_EMAIL, null);
		this.imageUrlPref = Pref.newStringPref(context, PREFS_NAME, KEY_IMAGE_URL, null);
	}


	public void signIn(GoogleApiClient googleApiClient) {
		Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);
		namePref.set(person.getDisplayName());
		// remove size query param from image url
		String imageUrl = Uri.parse(person.getImage().getUrl()).buildUpon().clearQuery().build().toString();
		imageUrlPref.set(imageUrl);
		String email = Plus.AccountApi.getAccountName(googleApiClient);
		emailPref.set(email);
	}

	public void signOut(GoogleApiClient googleApiClient) {
		// clear prefs
		namePref.clear();
		emailPref.clear();
		imageUrlPref.clear();

		// sign out of APIs
		Plus.AccountApi.clearDefaultAccount(googleApiClient);
		googleApiClient.disconnect();
		googleApiClient.connect();
	}

	public boolean isSignedIn() {
		return namePref.isSet();
	}

	public Account getAccount() {
		return new Account(namePref.get(), emailPref.get(), imageUrlPref.get());
	}

}