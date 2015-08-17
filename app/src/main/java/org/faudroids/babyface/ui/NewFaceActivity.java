package org.faudroids.babyface.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.photo.PhotoManager;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;


@ContentView(R.layout.activity_new_face)
public class NewFaceActivity extends AbstractActivity {

	private static final int REQUEST_CAPTURE_IMAGE = 42;

	@InjectView(R.id.layout_container) private RelativeLayout containerLayout;
	@InjectView(R.id.btn_continue) private ImageButton continueButton;

	private ProgressView progressView;
	private ImageView[] dotViews = new ImageView[3];
	private Progress currentProgress;

	// values needed to create a new face
	private String newName;
	private File photoFile;


	@Inject private FacesManager facesManager;
	@Inject private PhotoManager photoManager;

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

					private ImageView cameraView, photoView;

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
						cameraView = (ImageView) view.findViewById(R.id.img_camera);
						photoView = (ImageView) view.findViewById(R.id.img_photo);

						// click to start camera
						cameraView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								if (intent.resolveActivity(getPackageManager()) != null) {
									try {
										photoFile = photoManager.createImageFile();
										Timber.d("storing image as " + photoFile.getAbsolutePath());
									} catch (IOException ioe) {
										Timber.e(ioe, "failed to create image file");
										return;
									}

									intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
									startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
								}
							}
						});
						return view;
					}

					@Override
					public void onDataUpdated() {
						// toggle preview of photo
						if (photoFile != null) {
							cameraView.setVisibility(View.GONE);
							photoView.setVisibility(View.VISIBLE);
							Picasso.with(NewFaceActivity.this).load(photoFile).into(photoView);
						} else {
							cameraView.setVisibility(View.VISIBLE);
							photoView.setVisibility(View.GONE);
						}
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


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CAPTURE_IMAGE:
				if (resultCode != RESULT_OK) {
					photoFile = null;
					return;
				}
				progressView.onDataUpdated();
				break;
		}
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
		public void onDataUpdated() {
			// default is empty
		}
		protected abstract void doOnComplete();
		protected abstract View doCreateView(LayoutInflater inflater);

	}


}
