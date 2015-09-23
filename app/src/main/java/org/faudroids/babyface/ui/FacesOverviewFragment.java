package org.faudroids.babyface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.photo.ReminderManager;
import org.faudroids.babyface.utils.Pref;
import org.faudroids.babyface.videos.VideoConversionService;
import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class FacesOverviewFragment extends AbstractFragment implements ConnectionListener, OnBackPressedListener {

	private static final int
			REQUEST_ADD_FACE = 42,
			REQUEST_TAKE_PHOTO = 43,
			REQUEST_SHOW_SETTINGS = 44; // face might get deleted --> update UI

	private static final String STATE_FACE = "FACE";

	@InjectView(R.id.layout_sliding_panel) private SlidingUpPanelLayout slidingLayout;
	@InjectView(R.id.layout_profiles) private GridLayout facesLayout;

	@Inject private FacesManager facesManager;
	@Inject private PhotoManager photoManager;
	@Inject private ReminderManager reminderManager;
	@Inject private PhotoUtils photoUtils;
    @Inject private GoogleApiClientManager googleApiClientManager;

    @InjectView(R.id.img_profile) private ImageView profileView;
	@InjectView(R.id.txt_name) private TextView nameView;
	@InjectView(R.id.layout_take_photo) private View takePhotoView;
	@InjectView(R.id.layout_show_photos) private View showPhotosView;
	@InjectView(R.id.layout_create_movie) private View createMovieView;
	@InjectView(R.id.layout_settings) private View settingsView;
	private Face selectedFace;
	private PhotoManager.PhotoCreationResult photoCreationResult;
    private CompositeSubscription subscriptions = new CompositeSubscription();

	public FacesOverviewFragment() {
		super(R.layout.fragment_faces_overview);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle(R.string.children);
		slidingLayout.setPanelHeight(0);

		// on first start (and no faces) start face setup
		Pref<Boolean> firstStart = Pref.newBooleanPref(getActivity(), "org.faudroids.babyface.ui.FacesOverviewFragment", "firstStart", true);
		if (firstStart.get()) {
			firstStart.set(false);
			if (facesManager.getFaces().isEmpty()) {
				startActivityForResult(new Intent(getActivity(), NewFaceActivity.class), REQUEST_ADD_FACE);
			}
		}
	}


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_FACE, Parcels.wrap(selectedFace));
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            selectedFace = Parcels.unwrap(savedInstanceState.getParcelable(STATE_FACE));
        }
    }


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ADD_FACE:
				if (resultCode != Activity.RESULT_OK) return;
				setupFaces();
				break;

			case REQUEST_TAKE_PHOTO:
				if (resultCode != Activity.RESULT_OK) return;
				photoUtils.loadImage(photoManager.getRecentPhoto(selectedFace), profileView);
				break;

			case REQUEST_SHOW_SETTINGS:
				if (resultCode != FaceSettingsActivity.RESULT_FACE_DELETED) return;
				slidingLayout.post(new Runnable() {
					@Override
					public void run() {
						slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
					}
				});
				break;
		}
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_faces_overview, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_face:
				startActivityForResult(new Intent(getActivity(), NewFaceActivity.class), REQUEST_ADD_FACE);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onBackPressed() {
		if (!slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
			return false;
		}
		slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
		return true;
	}


	private void setupFaces() {
		List<Face> faces = facesManager.getFaces();
		Timber.d("loaded " + faces.size() + " faces");

		facesLayout.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(getActivity());

		// add profile layouts
		for (int i = 0; i < faces.size(); ++i) {
			// get view
			View profileView = inflater.inflate(R.layout.item_profile, facesLayout, false);
			final ImageView imageView = (ImageView) profileView.findViewById(R.id.img_profile);
			TextView nameView = (TextView) profileView.findViewById(R.id.txt_name);

			// fill face details
			final Face face = faces.get(i);
			nameView.setText(face.getName());
			photoUtils.loadImage(photoManager.getRecentPhoto(face), imageView);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
					setupSelectedFace(face);
				}
			});

			// add to grid view
			int row = i / 2;
			int column = i - (row * 2);
			GridLayout.LayoutParams params = createLayoutParams(row, column);
			facesLayout.addView(profileView, params);
		}
	}


	private void setupSelectedFace(final Face face) {
		this.selectedFace = face;
		nameView.setText(face.getName());
		photoUtils.loadImage(photoManager.getRecentPhoto(selectedFace), profileView);

		takePhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent capturePhotoIntent = new Intent(getActivity(), CapturePhotoActivity.class);
				capturePhotoIntent.putExtra(CapturePhotoActivity.EXTRA_FACE_NAME, selectedFace.getName());
				capturePhotoIntent.putExtra(CapturePhotoActivity.EXTRA_UPLOAD_PHOTO, true);
				startActivityForResult(capturePhotoIntent, REQUEST_TAKE_PHOTO);
			}
		});

		showPhotosView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent showPhotosIntent = new Intent(getActivity(), ShowPhotosActivity.class);
				showPhotosIntent.putExtra(ShowPhotosActivity.EXTRA_FACE, Parcels.wrap(face));
				startActivity(showPhotosIntent);
			}
		});

		createMovieView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(getActivity(), VideoConversionService.class);
				serviceIntent.putExtra(VideoConversionService.EXTRA_FACE, Parcels.wrap(face));
				getActivity().startService(serviceIntent);

				Intent activityIntent = new Intent(getActivity(), VideoConversionActivity.class);
				startActivity(activityIntent);
			}

		});

		settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				Intent settingsIntent = new Intent(getActivity(), FaceSettingsActivity.class);
				settingsIntent.putExtra(FaceSettingsActivity.EXTRA_FACE_NAME, selectedFace.getName());
				startActivityForResult(settingsIntent, REQUEST_SHOW_SETTINGS);
				getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
			}
		});
	}


	private GridLayout.LayoutParams createLayoutParams(int row, int column) {
		GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(column));
		params.width = (int) getResources().getDimension(R.dimen.profile_width);
		params.height = (int) getResources().getDimension(R.dimen.profile_height);
		return params;
	}


    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();
        googleApiClientManager.registerListener(this);
		((MainDrawerActivity) getActivity()).setOnBackPressedListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
        googleApiClientManager.unregisterListener(this);
		((MainDrawerActivity) getActivity()).removeOnBackPressedListener();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if(getActivity() != null) {
            setupFaces();
        }
    }


    @Override
    public void onConnectionSuspended(int i) { }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

}
