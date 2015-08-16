package org.faudroids.babyface.ui;


import android.view.View;
import android.widget.ImageView;

import org.faudroids.babyface.R;

public class ProgressBarUtils {

	private final ImageView[] dotViews;

	public ProgressBarUtils(View progressBarView) {
		this.dotViews = new ImageView[3];
		this.dotViews[0] = (ImageView) progressBarView.findViewById(R.id.img_dot1);
		this.dotViews[1] = (ImageView) progressBarView.findViewById(R.id.img_dot2);
		this.dotViews[2] = (ImageView) progressBarView.findViewById(R.id.img_dot3);
	}

	public void setProgress(Progress progress) {
		for (ImageView dotView : dotViews) {
			dotView.setImageResource(R.drawable.circle_not_selected);
		}
		dotViews[progress.idx].setImageResource(R.drawable.circle_selected);
	}


	public static enum Progress {

		STATUS_1(0), STATUS_2(1), STATUS_3(2);

		private final int idx;

		private Progress(int idx) {
			this.idx = idx;
		}

	}
}
