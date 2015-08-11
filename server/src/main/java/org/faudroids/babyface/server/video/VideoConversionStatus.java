package org.faudroids.babyface.server.video;


import java.io.File;

public class VideoConversionStatus {

	private final String videoId;

	private boolean isComplete;
	private File videoFile;
	private boolean isConversionSuccessful;

	public VideoConversionStatus(String videoId) {
		this.videoId = videoId;
		this.isComplete = false;
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

	public void setStatus(boolean isComplete, File videoFile, boolean isConversionSuccessful) {
		this.isComplete = isComplete;
		this.videoFile = videoFile;
		this.isConversionSuccessful = isConversionSuccessful;
	}

}
