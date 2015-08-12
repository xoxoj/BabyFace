package org.faudroids.babyface.server.video;


import java.io.File;

public class VideoConversionStatus {

	private final String videoId;

	private boolean isComplete;
	private File videoFile;
	private boolean isConversionSuccessful;
	private float downloadProgress, conversionProgress;

	public VideoConversionStatus(String videoId) {
		this.videoId = videoId;
		this.isComplete = false;
		this.downloadProgress = 0f;
		this.conversionProgress = 0f;
	}

	public String getVideoId() {
		return videoId;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public File getVideoFile() {
		return videoFile;
	}

	public boolean isConversionSuccessful() {
		return isConversionSuccessful;
	}

	public float getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(float downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	public float getConversionProgress() {
		return conversionProgress;
	}

	public void setConversionProgress(float conversionProgress) {
		this.conversionProgress = conversionProgress;
	}

	public void setStatus(boolean isComplete, File videoFile, boolean isConversionSuccessful) {
		this.isComplete = isComplete;
		this.videoFile = videoFile;
		this.isConversionSuccessful = isConversionSuccessful;
	}

}
