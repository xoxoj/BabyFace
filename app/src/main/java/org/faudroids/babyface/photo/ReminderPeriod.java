package org.faudroids.babyface.photo;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.roboguice.shaded.goole.common.base.Objects;


@Parcel
public class ReminderPeriod {

	private final long unitInSeconds;
	private final int amount;

	@JsonCreator
	@ParcelConstructor
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

}
