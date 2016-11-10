package smartthings.dw.oauth;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

import java.util.List;

public class User {

	private final String uuid;
	private final String userName;
	private final String email;
	private final String fullName;

	private final ImmutableSet<String> authorities;

	public User(String uuid, String userName, String email, String fullName, List<String> authorities) {
		this.uuid = uuid;
		this.userName = userName;
		this.email = email;
		this.fullName = fullName;
		this.authorities = ImmutableSet.copyOf(authorities);
	}

	public String getUserName() {
		return userName;
	}

	public String getUuid() {
		return uuid;
	}

	public String getEmail() { return email; }

	public String getFullName() { return fullName; }

	public ImmutableSet<String> getAuthorities() {
		return authorities;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("uuid", uuid)
			.add("userName", userName)
			.add("authorities", authorities)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		if (!uuid.equals(user.uuid)) return false;
		if (!userName.equals(user.userName)) return false;
		if (email != null ? !email.equals(user.email) : user.email != null) return false;
		if (fullName != null ? !fullName.equals(user.fullName) : user.fullName != null) return false;
		return authorities.equals(user.authorities);

	}

	@Override
	public int hashCode() {
		int result = uuid.hashCode();
		result = 31 * result + userName.hashCode();
		result = 31 * result + (email != null ? email.hashCode() : 0);
		result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
		result = 31 * result + authorities.hashCode();
		return result;
	}
}
