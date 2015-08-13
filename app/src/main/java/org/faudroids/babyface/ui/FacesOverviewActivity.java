package org.faudroids.babyface.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.roboguice.shaded.goole.common.base.Optional;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

@ContentView(R.layout.activity_faces_overview)
public class FacesOverviewActivity extends AbstractActivity {

	@InjectView(R.id.btn_add_face) private Button addFaceButton;
	@InjectView(R.id.btn_get_faces) private Button getFacesButton;
	@InjectView(R.id.btn_delete_faces) private Button deleteFacesButton;

	@Inject private FacesManager facesManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addFaceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				facesManager.addFace(new Face(UUID.randomUUID().toString(), "Bob", Optional.<File>absent()))
						.compose(new DefaultTransformer<Void>())
						.subscribe(new Action1<Void>() {
							@Override
							public void call(Void aVoid) {
								Timber.d("added faces successfully");
							}
						});
			}
		});

		getFacesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				facesManager.getFaces()
						.compose(new DefaultTransformer<List<Face>>())
						.subscribe(new Action1<List<Face>>() {
							@Override
							public void call(List<Face> faces) {
								Timber.d("found " + faces.size() + " faces");
								for (Face face : faces) {
									Timber.d(face.getId());
								}
							}
						});
			}
		});

		deleteFacesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				facesManager.deleteAllFaces()
						.compose(new DefaultTransformer<Void>())
						.subscribe(new Action1<Void>() {
							@Override
							public void call(Void aVoid) {
								Timber.d("deleted all faces");
							}
						});
			}
		});
	}
}
