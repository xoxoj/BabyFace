package org.faudroids.babyface.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.faudroids.babyface.R;
import org.faudroids.babyface.auth.AuthManager;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.photo.PhotoManager;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;


public class SettingsFragment extends AbstractFragment {

	@InjectView(R.id.txt_start_import) private TextView startImport;
	@InjectView(R.id.check_force_photo_landscape) private CheckBox forceLandscapePhotos;
	@InjectView(R.id.shareTextView) private TextView share;
	@InjectView(R.id.versionTextView) private TextView version;
	@InjectView(R.id.authorTextView) private TextView authors;
	@InjectView(R.id.creditsTextView) private TextView credits;
	@InjectView(R.id.logoutTextView) private TextView logout;

	@Inject private AuthManager authManager;
	@Inject private GoogleApiClientManager googleApiClientManager;
	@Inject private FacesImportViewHandler importViewHandler;
	@Inject private PhotoManager photoManager;


	public SettingsFragment() {
		super(R.layout.fragment_settings);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle(R.string.settings);

		startImport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				importViewHandler.showImportDialog((AbstractActivity) getActivity(), new FacesImportViewHandler.ImportListener() {
					@Override
					public void onSuccess(FacesImportViewHandler.ImportStatus status) {
						switch (status) {
							case NOTHING_TO_IMPORT:
								Toast.makeText(getActivity(), R.string.nothing_to_import, Toast.LENGTH_SHORT).show();
								break;
						}
						// nothing to do
					}

					@Override
					public void onError(Throwable throwable) {
						new AlertDialog.Builder(getActivity())
								.setTitle(R.string.error)
								.setMessage(R.string.photo_import_msg_error)
								.setPositiveButton(android.R.string.ok, null)
								.show();
					}
				}, null);
			}
		});

		forceLandscapePhotos.setChecked(photoManager.getForcePhotoLandscapeMode());
		forceLandscapePhotos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				photoManager.setForcePhotoLandscapeMode(isChecked);
			}
		});

		share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String appName= getString(R.string.app_name);
				String shareBody = getString(R.string.share_message, appName);
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, appName);
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)));
			}
		});

		version.setText(getVersion());

		setOnClickDialogForTextView(authors, R.string.about, R.string.about_content);

		setOnClickDialogForTextView(credits, R.string.credits, R.string.credits_content);

		logout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				authManager.signOut(googleApiClientManager.getGoogleApiClient());
				getActivity().finish();
				startActivity(new Intent(getActivity(), LoginActivity.class));
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
