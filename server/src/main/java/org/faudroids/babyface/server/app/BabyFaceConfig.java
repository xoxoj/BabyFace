package org.faudroids.babyface.server.app;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class BabyFaceConfig extends Configuration {

	private final int dummy;

	@JsonCreator
	public BabyFaceConfig(@JsonProperty("dummy") int dummy) {
		this.dummy = dummy;
		// nothing to do for now
	}

	public int getDummy() {
		return dummy;
	}

}
