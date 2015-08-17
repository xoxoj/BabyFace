package org.faudroids.babyface.faces;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.roboguice.shaded.goole.common.base.Optional;

import java.io.File;
import java.util.UUID;

/**
 * Information about one face (person).
 */
public class Face {

	private final String id;
	private final String name;
	@JsonIgnore
	private final Optional<File> mostRecentPhotoFile;
	private final long reminderPeriodInSeconds;

	private Face(String id, String name, Optional<File> mostRecentPhotoFile, long reminderPeriodInSeconds) {
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
		if (mostRecentPhotoFileName == null) this.mostRecentPhotoFile = Optional.absent();
		else this.mostRecentPhotoFile = Optional.of(new File(mostRecentPhotoFileName));
		this.reminderPeriodInSeconds = reminderPeriodInSeconds;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public Optional<File> getMostRecentPhotoFile() {
		return mostRecentPhotoFile;
	}

	public String getMostRecentPhotoFileName() {
		if (mostRecentPhotoFile.isPresent()) return mostRecentPhotoFile.get().getAbsolutePath();
		return null;
	}

	public long getReminderPeriodInSeconds() {
		return reminderPeriodInSeconds;
	}

	@Override
	public String toString() {
		String s = "[id = " + id + ", name = " + name + ", mostRecentPhotoFileName = ";
		if (mostRecentPhotoFile.isPresent()) s += mostRecentPhotoFile.get().getAbsolutePath();
		else s += "null";
		return s + ", reminderPeriodInSeconds = " + reminderPeriodInSeconds + "]";
	}


	public static class Builder {

		private final String id;
		private String name;
		private Optional<File> mostRecentPhotoFile = Optional.absent();
		private long reminderPeriodInSeconds;

		public Builder() {
			this.id = UUID.randomUUID().toString();
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setMostRecentPhotoFile(File mostRecentPhotoFile) {
			this.mostRecentPhotoFile = Optional.of(mostRecentPhotoFile);
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

		public Optional<File> getMostRecentPhotoFile() {
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
