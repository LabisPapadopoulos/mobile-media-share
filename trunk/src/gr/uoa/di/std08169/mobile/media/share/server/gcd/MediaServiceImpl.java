package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.datastore.DatastoreV1.BeginTransactionRequest;
import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.EntityResult;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.LookupRequest;
import com.google.api.services.datastore.DatastoreV1.LookupResponse;
import com.google.api.services.datastore.DatastoreV1.PropertyFilter;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.ReadOptions;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.DatastoreV1.Value;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.api.services.datastore.client.DatastoreOptions;
import com.google.protobuf.ByteString;

public class MediaServiceImpl implements ExtendedMediaService {
	private static final String MD5 = "MD5";
	private static final Pattern WHITESPACE_REGEX = Pattern.compile("\\s+");
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private final Datastore datastore;

	public static void main(String[] args) {
		try {
			final UserService us = new UserServiceImpl("mobile-media-share",
					"844662940292-897cju71t8u9jhe2l9tsslpgmthanc8r@developer.gserviceaccount.com",
					"/home/labis/Downloads/de8bf38b6e98e7e8dc88227929e58ab64f611e46-privatekey.p12");
			us.addUser("foo@bar.buz", "secret");
			us.addUser("foo1@bar.buz", "secret");
			us.addUser("foo2@bar.buz", "secret");
			System.out.println("First: " + us.isValidUser("foo@bar.buz", "secret"));
			System.out.println("Second: " + us.isValidUser("foo@bar.buz", "fjakfja"));
			us.getUsers("foo@bar.buz", 2);
//			final User u = us.getUser("lala@example.org");
//			if (u != null) {
//				System.out.println("email: " + u.getEmail());
//				System.out.println("name: " + u.getName());
//				System.out.println("photo: " + u.getPhoto());
//			}
//			us.addUser("foo1@bar.gr", "secret2");
//			System.out.println("Added user");
//			us.addUser("labis1@bar.buz", "secret");
//			System.out.println("Added user");
			
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
/*		try {
			final Set<Media> media = new HashSet<Media>();
			//Dhmiourgeia enos QueryBuilder gia to Query getPublicMedia
			final Query.Builder getPublicMedia = Query.newBuilder();
			//Kind gia to entity pou tha gurisei to query, san: FROM Media.class.getName()
			getPublicMedia.addKindBuilder().setName(Media.class.getName());
			//WHERE name = query
//			typePrefix: audio/
//			type: 		audio/mp3
//			typePrefix exartatai amfimonoshmanta apo type			
			if (type != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("typePrefix", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(type.getMimeTypePrefix())));
			if (user != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("user", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(user)));
			if(createdFrom != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("created", PropertyFilter.Operator.GREATER_THAN_OR_EQUAL, DatastoreHelper.makeValue(createdFrom)));
			if(createdTo != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("created", PropertyFilter.Operator.LESS_THAN_OR_EQUAL, DatastoreHelper.makeValue(createdTo)));
			if(editedFrom != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("edited", PropertyFilter.Operator.GREATER_THAN_OR_EQUAL, DatastoreHelper.makeValue(editedFrom)));
			if(editedTo != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("edited", PropertyFilter.Operator.LESS_THAN_OR_EQUAL, DatastoreHelper.makeValue(editedTo)));
			if(title != null)
				getPublicMedia.setFilter(DatastoreHelper.makeFilter("titleTokens", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(Utilities.normalize(title))));
			if(publik != null) { // ton noiazei to public
				if (publik) //thelei na brei mono public
					getPublicMedia.setFilter(DatastoreHelper.makeFilter("public", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(publik)));
				else { // thelei na brei mono private, opote mono ta dika tou
					getPublicMedia.setFilter(DatastoreHelper.makeFilter("public", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(false)));
					getPublicMedia.setFilter(DatastoreHelper.makeFilter("user", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(currentUser)));
				}
			} else {
//				public or mine
//				! ((!public) and (!mine));
				
			}
			for (EntityResult result : datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getPublicMedia).build()).getBatch().getEntityResultList()) {
				//Euresh twn pediwn tou antikeimenou
				final Map<String, Value> properties = DatastoreHelper.getPropertyMap(result.getEntity());
				final String id = result.getEntity().getKey().getPathElement(0).getName();
				final String mimeType = properties.containsKey("type") ? DatastoreHelper.getString(properties.get("type")) : null;
				final Long size = properties.containsKey("size") ? DatastoreHelper.getLong(properties.get("size")) : null;
				final Integer duration = properties.containsKey("duration") ?  Long.valueOf(properties.get("duration").getIntegerValue()).intValue() : null;
				final String username = properties.containsKey("user") ? DatastoreHelper.getString(properties.get("user")) : null;
				final User _user = null; // TODO get user from username
				final Date created = properties.containsKey("created") ? new Date(DatastoreHelper.getTimestamp(properties.get("created"))) : null;
				final Date edited = properties.containsKey("edited") ? new Date(DatastoreHelper.getTimestamp(properties.get("edited"))) : null;
				final String _title = properties.containsKey("title") ? DatastoreHelper.getString(properties.get("title")) : null;
				final BigDecimal latitude = properties.containsKey("latitude") ? new BigDecimal(DatastoreHelper.getDouble(properties.get("latitude"))) : null;
				final BigDecimal longitude = properties.containsKey("longitude") ? new BigDecimal(DatastoreHelper.getDouble(properties.get("longitude"))) : null;
				final boolean _public = properties.containsKey("public") ? DatastoreHelper.getBoolean(properties.get("public")) : null;
				media.add(new Media(id, mimeType, size, duration, _user, created, edited, _title, latitude, longitude, _public));
			}
			final List<Media> userList = new ArrayList<Media>();
			userList.addAll(media);
			LOGGER.info("Retrieved " + userList.size() + " users (query: " + query + ", limit: " + limit + ")");
			return new UserResult(userList.subList(0, limit), media.size());
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving users (query: " + query + ", limit: " + limit + ")", e);
			throw new UserServiceException("Error retrieving users (query: " + query + ", limit: " + limit + ")", e);
		} */		
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
	public void deleteMedia(final String id) throws MediaServiceException { //Google drive
		// TODO Auto-generated method stub
		
	}

	/*
	 * "INSERT INTO Media (id, type, size, duration, \"user\", " +
		"created, edited, title, latitude, longitude, public) " +
		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	 */
	@Override
	public void addMedia(final Media media, InputStream input) //Google Drive
			throws MediaServiceException {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException { //Google Drive
		// TODO Auto-generated method stub
		
	}
}
