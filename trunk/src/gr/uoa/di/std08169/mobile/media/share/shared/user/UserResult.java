package gr.uoa.di.std08169.mobile.media.share.shared.user;

import java.util.List;

public class UserResult {
	private final List<User> users;
	private final int total;
	
	public UserResult(final List<User> users, final int total) {
		this.users = users;
		this.total = total;
	}

	public List<User> getUsers() {
		return users;
	}

	public int getTotal() {
		return total;
	}
}
