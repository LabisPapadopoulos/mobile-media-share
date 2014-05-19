package gr.uoa.di.std08169.mobile.media.share.client.services.user;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;

//RemoteService: Epeidh o client tha ulopoihthei se javascript kai to back-end se java
//To userService einai ena servlet pou orizetai sto web.xml kai xekinaei me to pou xekinaei
//h efarmogh.
@RemoteServiceRelativePath("../userService") //S' auto to link tha vrisketai to remote service
public interface UserService extends RemoteService {
	// getUsers
	public UserResult getUsers(final String query, final int limit) throws UserServiceException;
	public User getUser(final String email) throws UserServiceException;
	public User getUserByToken(final String token) throws UserServiceException;
	public boolean isValidUser(final String email, final String password) throws UserServiceException;
	
	/**
	 * Prosthikh xrhsth an den uparxei.
	 * @param email to email tou user
	 * @param password to password tou user
	 * @return ena token pou anagnwrizei prosorina ton xrhsth an prostethike, alliws null an uparxei hdh o xrhsths.
	 * @throws UserServiceException gia kathe sfalma
	 */
	public String addUser(final String email, final String password) throws UserServiceException;
	
	/**
	 * Epexergasia enos xrhsth
	 * @param user o xrhsths pou tha allaxtei
	 * @param password to neo password tou xrhsth h null an de xreiazetai na allaxei
	 * @return ena token pou anagnwrizei prosorina ton xrhsth an xehase to password, alliws null. 
	 * @throws UserServiceException gia kathe sfalma
	 */
	public String editUser(final User user, final String password) throws UserServiceException;
	
	/**
	 * Diagrafh xrhsth
	 * @param email email tou xrhsth pros diagrafh
	 * @throws UserServiceException gia kathe sfalma
	 */
	public void deleteUser(final String email) throws UserServiceException;
}
