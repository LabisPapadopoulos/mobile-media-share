package gr.uoa.di.std08169.mobile.media.share.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;

//* extends RemoteServiceServlet: gia na borei na kaleitai mesw diktuou 
//(Gia na ulopoihthei to UserServiceAsync)
//* implements UserService: gia na sumperiferetai san userService
public class UserServiceImpl extends RemoteServiceServlet implements UserService {
	private static final long serialVersionUID = 1L;
	private static final String ADD_USER = "INSERT INTO Users (email, password, name, photo) " +
											"VALUES (?, ?, NULL, NULL);";
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	
	private DataSource dataSource; //antiproswpeuei afhrimena mia phgh dedomenwn (mia vash)
	
	//init gia to servlet
	@Override
	public void init() {
		//pairnei to xml tou spring
		//WebApplicationContextUtils: gia na pairnei application contexts
		//getServletContext(): pairnei to web.xml
		//getWebApplicationContext: pairnei to context pou orizetai sto applicationContext.xml
		//ftiaxnei ena aplication context sumfwna me auta pou orizontai sto web.xml
		//Pairnei ena pragma (bean) pou to lene dataSource.
		dataSource = (DataSource) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("dataSource", DataSource.class);
	}
	
	@Override
	public boolean addUser(final String email, final String password) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(ADD_USER);
				try {
					preparedStatement.setString(1, email);
					preparedStatement.setString(2, password);
					final int rows = preparedStatement.executeUpdate();
					LOGGER.log(Level.INFO, ((rows > 0) ? ("Added user " + email) : ("User " + email + " already exists")));
					return rows > 0;
				} finally {
					//kleinei to preparedStatement dedomenou oti uparxei hdh
					preparedStatement.close();
				}
			} finally {
				//kleinei to connection dedomenou oti uparxei hdh
				connection.close();
			}
		} catch (final SQLException e) {
			//Gia na to vlepei o diaxeirisths
			LOGGER.log(Level.WARNING, "Error adding user", e);
			//Gia to front end
			throw new UserServiceException("Error adding user", e);
		}
	}
}
