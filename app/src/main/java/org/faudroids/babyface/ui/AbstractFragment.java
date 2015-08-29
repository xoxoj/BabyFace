package org.faudroids.babyface.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.faudroids.babyface.R;

import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

abstract class AbstractFragment extends RoboFragment {

	private final int layoutResource;
	@InjectView(R.id.progressbar) private CircleProgressBar progressBar;

	public AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}

	protected void showProgressBar() {
		progressBar.setVisibility(View.VISIBLE);
	}

	protected void hideProgressBar() {
		progressBar.setVisibility(View.GONE);
	}

}
