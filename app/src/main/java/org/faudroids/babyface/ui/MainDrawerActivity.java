package org.faudroids.babyface.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
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
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.utils.DefaultTransformer;

import roboguice.inject.ContentView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import timber.log.Timber;


@ContentView(R.layout.activity_main)
public class MainDrawerActivity extends AbstractActivity implements Drawer.OnDrawerItemClickListener, ConnectionListener {

	private static final String STATE_FRAGMENT = "FRAGMENT";

    private static final int REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION = 43;

	private static final int
			ID_SHOW_FACES = 0,
			ID_SHOW_VIDEOS = 1,
			ID_SETTINGS = 2,
			ID_FEEDBACK = 3,
            ID_ABOUT = 4;

	private Drawer drawer;

	private int visibleFragmentId;
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
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (visibleFragment != null) {
			getFragmentManager().putFragment(outState, STATE_FRAGMENT, visibleFragment);
		}
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
				// TODO
				break;

			case ID_SETTINGS:
				// TODO
				break;

            case ID_ABOUT:
                showFragment(new AboutFragment(), true);

			default:
				return false;
		}

		visibleFragmentId = item.getIdentifier();
		return true;
	}


    @Override
    public void onStart() {
        super.onStart();
        googleApiClientManager.registerListener(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        googleApiClientManager.unregisterListener(this);
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


    @Override
    public void onConnected(Bundle bundle) {

        Person person = Plus.PeopleApi.getCurrentPerson(googleApiClientManager.getGoogleApiClient());
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
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                        //.withHeaderBackground(R.drawable.background)

                .addProfiles(
                        new ProfileDrawerItem().withName(person.getName().toString())
                                .withEmail(Plus.AccountApi.getAccountName
                                        (googleApiClientManager.getGoogleApiClient()))
                                .withIcon(Uri.parse(person.getImage().getUrl()).buildUpon().
                                        clearQuery().build().toString())
                )

                .withProfileImagesClickable(false)
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        // setup actual nav drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.faces).withIconTintingEnabled(true).withIcon(R.drawable.ic_faces).withIdentifier(ID_SHOW_FACES),
                        new PrimaryDrawerItem().withName(R.string.video).withIconTintingEnabled(true).withIcon(R.drawable.ic_movie).withIdentifier(ID_SHOW_VIDEOS)
                )
                .addStickyDrawerItems(
                        new PrimaryDrawerItem().withName("About").withIconTintingEnabled(true).withIcon(R.drawable.ic_about).withIdentifier(ID_ABOUT),
                        new PrimaryDrawerItem().withName(R.string.settings).withIconTintingEnabled(true).withIcon(R.drawable.ic_settings).withIdentifier(ID_SETTINGS),
                        new PrimaryDrawerItem().withName(R.string.feedback).withIconTintingEnabled(true).withIcon(R.drawable.ic_email).withIdentifier(ID_FEEDBACK)
                )
                .withOnDrawerItemClickListener(this)
                .build();

		if (visibleFragment == null) showFragment(new FacesOverviewFragment(), false);
		else showFragment(visibleFragment, true);
        visibleFragmentId = ID_SHOW_FACES;

		// TODO
		printGoogleAuthToken();
    }


    @Override
    public void onConnectionSuspended(int i) { }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // missing play services etc.
        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), MainDrawerActivity.this, 0).show();
            return;
        }

        // try resolving error
        try {
            connectionResult.startResolutionForResult(MainDrawerActivity.this, REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION);
        } catch (IntentSender.SendIntentException e) {
            Timber.e(e, "failed to resolve connection error");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESOLVE_GOOGLE_API_CLIENT_CONNECTION:
                googleApiClientManager.getGoogleApiClient().connect();
                break;
        }
    }


	private void printGoogleAuthToken() {
		Observable
				.defer(new Func0<Observable<String>>() {
					@Override
					public Observable<String> call() {
						try {
							String scope = "oauth2:" + Drive.SCOPE_APPFOLDER.toString();
							Timber.d("scope is " + scope);
							String token = GoogleAuthUtil.getToken(
									MainDrawerActivity.this,
									Plus.AccountApi.getAccountName(googleApiClientManager.getGoogleApiClient()),
									scope);
							return Observable.just(token);
						} catch (Exception e) {
							return Observable.error(e);
						}
					}
				})
				.compose(new DefaultTransformer<String>())
				.subscribe(new Action1<String>() {
					@Override
					public void call(String token) {
						Timber.d("google auth token: " + token);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to get token");
					}
				});


	}

}
