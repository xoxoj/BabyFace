package org.faudroids.babyface.server.auth;

import com.google.common.base.Objects;

public class User {

	private final String token;

	public User(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equal(token, user.token);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(token);
	}

}
