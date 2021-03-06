package org.faudroids.babyface.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;


/**
 * A view for configuring a new face instance. Can be chained to produce
 * a "setup wizard".
 */
abstract class NewFaceView {

	private final Context context;
	private final ViewGroup containerView;
	private final Face.Builder faceBuilder;
	protected final InputListener inputListener;

	private View view;

	public NewFaceView(Context context, ViewGroup containerView, Face.Builder faceBuilder, InputListener inputListener) {
		this.context = context;
		this.containerView = containerView;
		this.faceBuilder = faceBuilder;
		this.inputListener = inputListener;
	}

	/**
	 * Called when this setup step is complete.
	 */
	public final void onComplete() {
		doOnComplete();

		// animate sliding out
		Animation outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_left);
		view.startAnimation(outAnimation);
		outAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				containerView.post(new Runnable() {
					@Override
					public void run() {
						containerView.removeViewAt(0);
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
	}

	/**
	 * Called once to setup this view.
	 */
	public final View createView(LayoutInflater inflater) {
		view = doCreateView(inflater);
		Animation inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right);
		view.startAnimation(inAnimation);
		return view;
	}

	/**
	 * Called when this view should update itself (e.g. on external events).
	 */
	public void onDataUpdated() {
		// default is empty
	}

	protected abstract void doOnComplete();
	protected abstract View doCreateView(LayoutInflater inflater);


	public interface InputListener {

		void onInputChanged(boolean inputComplete);

	}

}
