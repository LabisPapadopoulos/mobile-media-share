package gr.uoa.di.std08169.mobile.media.share.server;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.datastore.DatastoreV1.BeginTransactionRequest;
import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.LookupRequest;
import com.google.api.services.datastore.DatastoreV1.LookupResponse;
import com.google.api.services.datastore.DatastoreV1.Property;
import com.google.api.services.datastore.DatastoreV1.ReadOptions;
import com.google.api.services.datastore.DatastoreV1.Value;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreOptions;
import com.google.protobuf.ByteString;

import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.User;
import gr.uoa.di.std08169.mobile.media.share.shared.UserResult;

public class GoogleCloudDatastoreUserServiceImpl implements UserService {
	private static final String MD5 = "MD5";
	private static final Logger LOGGER = Logger.getLogger(GoogleCloudDatastoreUserServiceImpl.class.getName());
	
	private final Datastore datastore;
	
	public static void main(String[] args) {
		try {
			final UserService us = new GoogleCloudDatastoreUserServiceImpl("mobile-media-share",
					"844662940292-897cju71t8u9jhe2l9tsslpgmthanc8r@developer.gserviceaccount.com",
					"/home/labis/Downloads/de8bf38b6e98e7e8dc88227929e58ab64f611e46-privatekey.p12");
			final User u = us.getUser("lala@example.org");
			if (u != null) {
				System.out.println("email: " + u.getEmail());
				System.out.println("name: " + u.getName());
				System.out.println("photo: " + u.getPhoto());
			}
			us.addUser("foo1@bar.gr", "secret2");
			System.out.println("Added user");
			us.addUser("labis1@bar.buz", "secret");
			System.out.println("Added user");
			
		} catch (UserServiceException e) {
			e.printStackTrace();
		}
	}
	
	public GoogleCloudDatastoreUserServiceImpl(final String dataset, final String serviceAccount, final String keyFile) throws UserServiceException {
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
			LOGGER.log(Level.WARNING, "Error initializing " + GoogleCloudDatastoreUserServiceImpl.class.getName(), e);
			throw new UserServiceException("Error initializing " + GoogleCloudDatastoreUserServiceImpl.class.getName(), e);
		} catch (final IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + GoogleCloudDatastoreUserServiceImpl.class.getName(), e);
			throw new UserServiceException("Error initializing " + GoogleCloudDatastoreUserServiceImpl.class.getName(), e);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + GoogleCloudDatastoreUserServiceImpl.class.getName(), e);
			throw new UserServiceException("Error initializing " + GoogleCloudDatastoreUserServiceImpl.class.getName(), e);
		}
	}

	@Override
	public UserResult getUsers(String query, int limit)
			throws UserServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * https://developers.google.com/datastore/docs/getstarted/start_java/
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
			User user = null;
			if (entity != null) {
				String name = null;
				String photo = null;
				for (Property property : entity.getPropertyList()) {
					if (property.getName().equals("name"))
						name = property.getValue().getStringValue();
					if (property.getName().equals("photo"))
						photo = property.getValue().getStringValue();
				}
				user = new User(email, name, photo);
			}
			LOGGER.info((entity == null) ? ("User " + email + " not found") : ("Retrieved user " + email));
			return user;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving user " + email, e);
			throw new UserServiceException("Error retrieving user " + email, e);
		}
	}

	@Override
	public boolean isValidUser(String email, String password)
			throws UserServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addUser(final String email, final String password) throws UserServiceException {
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
				//prosthikh tou pediou password me tin antistoixh timh
				entityBuilder.addProperty(Property.newBuilder().setName("password").setValue(Value.newBuilder().setStringValue(
						//16adikh kwdikopoihsh gia apofugh periergwn xarakthrwn
						Hex.encodeHexString(
						//Kwdikopoihsh tou string password se MD5
						MessageDigest.getInstance(MD5).digest(password.getBytes())
						))));
				//Fortwsh sto transaction ena insert gia to entity pou ftiaxthte
		        commitRequestBuilder.getMutationBuilder().addInsert(entityBuilder.build());
			}
			//ginetai to commit tou panw transaction (uparxei/den uparxei o xrhsths)
			datastore.commit(commitRequestBuilder.build());
			return (user == null);
		} catch (final DatastoreException e) {
			throw new UserServiceException("Error adding user", e);
		} catch (final NoSuchAlgorithmException e) {
			throw new UserServiceException("Error adding user", e);
		}
	}

}
