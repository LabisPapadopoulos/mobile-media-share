package gr.uoa.di.std08169.mobile.media.share.server.proxies;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;

//Ylopoiei ena userservice xrhsimopoiwntas ena allo userService. (Wrapper)
//Ylopoiei kai mnhmh cache gia ta apotelesmata twn anazhthsewn gia tous xrhstes
//Tha kaleitai o proxy kai to spring tha dialegei jdbcUserService 'h gcdUserService
public class UserServiceProxy implements UserService {
	//Xthsh antikeimenou anti gia HashMap 2ou epipedou (stin getUsers) pou antiprosopeuei ta orismata. 
	private class GetUsersArguments {
		private String query;
		private int limit;
		
		private GetUsersArguments(final String query, final int limit) {
			this.query = query;
			this.limit = limit;
		}
		
		//Override equals kai hashCode gia na katalhgei o sundiasmos twn idiwn
		//orismatwn panta stin idia thesh mesa sto HashMap
		@Override
		public boolean equals(final Object object) {
			return (object instanceof GetUsersArguments) &&
					query.equals(((GetUsersArguments) object).query) &&
					(limit == ((GetUsersArguments) object).limit);
		}
		
		@Override
		public int hashCode() {
			return query.hashCode() + limit;
		}
	}
	
	private final UserService userService;
	private final Map<GetUsersArguments, UserResult> getUsersCache;
	private final Map<String, User> getUserCache;
	
	public UserServiceProxy(final UserService userService) {
		this.userService = userService;
		//Sungxronismos sta maps
		getUsersCache = Collections.synchronizedMap(new HashMap<GetUsersArguments, UserResult>());
		getUserCache = Collections.synchronizedMap(new HashMap<String, User>());		
	}

	@Override
	public UserResult getUsers(final String query, final int limit) throws UserServiceException {
		final GetUsersArguments arguments = new GetUsersArguments(query, limit);
		UserResult result = getUsersCache.get(arguments);
		if (result == null) {
			result = userService.getUsers(query, limit);
			getUsersCache.put(arguments, result);
		}
		return result;
	}

	@Override
	public User getUser(final String email) throws UserServiceException {
		User user = getUserCache.get(email);
		if (user == null) {
			user = userService.getUser(email);
			if (user != null)
				getUserCache.put(email, user);
		}
		return user;
	}

	/**
	 * Den cacharetai gia logous asfaleias. Exallou xrhsimopoieitai mono apo to login mia fora gia kathe xrhsth.
	 */
	@Override
	public boolean isValidUser(final String email, final String password) throws UserServiceException {
		return userService.isValidUser(email, password);
	}

	@Override
	public String addUser(final String email, final String password) throws UserServiceException {
		getUsersCache.clear();
		return userService.addUser(email, password);
	}

	@Override
	public String editUser(final User user, final String password) throws UserServiceException {
		final String token = userService.editUser(user, password);
		getUsersCache.clear();
		//enhmerwsh tis allaghs gia ton sugkekrimeno xrhsth
		getUserCache.put(user.getEmail(), user);
		return token;
	}

	@Override
	public void deleteUser(final String email) throws UserServiceException {
		userService.deleteUser(email);
		getUsersCache.clear();
		//diagrafh sugkekrimenou xrhsth apo tin cache
		getUserCache.remove(email);
	}
}
