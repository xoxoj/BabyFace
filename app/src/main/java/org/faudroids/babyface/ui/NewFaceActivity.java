package org.faudroids.babyface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.photo.ReminderManager;
import org.faudroids.babyface.utils.DefaultTransformer;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;


@ContentView(R.layout.activity_new_face)
public class NewFaceActivity extends AbstractActivity implements NewFaceView.InputListener {

	private static final int REQUEST_CAPTURE_IMAGE = 42;

	@InjectView(R.id.layout_container) private RelativeLayout containerLayout;
	@InjectView(R.id.btn_continue) private ImageButton continueButton;

	private NewFaceView newFaceView;
	private ImageView[] dotViews;
	private Progress currentProgress;

	private Face.Builder faceBuilder = new Face.Builder();

	@Inject private FacesManager facesManager;
	@Inject private PhotoManager photoManager;
	@Inject private ReminderManager reminderManager;
	@Inject private PhotoUtils photoUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup progress
		this.dotViews = new ImageView[4];
		this.dotViews[0] = (ImageView) findViewById(R.id.img_dot1);
		this.dotViews[1] = (ImageView) findViewById(R.id.img_dot2);
		this.dotViews[2] = (ImageView) findViewById(R.id.img_dot3);
		this.dotViews[3] = (ImageView) findViewById(R.id.img_dot4);

		// setup next button
		continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setNextProgress();
			}
		});

		// start with first progress step
		setProgress(Progress.STATUS_1);
	}


	private void setNextProgress() {
		if (!currentProgress.equals(Progress.STATUS_4)) {
			newFaceView.onComplete();
			return;
		}

		// create final face
		final Face face = faceBuilder.build();
		facesManager.addFace(face)
				.compose(new DefaultTransformer<Void>())
				.subscribe(new Action1<Void>() {
					@Override
					public void call(Void aVoid) {
						Timber.d("adding face success");
						reminderManager.addReminder(face);
						finish();
					}
				});

		// start photo uploading
		photoManager.requestPhotoUpload();
	}


	private void setProgress(Progress progress) {
		this.currentProgress = progress;

		// set progress indicator
		for (ImageView dotView : dotViews) {
			dotView.setImageResource(R.drawable.circle_not_selected);
		}
		dotViews[progress.idx].setImageResource(R.drawable.circle_selected);

		// disable next button
		continueButton.setEnabled(false);

		// setup progress content
		switch (progress) {
			// get name
			case STATUS_1:
				newFaceView = createNameView();
				break;
			case STATUS_2:
				newFaceView = createPhotoView();
				break;
			case STATUS_3:
				newFaceView = createReminderView();
				break;
			case STATUS_4:
				newFaceView = createFinishView();
				break;
		}

		// show layout
		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = newFaceView.createView(inflater);
		containerLayout.addView(contentView);

		// toggle next button
		if (progress.equals(Progress.STATUS_4)) {
			continueButton.setEnabled(true);
			continueButton.setBackgroundResource(R.drawable.ic_check_with_background);
		} else {
			continueButton.setBackgroundResource(R.drawable.selector_next_button);
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CAPTURE_IMAGE:
				if (resultCode != RESULT_OK) return;
				newFaceView.onDataUpdated(data);
				break;
		}
	}


	@Override
	public void onInputChanged(boolean inputComplete) {
		continueButton.setEnabled(inputComplete);
	}


	public enum Progress {

		STATUS_1(0), STATUS_2(1), STATUS_3(2), STATUS_4(3);

		private final int idx;
		Progress(int idx) {
			this.idx = idx;
		}

	}


	/** Configures the name */
	private NewFaceView createNameView() {
		return new NewFaceView(NewFaceActivity.this, containerLayout, faceBuilder, this) {

			private EditText nameEditText;

			@Override
			protected View doCreateView(LayoutInflater inflater) {
				View view = inflater.inflate(R.layout.layout_new_face_step_1, containerLayout, false);
				nameEditText = (EditText) view.findViewById(R.id.edit_name);
				nameEditText.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) { }

					@Override
					public void afterTextChanged(Editable s) {
						inputListener.onInputChanged(!nameEditText.getText().toString().isEmpty());
					}
				});
				nameEditText.setOnKeyListener(new View.OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
							if (nameEditText.getText().toString().isEmpty()) return false;
							setNextProgress();
						}
						return false;
					}
				});
				return view;
			}

			@Override
			protected void doOnComplete() {
				faceBuilder.setName(nameEditText.getText().toString());
				setProgress(Progress.STATUS_2);
			}
		};
	}


	/** Takes the first photo */
	private NewFaceView createPhotoView() {
		return new NewFaceView(NewFaceActivity.this, containerLayout, faceBuilder, this) {

			private ImageView cameraView, photoView;

			@Override
			protected void doOnComplete() {
				setProgress(Progress.STATUS_3);
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
						Intent capturePhotoIntent = new Intent(NewFaceActivity.this, CapturePhotoActivity.class);
						capturePhotoIntent.putExtra(CapturePhotoActivity.EXTRA_FACE_ID, faceBuilder.getId());
						startActivityForResult(capturePhotoIntent, REQUEST_CAPTURE_IMAGE);
					}
				});
				return view;
			}

			@Override
			public void onDataUpdated(Intent data) {

				// toggle preview of photo
				if (photoManager.getRecentPhoto(faceBuilder.getId()).isPresent()) {
					cameraView.setVisibility(View.GONE);
					photoView.setVisibility(View.VISIBLE);
					photoUtils.loadImage(photoManager.getRecentPhoto(faceBuilder.getId()), photoView, R.dimen.profile_image_size_extra_large);
				} else {
					cameraView.setVisibility(View.VISIBLE);
					photoView.setVisibility(View.GONE);
				}

				inputListener.onInputChanged(photoManager.getRecentPhoto(faceBuilder.getId()).isPresent());
			}

		};
	}


	/** Configures the reminder period */
	private NewFaceView createReminderView() {
		return new NewFaceView(NewFaceActivity.this, containerLayout, faceBuilder, this) {

			private ViewGroup[] regularRowView = new ViewGroup[4];
			private ViewGroup[] customRowViews = new ViewGroup[4];
			private View regularView, customView;
			private EditText amountEditText;

			private int selectedIdx;

			@Override
			protected void doOnComplete() {
				long updatePeriod = Long.MAX_VALUE;

				if (customView.getVisibility() == View.GONE) {
					switch (selectedIdx) {
						case 0: // one day
							updatePeriod = 60 * 60 * 24;
							break;
						case 1: // one week
							updatePeriod = 60 * 60 * 24 * 7;
							break;
						case 2: // one month
							updatePeriod = 60 * 60 * 24 * 30;
							break;
					}
				} else {
					int amount = Integer.valueOf(amountEditText.getText().toString());
					int multiplier = 0;
					switch (selectedIdx) {
						case 0: // hours
							multiplier = 60 * 60;
							break;
						case 1: // days
							multiplier = 60 * 60 * 24;
							break;
						case 2: // weeks
							multiplier = 60 * 60 * 24 * 7;
							break;
						case 3: // months
							multiplier = 60 * 60 * 24 * 30;
							break;
					}
					updatePeriod = amount * multiplier;
				}
				Timber.d("setting reminder period to " + updatePeriod);
				faceBuilder.setReminderPeriodInSeconds(updatePeriod);
				setProgress(Progress.STATUS_4);
			}

			@Override
			protected View doCreateView(LayoutInflater inflater) {
				View view = inflater.inflate(R.layout.layout_new_face_step_3, containerLayout, false);

				regularView = view.findViewById(R.id.layout_regular);
				regularRowView[0] = (ViewGroup) view.findViewById(R.id.row_1);
				regularRowView[1] = (ViewGroup) view.findViewById(R.id.row_2);
				regularRowView[2] = (ViewGroup) view.findViewById(R.id.row_3);
				regularRowView[3] = (ViewGroup) view.findViewById(R.id.row_4);

				customView = view.findViewById(R.id.layout_custom);
				amountEditText = (EditText) view.findViewById(R.id.edit_amount);
				customRowViews[0] = (ViewGroup) view.findViewById(R.id.row_hours);
				customRowViews[1] = (ViewGroup) view.findViewById(R.id.row_days);
				customRowViews[2] = (ViewGroup) view.findViewById(R.id.row_weeks);
				customRowViews[3] = (ViewGroup) view.findViewById(R.id.row_months);

				// setup on clicks
				for (int idx = 0; idx < regularRowView.length; ++idx) {
					final int clickedIdx = idx;
					regularRowView[idx].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// switch to custom view
							if (clickedIdx == 3) {
								customView.setVisibility(View.VISIBLE);
								regularView.setVisibility(View.GONE);
								toggleSelected(customRowViews, 1);
								amountEditText.requestFocus();
								amountEditText.selectAll();
								return;
							}
							toggleSelected(regularRowView, clickedIdx);
						}
					});
					customRowViews[idx].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							toggleSelected(customRowViews, clickedIdx);

						}
					});

					// always complete
					inputListener.onInputChanged(true);
				}

				// set "every week" by default
				toggleSelected(regularRowView, 1);

				return view;
			}

			private void toggleSelected(ViewGroup[] rowViews, int selectedIdx) {
				this.selectedIdx = selectedIdx;
				for (int idx = 0; idx < rowViews.length; ++idx) {
					int txtColor = (idx == selectedIdx) ? getResources().getColor(R.color.accent) : getResources().getColor(android.R.color.white);
					int imgVisibility = (idx == selectedIdx) ? View.VISIBLE : View.GONE;
					((TextView) rowViews[idx].getChildAt(0)).setTextColor(txtColor);
					rowViews[idx].getChildAt(1).setVisibility(imgVisibility);
				}
			}
		};
	}


	/** Configures the reminder period */
	private NewFaceView createFinishView() {
		return new NewFaceView(NewFaceActivity.this, containerLayout, faceBuilder, this) {

			@Override
			protected void doOnComplete() { }

			@Override
			protected View doCreateView(LayoutInflater inflater) {
				return inflater.inflate(R.layout.layout_new_face_step_4, containerLayout, false);
			}
		};

	}

}
