package gr.uoa.di.std08169.mobile.media.share.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

//RemoteService: Epeidh o client tha ulopoihthei se javascript kai to back-end se java
//To userService einai ena servlet pou orizetai sto web.xml kai xekinaei me to pou xekinaei
//h efarmogh.
@RemoteServiceRelativePath("../userService") //S' auto to link tha vrisketai to remote service
public interface UserService extends RemoteService {
	//Prosthikh user an den uparxei
	public boolean addUser(final String email, final String password) throws UserServiceException;
	public boolean isValidUser(final String email, final String password) throws UserServiceException;
}
