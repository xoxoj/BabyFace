package org.faudroids.babyface.ui;

import android.os.Bundle;

import org.faudroids.babyface.R;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_face_overview)
public class FaceOverviewActivity extends AbstractActivity {

	public FaceOverviewActivity() {
		super(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("");
	}

}
