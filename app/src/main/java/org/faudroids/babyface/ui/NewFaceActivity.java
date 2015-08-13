package org.faudroids.babyface.ui;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.utils.DefaultTransformer;
import org.roboguice.shaded.goole.common.base.Optional;

import java.io.File;
import java.util.UUID;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;


@ContentView(R.layout.activity_new_face)
public class NewFaceActivity extends AbstractActivity {

	@InjectView(R.id.edit_name) private EditText nameEditText;
	@InjectView(R.id.layout_name) private TextInputLayout nameInputLayout;
	@InjectView(R.id.btn_add) private Button addButton;

	@Inject private FacesManager facesManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nameInputLayout.setErrorEnabled(true);

		// setup add button
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// check for empty name
				if (nameEditText.getText().toString().isEmpty()) {
					nameInputLayout.setError("Name must not be empty");
					return;
				}

				// add face
				Face face = new Face(UUID.randomUUID().toString(), nameEditText.getText().toString(), Optional.<File>absent());
				subscriptions.add(facesManager.addFace(face)
						.compose(new DefaultTransformer<Void>())
						.subscribe(new Action1<Void>() {
							@Override
							public void call(Void nothing) {
								setResult(RESULT_OK);
								finish();
							}
						}));
			}
		});
	}

}
