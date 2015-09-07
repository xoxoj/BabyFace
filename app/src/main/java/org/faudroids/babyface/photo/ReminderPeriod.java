package org.faudroids.babyface.photo;


import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.roboguice.shaded.goole.common.base.Objects;

public class ReminderPeriod implements Parcelable {

	private final long unitInSeconds;
	private final int amount;

	@JsonCreator
	public ReminderPeriod(
			@JsonProperty("unitInSeconds") long unitInSeconds,
			@JsonProperty("amount") int amount) {

		this.unitInSeconds = unitInSeconds;
		this.amount = amount;
	}

	public long getUnitInSeconds() {
		return unitInSeconds;
	}

	public int getAmount() {
		return amount;
	}

	@JsonIgnore
	public long toSeconds() {
		return amount * unitInSeconds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ReminderPeriod that = (ReminderPeriod) o;
		return Objects.equal(unitInSeconds, that.unitInSeconds) &&
				Objects.equal(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(unitInSeconds, amount);
	}

	@Override
	public String toString() {
		return "[unitInSeconds = " + unitInSeconds + ", amount = " + amount + "]";
	}

	protected ReminderPeriod(Parcel in) {
		unitInSeconds = in.readLong();
		amount = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(unitInSeconds);
		dest.writeInt(amount);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<ReminderPeriod> CREATOR = new Parcelable.Creator<ReminderPeriod>() {
		@Override
		public ReminderPeriod createFromParcel(Parcel in) {
			return new ReminderPeriod(in);
		}

		@Override
		public ReminderPeriod[] newArray(int size) {
			return new ReminderPeriod[size];
		}
	};
}
