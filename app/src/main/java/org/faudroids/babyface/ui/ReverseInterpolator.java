package org.faudroids.babyface.ui;

import android.view.animation.Interpolator;

/**
 * Reverses a given interpolator.
 */
public class ReverseInterpolator implements Interpolator {

	private final Interpolator interpolator;

	public ReverseInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	@Override
	public float getInterpolation(float input) {
		return 1 - interpolator.getInterpolation(input);
	}

}
