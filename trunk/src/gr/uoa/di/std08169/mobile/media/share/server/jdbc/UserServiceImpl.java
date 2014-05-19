package gr.uoa.di.std08169.mobile.media.share.server.jdbc;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Hex;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

//UserDao
public class UserServiceImpl implements UserService {
	private static final String GET_USERS = "SELECT email, status, name, photo " +
											"FROM Users " +
											"WHERE match(email, ?) OR match(name, ?) " +
											"LIMIT ?;";
	private static final String GET_TIMEDOUT_USERS = "SELECT email " +
														"FROM Users " +
														"WHERE CAST(((extract(epoch FROM now()) * 1000) - " +
														"(extract(epoch FROM tokenTimestamp) * 1000)) AS BIGINT) >= ?;";
	//apothikeush stin vash me MD5 gia logous asfaleias
	private static final String GET_USER = "SELECT status, name, photo " +
											"FROM Users " +
											"WHERE email = ?;";
	private static final String GET_USER_BY_TOKEN = "SELECT email, status, name, photo " +
													"FROM Users " +
													"WHERE token = ? AND " +
													//kai den exei lhxei
													"(CAST(((extract(epoch FROM now()) * 1000) - " +
													"(extract(epoch FROM tokenTimestamp) * 1000)) AS BIGINT) < ?);";
	private static final String IS_VALID_USER = "SELECT COUNT(*) AS count " +
												"FROM Users " +
												"WHERE email = ? AND password = md5(?) AND ((status = " + UserStatus.NORMAL.ordinal() + ") " +
																		"OR (status = " + UserStatus.ADMIN.ordinal() + "));";
																	//name, photo DEFAULT NULL apo tin vash
	private static final String ADD_USER = "INSERT INTO Users (email, password, status, tokenTimestamp, token) " +
											"VALUES (?, md5(?), ?, ?, ?);";
											//An to token tou xrhsth exei lhxei den ginetai edit
	private static final String EDIT_USER = "UPDATE Users " +
											"SET %sstatus = ?, tokenTimestamp = ?, token = ?, name = ?, photo = ? " +
																//An einai SQL NULL
											"WHERE email = ? AND ((tokenTimestamp IS NULL) OR " +
											//extract (epoch from timestamp): epistrefei to timestamp se seconds apo to
											//epoch (1/1/1970..). * 1000 ginetai se millisecond kai afairountai ta milliseconds tou twra
											//apo ta milliseconds tou tokenTimestamp.
											//Castaretai se long to apotelesma
											"(CAST(((extract(epoch FROM now()) * 1000) - " +
											"(extract(epoch FROM tokenTimestamp) * 1000)) AS BIGINT) < ?));";
	private static final String UPDATE_PASSWORD = "password = md5(?), ";
	private static final String DELETE_USER = "DELETE FROM Users " +
											  "WHERE email = ?;";
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	
	private final DataSource dataSource; //antiproswpeuei afhrimena mia phgh dedomenwn (mia vash)
	private final Long timeout;
	private Thread thread;

	public UserServiceImpl(final DataSource dataSource, final long timeout) {
		this.dataSource = dataSource;
		this.timeout = timeout;
	}
	
	public void init() {
		//thread pou trexei an timeout xrono gia na katharizei tous timedout xrhstes
		thread = new Thread(new Runnable() {	
			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							for (String email : getTimedoutUsers())
								deleteUser(email);
						} catch (final UserServiceException e) {
							LOGGER.log(Level.WARNING, "Error cleaning up timedout users", e);
						}
						Thread.sleep(timeout);
					}
				} catch (final InterruptedException e) {}
			}
		});
		//kanei background douleia kai borei 
		//na skotwthei opoiadhpote stigmh an thelei na termatisei h JVM
		thread.setDaemon(true);
		thread.start();
	}
	
	public void shutdown() {
		thread.interrupt();
	}
	
	@Override
	public UserResult getUsers(final String query, final int limit) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(GET_USERS);
				try {
					preparedStatement.setString(1, query);
					preparedStatement.setString(2, query);
					preparedStatement.setInt(3, limit);
					final ResultSet resultSet = preparedStatement.executeQuery();
					try {
						final List<User> users = new ArrayList<User>();
						while (resultSet.next())
							users.add(new User(resultSet.getString("email"), UserStatus.values()[resultSet.getInt("status")],
									resultSet.getString("name"), resultSet.getString("photo")));
						LOGGER.info("Retrieved " + users.size() + " users (query: " + query + ", limit: " + limit + ")");
						return new UserResult(users, users.size());
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
			LOGGER.log(Level.WARNING, "Error retrieving users (query: " + query + ", limit: " + limit + ")", e);
			throw new UserServiceException("Error retrieving users (query: " + query + ", limit: " + limit + ")", e);
		}
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
						final User user = resultSet.next() ? 
								new User(email, UserStatus.values()[resultSet.getInt("status")], resultSet.getString("name"), 
										resultSet.getString("photo")) : null;
						LOGGER.info((user == null) ? ("User " + email + " not found") : ("Retrieved user " + email));
						return user;
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
			LOGGER.log(Level.WARNING, "Error retrieving user " + email, e);
			throw new UserServiceException("Error retrieving user " + email, e);
		}
	}

	@Override
	public User getUserByToken(final String token) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_BY_TOKEN);
				try {
					preparedStatement.setString(1, token);
					preparedStatement.setLong(2, timeout);
					final ResultSet resultSet = preparedStatement.executeQuery();
					try {
						final User user = resultSet.next() ? new User(resultSet.getString("email"), UserStatus.values()[resultSet.getInt("status")], 
									resultSet.getString("name"), resultSet.getString("photo")) : null;
						LOGGER.info((user == null) ? ("User with token " + token + " not found") : ("Retrieved user with token " + token));
						return user;
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
			LOGGER.log(Level.WARNING, "Error retrieving user by token " + token, e);
			throw new UserServiceException("Error retrieving user by token " + token, e);
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
						LOGGER.info("User " + email + " is " + ((count > 0) ? "valid" : "invalid"));
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
	
	/*
	 * Prosthikh xrhsth ws pending an den uparxei kai epistrefei ena token 
	 */
	@Override
	public String addUser(final String email, final String password) throws UserServiceException {
		//check gia to xrhsth an einai stous kanonikous
		if (getUser(email) != null) {
			LOGGER.info("User " + email + " already exists");
			return null;
		}
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement addUser = connection.prepareStatement(ADD_USER);
				try {
					final Date date = new Date();
					addUser.setString(1, email);
					addUser.setString(2, password);
					addUser.setInt(3, UserStatus.PENDING.ordinal());
					addUser.setTimestamp(4, new Timestamp(date.getTime()));
					final String token = generateToken(email, date);
					addUser.setString(5, token);
					addUser.executeUpdate();
					LOGGER.info("User " + email + " added successfully");
					return token;
				} finally {
					addUser.close();
				}
			} finally {
				//kleinei to connection dedomenou oti uparxei hdh
				connection.close();
			}
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.WARNING, "Error adding user " + email, e);
			throw new UserServiceException("Error adding user " + email, e);
		} catch (final SQLException e) {
			//Gia na to vlepei o diaxeirisths
			LOGGER.log(Level.WARNING, "Error adding user " + email, e);
			//Gia to front end
			throw new UserServiceException("Error adding user " + email, e);
		}
	}

	@Override
	public String editUser(final User user, final String password) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				//An o xrhsths dwsei neo password 'h xexasei to password, tote to password allazei
				final boolean updatePassword = (password != null) || (user.getStatus() == UserStatus.FORGOT);				
				final PreparedStatement editUser = connection.prepareStatement(String.format(EDIT_USER, updatePassword ? UPDATE_PASSWORD : ""));
				try {
					final Date date = new Date();
					
					if (password != null)
						editUser.setString(1, password);
					else if (user.getStatus() == UserStatus.FORGOT)
						//svhnei to password me SQL NULL
						editUser.setNull(1, Types.CHAR);
					//-1 se ola se periptwsh pou den allazei to password
					editUser.setInt(updatePassword ? 2 : 1, user.getStatus().ordinal());
					String token = null;
					//dhmeiourgei token
					if (user.getStatus() == UserStatus.FORGOT) {
						//Se Forgot katastash
						editUser.setTimestamp(3, new Timestamp(date.getTime()));
						token = generateToken(user.getEmail(), date);
						editUser.setString(4, token);
					} else { //petaei to token
						editUser.setNull(updatePassword ? 3 : 2, Types.TIMESTAMP);
						editUser.setNull(updatePassword ? 4 : 3, Types.CHAR);
					}
					editUser.setString(updatePassword ? 5 : 4, user.getName());
					editUser.setString(updatePassword ? 6 : 5, user.getPhoto());
					editUser.setString(updatePassword ? 7 : 6, user.getEmail());
					editUser.setLong(updatePassword ? 8 : 7, timeout);
					editUser.executeUpdate();
					LOGGER.info("User " + user.getEmail() + " edited successfully");
					return token;
				} finally {
					editUser.close();
				}
			} finally {
				//kleinei to connection dedomenou oti uparxei hdh
				connection.close();
			}
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.WARNING, "Error editing user " + user.getEmail(), e);
			throw new UserServiceException("Error editing user " + user.getEmail(), e);
		} catch (final SQLException e) {
			//Gia na to vlepei o diaxeirisths
			LOGGER.log(Level.WARNING, "Error editing user " + user.getEmail(), e);
			//Gia to front end
			throw new UserServiceException("Error editing user " + user.getEmail(), e);
		}
	}

	@Override
	public void deleteUser(final String email) throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement deleteUser = connection.prepareStatement(DELETE_USER);
				try {
					deleteUser.setString(1, email);
					deleteUser.executeUpdate();
					LOGGER.info("User " + email + " deleted successfully");
				} finally {
					deleteUser.close();
				}
			} finally {
				//kleinei to connection dedomenou oti uparxei hdh
				connection.close();
			}
		} catch (final SQLException e) {
			//Gia na to vlepei o diaxeirisths
			LOGGER.log(Level.WARNING, "Error editing user " + email, e);
			//Gia to front end
			throw new UserServiceException("Error editing user " + email, e);
		}		
	}
	
	private String generateToken(final String email, final Date date) throws NoSuchAlgorithmException {
		/**
		 * @see http://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
		 */
		//Upologismos MD5
		final MessageDigest digest = MessageDigest.getInstance("MD5");
		//Desmeush buffer gia na graftei enas long (to  date) kai na diavastei san bytes
		final ByteBuffer registrationBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		registrationBytes.putLong(date.getTime());
		//Me update prostithentai dedomena
		digest.update(registrationBytes.array());
		digest.update(email.getBytes());
		//me to digest vgainei to teliko apotelesma kai kodikopoieitai se 16adikh morfh (apo xuma bytes)
		//gia na borei na stalthei ws link
		return Hex.encodeHexString(digest.digest());
	}
	
	private List<String> getTimedoutUsers() throws UserServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement getTimedoutUsers = connection.prepareStatement(GET_TIMEDOUT_USERS);
				try {
					getTimedoutUsers.setLong(1, timeout);
					final ResultSet resultSet = getTimedoutUsers.executeQuery();
					try {
						List<String> emails = new ArrayList<String>();
						while (resultSet.next())
							emails.add(resultSet.getString("email"));
						LOGGER.info("Found " + emails.size() +" timedout users");
						return emails;
					} finally {
						resultSet.close();
					}
				} finally {
					getTimedoutUsers.close();
				}
			} finally {
				//kleinei to connection dedomenou oti uparxei hdh
				connection.close();
			}
		} catch (final SQLException e) {
			//Gia na to vlepei o diaxeirisths
			LOGGER.log(Level.WARNING, "Error retrieving timedout users", e);
			//Gia to front end
			throw new UserServiceException("Error retrieving timedout users", e);
		}
	}
}
