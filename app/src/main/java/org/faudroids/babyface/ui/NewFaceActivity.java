package org.faudroids.babyface.ui;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.FacesManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_new_face)
public class NewFaceActivity extends AbstractActivity {

	@InjectView(R.id.layout_container) private RelativeLayout containerLayout;
	@InjectView(R.id.btn_continue) private ImageButton continueButton;

	private ProgressView progressView;
	private ImageView[] dotViews = new ImageView[3];
	private Progress currentProgress;

	private String newName;

	@Inject private FacesManager facesManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup progress
		this.dotViews = new ImageView[3];
		this.dotViews[0] = (ImageView) findViewById(R.id.img_dot1);
		this.dotViews[1] = (ImageView) findViewById(R.id.img_dot2);
		this.dotViews[2] = (ImageView) findViewById(R.id.img_dot3);

		// setup next button
		continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (progressView.onTryComplete()) {
					progressView.onComplete();
				}
			}
		});

		// start with first progress step
		setProgress(Progress.STATUS_1);
	}


	private void setProgress(Progress progress) {
		this.currentProgress = progress;

		// set progress indicator
		for (ImageView dotView : dotViews) {
			dotView.setImageResource(R.drawable.circle_not_selected);
		}
		dotViews[progress.idx].setImageResource(R.drawable.circle_selected);

		// setup progress content
		switch (progress) {
			// get name
			case STATUS_1:
				progressView = new ProgressView() {

					private EditText nameEditText;
					private TextInputLayout nameInputLayout;

					@Override
					public boolean onTryComplete() {
						// check for empty name
						if (nameEditText.getText().toString().isEmpty()) {
							nameInputLayout.setError("Name must not be empty");
							return false;
						}
						return true;
					}

					@Override
					protected View doCreateView(LayoutInflater inflater) {
						View view = inflater.inflate(R.layout.layout_new_face_step_1, containerLayout, false);
						this.nameEditText = (EditText) view.findViewById(R.id.edit_name);
						this.nameInputLayout = (TextInputLayout) view.findViewById(R.id.layout_name);
						this.nameInputLayout.setErrorEnabled(true);
						return view;
					}

					@Override
					protected void doOnComplete() {
						setProgress(Progress.STATUS_2);
						newName = nameEditText.getText().toString();
					}
				};

				break;
			case STATUS_2:
				progressView = new ProgressView() {
					@Override
					public boolean onTryComplete() {
						// TODO
						return false;
					}

					@Override
					protected void doOnComplete() {
						// TODO
					}

					@Override
					protected View doCreateView(LayoutInflater inflater) {
						View view = inflater.inflate(R.layout.layout_new_face_step_2, containerLayout, false);
						View cameraView = view.findViewById(R.id.img_camera);
						cameraView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Toast.makeText(NewFaceActivity.this, "hello", Toast.LENGTH_SHORT).show();
							}
						});
						return view;
					}
				};
				break;
			case STATUS_3:
				break;
		}

		// show layout
		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = progressView.createView(inflater);
		containerLayout.addView(contentView);
	}


	public enum Progress {

		STATUS_1(0), STATUS_2(1), STATUS_3(2);

		private final int idx;
		Progress(int idx) {
			this.idx = idx;
		}

	}


	private abstract class ProgressView {

		private View view;

		public final void onComplete() {
			doOnComplete();

			// animate sliding out
			Animation outAnimation = AnimationUtils.loadAnimation(NewFaceActivity.this, R.anim.slide_out);
			view.startAnimation(outAnimation);
			outAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) { }

				@Override
				public void onAnimationEnd(Animation animation) {
					containerLayout.post(new Runnable() {
						@Override
						public void run() {
							containerLayout.removeViewAt(0);
						}
					});
				}

				@Override
				public void onAnimationRepeat(Animation animation) { }
			});
		}

		public final View createView(LayoutInflater inflater) {
			view = doCreateView(inflater);
			Animation inAnimation = AnimationUtils.loadAnimation(NewFaceActivity.this, R.anim.slide_in);
			view.startAnimation(inAnimation);
			return view;
		}

		public abstract boolean onTryComplete();
		protected abstract void doOnComplete();
		protected abstract View doCreateView(LayoutInflater inflater);

	}


}
