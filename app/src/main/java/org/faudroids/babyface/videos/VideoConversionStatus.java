package org.faudroids.babyface.videos;


import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.Transient;

import java.io.File;

@Parcel
public class VideoConversionStatus {

	private final float progress;
	private final File videoFile;
	private boolean isError;

	@ParcelConstructor
	public VideoConversionStatus(
			float progress,
			File videoFile,
			boolean isError) {

		this.progress = progress;
		this.videoFile = videoFile;
		this.isError = isError;
	}

	public float getProgress() {
		return progress;
	}

	public File getVideoFile() {
		return videoFile;
	}

	public boolean isError() {
		return isError;
	}

	@Transient
	public boolean isComplete() {
		return (videoFile != null) || isError;
	}

}
