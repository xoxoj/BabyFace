package org.faudroids.babyface.photo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commonsware.cwac.cam2.CameraFragment;
import com.github.clans.fab.FloatingActionButton;

import org.faudroids.babyface.R;

/**
 * Fragment which shows the camera preview etc.
 *
 * Extends the camera fragment of the camera lib to "inject" a custom layout.
 */
public class CustomizedCameraFragment extends CameraFragment {

	private boolean showingBackCamera = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(new LayoutInflaterAdapter(getActivity(), R.layout.fragment_camera, inflater), container, savedInstanceState);

		// take picture when click on right layout instead of small button only
		View btnLayout = view.findViewById(R.id.layout_btn);
		btnLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				performCameraAction();
			}
		});

		// switch camera icon when changing camera
		final FloatingActionButton libSwitchBtn = (FloatingActionButton) view.findViewById(R.id.cwac_cam2_switch_camera);
		View coverSwitchBtn = view.findViewById(R.id.btn_switch_camera);
		coverSwitchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showingBackCamera = !showingBackCamera;
				if (showingBackCamera) libSwitchBtn.setImageResource(R.drawable.ic_camera_front_white_24dp);
				else libSwitchBtn.setImageResource(R.drawable.ic_camera_rear_white_24dp);
				libSwitchBtn.callOnClick();
			}
		});

		return view;
	}


	/**
	 * HACK
	 *
	 * Intercepts the "inflate" method to inflate a different layout.
	 */
	private static class LayoutInflaterAdapter extends LayoutInflater {

		private final int layoutResource;
		private final LayoutInflater inflater;

		public LayoutInflaterAdapter(Context context, int layoutResource, LayoutInflater inflater) {
			super(context);
			this.layoutResource = layoutResource;
			this.inflater = inflater;
		}

		@Override
		public LayoutInflater cloneInContext(Context newContext) {
			throw new UnsupportedOperationException("stub");
		}

		@Override
		public View inflate(int resource, ViewGroup container, boolean attachToRoot) {
			return inflater.inflate(layoutResource, container, attachToRoot);
		}

	}
}
