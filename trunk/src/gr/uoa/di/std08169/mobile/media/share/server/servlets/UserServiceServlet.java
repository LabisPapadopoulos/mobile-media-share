package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import org.springframework.web.context.support.WebApplicationContextUtils;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

//* extends RemoteServiceServlet: gia na borei na kaleitai mesw diktuou 
//(Gia na ulopoihthei to UserServiceAsync)
//* implements UserService: gia na sumperiferetai san userService
//GWT servlet pou kalei to interface userService pou ulopoieitai apo to UserServiceImpl
public class UserServiceServlet extends RemoteServiceServlet implements UserService {
	private static final long serialVersionUID = 1L;

	private UserService userService;

	//init gia to servlet
	@Override
	public void init() {
		//pairnei to xml tou spring
		//WebApplicationContextUtils: gia na pairnei application contexts
		//getServletContext(): pairnei to web.xml
		//getWebApplicationContext: pairnei to context pou orizetai sto applicationContext.xml
		//ftiaxnei ena aplication context sumfwna me auta pou orizontai sto web.xml
		//Pairnei ena pragma (bean) pou to lene userService gia na kanei douleies gia xrhstes
		//(to opoio to userService mhlaei me tin bash).
		userService = (UserService) WebApplicationContextUtils.
				getWebApplicationContext(getServletContext()).getBean("userService", UserService.class);
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
	public String addUser(final String email, final String password) throws UserServiceException {
		return userService.addUser(email, password);
	}

	@Override
	public String editUser(final User user, final String password) throws UserServiceException {
		return userService.editUser(user, password);
	}

	@Override
	public void deleteUser(final String email) throws UserServiceException {
		userService.deleteUser(email);
	}
}
