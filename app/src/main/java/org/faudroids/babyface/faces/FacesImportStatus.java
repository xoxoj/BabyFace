package org.faudroids.babyface.faces;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.Transient;

/**
 * Describes the current status of importing faces.
 */
@Parcel(Parcel.Serialization.BEAN)
public class FacesImportStatus {

	private final int facesToImportCount;
	private int importedFacesCount;
	private boolean hasError = false;

	public FacesImportStatus(int facesToImportCount) {
		this.facesToImportCount = facesToImportCount;
	}

	@ParcelConstructor
	public FacesImportStatus(int facesToImportCount, int importedFacesCount) {
		this.facesToImportCount = facesToImportCount;
		this.importedFacesCount = importedFacesCount;
	}

	@Transient
	public float getProgress() {
		return ((float) importedFacesCount / facesToImportCount);
	}

	@Transient
	public boolean isComplete() {
		return importedFacesCount == facesToImportCount || hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	public void onFaceImported() {
		++importedFacesCount;
	}

	public int getFacesToImportCount() {
		return facesToImportCount;
	}

	public int getImportedFacesCount() {
		return importedFacesCount;
	}

	public boolean getHasError() {
		return hasError;
	}

}
