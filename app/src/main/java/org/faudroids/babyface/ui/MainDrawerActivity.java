package org.faudroids.babyface.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;
import org.faudroids.babyface.auth.Account;
import org.faudroids.babyface.auth.AuthManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;


@ContentView(R.layout.activity_main)
public class MainDrawerActivity extends AbstractActivity implements Drawer.OnDrawerItemClickListener {

	private static final String
			STATE_FRAGMENT = "FRAGMENT",
			STATE_FRAGMENT_ID = "FRAGMENT_ID";

	private static final int
			ID_SHOW_FACES = 0,
			ID_SHOW_VIDEOS = 1,
			ID_SETTINGS = 2,
			ID_FEEDBACK = 3;

	private Drawer drawer;
	private AccountHeader accountHeader;
	@Inject private AuthManager authManager;

	private int visibleFragmentId = ID_SHOW_FACES;
	private Fragment visibleFragment;

	private OnBackPressedListener backPressedListener;


	public MainDrawerActivity() {
		super(true, true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FRAGMENT)) {
			visibleFragment = getFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT);
			visibleFragmentId = savedInstanceState.getInt(STATE_FRAGMENT_ID);
		}

		// setup image loading for nav drawer
		DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
			@Override
			public void set(ImageView imageView, Uri uri, Drawable drawable) {
				Picasso.with(imageView.getContext()).load(uri).into(imageView);
			}

			@Override
			public void cancel(ImageView imageView) {
				Picasso.with(imageView.getContext()).cancelRequest(imageView);
			}

			@Override
			public Drawable placeholder(Context context) {
				return null;
			}
		});

		// setup account in nav drawer
		Account account = authManager.getAccount();
		accountHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withProfileImagesClickable(false)
				.withSelectionListEnabledForSingleProfile(false)
				.withSavedInstance(savedInstanceState)
				.withHeaderBackground(R.drawable.drawer_background)
				.addProfiles(
						new ProfileDrawerItem()
								.withName(account.getName())
								.withEmail(account.getEmail())
								.withIcon(Uri.parse(account.getImageUrl()))
				)
				.build();

		// show first fragment
		if (visibleFragment == null) showFragment(new FacesOverviewFragment(), false);
		else showFragment(visibleFragment, true);

		// setup actual nav drawer
		drawer = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withAccountHeader(accountHeader)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.albums).withIconTintingEnabled(true).withIcon(R.drawable.ic_faces).withIdentifier(ID_SHOW_FACES),
						new PrimaryDrawerItem().withName(R.string.videos).withIconTintingEnabled(true).withIcon(R.drawable.ic_movie).withIdentifier(ID_SHOW_VIDEOS)
				)
				.addStickyDrawerItems(
						new PrimaryDrawerItem().withName(R.string.settings).withIconTintingEnabled(true).withIcon(R.drawable.ic_settings).withIdentifier(ID_SETTINGS),
						new PrimaryDrawerItem().withName(R.string.feedback).withIconTintingEnabled(true).withIcon(R.drawable.ic_email).withIdentifier(ID_FEEDBACK)
				)
				.withOnDrawerItemClickListener(this)
				.withSavedInstance(savedInstanceState)
				.build();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState = drawer.saveInstanceState(outState);
		outState = accountHeader.saveInstanceState(outState);
		if (visibleFragment != null) {
			getFragmentManager().putFragment(outState, STATE_FRAGMENT, visibleFragment);
			outState.putInt(STATE_FRAGMENT_ID, visibleFragmentId);
		}
		super.onSaveInstanceState(outState);
	}


	private void showFragment(Fragment fragment, boolean replace) {
		visibleFragment = fragment;
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (!replace) transaction.add(R.id.container, fragment);
		else transaction.replace(R.id.container, fragment);
		transaction.addToBackStack("").commit();
	}


	@Override
	public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long id, IDrawerItem item) {
		drawer.closeDrawer();
		if (item.getIdentifier() == visibleFragmentId) return false;

		switch (item.getIdentifier()) {
			case ID_FEEDBACK:
				String address = getString(R.string.address);
				String subject = getString(
						R.string.feedback_mail_subject,
						getString(R.string.app_name));
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
				Intent mailer = Intent.createChooser(intent, getString(R.string.feedback_mail_chooser));
				startActivity(mailer);
				return false;

			case ID_SHOW_FACES:
				showFragment(new FacesOverviewFragment(), true);
				break;

			case ID_SHOW_VIDEOS:
				showFragment(new VideosFragment(), true);
				break;

			case ID_SETTINGS:
				showFragment(new SettingsFragment(), true);
				break;

			default:
				return false;
		}

		visibleFragmentId = item.getIdentifier();
		return true;
	}



	@Override
	public void onBackPressed() {
		if (backPressedListener != null) {
			boolean eventHandled = backPressedListener.onBackPressed();
			if (eventHandled) return;
		}
		super.onBackPressed();
	}


	public void setOnBackPressedListener(OnBackPressedListener listener) {
		this.backPressedListener = listener;
	}


	public void removeOnBackPressedListener() {
		this.backPressedListener = null;
	}


}
