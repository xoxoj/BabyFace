package org.faudroids.babyface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.utils.DefaultTransformer;

import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

@ContentView(R.layout.activity_faces_overview)
public class FacesOverviewActivity extends AbstractActivity {

	private static final int REQUEST_ADD_FACE = 42;

	@InjectView(R.id.btn_add_face) private FloatingActionButton addFaceButton;
	@InjectView(R.id.layout_profiles) private GridLayout facesLayout;

	@Inject private FacesManager facesManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup add face button
		addFaceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(FacesOverviewActivity.this, NewFaceActivity.class), REQUEST_ADD_FACE);
			}
		});

		setupFaces();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ADD_FACE:
				if (resultCode != RESULT_OK) return;
				setupFaces();
		}
	}


	private void setupFaces() {
		subscriptions.add(facesManager.getFaces()
				.compose(new DefaultTransformer<List<Face>>())
				.subscribe(new Action1<List<Face>>() {
					@Override
					public void call(List<Face> faces) {
						Timber.d("loaded " + faces.size() + " faces");

						facesLayout.removeAllViews();
						LayoutInflater inflater = LayoutInflater.from(FacesOverviewActivity.this);
						for (int i = 0; i < faces.size(); ++i) {
							// get view
							View profileView = inflater.inflate(R.layout.item_profile, facesLayout, false);
							ImageView imageView = (ImageView) profileView.findViewById(R.id.img_profile);
							TextView nameView = (TextView) profileView.findViewById(R.id.txt_name);

							// fill face details
							Face face = faces.get(i);
							nameView.setText(face.getName());
							if (!face.getMostRecentPhotoFile().isPresent()) {
								Picasso.with(FacesOverviewActivity.this).load(R.drawable.ic_person).transform(new CircleTransformation()).into(imageView);
							} else {
								Picasso.with(FacesOverviewActivity.this).load(face.getMostRecentPhotoFile().get()).transform(new CircleTransformation()).into(imageView);
							}
							imageView.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									startActivity(new Intent(FacesOverviewActivity.this, FaceOverviewActivity.class));
								}
							});

							// add to grid view
							int row = i / 2;
							int column = i - (row * 2);
							GridLayout.Spec rowSpec = GridLayout.spec(row);
							GridLayout.Spec columnSpec = GridLayout.spec(column);
							GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
							facesLayout.addView(profileView, params);
						}
					}
				}));
	}

}
