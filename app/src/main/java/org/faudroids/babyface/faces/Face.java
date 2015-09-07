package org.faudroids.babyface.faces;


import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.roboguice.shaded.goole.common.base.Objects;

/**
 * Information about one face (person).
 */
public class Face implements Parcelable {

	private final String name;

	private long reminderPeriodInSeconds;
	private int reminderId;			// id used by android alarm framework
	private long lastReminderTrigger; // when alarm for this face was last triggered

	@JsonCreator
	public Face(
			@JsonProperty("name") String name,
			@JsonProperty("reminderPeriodInSeconds") long reminderPeriodInSeconds) {

		this.name = name;
		this.reminderPeriodInSeconds = reminderPeriodInSeconds;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public String getPhotoFolderName() {
		return name;
	}


	public long getReminderPeriodInSeconds() {
		return reminderPeriodInSeconds;
	}

	public void setReminderPeriodInSeconds(long reminderPeriodInSeconds) {
		this.reminderPeriodInSeconds = reminderPeriodInSeconds;
	}

	public int getReminderId() {
		return reminderId;
	}

	public void setReminderId(int reminderId) {
		this.reminderId = reminderId;
	}

	public long getLastReminderTrigger() {
		return lastReminderTrigger;
	}

	public void setLastReminderTrigger(long lastReminderTrigger) {
		this.lastReminderTrigger = lastReminderTrigger;
	}

	@Override
	public String toString() {
		return "[name = " + name + ", reminderPeriodInSeconds = " + reminderPeriodInSeconds
				+ ", reminderId = " + reminderId + ", lastReminderTrigger = " + lastReminderTrigger + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Face face = (Face) o;
		return Objects.equal(reminderPeriodInSeconds, face.reminderPeriodInSeconds) &&
				Objects.equal(name, face.name) &&
				Objects.equal(reminderId, face.reminderId) &&
				Objects.equal(lastReminderTrigger, face.lastReminderTrigger);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, reminderPeriodInSeconds, reminderId, lastReminderTrigger);
	}

	protected Face(Parcel in) {
		name = in.readString();
		reminderPeriodInSeconds = in.readLong();
		reminderId = in.readInt();
		lastReminderTrigger = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeLong(reminderPeriodInSeconds);
		dest.writeInt(reminderId);
		dest.writeLong(lastReminderTrigger);
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

		private String name;
		private long reminderPeriodInSeconds;

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setReminderPeriodInSeconds(long reminderPeriodInSeconds) {
			this.reminderPeriodInSeconds = reminderPeriodInSeconds;
			return this;
		}

		public String getName() {
			return name;
		}

		public long getReminderPeriodInSeconds() {
			return reminderPeriodInSeconds;
		}

		public Face build() {
			return new Face(name, reminderPeriodInSeconds);
		}

	}
}
