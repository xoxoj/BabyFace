package org.faudroids.babyface.faces;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.faudroids.babyface.photo.ReminderPeriod;
import org.parceler.Parcel;
import org.roboguice.shaded.goole.common.base.Objects;

/**
 * Information about one face (person).
 */
@Parcel
public class Face {

	private String name;

	private ReminderPeriod reminderPeriod;
	private int reminderId;			// id used by android alarm framework
	private long lastReminderTrigger; // when alarm for this face was last triggered

	public Face() { }

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
