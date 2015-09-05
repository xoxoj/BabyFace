package org.faudroids.babyface.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.faudroids.babyface.R;
import org.faudroids.babyface.google.GoogleApiClientManager;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;


public class SettingsFragment extends AbstractFragment {

	@InjectView(R.id.versionTextView) private TextView version;
	@InjectView(R.id.authorTextView) private TextView authors;
	@InjectView(R.id.creditsTextView) private TextView credits;
	@InjectView(R.id.logoutTextView) private TextView logout;

	@Inject private GoogleApiClientManager googleApiClientManager;


	public SettingsFragment() {
		super(R.layout.fragment_settings);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle(R.string.settings);

		version.setText(getVersion());

		setOnClickDialogForTextView(authors, R.string.about, R.string.about_content);

		setOnClickDialogForTextView(credits, R.string.credits, R.string.credits_content);

		logout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GoogleApiClient client = googleApiClientManager.getGoogleApiClient();
				Plus.AccountApi.clearDefaultAccount(client);
				client.disconnect();
				client.connect();
				getActivity().finish();
				startActivity(new Intent(getActivity(), MainDrawerActivity.class));
			}
		});
	}

	private String getVersion() {
		try {
			return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException nnfe) {
			Timber.e(nnfe, "failed to get version");
			return null;
		}
	}

	private AlertDialog.Builder setOnClickDialogForTextView(TextView textView, final int titleResourceId, final int msgResourceId) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
				.setTitle(titleResourceId)
				.setMessage(Html.fromHtml(getString(msgResourceId)))
				.setPositiveButton(android.R.string.ok, null);

		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = dialogBuilder.show();
				((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		});
		return dialogBuilder;
	}

}
