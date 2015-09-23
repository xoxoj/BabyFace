package org.faudroids.babyface.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.photo.PhotoInfo;
import org.faudroids.babyface.photo.PhotoManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

/**
 * Displays all photos for one face.
 */
@ContentView(R.layout.activity_show_photos)
public class ShowPhotosActivity extends AbstractActivity {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	private final DateFormat dateFormat = DateFormat.getDateInstance();

	@InjectView(R.id.img_photo) private ImageView photoView;
	@InjectView(R.id.btn_delete) private ImageButton deleteButton;
	@InjectView(R.id.btn_edit) private ImageButton editButton;

	private PhotoAdapter photoAdapter;
	@InjectView(R.id.list_photos) private RecyclerView photosList;

	@Inject private PhotoManager photoManager;

	private PhotoInfo selectedPhoto;

	public ShowPhotosActivity() {
		super(true, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Face face = getIntent().getParcelableExtra(EXTRA_FACE);

		// prep photos list
		photosList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		photoAdapter = new PhotoAdapter();
		photosList.setAdapter(photoAdapter);
		// photosList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

		// get photos
		List<PhotoInfo> photos = photoManager.getPhotosForFace(face);
		if (!photos.isEmpty()) setSelectedPhoto(photos.get(0));
		setupPhotos(photos);

		// setup buttons
		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ShowPhotosActivity.this, "stub", Toast.LENGTH_SHORT).show();
			}
		});
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(ShowPhotosActivity.this)
						.setTitle(R.string.delete_photo_title)
						.setMessage(R.string.delete_photo_msg)
						.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								final PhotoInfo photoToDelete = selectedPhoto;

								// find closest photo to select
								List<PhotoInfo> photos = photoAdapter.getPhotos();
								int idx = photos.indexOf(photoToDelete);
								photos.remove(idx);
								if (idx >= photos.size()) --idx;
								if (idx < 0) setSelectedPhoto(null);
								else setSelectedPhoto(photos.get(idx));
								setupPhotos(photos);

								// actually delete photo
								photoManager.deletePhoto(photoToDelete);
								photoManager.requestPhotoSync();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}
		});
	}


	private void setupPhotos(List<PhotoInfo> photos) {
		Timber.d("loaded " + photos.size() + " photos");
		int actionVisibility = photos.isEmpty() ? View.GONE : View.VISIBLE;
		editButton.setVisibility(actionVisibility);
		deleteButton.setVisibility(actionVisibility);
		photoAdapter.setPhotos(photos);
	}


	private void setSelectedPhoto(PhotoInfo photo) {
		if (photo != null) Timber.d("selecting " + photo.getPhotoFile().getAbsolutePath());
		selectedPhoto = photo;
		if (photo == null) {
			photoView.setImageResource(android.R.color.transparent);
			setTitle("");
		} else {
			Picasso.with(this).load(photo.getPhotoFile()).into(photoView);
			setTitle(dateFormat.format(photo.getCreationDate()));
		}
	}


	private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

		private final List<PhotoInfo> photos = new ArrayList<>();

		public void setPhotos(List<PhotoInfo> photos) {
			this.photos.clear();
			this.photos.addAll(photos);
			notifyDataSetChanged();
		}

		public List<PhotoInfo> getPhotos() {
			return new ArrayList<>(photos);
		}

		@Override
		public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
			return new PhotoViewHolder(view);
		}

		@Override
		public void onBindViewHolder(PhotoViewHolder photoViewHolder, int position) {
			photoViewHolder.setPhoto(photos.get(position));
		}

		@Override
		public int getItemCount() {
			return photos.size();
		}
	}


	private class PhotoViewHolder extends RecyclerView.ViewHolder {

		private final ImageView photoView, photoRollView;

		public PhotoViewHolder(View itemView) {
			super(itemView);
			this.photoView = (ImageView) itemView.findViewById(R.id.img_photo);
			this.photoRollView = (ImageView) itemView.findViewById(R.id.img_photo_roll);
		}

		public void setPhoto(final PhotoInfo photo) {
			Picasso.with(ShowPhotosActivity.this).load(photo.getPhotoFile()).resizeDimen(R.dimen.photo_thumbnail_width, R.dimen.photo_thumbnail_height).centerCrop().into(photoView);
			photoView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setSelectedPhoto(photo);
					photoRollView.setImageResource(R.drawable.ic_movie_roll_overlay_selected);
					photoAdapter.notifyDataSetChanged();
				}
			});
			if (photo.equals(selectedPhoto)) photoRollView.setImageResource(R.drawable.ic_movie_roll_overlay_selected);
			else photoRollView.setImageResource(R.drawable.ic_movie_roll_overlay);
		}

	}

}
