package gr.uoa.di.std08169.mobile.media.share.server;

import org.springframework.web.context.support.WebApplicationContextUtils;

import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.User;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

//* extends RemoteServiceServlet: gia na borei na kaleitai mesw diktuou 
//(Gia na ulopoihthei to UserServiceAsync)
//* implements UserService: gia na sumperiferetai san userService
//GWT servlet
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
				getWebApplicationContext(getServletContext()).getBean("userService", UserServiceImpl.class);
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
