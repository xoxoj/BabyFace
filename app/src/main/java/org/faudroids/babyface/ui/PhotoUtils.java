package org.faudroids.babyface.ui;


import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.faudroids.babyface.R;
import org.roboguice.shaded.goole.common.base.Optional;

import java.io.File;

import javax.inject.Inject;

import timber.log.Timber;

public class PhotoUtils {

	private final Context context;

	@Inject
	PhotoUtils(Context context) {
		this.context = context;
	}

	public void loadImage(Optional<File> photoFile, ImageView target) {
		loadImage(photoFile, target, R.dimen.profile_image_size_large);
	}


	public void loadImage(Optional<File> photoFile, ImageView target, int dimensResource) {
		RequestCreator requestCreator;
		if (photoFile.isPresent()) {
			Timber.d("loading image " + photoFile.get().getAbsolutePath());
			requestCreator = Picasso.with(context).load(photoFile.get());
		} else {
			requestCreator = Picasso.with(context).load(R.drawable.ic_person);
		}
		requestCreator
				.resizeDimen(dimensResource, dimensResource)
				.centerCrop()
				.into(target);
	}

}
