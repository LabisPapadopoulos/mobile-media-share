package gr.uoa.di.std08169.mobile.media.share.server.proxies;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;

//Ylopoiei ena userservice xrhsimopoiwntas ena allo userService. (Wrapper)
//Tha kaleitai o proxy kai to spring tha dialegei jdbcUserService 'h gcdUserService
public class UserServiceProxy implements UserService {
	private final UserService userService;
	
	public UserServiceProxy(final UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserResult getUsers(final String query, final int limit) throws UserServiceException {
		return userService.getUsers(query, limit);
	}

	@Override
	public User getUser(final String email) throws UserServiceException {
		return userService.getUser(email);
	}

	@Override
	public boolean isValidUser(final String email, final String password) throws UserServiceException {
		return userService.isValidUser(email, password);
	}

	@Override
	public boolean addUser(final String email, final String password) throws UserServiceException {
		return userService.addUser(email, password);
	}
}
