package org.faudroids.babyface.faces;


import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.faudroids.babyface.photo.ReminderPeriod;
import org.roboguice.shaded.goole.common.base.Objects;

/**
 * Information about one face (person).
 */
public class Face implements Parcelable {

	private final String name;

	private ReminderPeriod reminderPeriod;
	private int reminderId;			// id used by android alarm framework
	private long lastReminderTrigger; // when alarm for this face was last triggered

	@JsonCreator
	public Face(
			@JsonProperty("name") String name,
			@JsonProperty("reminderPeriod") ReminderPeriod reminderPeriod) {

		this.name = name;
		this.reminderPeriod = reminderPeriod;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public String getPhotoFolderName() {
		return name;
	}


	public ReminderPeriod getReminderPeriod() {
		return reminderPeriod;
	}

	public void setReminderPeriod(ReminderPeriod reminderPeriod) {
		this.reminderPeriod = reminderPeriod;
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
		return "[name = " + name + ", reminderPeriod = " + reminderPeriod
				+ ", reminderId = " + reminderId + ", lastReminderTrigger = " + lastReminderTrigger + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Face face = (Face) o;
		return Objects.equal(reminderPeriod, face.reminderPeriod) &&
				Objects.equal(name, face.name) &&
				Objects.equal(reminderId, face.reminderId) &&
				Objects.equal(lastReminderTrigger, face.lastReminderTrigger);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, reminderPeriod, reminderId, lastReminderTrigger);
	}

	protected Face(Parcel in) {
		name = in.readString();
		reminderPeriod = in.readParcelable(ReminderPeriod.class.getClassLoader());
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
		dest.writeParcelable(reminderPeriod, 0);
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
		private ReminderPeriod reminderPeriod;

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public String getName() {
			return name;
		}

		public Builder setReminderPeriod(ReminderPeriod reminderPeriod) {
			this.reminderPeriod = reminderPeriod;
			return this;
		}

		public ReminderPeriod getReminderPeriod() {
			return reminderPeriod;
		}

		public Face build() {
			return new Face(name, reminderPeriod);
		}

	}
}
