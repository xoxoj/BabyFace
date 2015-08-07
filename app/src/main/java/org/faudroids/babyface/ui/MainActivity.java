package org.faudroids.babyface.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.faudroids.babyface.PhotoManager;
import org.faudroids.babyface.R;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

	private static final int REQUEST_CAPTURE_IMAGE = 42;

	@InjectView(R.id.btn_camera) private Button cameraButton;
	@InjectView(R.id.btn_photo_count) private Button photoCountButton;

	@Inject private PhotoManager photoManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup camera button
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (intent.resolveActivity(getPackageManager()) != null) {
					File imageFile;
					try {
						imageFile = photoManager.createImageFile();
						Timber.d("storing image as " + imageFile.getAbsolutePath());
					} catch (IOException ioe) {
						Timber.e(ioe, "failed to create image file");
						return;
					}

					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
					startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
				}
			}
		});

		// setup count photos button
		photoCountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int photoCount = 0;
				for (File file : photoManager.getRootStorageDir().listFiles()) {
					if (file.getName().toLowerCase().endsWith(".jpg")) ++photoCount;
				}
				Toast.makeText(MainActivity.this, "Found " + photoCount + " photos", Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CAPTURE_IMAGE:
				if (resultCode != RESULT_OK) return;
				Toast.makeText(this, "Image taking success", Toast.LENGTH_SHORT).show();
				break;
		}
	}

}
