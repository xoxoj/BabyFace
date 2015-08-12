package org.faudroids.babyface.videos;


import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.roboguice.shaded.goole.common.base.Objects;

@JsonIgnoreProperties
public class VideoConversionStatus implements Parcelable {

	private final String videoId;
	private final boolean isComplete;
	private final Boolean isConversionSuccessful;
	private final float progress;

	@JsonCreator
	public VideoConversionStatus(
			@JsonProperty("videoId") String videoId,
			@JsonProperty("isComplete") boolean isComplete,
			@JsonProperty("isConversionSuccessful") Boolean isConversionSuccessful,
			@JsonProperty("progress") float progress) {

		this.videoId = videoId;
		this.isComplete = isComplete;
		this.isConversionSuccessful = isConversionSuccessful;
		this.progress = progress;
	}

	public String getVideoId() {
		return videoId;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public Boolean getIsConversionSuccessful() {
		return isConversionSuccessful;
	}

	public float getProgress() {
		return progress;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VideoConversionStatus that = (VideoConversionStatus) o;
		return Objects.equal(isComplete, that.isComplete) &&
				Objects.equal(videoId, that.videoId) &&
				Objects.equal(isConversionSuccessful, that.isConversionSuccessful) &&
				Objects.equal(progress, that.progress);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(videoId, isComplete, isConversionSuccessful, progress);
	}


	protected VideoConversionStatus(Parcel in) {
		videoId = in.readString();
		isComplete = in.readByte() != 0x00;
		byte isConversionSuccessfulVal = in.readByte();
		isConversionSuccessful = isConversionSuccessfulVal == 0x02 ? null : isConversionSuccessfulVal != 0x00;
		progress = in.readFloat();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(videoId);
		dest.writeByte((byte) (isComplete ? 0x01 : 0x00));
		if (isConversionSuccessful == null) {
			dest.writeByte((byte) (0x02));
		} else {
			dest.writeByte((byte) (isConversionSuccessful ? 0x01 : 0x00));
		}
		dest.writeFloat(progress);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<VideoConversionStatus> CREATOR = new Parcelable.Creator<VideoConversionStatus>() {
		@Override
		public VideoConversionStatus createFromParcel(Parcel in) {
			return new VideoConversionStatus(in);
		}

		@Override
		public VideoConversionStatus[] newArray(int size) {
			return new VideoConversionStatus[size];
		}
	};
}
