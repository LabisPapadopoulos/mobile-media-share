package gr.uoa.di.std08169.mobile.media.share.server;

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

import javax.sql.DataSource;

import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaType;

public class MediaServiceImpl implements MediaService {
	private static final String GET_MEDIA = "SELECT id, type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
											"FROM Media " +
											"WHERE TRUE%s%s%s%s%s%s%s%s%s " +
											"LIMIT ? OFFSET ?;";
	//concat string se postgres -> ||
	private static final String TITLE_FILTER = " AND title LIKE ('%' || ? || '%')";
	private static final String TYPE_FILTER = " AND type LIKE (? || '%')"; //Typos kai meta otidhpote (%)
	private static final String USER_FILTER = " AND user = ?";
	private static final String CREATED_FROM_FILTER = " AND created >= ?";
	private static final String CREATED_TO_FILTER = " AND created <= ?";
	private static final String EDITED_FROM_FILTER = " AND edited >= ?";
	private static final String EDITED_TO_FILTER = " AND edited <= ?";
	private static final String PUBLIC_FILTER = " AND public = ?";
	private static final String ORDERING = " ORDER BY %s %s";
	private static final String ASCENDING = "ASC";
	private static final String DESCENDING = "DESC";
	
	private static final String COUNT_MEDIA = 	"SELECT COUNT(*) AS total " +
												"FROM Media " +
												"WHERE TRUE%s%s%s%s%s%s%s%s;";
	
	private static final String ADD_MEDIA = "INSERT INTO Media (id, type, size, duration, \"user\", " +
											"created, edited, title, latitude, longitude, public) " +
											"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private DataSource dataSource;
	private UserService userService;
	
	//Setters gia ta beans
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setUserService(final UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public MediaResult getMedia(final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo,
			final Boolean publik, final Integer start, final Integer length, final String orderField,
			final boolean ascending) throws MediaServiceException {
		
		//Xtisimo tou string me oles ta pithana filtra pou exei dialexei o xrhsths
		final String getMediaQuery = 
				String.format(GET_MEDIA, (title == null) ? "" : TITLE_FILTER, 
				(type == null) ? "" : TYPE_FILTER, (user == null) ? "" : USER_FILTER,
				(createdFrom == null) ? "" : CREATED_FROM_FILTER, (createdTo == null) ? "" : CREATED_TO_FILTER, 
				(editedFrom == null) ? "" : EDITED_FROM_FILTER, (editedTo == null) ? "" : EDITED_TO_FILTER, 
				(publik == null) ? "" : PUBLIC_FILTER,
				(orderField == null) ? "" : String.format(ORDERING, orderField,
						ascending ? ASCENDING : DESCENDING));
		//Xtisimo string gia to plithos twn media pou uparxoun stin vash
		final String countMediaQuery = 
				String.format(COUNT_MEDIA, (title == null) ? "" : TITLE_FILTER, 
				(type == null) ? "" : TYPE_FILTER, (user == null) ? "" : USER_FILTER,
				(createdFrom == null) ? "" : CREATED_FROM_FILTER, (createdTo == null) ? "" : CREATED_TO_FILTER, 
				(editedFrom == null) ? "" : EDITED_FROM_FILTER, (editedTo == null) ? "" : EDITED_TO_FILTER, 
				(publik == null) ? "" : PUBLIC_FILTER);
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement getMedia = connection.prepareStatement(getMediaQuery);
				try {
					final PreparedStatement countMedia = connection.prepareStatement(countMediaQuery);
					try {
						int parameter = 1;
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
			LOGGER.log(Level.WARNING, "Error retriving media", e);
			throw new MediaServiceException("Error retriving media", e);
		}
	}
	
	@Override
	public void addMedia(final Media media) throws MediaServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement addMedia = connection.prepareStatement(ADD_MEDIA);
				try {
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
					LOGGER.info("Added media " + media);
				} finally {
					addMedia.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error adding media " + media, e);
			throw new MediaServiceException("Error adding media " + media, e);
		}
	}
}
