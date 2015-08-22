package org.faudroids.babyface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.google.ConnectionListener;
import org.faudroids.babyface.google.GoogleApiClientManager;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.photo.ReminderManager;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.faudroids.babyface.videos.VideoConversionService;

import java.util.List;

import javax.inject.Inject;

import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class FacesOverviewFragment extends RoboFragment implements ConnectionListener, OnBackPressedListener {

	private static final int
			REQUEST_ADD_FACE = 42,
			REQUEST_TAKE_PHOTO = 43;

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
	@InjectView(R.id.layout_create_movie) private View createMovieView;
	@InjectView(R.id.layout_settings) private View settingsView;
	private Face selectedFace;
	private PhotoManager.PhotoCreationResult photoCreationResult;
    private CompositeSubscription subscriptions = new CompositeSubscription();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faces_overview, container, false);
    }


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		slidingLayout.setPanelHeight(0);
	}



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_FACE, selectedFace);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            selectedFace = savedInstanceState.getParcelable(STATE_FACE);
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
				photoUtils.loadImage(photoManager.getRecentPhoto(selectedFace.getId()), profileView);
		}
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
		subscriptions.add(facesManager.getFaces()
				.compose(new DefaultTransformer<List<Face>>())
				.subscribe(new Action1<List<Face>>() {
					@Override
					public void call(List<Face> faces) {
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
							photoUtils.loadImage(photoManager.getRecentPhoto(face.getId()), imageView);
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

						// add "add profile" layout
						View addProfileView = inflater.inflate(R.layout.item_profile_add, facesLayout, false);
						int row = faces.size() / 2;
						int column = faces.size() - (row * 2);
						GridLayout.LayoutParams params = createLayoutParams(row, column);
						facesLayout.addView(addProfileView, params);
						addProfileView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								startActivityForResult(new Intent(getActivity(), NewFaceActivity.class), REQUEST_ADD_FACE);
							}
						});

					}
				}));
	}


	private void setupSelectedFace(final Face face) {
		this.selectedFace = face;
		nameView.setText(face.getName());
		photoUtils.loadImage(photoManager.getRecentPhoto(selectedFace.getId()), profileView);

		takePhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent capturePhotoIntent = new Intent(getActivity(), CapturePhotoActivity.class);
				capturePhotoIntent.putExtra(CapturePhotoActivity.EXTRA_FACE_ID, selectedFace.getId());
				capturePhotoIntent.putExtra(CapturePhotoActivity.EXTRA_UPLOAD_PHOTO, true);
				startActivityForResult(capturePhotoIntent, REQUEST_TAKE_PHOTO);
			}
		});

		createMovieView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(getActivity(), VideoConversionService.class);
				serviceIntent.putExtra(VideoConversionService.EXTRA_FACE, face);
				getActivity().startService(serviceIntent);

				Intent activityIntent = new Intent(getActivity(), VideoConversionActivity.class);
				activityIntent.putExtra(VideoConversionActivity.EXTRA_FACE, face);
				startActivity(activityIntent);
			}

		});

		settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				Toast.makeText(getActivity(), "Dummy", Toast.LENGTH_SHORT).show();
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
