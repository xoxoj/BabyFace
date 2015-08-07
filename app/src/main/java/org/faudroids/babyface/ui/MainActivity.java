package org.faudroids.babyface.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.faudroids.babyface.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

	private static final int REQUEST_CAPTURE_IMAGE = 42;

	@InjectView(R.id.btn_camera) private Button cameraButton;
	@InjectView(R.id.btn_photo_count) private Button photoCountButton;

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
						imageFile = createImageFile();
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
				File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BabyFace");
				int photoCount = 0;
				for (File file : storageDir.listFiles()) {
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


	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BabyFace");
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdirs();
			if (!success) Timber.e("failed to create dir " + storageDir.getAbsolutePath());
		}
		return File.createTempFile(imageFileName, ".jpg", storageDir);
	}

}
