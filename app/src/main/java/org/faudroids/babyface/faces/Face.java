package org.faudroids.babyface.faces;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.UUID;

/**
 * Information about one face (person).
 */
public class Face {

	private final String id;
	private final String name;
	@JsonIgnore
	private final File mostRecentPhotoFile;
	private final long reminderPeriodInSeconds;

	private Face(String id, String name, File mostRecentPhotoFile, long reminderPeriodInSeconds) {
		this.id = id;
		this.name = name;
		this.mostRecentPhotoFile = mostRecentPhotoFile;
		this.reminderPeriodInSeconds = reminderPeriodInSeconds;
	}

	@JsonCreator
	public Face(
			@JsonProperty("id") String id,
			@JsonProperty("name") String name,
			@JsonProperty("mostRecentPhotoFileName") String mostRecentPhotoFileName,
			@JsonProperty("reminderPeriodInSeconds") long reminderPeriodInSeconds) {

		this.id = id;
		this.name = name;
		this.mostRecentPhotoFile = new File(mostRecentPhotoFileName);
		this.reminderPeriodInSeconds = reminderPeriodInSeconds;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public File getMostRecentPhotoFile() {
		return mostRecentPhotoFile;
	}

	public String getMostRecentPhotoFileName() {
		return mostRecentPhotoFile.getAbsolutePath();
	}

	public long getReminderPeriodInSeconds() {
		return reminderPeriodInSeconds;
	}

	@Override
	public String toString() {
		return "[id = " + id + ", name = " + name + ", mostRecentPhotoFileName = " + mostRecentPhotoFile.getAbsolutePath() + ", reminderPeriodInSeconds = " + reminderPeriodInSeconds + "]";
	}


	public static class Builder {

		private final String id;
		private String name;
		private File mostRecentPhotoFile;
		private long reminderPeriodInSeconds;

		public Builder() {
			this.id = UUID.randomUUID().toString();
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setMostRecentPhotoFile(File mostRecentPhotoFile) {
			this.mostRecentPhotoFile = mostRecentPhotoFile;
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

		public File getMostRecentPhotoFile() {
			return mostRecentPhotoFile;
		}

		public long getReminderPeriodInSeconds() {
			return reminderPeriodInSeconds;
		}

		public Face build() {
			return new Face(id, name, mostRecentPhotoFile, reminderPeriodInSeconds);
		}

	}
}
