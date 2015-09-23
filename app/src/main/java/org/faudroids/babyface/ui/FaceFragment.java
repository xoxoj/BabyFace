package org.faudroids.babyface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.videos.VideoConversionService;
import org.parceler.Parcels;

import javax.inject.Inject;

import roboguice.inject.InjectView;

public class FaceFragment extends AbstractFragment {

	private static final int
			REQUEST_TAKE_PHOTO = 43,
			REQUEST_SHOW_SETTINGS = 44;

	private static final String EXTRA_FACE = "FACE";

	public static FaceFragment newInstance(Face face) {
		FaceFragment faceFragment = new FaceFragment();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_FACE, Parcels.wrap(face));
		faceFragment.setArguments(args);
		return faceFragment;
	}


	@Inject private PhotoManager photoManager;
	@Inject private PhotoUtils photoUtils;

    @InjectView(R.id.img_profile) private ImageView profileView;
	@InjectView(R.id.layout_take_photo) private View takePhotoView;
	@InjectView(R.id.layout_show_photos) private View showPhotosView;
	@InjectView(R.id.layout_create_movie) private View createMovieView;
	@InjectView(R.id.layout_show_movies) private View showMoviesView;
	@InjectView(R.id.layout_settings) private View settingsView;

	private Face face;

	public FaceFragment() {
		super(R.layout.fragment_face);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		face = Parcels.unwrap(getArguments().getParcelable(EXTRA_FACE));
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle(face.getName());
		setupSelectedFace(face);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO:
				if (resultCode != Activity.RESULT_OK) return;
				photoUtils.loadImage(photoManager.getRecentPhoto(face), profileView);
				break;

			case REQUEST_SHOW_SETTINGS:
				if (resultCode != FaceSettingsActivity.RESULT_FACE_DELETED) return;
				((MainDrawerActivity) getActivity()).onVisibleFaceDeleted();
				break;
		}
	}


	private void setupSelectedFace(final Face face) {
		this.face = face;
		photoUtils.loadImage(photoManager.getRecentPhoto(this.face), profileView);

		takePhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent capturePhotoIntent = new Intent(getActivity(), CapturePhotoActivity.class);
				capturePhotoIntent.putExtra(CapturePhotoActivity.EXTRA_FACE_NAME, FaceFragment.this.face.getName());
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

		showMoviesView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ShowVideosActivity.class);
				intent.putExtra(ShowVideosActivity.EXTRA_FACE, Parcels.wrap(face));
				startActivity(intent);
			}
		});

		settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				Intent settingsIntent = new Intent(getActivity(), FaceSettingsActivity.class);
				settingsIntent.putExtra(FaceSettingsActivity.EXTRA_FACE_NAME, FaceFragment.this.face.getName());
				startActivityForResult(settingsIntent, REQUEST_SHOW_SETTINGS);
				getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
			}
		});
	}

}
