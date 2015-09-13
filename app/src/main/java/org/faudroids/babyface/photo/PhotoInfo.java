package org.faudroids.babyface.photo;

import org.faudroids.babyface.faces.Face;
import org.roboguice.shaded.goole.common.base.Objects;

import java.io.File;
import java.util.Date;

/**
 * Describes where a photo is stored locally and which {@link org.faudroids.babyface.faces.Face}
 * is belongs to.
 * Supports comparison via the creation date.
 */
public class PhotoInfo implements Comparable<PhotoInfo> {

	private final Face face;
	private final File photoFile;
	private final Date creationDate;

	public PhotoInfo(Face face, File photoFile, Date creationDate) {
		this.face = face;
		this.photoFile = photoFile;
		this.creationDate = creationDate;
	}

	public Face getFace() {
		return face;
	}

	public File getPhotoFile() {
		return photoFile;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PhotoInfo info = (PhotoInfo) o;
		return Objects.equal(face, info.face) &&
				Objects.equal(photoFile, info.photoFile) &&
				Objects.equal(creationDate, info.creationDate);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(face, photoFile, creationDate);
	}

	@Override
	public int compareTo(PhotoInfo another) {
		return creationDate.compareTo(another.creationDate);
	}

}
