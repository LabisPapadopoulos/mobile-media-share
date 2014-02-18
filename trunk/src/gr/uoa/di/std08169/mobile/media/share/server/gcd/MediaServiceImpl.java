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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.google.api.services.datastore.DatastoreV1.Filter;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.Property;
import com.google.api.services.datastore.DatastoreV1.PropertyFilter;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.DatastoreV1.Value;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.api.services.datastore.client.DatastoreOptions;
import com.google.protobuf.ByteString;

public class MediaServiceImpl implements ExtendedMediaService {
	private static final Pattern WORD_REGEX = Pattern.compile("\\W+");
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private final Datastore datastore;
	private final UserService userService;
	
	public MediaServiceImpl(final String dataset, final String serviceAccount, final String keyFile, final UserService userService) throws UserServiceException {
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
			this.userService = userService;
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
		//Xrhsh listas gia na borei na valei pollapla filtra s' ena query (sto Where)
		//Lista apo filters gia to Query publicMedia.
		final List<Filter> publicMediaFilters = new ArrayList<Filter>();
		//Lista apo filters gia to Query myPrivateMedia.
		final List<Filter> myPrivateMediaFilters = new ArrayList<Filter>();
		//ola ta public media
		publicMediaFilters.add(DatastoreHelper.makeFilter("public", PropertyFilter.Operator.EQUAL,
				DatastoreHelper.makeValue(true)).build());
		//mono private media
		myPrivateMediaFilters.add(DatastoreHelper.makeFilter("public", PropertyFilter.Operator.EQUAL,
				DatastoreHelper.makeValue(false)).build());
		//kai mono media tou currentUser
		myPrivateMediaFilters.add(DatastoreHelper.makeFilter("user", PropertyFilter.Operator.EQUAL,
				DatastoreHelper.makeValue(currentUser)).build());
		if (title != null) {
			final Filter titleFilter = DatastoreHelper.makeFilter("titleToken", PropertyFilter.Operator.EQUAL,
					//gia na petaei tonous, na mageireuei kefalaia - mikra
					DatastoreHelper.makeValue(Utilities.normalize(title))).build();
			publicMediaFilters.add(titleFilter);
			myPrivateMediaFilters.add(titleFilter);
		}
//		typePrefix: audio/
//		type: 		audio/mp3
//		typePrefix exartatai amfimonoshmanta apo type
		if (type != null) {
			final Filter typeFilter = DatastoreHelper.makeFilter("typePrefix", PropertyFilter.Operator.EQUAL,
					DatastoreHelper.makeValue(type.getMimeTypePrefix())).build();
			publicMediaFilters.add(typeFilter);
			myPrivateMediaFilters.add(typeFilter);
		}
		if (user != null) {
			final Filter userFilter = DatastoreHelper.makeFilter("user", PropertyFilter.Operator.EQUAL,
					DatastoreHelper.makeValue(user)).build();
			publicMediaFilters.add(userFilter);
			myPrivateMediaFilters.add(userFilter);
		}
		//Dhmiourgeia enos QueryBuilder gia to Query getPublicMedia kai enos allou gia to Query getMyPrivateMedia 
		final Query.Builder getPublicMedia = Query.newBuilder();
		getPublicMedia.addKindBuilder().setName(Media.class.getName());
		getPublicMedia.setFilter(DatastoreHelper.makeFilter(publicMediaFilters));
		final Query.Builder getMyPrivateMedia = Query.newBuilder();
		//Kind gia to entity pou tha gurisoun ta queries, san: FROM Media.class.getName()
		getMyPrivateMedia.addKindBuilder().setName(Media.class.getName());
		getMyPrivateMedia.setFilter(DatastoreHelper.makeFilter(myPrivateMediaFilters));
		try {
			//Arxikopoihsh adeiwn listwn gia na gemisoun mono an ektelestei to antistoixo query
			List<EntityResult> publicMedia = Collections.<EntityResult>emptyList();
			List<EntityResult> myPrivateMedia = Collections.<EntityResult>emptyList();
			if (publik == null) { //epistrefontai kai ta public media kai ta myPrivate media (de mas noiazei katastash public) 
				publicMedia = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getPublicMedia).build()).getBatch().getEntityResultList();
				myPrivateMedia = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getMyPrivateMedia).build()).getBatch().getEntityResultList();
			} else if (publik) { //epistrefontai mono ta public media
				publicMedia = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getPublicMedia).build()).getBatch().getEntityResultList();
			} else { //epistrefontai mono ta my private media
				myPrivateMedia = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getMyPrivateMedia).build()).getBatch().getEntityResultList();
			}
			final List<Media> media = new ArrayList<Media>();
			for (EntityResult result : publicMedia)
				media.add(parseMedia(result));
			for (EntityResult result : myPrivateMedia)
				media.add(parseMedia(result));

			//Epeidh den upostirizei pollaples suntikes anisotitas sto idio Query, tha filtraroume meta
			//Filtrarisma apotelesmatwn gia longitude, created, edited pou den boroun na boun sto filtro
			//Xrhsh Iterator gia diagrafh plhroforias oso diasxizetai h lista twn apotelesmatwn
			final Iterator<Media> i = media.iterator();
			while (i.hasNext()) {
				Media medium = i.next();
				if (((minLatitude != null) && (medium.getLatitude().compareTo(minLatitude) < 0)) || // latitude < minlat
						((maxLatitude != null) && (medium.getLatitude().compareTo(maxLatitude) > 0)) || // latitude > maxlat
						((minLongitude != null) && (medium.getLongitude().compareTo(minLongitude) < 0)) || // longitude < minlng
						((maxLongitude != null) && (medium.getLongitude().compareTo(maxLongitude) > 0)) || // longitude > maxlng
						((createdFrom != null) && (medium.getCreated().compareTo(createdFrom) < 0)) || // created < createdFrom
						((createdTo != null) && (medium.getCreated().compareTo(createdTo) > 0)) || // created > createdTo
						((editedFrom != null) && (medium.getEdited().compareTo(editedFrom) < 0)) || // edited < editedFrom
						((editedTo != null) && (medium.getEdited().compareTo(editedTo) > 0))) // edited > editedTo
					i.remove();
			}
			
			LOGGER.info("Retrieved " + media.size() + " media");
			return media;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving media", e);
			throw new MediaServiceException("Error retrieving media", e);
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error retrieving media", e);
			throw new MediaServiceException("Error retrieving media", e);
		}
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
	public void addMedia(final Media media, InputStream input) throws MediaServiceException { //Google Drive
		try {
			//Dhmiourgeia tou transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			final Entity.Builder entityBuilder = Entity.newBuilder(); 
			//Orismos tou kleidiou tou pinaka (primary key)
			entityBuilder.setKey(Key.newBuilder().addPathElement(
					//san na anoikei ston pinaka Media me anagnwristiko to ID tou
					Key.PathElement.newBuilder().setKind(Media.class.getName()).setName(media.getId())));
			entityBuilder.addProperty(Property.newBuilder().setName("type").setValue(DatastoreHelper.makeValue(media.getType())));
			entityBuilder.addProperty(Property.newBuilder().setName("typePrefix").setValue(DatastoreHelper.makeValue(MediaType.getMediaType(media.getType()).getMimeTypePrefix())));
			entityBuilder.addProperty(Property.newBuilder().setName("size").setValue(DatastoreHelper.makeValue(media.getSize())));
			entityBuilder.addProperty(Property.newBuilder().setName("duration").setValue(DatastoreHelper.makeValue(media.getDuration())));
			entityBuilder.addProperty(Property.newBuilder().setName("user").setValue(DatastoreHelper.makeValue(media.getUser().getEmail())));
			entityBuilder.addProperty(Property.newBuilder().setName("created").setValue(DatastoreHelper.makeValue(media.getCreated().getTime())));
			entityBuilder.addProperty(Property.newBuilder().setName("edited").setValue(DatastoreHelper.makeValue(media.getEdited().getTime())));
			entityBuilder.addProperty(Property.newBuilder().setName("title").setValue(DatastoreHelper.makeValue(media.getTitle())));
			final List<Value> values = new ArrayList<Value>();
			for (String token : WORD_REGEX.split(media.getTitle()))
				values.add(DatastoreHelper.makeValue(Utilities.normalize(token)).build());
			//san value vazoume oles tis times ths listas
			entityBuilder.addProperty(Property.newBuilder().setName("titleToken").setValue(DatastoreHelper.makeValue(values)));
			entityBuilder.addProperty(Property.newBuilder().setName("latitude").setValue(DatastoreHelper.makeValue(media.getLatitude().doubleValue())));
			entityBuilder.addProperty(Property.newBuilder().setName("longitude").setValue(DatastoreHelper.makeValue(media.getLongitude().doubleValue())));
			entityBuilder.addProperty(Property.newBuilder().setName("public").setValue(DatastoreHelper.makeValue(media.isPublic())));
			//Fortwsh sto transaction ena insert gia to entity pou ftiaxthte
	        commitRequestBuilder.getMutationBuilder().addInsert(entityBuilder.build());
	
	        // add se google drive
	        
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info("Added media " + media);
		} catch (final DatastoreException e) {
			throw new MediaServiceException("Error adding media", e);
		}
	}

	
	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException { //Google Drive
		// TODO Auto-generated method stub
		
	}
	
	private Media parseMedia(final EntityResult result) throws UserServiceException {
		//Euresh twn pediwn tou antikeimenou
		final Map<String, Value> properties = DatastoreHelper.getPropertyMap(result.getEntity());
		final String id = result.getEntity().getKey().getPathElement(0).getName();
		final String mimeType = properties.containsKey("type") ? DatastoreHelper.getString(properties.get("type")) : null;
		final Long size = properties.containsKey("size") ? DatastoreHelper.getLong(properties.get("size")) : null;
		final Integer duration = properties.containsKey("duration") ?  Long.valueOf(properties.get("duration").getIntegerValue()).intValue() : null;
		final String username = properties.containsKey("user") ? DatastoreHelper.getString(properties.get("user")) : null;
		final User user = userService.getUser(username);
		final Date created = properties.containsKey("created") ? new Date(DatastoreHelper.getTimestamp(properties.get("created"))) : null;
		final Date edited = properties.containsKey("edited") ? new Date(DatastoreHelper.getTimestamp(properties.get("edited"))) : null;
		final String title = properties.containsKey("title") ? DatastoreHelper.getString(properties.get("title")) : null;
		final BigDecimal latitude = properties.containsKey("latitude") ? new BigDecimal(DatastoreHelper.getDouble(properties.get("latitude"))) : null;
		final BigDecimal longitude = properties.containsKey("longitude") ? new BigDecimal(DatastoreHelper.getDouble(properties.get("longitude"))) : null;
		final boolean publik = properties.containsKey("public") ? DatastoreHelper.getBoolean(properties.get("public")) : null;
		return new Media(id, mimeType, size, duration, user, created, edited, title, latitude, longitude, publik);
	}
}
