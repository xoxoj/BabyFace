package org.faudroids.babyface.photo;


import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.CameraFragment;

/**
 * Activity which shows the camera preview etc.
 *
 * It "injects" a custom layout to show during photo capturing.
 */
public class CustomizedCameraActivity extends CameraActivity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// hide status bar
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
	}

	@Override
	protected CameraFragment buildFragment() {
		CameraFragment fragment = super.buildFragment();
		CustomizedCameraFragment customizedFragment = new CustomizedCameraFragment();
		customizedFragment.setArguments(fragment.getArguments());
		return customizedFragment;
	}


	public static class IntentBuilder extends AbstractCameraActivity.IntentBuilder {

		public IntentBuilder(Context context) {
			super(context, CustomizedCameraActivity.class);
		}

		public IntentBuilder skipConfirm() {
			result.putExtra(EXTRA_CONFIRM, false);
			return(this);
		}

	}

}
