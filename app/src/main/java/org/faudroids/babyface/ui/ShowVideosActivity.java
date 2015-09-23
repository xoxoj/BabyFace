package org.faudroids.babyface.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.Face;
import org.faudroids.babyface.faces.FacesManager;
import org.faudroids.babyface.videos.VideoInfo;
import org.faudroids.babyface.videos.VideoManager;
import org.parceler.Parcels;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_show_videos)
public class ShowVideosActivity extends AbstractActivity {

	public static final String EXTRA_FACE = "EXTRA_FACE";

	@InjectView(R.id.txt_empty) private TextView emptyView;
	@InjectView(R.id.list_videos) private RecyclerView videosView;
	private VideoAdapter videoAdapter;

	@Inject private FacesManager facesManager;
	@Inject private VideoManager videoManager;

	private Face face;


	public ShowVideosActivity() {
		super(true, false);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		face = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_FACE));
		setTitle(face.getName() + " " + getString(R.string.videos));

		videosView.setLayoutManager(new LinearLayoutManager(this));
		videoAdapter = new VideoAdapter();
		videosView.setAdapter(videoAdapter);
		videosView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
		setupVideos();
	}


	private void setupVideos() {
		List<VideoInfo> videos = videoManager.getVideosForFace(face);
		Collections.sort(videos, new Comparator<VideoInfo>() {
			@Override
			public int compare(VideoInfo lhs, VideoInfo rhs) {
				return lhs.getCreationDate().compareTo(rhs.getCreationDate());
			}
		});
		emptyView.setVisibility(videos.isEmpty() ? View.VISIBLE : View.GONE);
		videoAdapter.setVideos(videos);
	}


	private class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

		private final List<VideoInfo> videos = new ArrayList<>();

		public void setVideos(List<VideoInfo> videos) {
			this.videos.clear();
			this.videos.addAll(videos);
			notifyDataSetChanged();
		}

		@Override
		public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
			return new VideoViewHolder(view);
		}

		@Override
		public void onBindViewHolder(VideoViewHolder videoViewHolder, int position) {
			videoViewHolder.setVideo(videos.get(position));
		}

		@Override
		public int getItemCount() {
			return videos.size();
		}
	}


	private class VideoViewHolder extends RecyclerView.ViewHolder {

		private final View itemView;
		private final TextView dateTextView;

		public VideoViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			this.dateTextView = (TextView) itemView.findViewById(R.id.txt_date);
		}

		public void setVideo(final VideoInfo video) {
			dateTextView.setText(DateFormat.getDateTimeInstance().format(video.getCreationDate()));
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Uri videoUri = Uri.parse("file:///" + video.getVideoFile().getAbsolutePath());
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(videoUri, "video/mp4");
					startActivity(intent);
				}
			});
		}

	}

}
