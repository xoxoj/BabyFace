package org.faudroids.babyface.faces;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Information about one face (person).
 */
public class Face {

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
