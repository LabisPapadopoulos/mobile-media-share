package gr.uoa.di.std08169.mobile.media.share.server.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypeException;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

public class MediaServiceImpl implements ExtendedMediaService {
	//epistrefontai ola ta public media, alla kai ta private tou kathe xrhsth
	private static final String GET_MEDIA = "SELECT id, type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
											"FROM Media " +
											"WHERE TRUE%s%s%s%s%s%s%s%s%s%s " +
											"LIMIT ? OFFSET ?;";
	private static final String GET_MEDIA_2 = "SELECT id, type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
											"FROM Media " +
											"WHERE (latitude >= ?) AND (latitude <= ?) " + //minlat <= latitude <= maxlat
											"AND (longitude >= ?) AND (longitude <= ?)%s%s%s%s%s%s%s%s%s;"; //minlng <= longitude <= maxlng
	private static final String GET_MEDIUM = "SELECT type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
											"FROM Media " +
											"WHERE id = ?;";
	//concat string se postgres -> ||
	private static final String CURRENT_USER_FILTER = " AND (public OR \"user\" = ?)"; 
	private static final String TITLE_FILTER = " AND match(title, ?)"; //match: sunartish stin postgres
	private static final String TYPE_FILTER = " AND type LIKE (? || '%')"; //Typos kai meta otidhpote (%)
	private static final String USER_FILTER = " AND (\"user\" = ?)";
	private static final String CREATED_FROM_FILTER = " AND (created >= ?)";
	private static final String CREATED_TO_FILTER = " AND (created <= ?)";
	private static final String EDITED_FROM_FILTER = " AND (edited >= ?)";
	private static final String EDITED_TO_FILTER = " AND (edited <= ?)";
	private static final String PUBLIC_FILTER = " AND (public = ?)";
	private static final String ORDERING = " ORDER BY %s %s";
	private static final String ASCENDING = "ASC";
	private static final String DESCENDING = "DESC";
	
	private static final String COUNT_MEDIA = 	"SELECT COUNT(*) AS total " +
												"FROM Media " +
												"WHERE TRUE%s%s%s%s%s%s%s%s%s;";
	
	private static final String ADD_MEDIA = "INSERT INTO Media (id, type, size, duration, \"user\", " +
											"created, edited, title, latitude, longitude, public) " +
											"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String EDIT_MEDIA = "UPDATE Media " +
											 "SET edited = now(), " +
										 		 "title = ?, " +
												 "latitude = ?," +
												 "longitude = ?," +
												 "public = ? " +
											 "WHERE id = ?;";
	private static final String DELETE_MEDIA = "DELETE FROM Media " +
												"WHERE id = ?;";
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private final DataSource dataSource;
	private final File mediaDir;
	private final int bufferSize;
	private final UserService userService;
	
	public MediaServiceImpl(final DataSource dataSource, final String mediaDir, final int bufferSize, final UserService userService) {
		this.dataSource = dataSource;
		this.mediaDir = new File(mediaDir);
		this.bufferSize = bufferSize;
		this.userService = userService;
	}
	
	@Override
	public MediaResult getMedia(final User currentUser, final String title, final MediaType type,
			final String user, final Date createdFrom, final Date createdTo, final Date editedFrom,
			final Date editedTo, final Boolean publik, final Integer start, final Integer length,
			final String orderField, final boolean ascending) throws MediaServiceException {
		
		//Xtisimo tou string me oles ta pithana filtra pou exei dialexei o xrhsths
		final String getMediaQuery = 
				String.format(GET_MEDIA, 
				(currentUser.getStatus() == UserStatus.ADMIN) ? "" : CURRENT_USER_FILTER,
				(title == null) ? "" : TITLE_FILTER, 
				(type == null) ? "" : TYPE_FILTER,
				(user == null) ? "" : USER_FILTER,
				(createdFrom == null) ? "" : CREATED_FROM_FILTER,
				(createdTo == null) ? "" : CREATED_TO_FILTER, 
				(editedFrom == null) ? "" : EDITED_FROM_FILTER,
				(editedTo == null) ? "" : EDITED_TO_FILTER, 
				(publik == null) ? "" : PUBLIC_FILTER,
				(orderField == null) ? "" : String.format(ORDERING, orderField, ascending ? ASCENDING : DESCENDING));
		//Xtisimo string gia to plithos twn media pou uparxoun stin vash
		final String countMediaQuery = 
				String.format(COUNT_MEDIA,
				(currentUser.getStatus() == UserStatus.ADMIN) ? "" : CURRENT_USER_FILTER,
				(title == null) ? "" : TITLE_FILTER, 
				(type == null) ? "" : TYPE_FILTER,
				(user == null) ? "" : USER_FILTER,
				(createdFrom == null) ? "" : CREATED_FROM_FILTER,
				(createdTo == null) ? "" : CREATED_TO_FILTER, 
				(editedFrom == null) ? "" : EDITED_FROM_FILTER,
				(editedTo == null) ? "" : EDITED_TO_FILTER, 
				(publik == null) ? "" : PUBLIC_FILTER);
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement getMedia = connection.prepareStatement(getMediaQuery);
				try {
					final PreparedStatement countMedia = connection.prepareStatement(countMediaQuery);
					try {
						int parameter = 1;
						if (currentUser.getStatus() != UserStatus.ADMIN) {
							getMedia.setString(parameter, currentUser.getEmail());
							countMedia.setString(parameter++, currentUser.getEmail());
						}
						if (title != null) {
							getMedia.setString(parameter, title);
							countMedia.setString(parameter++, title);
						}
						if (type != null) {
							getMedia.setString(parameter, type.getMimeTypePrefix());
							countMedia.setString(parameter++, type.getMimeTypePrefix());
						}
						if (user != null) {
							getMedia.setString(parameter, user);
							countMedia.setString(parameter++, user);
						}
						if (createdFrom != null) {
							final Timestamp timestamp = new Timestamp(createdFrom.getTime());
							getMedia.setTimestamp(parameter, timestamp);
							countMedia.setTimestamp(parameter++, timestamp);
						}
						if (createdTo != null) {
							final Timestamp timestamp = new Timestamp(createdTo.getTime());
							getMedia.setTimestamp(parameter, timestamp);
							countMedia.setTimestamp(parameter++, timestamp);							
						}
						if (editedFrom != null) {
							final Timestamp timestamp = new Timestamp(editedFrom.getTime());
							getMedia.setTimestamp(parameter, timestamp);
							countMedia.setTimestamp(parameter++, timestamp);
						}
						if (editedTo != null) {
							final Timestamp timestamp = new Timestamp(editedTo.getTime());
							getMedia.setTimestamp(parameter, timestamp);
							countMedia.setTimestamp(parameter++, timestamp);
						}
						if (publik != null) {
							getMedia.setBoolean(parameter, publik);
							countMedia.setBoolean(parameter++, publik);
						}
						getMedia.setInt(parameter++, length);
						getMedia.setInt(parameter, start);
						final ResultSet getMediaResultSet = getMedia.executeQuery();
						try {
							final ResultSet countMediaResultSet = countMedia.executeQuery();
							try {
								final List<Media> mediaList = new ArrayList<Media>();
								while(getMediaResultSet.next()) {
									final String id = getMediaResultSet.getString("id");
									try {
										mediaList.add(new Media(id, getMediaResultSet.getString("type"), 
												getMediaResultSet.getLong("size"), getMediaResultSet.getInt("duration"),
												userService.getUser(getMediaResultSet.getString("user")),
												getMediaResultSet.getTimestamp("created"), 
												getMediaResultSet.getTimestamp("edited"), getMediaResultSet.getString("title"),
												getMediaResultSet.getBigDecimal("latitude"), getMediaResultSet.getBigDecimal("longitude"),
												getMediaResultSet.getBoolean("public")));
									} catch (final UserServiceException e) {
										LOGGER.log(Level.WARNING, "Error resolving user of media " + id, e);
									}
								}
								final int total = (countMediaResultSet.next()) ? countMediaResultSet.getInt("total") : 0;
								LOGGER.info("Retrieved " + mediaList.size() + " media");
								return new MediaResult(mediaList, total);
							} finally {
								countMediaResultSet.close();
							}
						} finally {
							getMediaResultSet.close();
						}
					} finally {
						countMedia.close();
					}
				} finally {
					getMedia.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error retrieving media", e);
			throw new MediaServiceException("Error retrieving media", e);
		}
	}
	
	@Override
	public List<Media> getMedia(final User currentUser, final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude, 
			final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude) throws MediaServiceException {
		
				//Xtisimo tou string me oles ta pithana filtra pou exei dialexei o xrhsths
				final String getMediaQuery = 
						String.format(GET_MEDIA_2, 
						(currentUser.getStatus() == UserStatus.ADMIN) ? "" : CURRENT_USER_FILTER, 
						(title == null) ? "" : TITLE_FILTER, 
						(type == null) ? "" : TYPE_FILTER,
						(user == null) ? "" : USER_FILTER,
						(createdFrom == null) ? "" : CREATED_FROM_FILTER,
						(createdTo == null) ? "" : CREATED_TO_FILTER, 
						(editedFrom == null) ? "" : EDITED_FROM_FILTER,
						(editedTo == null) ? "" : EDITED_TO_FILTER, 
						(publik == null) ? "" : PUBLIC_FILTER);
				try {
					final Connection connection = dataSource.getConnection();
					try {
						final PreparedStatement getMedia = connection.prepareStatement(getMediaQuery);
						try {
							int parameter = 1;
							getMedia.setBigDecimal(parameter++, minLatitude);
							getMedia.setBigDecimal(parameter++, maxLatitude);
							getMedia.setBigDecimal(parameter++, minLongitude);
							getMedia.setBigDecimal(parameter++, maxLongitude);
							if (currentUser.getStatus() != UserStatus.ADMIN)
								getMedia.setString(parameter++, currentUser.getEmail());
							if (title != null)
								getMedia.setString(parameter++, title);
							if (type != null)
								getMedia.setString(parameter++, type.getMimeTypePrefix());
							if (user != null)
								getMedia.setString(parameter++, user);
							if (createdFrom != null) {
								final Timestamp timestamp = new Timestamp(createdFrom.getTime());
								getMedia.setTimestamp(parameter++, timestamp);
							}
							if (createdTo != null) {
								final Timestamp timestamp = new Timestamp(createdTo.getTime());
								getMedia.setTimestamp(parameter++, timestamp);							
							}
							if (editedFrom != null) {
								final Timestamp timestamp = new Timestamp(editedFrom.getTime());
								getMedia.setTimestamp(parameter++, timestamp);
							}
							if (editedTo != null) {
								final Timestamp timestamp = new Timestamp(editedTo.getTime());
								getMedia.setTimestamp(parameter++, timestamp);
							}
							if (publik != null)
								getMedia.setBoolean(parameter++, publik);
							final ResultSet getMediaResultSet = getMedia.executeQuery();
							try {
								final List<Media> media = new ArrayList<Media>();
								while(getMediaResultSet.next()) {
									final String id = getMediaResultSet.getString("id");
									try {
										media.add(new Media(id, getMediaResultSet.getString("type"), 
												getMediaResultSet.getLong("size"), getMediaResultSet.getInt("duration"),
												userService.getUser(getMediaResultSet.getString("user")),
												getMediaResultSet.getTimestamp("created"), 
												getMediaResultSet.getTimestamp("edited"), getMediaResultSet.getString("title"),
												getMediaResultSet.getBigDecimal("latitude"), getMediaResultSet.getBigDecimal("longitude"),
												getMediaResultSet.getBoolean("public")));
									} catch (final UserServiceException e) {
										LOGGER.log(Level.WARNING, "Error resolving user of media " + id, e);
									}
								}
								LOGGER.info("Retrieved " + media.size() + " media");
								return media;
							} finally {
								getMediaResultSet.close();
							}
						} finally {
							getMedia.close();
						}
					} finally {
						connection.close();
					}
				} catch (final SQLException e) {
					LOGGER.log(Level.WARNING, "Error retrieving media", e);
					throw new MediaServiceException("Error retrieving media", e);
				}
	}
	
	@Override
	public Media getMedia(final String id) throws MediaServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement getMedia = connection.prepareStatement(GET_MEDIUM);
				try {
					getMedia.setString(1, id);
					final ResultSet resultSet = getMedia.executeQuery();
					try {
						Media media = null;
						if (resultSet.next()) {
							media = new Media(id, resultSet.getString("type"), resultSet.getLong("size"), resultSet.getInt("duration"),
									userService.getUser(resultSet.getString("user")), resultSet.getTimestamp("created"),
									resultSet.getTimestamp("edited"), resultSet.getString("title"), resultSet.getBigDecimal("latitude"),
									resultSet.getBigDecimal("longitude"), resultSet.getBoolean("public"));
						}
						LOGGER.info((media == null) ? ("Media " + id + " not found") : ("Retrieved media " + id));
						return media;
					} finally {
						resultSet.close();
					}
				} finally {
					getMedia.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error retrieving media " + id, e);
			throw new MediaServiceException("Error retrieving media " + id, e);
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error retrieving media " + id, e);
			throw new MediaServiceException("Error retrieving media " + id, e);
		}
	}
	
	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException {
		try {
			final Media media = getMedia(id);
			if (media == null) {
				LOGGER.warning("Error downloading media " + id);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found"); // 404 Not found
				return;
			}
			response.setContentType(media.getType());
			response.setHeader("Content-disposition", "attachment; filename=" + media.getTitle() +
					//Vriskei tin epektash tou arxeiou kai tin prosthetei ston titlo tou arxeiou sto katevasma
					TikaConfig.getDefaultConfig().getMimeRepository().forName(media.getType()).getExtension());
			//Vriskei to arxeio me sugkekrimeno id
			final File file = new File(mediaDir, id);
			final FileInputStream input = new FileInputStream(file);
			try {
				final byte[] buffer = new byte[bufferSize];
				int read = 0;
				while((read = input.read(buffer)) > 0)
					//stelnei to arxeio ston xrhsth
					response.getOutputStream().write(buffer, 0, read);								
			} finally {
				input.close();
				response.getOutputStream().close();
			}
			LOGGER.info("Downloaded media " + id);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error downloading media " + id, e);
			throw new MediaServiceException("Error downloading media " + id, e);			
		} catch (final MimeTypeException e) {
			LOGGER.log(Level.WARNING, "Error downloading media " + id, e);
			throw new MediaServiceException("Error downloading media " + id, e);			
		}
	}
	
	@Override
	public void addMedia(final Media media, final InputStream input) throws MediaServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				//Xekinaei ena transaction
				connection.setAutoCommit(false);
				final PreparedStatement addMedia = connection.prepareStatement(ADD_MEDIA);
				try {
					// add in db
					addMedia.setString(1, media.getId());
					addMedia.setString(2, media.getType());
					addMedia.setLong(3, media.getSize());
					addMedia.setInt(4, media.getDuration());
					addMedia.setString(5, media.getUser().getEmail());
					addMedia.setTimestamp(6, new Timestamp(media.getCreated().getTime()));
					addMedia.setTimestamp(7, new Timestamp(media.getEdited().getTime()));
					addMedia.setString(8, media.getTitle());
					addMedia.setBigDecimal(9, media.getLatitude());
					addMedia.setBigDecimal(10, media.getLongitude());
					addMedia.setBoolean(11, media.isPublic());
					addMedia.executeUpdate();
					// add to disk
					final File file = new File(mediaDir, media.getId());
					file.createNewFile();
					try {
						final FileOutputStream output = new FileOutputStream(file);
						try {
							final byte[] buffer = new byte[bufferSize];
							int read = 0;
							while((read = input.read(buffer)) > 0)
								output.write(buffer, 0, read);								
						} finally {
							output.close();
						}
					//otidhpote borei na ginei throw (akoma kai error)
					} catch (final IOException e) {
						file.delete();
						throw e;
					}
					//epikurwsh tou transaction
					connection.commit();
					LOGGER.info("Added media " + media);
				} finally {
					addMedia.close();
				}
			} catch (final IOException e) {
				//epanafora stin arxikh tous katastash ta panta
				connection.rollback();
				//enhmerwsh kai to upoloipo stack (autoi pou to kalesan)
				//oti kati den phge kala.
				throw e;
			} finally {
				connection.close();
			}
		}catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error adding media " + media, e);
			throw new MediaServiceException("Error adding media " + media, e);
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error adding media " + media, e);
			throw new MediaServiceException("Error adding media " + media, e);
		}
	}
	
	@Override
	public void editMedia(Media media) throws MediaServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement editMedia = connection.prepareStatement(EDIT_MEDIA);
				try {
					editMedia.setString(1, media.getTitle());
					editMedia.setBigDecimal(2, media.getLatitude());
					editMedia.setBigDecimal(3, media.getLongitude());
					editMedia.setBoolean(4, media.isPublic());
					editMedia.setString(5, media.getId());//to UUID
					editMedia.executeUpdate();
				} finally {
					editMedia.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error editing media " + media, e);
			throw new MediaServiceException("Error editing media " + media, e);
		}
	}
	
	@Override
	public void deleteMedia(final String id) throws MediaServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement deleteMedia = connection.prepareStatement(DELETE_MEDIA);
				try {
					//Diagrafh tou media apo tin vash
					deleteMedia.setString(1, id);
					deleteMedia.executeUpdate();
					//Diagrafh tou media apo ton disko
					new File(mediaDir, id).delete();
					LOGGER.info("Deleted media " + id);
				} finally {
					deleteMedia.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error deleting media " + id, e);
			throw new MediaServiceException("Error deleting media " + id, e);
		}
	}
}
