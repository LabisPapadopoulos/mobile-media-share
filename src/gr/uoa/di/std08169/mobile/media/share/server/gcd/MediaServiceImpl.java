package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.datastore.DatastoreV1.BeginTransactionRequest;
import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.LookupRequest;
import com.google.api.services.datastore.DatastoreV1.LookupResponse;
import com.google.api.services.datastore.DatastoreV1.ReadOptions;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreOptions;
import com.google.protobuf.ByteString;

public class MediaServiceImpl implements ExtendedMediaService {
	private static final String MD5 = "MD5";
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private final Datastore datastore;

	public static void main(String[] args) {
		try {
			final UserService us = new UserServiceImpl("mobile-media-share",
					"844662940292-897cju71t8u9jhe2l9tsslpgmthanc8r@developer.gserviceaccount.com",
					"/home/labis/Downloads/de8bf38b6e98e7e8dc88227929e58ab64f611e46-privatekey.p12");
		} catch (UserServiceException e) {
			e.printStackTrace();
		}
	}
	
	public MediaServiceImpl(final String dataset, final String serviceAccount, final String keyFile) throws UserServiceException {
		try {
			//Gia kathe project h google dinei enan eikoniko logariasmo gia sundesh sto Google Data 
			//Dhmiourgeia anagnwristikou eikonikou xrhsth
			final GoogleCredential credential = new GoogleCredential.Builder()
				//zhtaei asfales sundesh me ssl
		        .setTransport(GoogleNetHttpTransport.newTrustedTransport())
		        .setJsonFactory(new JacksonFactory())
		        //se poia domains epitrepetai na sundethei
		        .setServiceAccountScopes(DatastoreOptions.SCOPES)
		        //tou username tou eikonikou xrhsth
		        .setServiceAccountId(serviceAccount)
		        //pistopoihtiko tou eikonikou xrhsth
		        .setServiceAccountPrivateKeyFromP12File(new File(keyFile))
		        .build();
			//Sundesh me to cloud (Google Cloud Datastore)
			datastore = DatastoreFactory.get().
					//Dhlwnei se poio dataset (onoma vashs) tha sundethei me ta antistoixa username kai pistopoihtiko
					create(new DatastoreOptions.Builder().dataset(dataset).credential(credential).build());
		} catch (final GeneralSecurityException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + UserServiceImpl.class.getName(), e);
			throw new UserServiceException("Error initializing " + UserServiceImpl.class.getName(), e);
		} catch (final IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + UserServiceImpl.class.getName(), e);
			throw new UserServiceException("Error initializing " + UserServiceImpl.class.getName(), e);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + UserServiceImpl.class.getName(), e);
			throw new UserServiceException("Error initializing " + UserServiceImpl.class.getName(), e);
		}
	}

	/* "SELECT id, type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
		"FROM Media " +
		"WHERE (public OR \"user\" = ?)%s%s%s%s%s%s%s%s%s " +
		"LIMIT ? OFFSET ?;"
		
		String TITLE_FILTER = " AND match(title, ?)"; //match: sunartish stin postgres
		String TYPE_FILTER = " AND type LIKE (? || '%')"; //Typos kai meta otidhpote (%)
		String USER_FILTER = " AND (\"user\" = ?)";
		String CREATED_FROM_FILTER = " AND (created >= ?)";
		String CREATED_TO_FILTER = " AND (created <= ?)";
		String EDITED_FROM_FILTER = " AND (edited >= ?)";
		String EDITED_TO_FILTER = " AND (edited <= ?)";
		String PUBLIC_FILTER = " AND (public = ?)";
		String ORDERING = " ORDER BY %s %s";
		String ASCENDING = "ASC";
		String DESCENDING = "DESC";
	 */
	@Override
	public MediaResult getMedia(final String currentUser, final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending) throws MediaServiceException {
		// TODO Auto-generated method stub
		
//		try {
//			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
//			
//			LookupResponse result = datastore.lookup(
//					LookupRequest.newBuilder().addKey(
//							Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(Media.class.getName()).setName(email))
//					).setReadOptions(ReadOptions.newBuilder().setTransaction(transaction).build()).build());
//			
//			datastore.commit(CommitRequest.newBuilder().setTransaction(transaction).build());
			
//		} catch (DatastoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		return null;
	}

	/*
	 * SELECT id, type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
		"FROM Media " +
		"WHERE (public OR \"user\" = ?) " +
		"AND (latitude >= ?) AND (latitude <= ?) " + //minlat <= latitude <= maxlat
		"AND (longitude >= ?) AND (longitude <= ?)%s%s%s%s%s%s%s%s;"; //minlng <= longitude <= maxlng
	 */
	@Override
	public List<Media> getMedia(final String currentUser, final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo,
			final Boolean publik, final BigDecimal minLatitude, final BigDecimal minLongitude,
			final BigDecimal maxLatitude, final BigDecimal maxLongitude) throws MediaServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * "SELECT type, size, duration, \"user\", created, edited, title, latitude, longitude, public " +
		"FROM Media " +
		"WHERE id = ?;";
	 */
	@Override
	public Media getMedia(final String id) throws MediaServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * "DELETE FROM Media " +
		"WHERE id = ?;";
	 */
	@Override
	public void deleteMedia(final String id) throws MediaServiceException {
		// TODO Auto-generated method stub
		
	}

	/*
	 * "INSERT INTO Media (id, type, size, duration, \"user\", " +
		"created, edited, title, latitude, longitude, public) " +
		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	 */
	@Override
	public void addMedia(final Media media, InputStream input)
			throws MediaServiceException {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException {
		// TODO Auto-generated method stub
		
	}
}
