package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadService;
import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.download.Download;

public class DownloadServiceImpl implements DownloadService {
	private static final Logger LOGGER = Logger.getLogger(DownloadServiceImpl.class.getName());
	
	private final Datastore datastore;
	private final long timeout;
	private Thread thread;
	
	public DownloadServiceImpl(final String dataset, final String serviceAccount, final String keyFile, final long timeout) throws DownloadServiceException {
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
			LOGGER.log(Level.WARNING, "Error initializing " + DownloadServiceImpl.class.getName(), e);
			throw new DownloadServiceException("Error initializing " + DownloadServiceImpl.class.getName(), e);
		} catch (final IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + DownloadServiceImpl.class.getName(), e);
			throw new DownloadServiceException("Error initializing " + DownloadServiceImpl.class.getName(), e);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error initializing " + DownloadServiceImpl.class.getName(), e);
			throw new DownloadServiceException("Error initializing " + DownloadServiceImpl.class.getName(), e);
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
	
	/**
	 * SELECT media, user, client, timestamp
	 * FROM Downloads
	 * WHERE token = ?;
	 */
	@Override
	public Download getDownload(final String token) throws DownloadServiceException {
		try {
			// begin transaction
			final ByteString transaction = datastore.beginTransaction(BeginTransactionRequest.newBuilder().build()).getTransaction();
			// execute query
			//anazhthsh sugkekrimenou download me id (to token tou)
			LookupResponse result = datastore.lookup(
					// Dhmiougeia eperwthmatos (san prepare statement)
					LookupRequest.newBuilder().addKey( //prosthikh key (sunthikh where) sto query
					// prosthikh mias aplhs sunthikhs (sto where) opou na einai (setKind) "Download" kai to onoma tou (setName) na einai to token
					Key.newBuilder().addPathElement(Key.PathElement.newBuilder().setKind(Download.class.getName()).setName(token))
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
				LOGGER.info("Download " + token + " not found");
				return null;
			}
			//Olo to row ektos apo to primary key tou entity.
			final Map<String, Value> properties = DatastoreHelper.getPropertyMap(entity);
			final String media = properties.containsKey("media") ? DatastoreHelper.getString(properties.get("media")) : null;
			final String user = properties.containsKey("user") ? DatastoreHelper.getString(properties.get("user")) : null;
			final String client = properties.containsKey("client") ? DatastoreHelper.getString(properties.get("client")) : null;
			final Date timestamp = properties.containsKey("timestamp") ? new Date(DatastoreHelper.getLong(properties.get("timestamp"))) : null;
			LOGGER.info("Retrieved download " + token);
			return new Download(token, media, user, client, timestamp);
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving download " + token, e);
			throw new DownloadServiceException("Error retrieving download " + token, e);
		}
	}

	@Override
	public void addDownload(final Download download) throws DownloadServiceException {
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
					//san na anoikei ston pinaka Download me anagnwristiko to token tou
					Key.PathElement.newBuilder().setKind(Download.class.getName()).setName(download.getToken())));
			//Gemisma pediwn tou (san) pinaka - tou entity
			//prosthikh tou pediou media me tin antistoixh timh
			entityBuilder.addProperty(Property.newBuilder().setName("media").setValue(DatastoreHelper.makeValue(download.getMedia())));
			entityBuilder.addProperty(Property.newBuilder().setName("user").setValue(DatastoreHelper.makeValue(download.getUser())));
			entityBuilder.addProperty(Property.newBuilder().setName("client").setValue(DatastoreHelper.makeValue(download.getClient())));
			entityBuilder.addProperty(Property.newBuilder().setName("timestamp").setValue(DatastoreHelper.makeValue(new Date().getTime())));

			//Fortwsh sto transaction ena insert gia to entity pou ftiaxthte
	        commitRequestBuilder.getMutationBuilder().addInsert(entityBuilder.build());
			//ginetai to commit tou panw transaction
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info("Added download " + download);
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error adding download", e);
			throw new DownloadServiceException("Error adding download", e);
		}
	}

	@Override
	public void deleteDownloads(final Date timestampTo) throws DownloadServiceException {
		for (final String token : getTimedoutDownloads(timestampTo)) {
			deleteDownload(token);
		}
	}
	
	private List<String> getTimedoutDownloads(final Date timestampTo) throws DownloadServiceException {
		try {
			//Dhmiourgeia enos QueryBuilder gia to Query getTimedoutUsers
			final Query.Builder getTimedoutDownloads = Query.newBuilder();
			//Kind gia to entity pou tha gurisei to query, san: FROM Download.class.getName()
			getTimedoutDownloads.addKindBuilder().setName(Download.class.getName());
			//WHERE timestamp <= timestampTO;
			getTimedoutDownloads.setFilter(DatastoreHelper.makeFilter("timestamp", PropertyFilter.Operator.LESS_THAN_OR_EQUAL,
									//timestampTo
					DatastoreHelper.makeValue(timestampTo)));
			final List<String> tokens = new ArrayList<String>();
			//Apotelesma tis ekteleshs tou query (apotelesmata kai metadata gi' auta)
			final QueryResultBatch batch = datastore.runQuery(RunQueryRequest.newBuilder().setQuery(getTimedoutDownloads).build()).getBatch();
			//Epitrofh twn katharwn apotelesmatwn apo to batch (Entities)
			final List<EntityResult> entities = batch.getEntityResultList();
			for (EntityResult entityResult : entities)				
				tokens.add(entityResult.getEntity().getKey().getPathElement(0).getName());
			LOGGER.info("Found " + tokens.size() +" timedout downloads");
			return tokens;
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error retrieving timedout downloads", e);
			//Gia to front end
			throw new DownloadServiceException("Error retrieving timedout downloads", e);
		}
	}
	
	private void deleteDownload(final String token) throws DownloadServiceException {
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
					//san na anoikei ston pinaka Download me anagnwristiko to token tou
					Key.PathElement.newBuilder().setKind(Download.class.getName()).setName(token)));
	        
			//ginetai to commit tou panw transaction
			datastore.commit(commitRequestBuilder.build());
			LOGGER.info("Download deleted successfully");
		} catch (final DatastoreException e) {
			LOGGER.log(Level.WARNING, "Error deleting download", e);
			throw new DownloadServiceException("Error deleting download", e);
		}
	}
}
