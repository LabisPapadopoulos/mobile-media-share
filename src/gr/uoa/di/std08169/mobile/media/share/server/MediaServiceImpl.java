package gr.uoa.di.std08169.mobile.media.share.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.Media;

public class MediaServiceImpl implements MediaService {
	private static final String ADD_MEDIA = "INSERT INTO Media (id, type, size, duration, \"user\", " +
											"created, edited, title, latitude, longitude, public) " +
											"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private DataSource dataSource;
	
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
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
					LOGGER.info("Added media " + media.getId());
				} finally {
					addMedia.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error adding media", e);
			throw new MediaServiceException("Error adding media", e);
		}
	}
}
