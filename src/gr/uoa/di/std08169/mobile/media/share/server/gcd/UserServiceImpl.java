package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;

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
import com.google.api.services.datastore.DatastoreV1.Property;
import com.google.api.services.datastore.DatastoreV1.PropertyFilter;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.QueryResultBatch;
import com.google.api.services.datastore.DatastoreV1.ReadOptions;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.DatastoreV1.Value;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.api.services.datastore.client.DatastoreOptions;
import com.google.protobuf.ByteString;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.Utilities;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

public class UserServiceImpl implements UserService {
	//Regular expression gia na spaei to mail sto '@' kai stin '.'
	private static final Pattern EMAIL_REGEX = Pattern.compile("[@\\.]");
	//Regular expression gia na spaei to name sta white spaces: ' ' 
	private static final Pattern NAME_REGEX = Pattern.compile("\\s");
	private static final String MD5 = "MD5";
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	
	private final Datastore datastore;
	private final long timeout;
	private Thread thread;
		
	public UserServiceImpl(final String dataset, final String serviceAccount, final String keyFile, final long timeout) throws UserServiceException {
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
			this.timeout = timeout; 
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

	/**
	 * @see https://developers.google.com/datastore/docs/concepts/queries#Datastore_Key_filters
	 *	
	 *	SELECT email, status, name, photo
	 *	FROM Users
	 *	--	WHERE match(email, ?) OR match(name, ?)
	 *	WHERE normalizedUser = ? -- normalizedUser -> tokens (lexeis) apo email kai onoma
	 *	LIMIT ?;
	 */
	@Override
	public UserResult getUsers(final String query, final int limit) throws UserServiceException {
		try {
			//Dhmiourgeia enos QueryBuilder gia to Query getUsers
			final Query.Builder getUsers = Query.newBuilder();
			//Kind gia to entity pou tha gurisei to query, san: FROM User.class.getName()
			getUsers.addKindBuilder().setName(User.class.getName());
			//WHERE token = query
			getUsers.setFilter(DatastoreHelper.makeFilter("normalizedUser", PropertyFilter.Operator.EQUAL,
									//tokens (lexeis) apo email kai onoma
					DatastoreHelper.makeValue(Utilities.normalize(query))));
			//Orio enos parapanw apotelesmatos (limit + 1) gia na doume an uparxoun kai alla
			getUsers.setLimit(limit + 1);
			final List<User> users = new ArrayList<User>();
			//Apotelesma tis ekteleshs tou query (apotelesmata kai metadata gi' auta)
			final QueryResultBatch batch = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getUsers).build()).getBatch();
			//Epitrofh twn katharwn apotelesmatwn apo to batch (Entities)
			final List<EntityResult> entities = batch.getEntityResultList();
			//opoio teleiwsei pio nwris, h lista 'h to limit stamataei to loop
			for (int i = 0; (i < limit) && (i < entities.size()); i++) {
				final Entity entity = entities.get(i).getEntity();
				//onoma sthlhs kai timh
				final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
				final String email = entity.getKey().getPathElement(0).getName();
				final UserStatus status = properties.containsKey("status") ?
						UserStatus.values()[Long.valueOf(DatastoreHelper.getLong(properties.get("status"))).intValue()] : null;
				final String name = properties.containsKey("name") ? DatastoreHelper.getString(properties.get("name")) : null;
				final String photo = properties.containsKey("photo") ? DatastoreHelper.getString(properties.get("photo")) : null;
				users.add(new User(email, status, name, photo));
			}
			LOGGER.info("Retrieved " + users.size() + " users (query: " + query + ", limit: " + limit + ")");
			return new UserResult(users, (batch.getEntityResultCount() > limit) ? -1 : users.size());
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving users (query: " + query + ", limit: " + limit + ")", e);
			throw new UserServiceException("Error retrieving users (query: " + query + ", limit: " + limit + ")", e);
		}
	}

	/**
	 * @see https://developers.google.com/datastore/docs/getstarted/start_java/
	 * 
	 *  SELECT status, name, photo
	 * 	FROM Users
	 *	WHERE email = ?;
	 */
	@Override
	public User getUser(final String email) throws UserServiceException {
		try {
			// begin transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			// execute query
			//anazhthsh sugkekrimenou xrhsth me id (to e-mail tou)
			LookupResponse result = datastore.lookup(
					// Dhmiougeia eperwthmatos (san prepare statement)
					LookupRequest.newBuilder().addKey( //prosthikh key (sunthikh where) sto query
					// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "User" kai to onoma tou (setName) na einai to email
					Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(User.class.getName()).setName(email))
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
				LOGGER.info("User " + email + " not found");
				return null;
			}
			//Olo to row ektos apo to primary key tou entity.
			//px: [password: md5 tou password, name: onoma tou xrhsth, photo: eikona xrhsth]
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			final UserStatus status = properties.containsKey("status") ?
					UserStatus.values()[Long.valueOf(DatastoreHelper.getLong(properties.get("status"))).intValue()] : null; 
			final String name = properties.containsKey("name") ? DatastoreHelper.getString(properties.get("name")) : null;
			final String photo = properties.containsKey("photo") ? DatastoreHelper.getString(properties.get("photo")) : null;
			LOGGER.info("Retrieved user " + email);
			return new User(email, status, name, photo);
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving user " + email, e);
			throw new UserServiceException("Error retrieving user " + email, e);
		}
	}
	
	/**
	 *  SELECT email, status, name, photo
	 *	FROM Users
	 *	WHERE token = ? AND
	 *  now - timeout < tokenTimestamp
	 */
	@Override
	public User getUserByToken(final String token) throws UserServiceException {
		try {
			//Dhmiourgeia enos QueryBuilder gia to Query getUsers
			final Query.Builder getUserByToken = Query.newBuilder();
			//Kind gia to entity pou tha gurisei to query, san: FROM User.class.getName()
			getUserByToken.addKindBuilder().setName(User.class.getName());
			//WHERE token = query
			getUserByToken.setFilter(DatastoreHelper.makeFilter("token", PropertyFilter.Operator.EQUAL, DatastoreHelper.makeValue(token)));
			//Apotelesma tis ekteleshs tou query (apotelesmata kai metadata gi' auta)
			//Epitrofh twn katharwn apotelesmatwn apo to batch (Entities)
			final List<EntityResult> entities = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getUserByToken).build()).
					getBatch().getEntityResultList();
			final Entity entity = (entities.size() > 0) ? entities.get(0).getEntity() : null;
			if (entity == null) {
				LOGGER.info("User with token " + token + " not found");
				return null;
			}
			//onoma sthlhs kai timh
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			//AND tokenTimestamp + timeout > now
			//expirationDate = tokenTimestamp + timeout
			final Date expirationDate = properties.containsKey("tokenTimestamp") ? 
					new Date(DatastoreHelper.getLong(properties.get("tokenTimestamp")) + timeout) : null;
			//expirationDate > now -> after: den exei lhxei to token tou xrhsth
			if ((expirationDate == null) || expirationDate.before(new Date())) { //an einai null 'h exei lhxei to token tou xrhsth
				LOGGER.info("User with token " + token + " not found");
				return null;
			}
			final String email = entity.getKey().getPathElement(0).getName();
			final UserStatus status = properties.containsKey("status") ?
					UserStatus.values()[Long.valueOf(DatastoreHelper.getLong(properties.get("status"))).intValue()] : null;
			final String name = properties.containsKey("name") ? DatastoreHelper.getString(properties.get("name")) : null;
			final String photo = properties.containsKey("photo") ? DatastoreHelper.getString(properties.get("photo")) : null;
			final User user = new User(email, status, name, photo);
			LOGGER.info("Retrieved user with token " + token);
			return user;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving user with token " + token, e);
			throw new UserServiceException("Error retrieving user with token " + token, e);
		}
	}

	/**
	 *	SELECT *
	 *	FROM Users
	 *	WHERE email = ?;
	 *
	 *	kai elegxos:
	 *	if (user.password == password) && ((user.status == NORMAL) || (user.status == ADMIN))
	 * 
	 */
	@Override
	public boolean isValidUser(final String email, final String password) throws UserServiceException {
		try {
			// begin transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			// execute query
			LookupResponse result = datastore.lookup(
					// Dhmiougeia eperwthmatos (san prepare statement)
					LookupRequest.newBuilder().addKey( //prosthikh key (sunthikh where) sto query
					// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "User" kai to onoma tou (setName) na einai to email
					Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(User.class.getName()).setName(email))
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
				LOGGER.info("User " + email + " is invalid");
				return false;
			}
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			final UserStatus status = properties.containsKey("status") ? 
					UserStatus.values()[Long.valueOf(DatastoreHelper.getLong(properties.get("status"))).intValue()] : null;
			final String md5Password = properties.containsKey("password") ? DatastoreHelper.getString(properties.get("password")) : null;
			//An o xrhsths den einai normal oute kai admin 'h den exei password
			if (((status != UserStatus.NORMAL) && (status != UserStatus.ADMIN)) || (password == null)) {
				LOGGER.info("User " + email + " is invalid");
				return false;
			}
			final boolean valid = md5Password.equals(Hex.encodeHexString(
							//Kwdikopoihsh tou string password se MD5
							MessageDigest.getInstance(MD5).digest(password.getBytes())));
			LOGGER.info("User " + email + " is " + (valid ? "valid" : "invalid"));
			return valid;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error validating user " + email, e);
			throw new UserServiceException("Error validating user " + email, e);
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.WARNING, "Error validating user " + email, e);
			throw new UserServiceException("Error validating user " + email, e);
		}
	}

	/**
	 *  INSERT INTO Users (email, normalizedUser, password, status, tokenTimestamp, token)
	 *	VALUES (?, ?, ?, ?, ?, ?);
	 */
	@Override
	public String addUser(final String email, final String password) throws UserServiceException {
		try {
			//Dhmiourgeia tou transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			final User user = getUser(email);
			if (user != null) { // o xrhsths den yparxei hdh
				LOGGER.info("User " + email + " already exists");
				return null;
			}
			final Date date = new Date();
			final Entity.Builder entityBuilder = Entity.newBuilder(); 
			//Orismos tou kleidiou tou pinaka (primary key)
			entityBuilder.setKey(Key.newBuilder().addPathElement(
					//san na anoikei ston pinaka User me anagnwristiko to email tou
					Key.PathElement.newBuilder().setKind(User.class.getName()).setName(email)));
			//Gemisma pediwn tou (san) pinaka - tou entity
			//prosthikh stin sthlh token tis lexeis tou mail
			//ftiaxnoume lista apo values (me tis lexeis tou mail)
			final List<Value> values = new ArrayList<Value>();
			for (String token : EMAIL_REGEX.split(email))
				values.add(DatastoreHelper.makeValue(Utilities.normalize(token)).build());
			//san value vazoume oles tis times ths listas
			entityBuilder.addProperty(Property.newBuilder().setName("normalizedUser").setValue(DatastoreHelper.makeValue(values)));
			//prosthikh tou pediou password me tin antistoixh timh
			entityBuilder.addProperty(Property.newBuilder().setName("password").setValue(DatastoreHelper.makeValue(
					//16adikh kwdikopoihsh gia apofugh periergwn xarakthrwn
					Hex.encodeHexString(
					//Kwdikopoihsh tou string password se MD5
					MessageDigest.getInstance(MD5).digest(password.getBytes())
					))));
			entityBuilder.addProperty(Property.newBuilder().setName("status").setValue(
					DatastoreHelper.makeValue(UserStatus.PENDING.ordinal())));
			entityBuilder.addProperty(Property.newBuilder().setName("tokenTimestamp").setValue(DatastoreHelper.makeValue(date.getTime())));
			
			final String token = Utilities.generateToken(email, date);
			entityBuilder.addProperty(Property.newBuilder().setName("token").setValue(DatastoreHelper.makeValue(token)));
			//Fortwsh sto transaction ena insert gia to entity pou ftiaxthte
	        commitRequestBuilder.getMutationBuilder().addInsert(entityBuilder.build());
			//ginetai to commit tou panw transaction (uparxei/den uparxei o xrhsths)
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info("Added user " + email);
			return token;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error adding user", e);
			throw new UserServiceException("Error adding user", e);
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.WARNING, "Error adding user", e);
			throw new UserServiceException("Error adding user", e);
		}
	}

	/**
	 * UPDATE Users 
	   SET (password = ?,) status = ?, tokenTimestamp = ?, token = ?, name = ?, photo = ?
	   WHERE email = ? AND ((tokenTimestamp IS NULL) OR
	   		(now - timeout < tokenTimestamp))
	 */
	@Override
	public String editUser(final User user, final String password) throws UserServiceException {
		try {
			final Date date = new Date();
			// begin transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//anazhthsh xrhsth gia na doume an exei ligmeno token
			LookupResponse result = datastore.lookup( 
					// Dhmiougeia eperwthmatos (san prepare statement)
					LookupRequest.newBuilder().addKey( //prosthikh key (sunthikh where) sto query
					// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "User" kai to onoma tou (setName) na einai to email
					Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(User.class.getName()).setName(user.getEmail()))
					//Epiloges (setReadOptions) gia to pws tha diavasei to apotelesma (san ResultSet) mesw 
													//tis ekteleshs tou transaction pou dhmiourghthike prin
					).setReadOptions(ReadOptions.newBuilder().setTransaction(transaction).build()).build());
			//Eggrafh (oti eferne to resultSet se rows)
			final Entity entity = 
				 //hasNext()
				(result.getFoundCount() > 0) ?
				// h prwth alliws null
				result.getFound(0).getEntity() : null;
			if (entity == null) {
				LOGGER.log(Level.WARNING, "Error editing user: not found");
				return null;
			}
			//Olo to row ektos apo to primary key tou entity.
			//px: [password: md5 tou password, name: onoma tou xrhsth, photo: eikona xrhsth]
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			//(now - timeout < tokenTimestamp) =>
			//(now < tokenTimestamp + timeout)
			final Date expirationDate = properties.containsKey("tokenTimestamp") ?
					new Date(DatastoreHelper.getLong(properties.get("tokenTimestamp")) + timeout) : null;
			if ((expirationDate != null) && expirationDate.before(date)) { //exei lhxei to token tou xrhsth 
				LOGGER.log(Level.WARNING, "Error editing user: user token expired");
				return null;
			}
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			final Entity.Builder entityBuilder = Entity.newBuilder(); 
			//Orismos tou kleidiou tou pinaka (primary key)
			entityBuilder.setKey(Key.newBuilder().addPathElement(
					//san na anoikei ston pinaka User me anagnwristiko to email tou
					Key.PathElement.newBuilder().setKind(User.class.getName()).setName(user.getEmail())));
			//Gemisma pediwn tou (san) pinaka - tou entity
			//prosthikh stin sthlh token tis lexeis tou mail
			//ftiaxnoume lista apo values (me tis lexeis tou mail)
			final List<Value> values = new ArrayList<Value>();
			for (String token : EMAIL_REGEX.split(user.getEmail()))
				values.add(DatastoreHelper.makeValue(Utilities.normalize(token)).build());
			//enhmerwsh tou normalize pediou kai me to onoma tou xrhsth
			if (user.getName() != null) {
				for (String token : NAME_REGEX.split(user.getName()))
					values.add(DatastoreHelper.makeValue(Utilities.normalize(token)).build());
			}
			//san value vazoume oles tis times ths listas
			entityBuilder.addProperty(Property.newBuilder().setName("normalizedUser").setValue(DatastoreHelper.makeValue(values)));
			if (password != null)
				//prosthikh tou pediou password me tin antistoixh timh
				entityBuilder.addProperty(Property.newBuilder().setName("password").setValue(DatastoreHelper.makeValue(
						//16adikh kwdikopoihsh gia apofugh periergwn xarakthrwn
						Hex.encodeHexString(
						//Kwdikopoihsh tou string password se MD5
						MessageDigest.getInstance(MD5).digest(password.getBytes())
						))));
			//an o xrhsths den exei xexasei to password tou kai exei hdh palio, to antigrafoume (logo edit)
			else if ((user.getStatus() != UserStatus.FORGOT) && properties.containsKey("password"))
				entityBuilder.addProperty(Property.newBuilder().setName("password").setValue(
						DatastoreHelper.makeValue(DatastoreHelper.getString(properties.get("password")))));
			
			entityBuilder.addProperty(Property.newBuilder().setName("status").setValue(
					DatastoreHelper.makeValue(user.getStatus().ordinal())));
			String token = null;
			if (user.getStatus() == UserStatus.FORGOT) { //vale token, timestamp
				entityBuilder.addProperty(Property.newBuilder().setName("tokenTimestamp").setValue(DatastoreHelper.makeValue(date.getTime())));
				token = Utilities.generateToken(user.getEmail(), date);
				entityBuilder.addProperty(Property.newBuilder().setName("token").setValue(DatastoreHelper.makeValue(token)));
			}
			if (user.getName() != null)
				entityBuilder.addProperty(Property.newBuilder().setName("name").setValue(DatastoreHelper.makeValue(user.getName())));
			if (user.getPhoto() != null)
				entityBuilder.addProperty(Property.newBuilder().setName("photo").setValue(DatastoreHelper.makeValue(user.getPhoto())));
			//Fortwsh sto transaction ena update gia to entity pou ftiaxthte
	        commitRequestBuilder.getMutationBuilder().addUpdate(entityBuilder.build());
			//ginetai to commit tou panw transaction (uparxei/den uparxei o xrhsths)
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info("User " + user.getEmail() + " edited successfully");
			return token;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error editing user", e);
			throw new UserServiceException("Error editing user", e);
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.WARNING, "Error editing user", e);
			throw new UserServiceException("Error editing user", e);
		}
	}

	/**
	 * 
	 */
	@Override
	public void deleteUser(final String email) throws UserServiceException {
		try {
			//Dhmiourgeia tou transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			//Dhmiourgeia mhnumatos gia to commit tou transaction 
			final CommitRequest.Builder commitRequestBuilder = CommitRequest.newBuilder();
			//Thetei to transaction sto commit
			commitRequestBuilder.setTransaction(transaction);
			//Fortwsh sto transaction ena delete
			//kanei delete sumfwna me kapoio key
	        commitRequestBuilder.getMutationBuilder().addDelete(Key.newBuilder().addPathElement(
					//san na anoikei ston pinaka User me anagnwristiko to email tou
					Key.PathElement.newBuilder().setKind(User.class.getName()).setName(email)));
			//ginetai to commit tou panw transaction (uparxei/den uparxei o xrhsths)
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info("User " + email + " deleted successfully");
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error deleting user", e);
			throw new UserServiceException("Error deleting user", e);
		}
	}
	
	/**
	 * SELECT email
	 * FROM Users
	 * WHERE tokenTimestamp <= now() - timeout;
	 *
	 */
	private List<String> getTimedoutUsers() throws UserServiceException {
		try {
			//Dhmiourgeia enos QueryBuilder gia to Query getTimedoutUsers
			final Query.Builder getTimedoutUsers = Query.newBuilder();
			//Kind gia to entity pou tha gurisei to query, san: FROM User.class.getName()
			getTimedoutUsers.addKindBuilder().setName(User.class.getName());
			//WHERE tokenTimestamp <= now() - timeout;
			getTimedoutUsers.setFilter(DatastoreHelper.makeFilter("tokenTimestamp", PropertyFilter.Operator.LESS_THAN_OR_EQUAL,
									//now() - timeout
					DatastoreHelper.makeValue(new Date().getTime() - timeout)));
			final List<String> emails = new ArrayList<String>();
			//Apotelesma tis ekteleshs tou query (apotelesmata kai metadata gi' auta)
			final QueryResultBatch batch = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getTimedoutUsers).build()).getBatch();
			//Epitrofh twn katharwn apotelesmatwn apo to batch (Entities)
			final List<EntityResult> entities = batch.getEntityResultList();
			for (EntityResult entityResult : entities)				
				emails.add(entityResult.getEntity().getKey().getPathElement(0).getName());
			LOGGER.info("Found " + emails.size() +" timedout users");
			return emails;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving timedout users", e);
			//Gia to front end
			throw new UserServiceException("Error retrieving timedout users", e);
		}
	}
}
