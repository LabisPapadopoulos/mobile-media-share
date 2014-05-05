package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserResult;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

public class UserServiceImpl implements UserService {
	//Regular expression gia na spaei to mail sto '@' kai stin '.'
	private static final Pattern EMAIL_REGEX = Pattern.compile("[@\\.]");
	private static final String MD5 = "MD5";
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	
	private final Datastore datastore;
		
	public UserServiceImpl(final String dataset, final String serviceAccount, final String keyFile) throws UserServiceException {
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

	/**
	 * @see https://developers.google.com/datastore/docs/concepts/queries#Datastore_Key_filters
	 *
	 *	(
	 *		SELECT *
	 *		FROM Users
	 *		WHERE email = ?
	 *	) UNION (
	 *		SELECT *
	 *		FROM Users
	 *		WHERE name = ?
	 *	);
	 *
	 *	SELECT * FROM Users WHERE TRUE AND (match(email, ?) OR match(name, ?)) LIMIT ?;
	 *	foo: (match(email, ?) OR match(name, ?))
	 *	SELECT * FROM Users WHERE TRUE AND foo = TRUE LIMIT ?
	 *	
	 *	
	 */
	@Override
	public UserResult getUsers(final String query, final int limit) throws UserServiceException {
		try {
			//Dhmiourgeia enos QueryBuilder gia to Query getUsers
			final Query.Builder getUsers = Query.newBuilder();
			//Kind gia to entity pou tha gurisei to query, san: FROM User.class.getName()
			getUsers.addKindBuilder().setName(User.class.getName());
			//WHERE token = query
			getUsers.setFilter(DatastoreHelper.makeFilter("token", PropertyFilter.Operator.EQUAL,
					DatastoreHelper.makeValue(Utilities.normalize(query))));
			//Orio enos parapanw apotelesmatos (limit + 1) gia na doume an uparxoun kai alla
			getUsers.setLimit(limit + 1);
			final List<User> users = new ArrayList<User>();
			//Apotelesma tis ekteleshs tou query (apotelesmata kai metadata gi' auta)
			final QueryResultBatch batch = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getUsers).build()).getBatch();
			//Epitrofh twn katharwn apotelesmatwn apo to batch (Entities)
			final List<EntityResult> entities = batch.getEntityResultList();
			for (int i = 0; (i < limit) && (i < entities.size()); i++) { // opoio teleiwsei pio nwris, h lista 'h to limit
				final Entity entity = entities.get(i).getEntity();
				//onoma sthlhs kai timh
				final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
				final String email = entity.getKey().getPathElement(0).getName();
				final UserStatus status = properties.containsKey("status") ?
						UserStatus.values()[Long.valueOf(DatastoreHelper.getLong(properties.get("admin"))).intValue()] : null;
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
	 *  SELECT *
	 *	FROM Users
	 *	WHERE email = ?;
	 */
	@Override
	public User getUser(final String email) throws UserServiceException {
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
	 *	SELECT *
	 *	FROM Users
	 *	WHERE email = ?;
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
			final String md5Password = properties.containsKey("password") ? DatastoreHelper.getString(properties.get("password")) : null;
			if (password == null) {
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
	 *  INSERT INTO Users (email, password)
	 *	VALUES (?, md5(?));
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
			if (user == null) { // o xrhsths den yparxei hdh
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
				entityBuilder.addProperty(Property.newBuilder().setName("token").setValue(DatastoreHelper.makeValue(values)));
				//prosthikh tou pediou password me tin antistoixh timh
				entityBuilder.addProperty(Property.newBuilder().setName("password").setValue(DatastoreHelper.makeValue(
						//16adikh kwdikopoihsh gia apofugh periergwn xarakthrwn
						Hex.encodeHexString(
						//Kwdikopoihsh tou string password se MD5
						MessageDigest.getInstance(MD5).digest(password.getBytes())
						))));
				// TODO add status, token, token timestamp
				// TODO return token
				//Fortwsh sto transaction ena insert gia to entity pou ftiaxthte
		        commitRequestBuilder.getMutationBuilder().addInsert(entityBuilder.build());
			}
			//ginetai to commit tou panw transaction (uparxei/den uparxei o xrhsths)
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info(((user == null) ? ("Added user " + email) : ("User " + email + " already exists")));
//			return (user == null);
			return null;
		} catch (final DatastoreException e) {
			throw new UserServiceException("Error adding user", e);
		} catch (final NoSuchAlgorithmException e) {
			throw new UserServiceException("Error adding user", e);
		}
	}

	@Override
	public String editUser(final User user, final String password) throws UserServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(final String email) throws UserServiceException {
		// TODO Auto-generated method stub
	}
}
