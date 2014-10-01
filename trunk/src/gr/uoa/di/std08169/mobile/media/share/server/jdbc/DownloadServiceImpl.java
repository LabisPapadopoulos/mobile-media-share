package gr.uoa.di.std08169.mobile.media.share.server.jdbc;

import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadService;
import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.download.Download;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DownloadServiceImpl implements DownloadService {
	private static final Logger LOGGER = Logger.getLogger(DownloadServiceImpl.class.getName());
	private static final String GET_DOWNLOAD =  "SELECT media, \"user\", client, \"timestamp\" " +
												"FROM Downloads " +
												"WHERE token = ? AND \"timestamp\" > ?;";
	private static final String DELETE_DOWNLOAD = "DELETE FROM Downloads " +
												  "WHERE token = ?;";
	private static final String ADD_DOWNLOAD =  "INSERT INTO Downloads " +
												"(token, media, \"user\", client, \"timestamp\") " +
												"VALUES (?, ?, ?, ?, ?);";
	private static final String DELETE_DOWNLOADS =  "DELETE FROM Downloads " +
													"WHERE \"timestamp\" <= ?;";
	
	private final DataSource dataSource; 
	private final long timeout;
	private Thread thread;
	
	public DownloadServiceImpl(final DataSource dataSource, final long timeout) {
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
							//diagrafontai osa einai: timestamp + timeout <= now() =>
							//timestamp <= now() - timeout
							deleteDownloads(new Date(new Date().getTime() - timeout));
						} catch (final DownloadServiceException e) {
							LOGGER.log(Level.WARNING, "Error cleaning up timedout downloads", e);
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
	public Download getDownload(final String token) throws DownloadServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement getDownload = connection.prepareStatement(GET_DOWNLOAD);
				try {
					final PreparedStatement deleteDownload = connection.prepareStatement(DELETE_DOWNLOAD);
					try {
						getDownload.setString(1, token);
						//to download to thelei na mhn exei lhxei
						getDownload.setTimestamp(2, new Timestamp(new Date().getTime() - timeout));
						deleteDownload.setString(1, token);
						//anoigei ena transaction gia na kanei duo praxeis mazi (na epistrepsei ena 
						//download kai na to svhsei meta). An kati apo ta duo paei strava na mhn kanei tipota apo ta duo.
						connection.setAutoCommit(false);
						try {
							final ResultSet resultSet = getDownload.executeQuery();
							try {
								final Download download = (resultSet.next()) ?
										new Download(token, resultSet.getString("media"), resultSet.getString("user"),
												resultSet.getString("client"),
												new Date(resultSet.getTimestamp("timestamp").getTime())) : null;
								deleteDownload.executeUpdate();
								connection.commit();
								LOGGER.info((download == null) ? ("Download " + token + " not found") : ("Retrieved download " + token)); 
								return download;
							} finally {
								resultSet.close();
							}
						} catch (SQLException e) {
							//ta xana epanaferei stin arxikh tous katastash se periptwsh apotuxias
							connection.rollback();
							throw e;
						}
					} finally {
						deleteDownload.close();
					}
				} finally {
					getDownload.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error retrieving download " + token, e);
			throw new DownloadServiceException("Error retrieving download " + token, e);
		}
	}

	@Override
	public void addDownload(final Download download) throws DownloadServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(ADD_DOWNLOAD);
				try {
					preparedStatement.setString(1, download.getToken());
					preparedStatement.setString(2, download.getMedia());
					preparedStatement.setString(3, download.getUser());
					preparedStatement.setString(4, download.getClient());
					preparedStatement.setTimestamp(5, new Timestamp(download.getTimestamp().getTime()));
					preparedStatement.executeUpdate();
					LOGGER.info("Added download " + download);
				} finally {
					preparedStatement.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error adding download " + download, e);
			throw new DownloadServiceException("Error adding download " + download, e);
			
		}
	}

	@Override
	public void deleteDownloads(final Date timestampTo) throws DownloadServiceException {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_DOWNLOADS);
				try {
					preparedStatement.setTimestamp(1, new Timestamp(timestampTo.getTime()));
					preparedStatement.executeUpdate();
					LOGGER.info("Deleted downloads up to " + timestampTo);
				} finally {
					preparedStatement.close();
				}
			} finally {
				connection.close();
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "Error deleting downloads up to " + timestampTo, e);
			throw new DownloadServiceException("Error deleting downloads up to " + timestampTo, e);
		}
	}
}
