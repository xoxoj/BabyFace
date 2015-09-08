package org.faudroids.babyface.ui;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.photo.PhotoManager;
import org.faudroids.babyface.utils.DefaultTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Displays all photos for one face.
 */
@ContentView(R.layout.activity_show_photos)
public class ShowPhotosActivity extends AbstractActivity {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	@InjectView(R.id.img_photo) private ImageView photoView;
	private PhotoAdapter photoAdapter;
	@InjectView(R.id.list_photos) private RecyclerView photosList;

	@Inject private PhotoManager photoManager;

	private File selectedPhotoFile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Face face = getIntent().getParcelableExtra(EXTRA_FACE);

		// prep photos list
		photosList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		photoAdapter = new PhotoAdapter();
		photosList.setAdapter(photoAdapter);
		// photosList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

		// download photos
		photoManager
				.getPhotosForFace(face)
				.compose(new DefaultTransformer<List<File>>())
				.subscribe(new Action1<List<File>>() {
					@Override
					public void call(List<File> photoFiles) {
						Timber.d("loaded " + photoFiles.size() + " photos");
						if (!photoFiles.isEmpty()) setSelectedPhotoFile(photoFiles.get(0));
						photoAdapter.setPhotoFiles(photoFiles);
					}
				});
	}


	private void setSelectedPhotoFile(File photoFile) {
		selectedPhotoFile = photoFile;
		Picasso.with(this).load(selectedPhotoFile).into(photoView);
	}


	private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

		private final List<File> photoFiles = new ArrayList<>();

		public void setPhotoFiles(List<File> photoFiles) {
			this.photoFiles.clear();
			this.photoFiles.addAll(photoFiles);
			notifyDataSetChanged();
		}

		@Override
		public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
			return new PhotoViewHolder(view);
		}

		@Override
		public void onBindViewHolder(PhotoViewHolder photoViewHolder, int position) {
			photoViewHolder.setPhoto(photoFiles.get(position));
		}

		@Override
		public int getItemCount() {
			return photoFiles.size();
		}
	}


	private class PhotoViewHolder extends RecyclerView.ViewHolder {

		private final ImageView photoView, photoRollView;

		public PhotoViewHolder(View itemView) {
			super(itemView);
			this.photoView = (ImageView) itemView.findViewById(R.id.img_photo);
			this.photoRollView = (ImageView) itemView.findViewById(R.id.img_photo_roll);
		}

		public void setPhoto(final File photoFile) {
			Picasso.with(ShowPhotosActivity.this).load(photoFile).resizeDimen(R.dimen.photo_thumbnail_width, R.dimen.photo_thumbnail_height).centerCrop().into(photoView);
			photoView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setSelectedPhotoFile(photoFile);
					photoRollView.setImageResource(R.drawable.ic_movie_roll_overlay_selected);
					photoAdapter.notifyDataSetChanged();
				}
			});
			if (photoFile.equals(selectedPhotoFile)) photoRollView.setImageResource(R.drawable.ic_movie_roll_overlay_selected);
			else photoRollView.setImageResource(R.drawable.ic_movie_roll_overlay);
		}

	}

}
