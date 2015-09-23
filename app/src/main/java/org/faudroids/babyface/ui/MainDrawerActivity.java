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
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;


@ContentView(R.layout.activity_main)
public class MainDrawerActivity extends AbstractActivity implements Drawer.OnDrawerItemClickListener {

	private static final String
			STATE_FRAGMENT = "FRAGMENT";

	private static final int
			ID_NEW_ALBUM = 1,
			ID_SETTINGS = 2,
			ID_FEEDBACK = 3;

	private static final int
			REQUEST_ADD_FACE = 42;

	private Drawer drawer;
	private AccountHeader accountHeader;
	@Inject private AuthManager authManager;
	@Inject private FacesManager facesManager;

	private Fragment visibleFragment;


	public MainDrawerActivity() {
		super(true, true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FRAGMENT)) {
			visibleFragment = getFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT);
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


		// setup actual nav drawer
		List<IDrawerItem> drawerItems = facesToDrawerItems();
		DrawerBuilder drawerBuilder = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withAccountHeader(accountHeader)
				.addStickyDrawerItems(
						new PrimaryDrawerItem().withName(R.string.settings).withIconTintingEnabled(true).withIcon(R.drawable.ic_settings).withIdentifier(ID_SETTINGS),
						new PrimaryDrawerItem().withName(R.string.feedback).withIconTintingEnabled(true).withIcon(R.drawable.ic_email).withIdentifier(ID_FEEDBACK)
				)
				.withOnDrawerItemClickListener(this)
				.withSavedInstance(savedInstanceState);
		for (IDrawerItem item : drawerItems) drawerBuilder.addDrawerItems(item);
		drawerBuilder.addDrawerItems(createAddFaceDrawerItem());
		drawer = drawerBuilder.build();

		if (drawerItems.isEmpty()) {
			// add first face
			startActivityForResult(new Intent(this, NewFaceActivity.class), REQUEST_ADD_FACE);
		} else {
			// show first fragment
			if (visibleFragment == null) {
				Face face = (Face) drawerItems.get(0).getTag();
				showFragment(FaceFragment.newInstance(face), false);
			} else {
				showFragment(visibleFragment, true);
			}
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case REQUEST_ADD_FACE:
				List<IDrawerItem> drawerItems = facesToDrawerItems();

				if (drawerItems.isEmpty()) {
					// user choose to abort adding new face, "let them go"
					finish();
					return;
				}

				if (resultCode != RESULT_OK) return;
				Face newFace = Parcels.unwrap(data.getParcelableExtra(NewFaceActivity.EXTRA_FACE));

				drawer.removeAllItems();
				for (IDrawerItem item : drawerItems) drawer.addItem(item);
				drawer.addItem(createAddFaceDrawerItem());

				// select new face
				int selectedPos = 0;
				for (int i = 0; i < drawerItems.size(); ++i) {
					if (drawerItems.get(i).getTag().equals(newFace)) {
						selectedPos = i;
						break;
					}
				}
				drawer.setSelection(selectedPos);
				break;
		}
	}


	private List<IDrawerItem> facesToDrawerItems() {
		List<IDrawerItem> drawerItems = new ArrayList<>();
		for (Face face : facesManager.getFaces()) {
			drawerItems.add(new PrimaryDrawerItem()
					.withName(face.getName())
					.withIconTintingEnabled(true)
					.withIcon(R.drawable.ic_faces)
					.withTag(face));
		}
		return drawerItems;
	}


	private IDrawerItem createAddFaceDrawerItem() {
		return new PrimaryDrawerItem()
				.withName(getString(R.string.add_album))
				.withIconTintingEnabled(true)
				.withIcon(R.drawable.ic_add_white_36px)
				.withIdentifier(ID_NEW_ALBUM);
	}


	@Override
	public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long id, IDrawerItem item) {
		drawer.closeDrawer();

		// face fragments
		if (item.getTag() != null && (item.getTag() instanceof Face)) {
			showFragment(FaceFragment.newInstance((Face) item.getTag()), true);
			return true;
		}

		// new album + settings + about
		switch (item.getIdentifier()) {
			case ID_NEW_ALBUM:
				startActivityForResult(new Intent(this, NewFaceActivity.class), REQUEST_ADD_FACE);
				return true;

			case ID_FEEDBACK:
				String address = getString(R.string.address);
				String subject = getString(
						R.string.feedback_mail_subject,
						getString(R.string.app_name));
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
				Intent mailer = Intent.createChooser(intent, getString(R.string.feedback_mail_chooser));
				startActivity(mailer);
				return true;

			case ID_SETTINGS:
				showFragment(new SettingsFragment(), true);
				return true;

			default:
				return false;
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState = drawer.saveInstanceState(outState);
		outState = accountHeader.saveInstanceState(outState);
		if (visibleFragment != null) {
			getFragmentManager().putFragment(outState, STATE_FRAGMENT, visibleFragment);
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

}
