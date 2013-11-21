package gr.uoa.di.std08169.mobile.media.share.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import gr.uoa.di.std08169.mobile.media.share.shared.User;
import gr.uoa.di.std08169.mobile.media.share.shared.UserResult;

//RemoteService: Epeidh o client tha ulopoihthei se javascript kai to back-end se java
//To userService einai ena servlet pou orizetai sto web.xml kai xekinaei me to pou xekinaei
//h efarmogh.
@RemoteServiceRelativePath("../userService") //S' auto to link tha vrisketai to remote service
public interface UserService extends RemoteService {
	// getUsers
	public UserResult getUsers(final String query, final int limit) throws UserServiceException;
	public User getUser(final String email) throws UserServiceException;
	public boolean isValidUser(final String email, final String password) throws UserServiceException;
	
	/**
	 * Add user if does not exist.
	 * @param email the email of the user
	 * @param password the password of the user
	 * @return true if user was added, false if user already exists
	 * @throws UserServiceException if any errors occur
	 */
	public boolean addUser(final String email, final String password) throws UserServiceException;
	// editUser
	// deleteUser
}
