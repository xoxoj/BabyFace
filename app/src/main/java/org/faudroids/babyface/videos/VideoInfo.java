package org.faudroids.babyface.videos;

import org.faudroids.babyface.faces.Face;
import org.roboguice.shaded.goole.common.base.Objects;

import java.io.File;
import java.util.Date;

/**
 * Describes where a video is stored locally and which {@link org.faudroids.babyface.faces.Face}
 * is belongs to.
 */
public class VideoInfo {

	private final Face face;
	private final File videoFile;
	private final Date creationDate;

	public VideoInfo(Face face, File videoFile, Date creationDate) {
		this.face = face;
		this.videoFile = videoFile;
		this.creationDate = creationDate;
	}

	public Face getFace() {
		return face;
	}

	public File getVideoFile() {
		return videoFile;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VideoInfo videoInfo = (VideoInfo) o;
		return Objects.equal(face, videoInfo.face) &&
				Objects.equal(videoFile, videoInfo.videoFile) &&
				Objects.equal(creationDate, videoInfo.creationDate);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(face, videoFile, creationDate);
	}

}
