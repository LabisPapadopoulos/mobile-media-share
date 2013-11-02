package gr.uoa.di.std08169.mobile.media.share.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.User;

//UserDao
public class UserServiceImpl implements UserService {
	//apothikeush stin vash me MD5 gia logous asfaleias
	private static final String GET_USER = "SELECT name, photo " +
											"FROM Users " +
											"WHERE email = ?;";
	private static final String IS_VALID_USER = "SELECT COUNT(*) " +
												"FROM Users " +
												"WHERE email = ? AND password = md5(?);";
	private static final String ADD_USER = "INSERT INTO Users (email, password, name, photo) " +
											"VALUES (?, md5(?), NULL, NULL);";
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	
	private DataSource dataSource; //antiproswpeuei afhrimena mia phgh dedomenwn (mia vash)
	
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public User getUser(final String email) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(GET_USER);
				try {
					preparedStatement.setString(1, email);
					final ResultSet resultSet = preparedStatement.executeQuery();
					try {
						return resultSet.next() ? new User(email, resultSet.getString("name"), resultSet.getString("photo")) : null;
					} finally {
						resultSet.close();
					}
				} finally {
					preparedStatement.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error retrieving user", e);
			throw new UserServiceException("Error retrieving user", e);
		}
	}
	
	@Override
	public boolean isValidUser(final String email, final String password) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(IS_VALID_USER);
				try {
					preparedStatement.setString(1, email);
					preparedStatement.setString(2, password);
					final ResultSet resultSet = preparedStatement.executeQuery();
					try {
						final int count = resultSet.next() ? resultSet.getInt("count") : 0;
						LOGGER.log(Level.INFO, "User " + email + " is " + ((count > 0) ? "valid" : "invalid"));
						return (count > 0);
					} finally {
						resultSet.close();
					}
				} finally {
					preparedStatement.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error validating user " + email, e);
			throw new UserServiceException("Error validating user " + email, e);
		}
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
			LOGGER.log(Level.WARNING, "Error adding user " + email, e);
			//Gia to front end
			throw new UserServiceException("Error adding user " + email, e);
		}
	}
}
