package org.faudroids.babyface.ui;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import roboguice.fragment.provided.RoboFragment;

abstract class AbstractFragment extends RoboFragment {

	private final int layoutResource;

	public AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof AbstractActivity)) throw new IllegalStateException("activity must extend " + AbstractActivity.class.getName());
	}

	protected void showProgressBar() {
		((AbstractActivity) getActivity()).showProgressBar();
	}

	protected void hideProgressBar() {
		((AbstractActivity) getActivity()).hideProgressBar();
	}

}
