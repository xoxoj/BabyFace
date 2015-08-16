package org.faudroids.babyface.ui;

import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.Window;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_face_overview)
public class FaceOverviewActivity extends AbstractActivity {

	@InjectView(R.id.img_profile) private ImageView profileImageView;

	public FaceOverviewActivity() {
		super(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// set enter / exit animations (before setting content!)
		if (Build.VERSION.SDK_INT >= 21) {
			getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
			Transition transition = new Slide(Gravity.BOTTOM);
			transition.excludeTarget(android.R.id.statusBarBackground, true);
			transition.excludeTarget(R.id.toolbar, true);
			getWindow().setEnterTransition(transition);
			getWindow().setReturnTransition(transition);
		}

		super.onCreate(savedInstanceState);
		setTitle("");

		Picasso.with(FaceOverviewActivity.this)
				.load(R.drawable.ic_person)
				.transform(new CircleTransformation(
						getResources().getColor(R.color.primary),
						getResources().getColor(R.color.primary_very_dark)))
				.into(profileImageView);
	}

}
