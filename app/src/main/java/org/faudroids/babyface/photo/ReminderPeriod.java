package org.faudroids.babyface.photo;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.roboguice.shaded.goole.common.base.Objects;

public class ReminderPeriod {

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

}
