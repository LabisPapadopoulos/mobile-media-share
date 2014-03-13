package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypeException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.datastore.DatastoreV1.BeginTransactionRequest;
import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.EntityResult;
import com.google.api.services.datastore.DatastoreV1.Filter;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.LookupRequest;
import com.google.api.services.datastore.DatastoreV1.LookupResponse;
import com.google.api.services.datastore.DatastoreV1.Property;
import com.google.api.services.datastore.DatastoreV1.PropertyFilter;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.ReadOptions;
import com.google.api.services.datastore.DatastoreV1.RollbackRequest;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.DatastoreV1.Value;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.api.services.datastore.client.DatastoreOptions;
import com.google.api.services.drive.Drive;
import com.google.protobuf.ByteString;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

public class MediaServiceImpl implements ExtendedMediaService {
	/*
	 * see https://developers.google.com/drive/web/scopes (Google Drive scopes)
	 */
	private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive";
	private static final Pattern WORD_REGEX = Pattern.compile("\\W+");
	private static final String TITLE = "title";
	private static final String TYPE = "type";
	private static final String SIZE = "size";
	private static final String DURATION = "duration";
	//user se dipla autakia epeidh einai desmeumenh lexh gia tin ulopoihsh tis vashs
	private static final String USER = "\"user\"";
	private static final String CREATED = "created";
	private static final String EDITED = "edited";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String PUBLIC = "public";
	private static final Logger LOGGER = Logger.getLogger(MediaServiceImpl.class.getName());
	
	private final Datastore datastore;
	private final Drive drive;
	private final UserService userService;
	
	public MediaServiceImpl(final String dataset, final String serviceAccount, final String keyFile, final UserService userService) throws UserServiceException {
		try {
			//Gia kathe project h google dinei enan eikoniko logariasmo gia sundesh sto Google Data 
			//Dhmiourgeia anagnwristikou eikonikou xrhsth

			//Metaferei dedomena mesw http sto paraskhnio 
			final HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport(); //https
		    //Metatrepei dedomena se json gia na metaferthoun mesw http apo to transport
			final JacksonFactory jsonFactory = new JacksonFactory();
			
			
			//Scope gia n' anagnwrisei poies uphresies borei na sundethei o logariasmos xrhsth (serviceAccount)
			//sto Google Datastore, Drive
			final List<String> scopes = new ArrayList<String>();
			scopes.addAll(DatastoreOptions.SCOPES);
			scopes.add(DRIVE_SCOPE);
			
			final GoogleCredential credential = new GoogleCredential.Builder()
				//zhtaei asfales sundesh me ssl
		        .setTransport(transport)
		        .setJsonFactory(jsonFactory)
		        //se poia domains epitrepetai na sundethei (sto Datastore)
		        .setServiceAccountScopes(scopes)
		        //tou username tou eikonikou xrhsth
		        .setServiceAccountId(serviceAccount)
		        //pistopoihtiko tou eikonikou xrhsth
		        .setServiceAccountPrivateKeyFromP12File(new File(keyFile))
		        .build();
			
			//Sundesh me to cloud (Google Cloud Datastore)
			datastore = DatastoreFactory.get().
					//Dhlwnei se poio dataset (onoma vashs) tha sundethei me ta antistoixa username kai pistopoihtiko
					create(new DatastoreOptions.Builder().dataset(dataset).credential(credential).build());			
			//Sundesh me to google drive
			drive = new Drive.Builder(transport, jsonFactory, credential).build();
			
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
		
		final List<Media> media = getMedia(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik, null, null,
				null, null);
		Collections.sort(media, new Comparator<Media>() {
			@Override
			public int compare(final Media media1, final Media media2) {
				int comparison = 0;
				if (TITLE.equals(orderField))
					//Gia ASC: to media1 einai megalutero apo to media2 an o titlos tou media1 einai megaluteros apo ton titlo tou media2 
					comparison = media1.getTitle().compareTo(media2.getTitle());
				else if (TYPE.equals(orderField))
					comparison = media1.getType().compareTo(media2.getType());
				else if (SIZE.equals(orderField))
					comparison = Long.valueOf(media1.getSize() - media2.getSize()).intValue();
				else if (DURATION.equals(orderField))
					comparison = media1.getDuration() - media2.getDuration();
				else if (USER.equals(orderField))
					comparison = media1.getUser().compareTo(media2.getUser());
				else if (CREATED.equals(orderField))
					comparison = media1.getCreated().compareTo(media2.getCreated());
				else if (EDITED.equals(orderField))
					comparison = media1.getEdited().compareTo(media2.getEdited());
				else if (LATITUDE.equals(orderField))
					comparison = media1.getLatitude().compareTo(media2.getLatitude());
				else if (LONGITUDE.equals(orderField))
					comparison = media1.getLongitude().compareTo(media2.getLongitude());
				else if (PUBLIC.equals(orderField)) //taxinomoume prwta ta private kai meta ta public
					comparison = media1.isPublic() ? //to media1 einai public
							//an kai to media2 einai public -> comparison = 0 (isa)
							//alliws to media2 einai private -> comparison = 1 (prwta to media2)
							(media2.isPublic() ? 0 : 1) :
							//to media1 einai private
							//an to media2 einai public -> comparison = -1 (prwta to media1)
							//alliws kai to media2 einai private -> comparison = 0 (isa)
							(media2.isPublic() ? -1 : 0);
				else
					comparison = 0;				
				//se periptwsh DESC einai to antitheto (-1) tou ASC  
				return comparison * (ascending ? 1 : -1);
			}
		});
		
		final List<Media> result = (start >= media.size()) ? Collections.<Media>emptyList() :
				((start + length <= media.size()) ? media.subList(start, start + length) : 
				media.subList(start, media.size()));
		LOGGER.info("Retrieved " + result.size() + " media");
		return new MediaResult(result, media.size());
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
				media.add(parseMedia(result.getEntity()));
			for (EntityResult result : myPrivateMedia)
				media.add(parseMedia(result.getEntity()));

			//Epeidh den upostirizei pollaples suntikes anisotitas sto idio Query, tha filtraroume meta
			//Filtrarisma apotelesmatwn gia longitude, created, edited pou den boroun na boun sto filtro
			//Xrhsh Iterator gia diagrafh plhroforias oso diasxizetai h lista twn apotelesmatwn
			final Iterator<Media> i = media.iterator();
			while (i.hasNext()) {
				Media medium = i.next();
				if (((minLatitude != null) && (medium.getLatitude().compareTo(minLatitude) < 0)) || 		// latitude < minlat
						((maxLatitude != null) && (medium.getLatitude().compareTo(maxLatitude) > 0)) || 	// latitude > maxlat
						((minLongitude != null) && (medium.getLongitude().compareTo(minLongitude) < 0)) || 	// longitude < minlng
						((maxLongitude != null) && (medium.getLongitude().compareTo(maxLongitude) > 0)) || 	// longitude > maxlng
						((createdFrom != null) && (medium.getCreated().compareTo(createdFrom) < 0)) || 		// created < createdFrom
						((createdTo != null) && (medium.getCreated().compareTo(createdTo) > 0)) || 			// created > createdTo
						((editedFrom != null) && (medium.getEdited().compareTo(editedFrom) < 0)) || 		// edited < editedFrom
						((editedTo != null) && (medium.getEdited().compareTo(editedTo) > 0))) 				// edited > editedTo
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
		try {
			//Dhmiourgeia tou transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			//Dhmiourgeia mhnumatos gia pithano rollback tou transaction
			final RollbackRequest.Builder rollbackRequestBuilder = RollbackRequest.newBuilder();
			rollbackRequestBuilder.setTransaction(transaction);
			// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "Media" kai to onoma tou (setName) na einai to email
			final Key.Builder key = Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(Media.class.getName()).setName(id));
			try {
				final LookupResponse result = datastore.lookup(
						// Dhmiougeia eperwthmatos (san prepare statement)
						LookupRequest.newBuilder().addKey(key) //prosthikh key (sunthikh where) sto query
						//Epiloges (setReadOptions) gia to pws tha diavasei to apotelesma (san ResultSet) mesw 
														//tis ekteleshs tou transaction pou dhmiourghthike prin
						.setReadOptions(ReadOptions.newBuilder().setTransaction(transaction).build()).build());
				// commit me vash to arxiko transaction
				//Eggrafh (oti eferne to resultSet se rows)
				final Entity entity = 
					 //hasNext()
					(result.getFoundCount() > 0) ?
					// h prwth alliws null
					result.getFound(0).getEntity() : null;
				if (entity == null) {
					datastore.commit(commitRequestBuilder.build());
					LOGGER.info("Media " + id + " not found");
					return null;
				}
				final Media media = parseMedia(entity);
				datastore.commit(commitRequestBuilder.build());
				LOGGER.info("Retrieved media " + id);
				return media;
			} catch (final DatastoreException e) {
				datastore.rollback(rollbackRequestBuilder.build());
				LOGGER.log(Level.WARNING, "Error retrieving media " + id, e);
				throw new MediaServiceException("Error retrieving media " + id, e);
			} catch (final UserServiceException e) {
				datastore.rollback(rollbackRequestBuilder.build());
				LOGGER.log(Level.WARNING, "Error retrieving media " + id, e);
				throw new MediaServiceException("Error retrieving media " + id, e);
			}
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error  retrieving media " + id, e);
			throw new MediaServiceException("Error retrieving media " + id, e);
		}
	}
	
	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException { //Google Drive
		try {
			
			// begin transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			// execute query
			LookupResponse result = datastore.lookup(
					// Dhmiougeia eperwthmatos (san prepare statement)
					LookupRequest.newBuilder().addKey( //prosthikh key (sunthikh where) sto query
					// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "User" kai to onoma tou (setName) na einai to email
					Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(Media.class.getName()).setName(id))
					//Epiloges (setReadOptions) gia to pws tha diavasei to apotelesma (san ResultSet) mesw 
													//tis ekteleshs tou transaction pou dhmiourghthike prin
					).setReadOptions(ReadOptions.newBuilder().setTransaction(transaction).build()).build());
			// commit me vash to arxiko transaction
			datastore.commit(CommitRequest.newBuilder().setTransaction(transaction).build());
			//Eggrafh (oti eferne to resultSet se rows)
			final Entity entity = 
				 //hasNext()
				(result.getFoundCount() > 0) ?
				// h prwth alliws null
				result.getFound(0).getEntity() : null;
			if (entity == null) {
				LOGGER.warning("Error downloading media " + id);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found"); // 404 Not found
				return;
			}
			//Olo to row ektos apo to primary key tou entity.
			//px: [password: md5 tou password, name: onoma tou xrhsth, photo: eikona xrhsth]
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			final String title = properties.containsKey("title") ? DatastoreHelper.getString(properties.get("title")) : null;
			final String type = properties.containsKey("type") ? DatastoreHelper.getString(properties.get("type")) : null;
			final String driveId = properties.containsKey("driveId") ? DatastoreHelper.getString(properties.get("driveId")) : null;
			if (driveId == null) {
				LOGGER.warning("Error downloading media " + id);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found"); // 404 Not found
				return;
			}
			response.setContentType(type);
			response.setHeader("Content-disposition", "attachment; filename=" + title +
					//Vriskei tin epektash tou arxeiou kai tin prosthetei ston titlo tou arxeiou sto katevasma
					TikaConfig.getDefaultConfig().getMimeRepository().forName(type).getExtension());
			final byte[] buffer = new byte[1024];
			final InputStream input = drive.getRequestFactory().buildGetRequest(
					new GenericUrl(drive.files().get(driveId).execute().getDownloadUrl())).execute().getContent();
			try {
				final ServletOutputStream output = response.getOutputStream();
				try {
					int read = 0;
					while (read != -1) {
						read = input.read(buffer);
						output.write(buffer, 0, read);
					}
				} finally {
					output.close();
				}
			} finally {
				input.close();
			}
			LOGGER.info("Downloaded media " + id);
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error downloading media " + id, e);
			throw new MediaServiceException("Error downloading media " + id, e);	
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error downloading media " + id, e);
			throw new MediaServiceException("Error downloading media " + id, e);	
		} catch (final MimeTypeException e) {
			LOGGER.log(Level.WARNING, "Error downloading media " + id, e);
			throw new MediaServiceException("Error downloading media " + id, e);	
		}
	}
	
	/*
	 * "INSERT INTO Media (id, type, size, duration, \"user\", " +
		"created, edited, title, latitude, longitude, public) " +
		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		
		
	 * Save file se google drive kai ta metadedomena tou se google cloud datastore 
	 * see https://developers.google.com/drive/web/examples/java#saving_new_files
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
			//Dhmiourgeia mhnumatos gia pithano rollback tou transaction
			final RollbackRequest.Builder rollbackRequestBuilder = RollbackRequest.newBuilder();
			rollbackRequestBuilder.setTransaction(transaction);
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
			entityBuilder.addProperty(Property.newBuilder().setName("created").setValue(DatastoreHelper.makeValue(media.getCreated())));
			entityBuilder.addProperty(Property.newBuilder().setName("edited").setValue(DatastoreHelper.makeValue(media.getEdited())));
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
	        // add se google drive
			String driveId = null;
	        try {
	        	//Google Drive arxeio
	        	final com.google.api.services.drive.model.File driveFile = new com.google.api.services.drive.model.File();
	        	driveFile.setMimeType(media.getType());
	        	driveFile.setCreatedDate(new DateTime(media.getCreated()));
	        	driveFile.setModifiedDate(new  DateTime(media.getEdited()));
	        	driveFile.setTitle(media.getTitle());
	        	//apothikeush twn dedomenwn tou arxeiou sto google drive
	        	//insert: minima gia apothikeush tou arxeiou
	        	//execute: ektelei to insert
	        	driveId = drive.files().insert(driveFile, new InputStreamContent(media.getType(), input)).execute().getId();
	        	//apothikeush tou id tou Google Drive sto Google Datastore gia to sugkekrimeno arxeio
	        	entityBuilder.addProperty(Property.newBuilder().setName("driveId").setValue(DatastoreHelper.makeValue(driveId)));
	        	//apothikeush sto Google Datastore 
				commitRequestBuilder.getMutationBuilder().addInsert(entityBuilder.build());
				datastore.commit(commitRequestBuilder.build());
			} catch (final IOException e) { //apotuxia apothikeushs twn dedomenwn tou arxeiou sto google drive
				datastore.rollback(rollbackRequestBuilder.build());
				throw e;
			} catch (final DatastoreException e) { //apotuxia apothikeushs twn metadedomenwn tou arxeiou sto google drive
				if (driveId != null)
					drive.files().delete(driveId);
				throw e;
			}
	        LOGGER.info("Added media " + media);
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error adding media", e);
			throw new MediaServiceException("Error adding media", e);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error adding media", e);
			throw new MediaServiceException("Error adding media", e);
		}
	}

	@Override
	public void editMedia(Media media) throws MediaServiceException {
		try {
			//Dhmiourgeia tou transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			//Dhmiourgeia mhnumatos gia pithano rollback tou transaction
			final RollbackRequest.Builder rollbackRequestBuilder = RollbackRequest.newBuilder();
			rollbackRequestBuilder.setTransaction(transaction);
			final Entity.Builder entityBuilder = Entity.newBuilder(); 
			//Orismos tou kleidiou tou pinaka (primary key)
			entityBuilder.setKey(Key.newBuilder().addPathElement(
					//san na anoikei ston pinaka Media me anagnwristiko to ID tou
					Key.PathElement.newBuilder().setKind(Media.class.getName()).setName(media.getId())));
			entityBuilder.addProperty(Property.newBuilder().setName("edited").setValue(DatastoreHelper.makeValue(new Date())));
			entityBuilder.addProperty(Property.newBuilder().setName("title").setValue(DatastoreHelper.makeValue(media.getTitle())));
			final List<Value> values = new ArrayList<Value>();
			for (String token : WORD_REGEX.split(media.getTitle()))
				values.add(DatastoreHelper.makeValue(Utilities.normalize(token)).build());
			//san value vazoume oles tis times ths listas
			entityBuilder.addProperty(Property.newBuilder().setName("titleToken").setValue(DatastoreHelper.makeValue(values)));
			entityBuilder.addProperty(Property.newBuilder().setName("latitude").setValue(DatastoreHelper.makeValue(media.getLatitude().doubleValue())));
			entityBuilder.addProperty(Property.newBuilder().setName("longitude").setValue(DatastoreHelper.makeValue(media.getLongitude().doubleValue())));
			entityBuilder.addProperty(Property.newBuilder().setName("public").setValue(DatastoreHelper.makeValue(media.isPublic())));
			//Fortwsh sto transaction ena update gia to entity pou ftiaxthte
			commitRequestBuilder.getMutationBuilder().addUpdate(entityBuilder.build());
			try {
				datastore.commit(commitRequestBuilder.build());
		        LOGGER.info("Edited media " + media);
			} catch (final DatastoreException e) { //apotuxia apothikeushs twn metadedomenwn tou arxeiou sto google drive
				datastore.rollback(rollbackRequestBuilder.build());
				LOGGER.log(Level.WARNING, "Error editing media " + media, e);
				throw new MediaServiceException("Error editing media " + media, e);
			}
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error editing media " + media, e);
			throw new MediaServiceException("Error editing media " + media, e);
		}
	}
	
	/*
	 * "DELETE FROM Media " +
		"WHERE id = ?;";
	 */
	@Override
	public void deleteMedia(final String id) throws MediaServiceException { //Google drive
		try {
			//Dhmiourgeia tou transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			//Dhmiourgeia mhnumatos gia pithano rollback tou transaction
			final RollbackRequest.Builder rollbackRequestBuilder = RollbackRequest.newBuilder();
			rollbackRequestBuilder.setTransaction(transaction);
			
			// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "Media" kai to onoma tou (setName) na einai to email
			final Key.Builder key = Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(Media.class.getName()).setName(id));
			
			LookupResponse result = datastore.lookup(
					// Dhmiougeia eperwthmatos (san prepare statement)
					LookupRequest.newBuilder().addKey(key) //prosthikh key (sunthikh where) sto query
					//Epiloges (setReadOptions) gia to pws tha diavasei to apotelesma (san ResultSet) mesw 
													//tis ekteleshs tou transaction pou dhmiourghthike prin
					.setReadOptions(ReadOptions.newBuilder().setTransaction(transaction).build()).build());
			//Eggrafh (oti eferne to resultSet se rows)
			final Entity entity = 
				 //hasNext()
				(result.getFoundCount() > 0) ?
				// h prwth alliws null
				result.getFound(0).getEntity() : null;
			if (entity == null) {
				LOGGER.warning("Error deleting media " + id + ": media not found");
				throw new MediaServiceException("Error deleting media " + id + ": media not found");
			}
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			final String driveId = properties.containsKey("driveId") ? DatastoreHelper.getString(properties.get("driveId")) : null;
			if (driveId == null) {
				LOGGER.warning("Error deleting media " + id + ": media not found");
				throw new MediaServiceException("Error deleting media " + id + ": media not found");
			}
			// delete media apo datastore
			commitRequestBuilder.getMutationBuilder().addDelete(key);
	        try {	        	
	        	//delete: minima gia diagrafh tou arxeiou
	        	//execute: ektelei to delete
	        	//Diagrafh tou arxeiou apo to Google Drive		
    			drive.files().delete(driveId).execute();
				datastore.commit(commitRequestBuilder.build());
			} catch (final IOException e) { //apotuxia diagrafhs twn dedomenwn tou arxeiou sto google drive
				datastore.rollback(rollbackRequestBuilder.build());
				throw e;
			}
	        LOGGER.info("Deleted media " + id);
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error deleting media " + id, e);
			throw new MediaServiceException("Error deleting media " + id, e);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error deleting media " + id, e);
			throw new MediaServiceException("Error deleting media " + id, e);
		}
	}

	private Media parseMedia(final Entity entity) throws UserServiceException {
		//Euresh twn pediwn tou antikeimenou
		final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
		final String id = entity.getKey().getPathElement(0).getName();
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
