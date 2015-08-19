package org.faudroids.babyface.faces;


import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Information about one face (person).
 */
public class Face implements Parcelable {

	private final String id;
	private final String name;
	private final long reminderPeriodInSeconds;

	@JsonCreator
	public Face(
			@JsonProperty("id") String id,
			@JsonProperty("name") String name,
			@JsonProperty("reminderPeriodInSeconds") long reminderPeriodInSeconds) {

		this.id = id;
		this.name = name;
		this.reminderPeriodInSeconds = reminderPeriodInSeconds;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getReminderPeriodInSeconds() {
		return reminderPeriodInSeconds;
	}

	@Override
	public String toString() {
		return "[id = " + id + ", name = " + name + ", reminderPeriodInSeconds = " + reminderPeriodInSeconds + "]";
	}

	protected Face(Parcel in) {
		id = in.readString();
		name = in.readString();
		reminderPeriodInSeconds = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeLong(reminderPeriodInSeconds);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Face> CREATOR = new Parcelable.Creator<Face>() {
		@Override
		public Face createFromParcel(Parcel in) {
			return new Face(in);
		}

		@Override
		public Face[] newArray(int size) {
			return new Face[size];
		}
	};

	public static class Builder {

		private final String id;
		private String name;
		private long reminderPeriodInSeconds;

		public Builder() {
			this.id = UUID.randomUUID().toString();
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setReminderPeriodInSeconds(long reminderPeriodInSeconds) {
			this.reminderPeriodInSeconds = reminderPeriodInSeconds;
			return this;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public long getReminderPeriodInSeconds() {
			return reminderPeriodInSeconds;
		}

		public Face build() {
			return new Face(id, name, reminderPeriodInSeconds);
		}

	}
}
