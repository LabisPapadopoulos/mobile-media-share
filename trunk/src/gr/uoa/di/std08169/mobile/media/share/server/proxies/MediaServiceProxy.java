package gr.uoa.di.std08169.mobile.media.share.server.proxies;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;

//Ylopoiei ena mediaservice xrhsimopoiwntas ena allo mediaService. (Wrapper)
//Ylopoiei kai mnhmh cache gia ta apotelesmata twn anazhthsewn gia ta media
//Tha kaleitai o proxy kai to spring tha dialegei jdbcMediaService 'h gcdMediaService
public class MediaServiceProxy implements ExtendedMediaService {
	//abstract: den boroume na ftiaxoume auto to antikeimeno (einai san interface)
	private abstract class GetMediaCommonArguments {
		private final String currentUser;
		private final String title;
		private final MediaType type;
		private final String user;
		private final Date createdFrom;
		private final Date createdTo;
		private final Date editedFrom;
		private final Date editedTo;
		private final Boolean publik;
		
		private GetMediaCommonArguments(final String currentUser, final String title, final MediaType type, final String user,
				final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik) {
			this.currentUser = currentUser;
			this.title = title;
			this.type = type;
			this.user = user;
			this.createdFrom = createdFrom;
			this.createdTo = createdTo;
			this.editedFrom = editedFrom;
			this.editedTo = editedTo;
			this.publik = publik;
		}
		
		@Override
		public boolean equals(final Object object) {
			if (object instanceof GetMediaCommonArguments) {
				final GetMediaCommonArguments arguments = (GetMediaCommonArguments) object;
				return 	((currentUser == null) ? (arguments.currentUser == null) : currentUser.equals(arguments.currentUser)) &&
						((title == null) ? (arguments.title == null) : title.equals(arguments.title)) &&
						((type == null) ? (arguments.type == null) : type.equals(arguments.type)) &&
						((user == null) ? (arguments.user == null) : user.equals(arguments.user)) &&
						((createdFrom == null) ? (arguments.createdFrom == null) : createdFrom.equals(arguments.createdFrom)) &&
						((createdTo == null) ? (arguments.createdTo == null) : createdTo.equals(arguments.createdTo)) &&
						((editedFrom == null) ? (arguments.editedFrom == null) : editedFrom.equals(arguments.editedFrom)) &&
						((editedTo == null) ? (arguments.editedTo == null) : editedTo.equals(arguments.editedTo)) &&
						((publik == null) ? (arguments.publik == null) : publik.equals(arguments.publik));
			} else
				return false;
		}
		
		@Override
		public int hashCode() {
			return currentUser.hashCode() +
					((title == null) ? 0 : title.hashCode()) +
					((type == null) ? 0 : type.hashCode()) + 
					((user == null) ? 0 : user.hashCode()) + 
					((createdFrom == null) ? 0 : createdFrom.hashCode()) +
					((createdTo == null) ? 0 : createdTo.hashCode()) + 
					((editedFrom == null) ? 0 : editedFrom.hashCode()) +
					((editedTo == null) ? 0 : editedTo.hashCode()) +
					((publik == null) ? 0 : publik.hashCode());
		}
	}
	
	private class GetMediaAsMediaResultArguments extends GetMediaCommonArguments {
		private final Integer start;
		private final Integer length;
		private final String orderField;
		private final boolean ascending;
		
		private GetMediaAsMediaResultArguments(final String currentUser, final String title, final MediaType type, final String user,
				final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
				final Integer start, final Integer length, final String orderField, final boolean ascending) {
			super(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik);
			this.start = start;
			this.length = length;
			this.orderField = orderField;
			this.ascending = ascending;
		}
		
		@Override
		public boolean equals(final Object object) {
			if (object instanceof GetMediaAsMediaResultArguments) {
				final GetMediaAsMediaResultArguments arguments = (GetMediaAsMediaResultArguments) object;
				return super.equals(arguments) &&
						((start == null) ? (arguments.start == null) : start.equals(arguments.start)) &&
						((length == null) ? (arguments.length == null) : length.equals(arguments.length)) &&
						((orderField == null) ? (arguments.orderField == null) : orderField.equals(arguments.orderField)) &&
						(ascending == arguments.ascending);
			} else
				return false;

		}
		
		@Override
		public int hashCode() {
			return super.hashCode() + 
					((start == null) ? 0 : start.hashCode()) + 
					((length == null) ? 0 : length.hashCode()) +
					((orderField == null) ? 0 : orderField.hashCode()) +
					(ascending ? 1 : 0);
		}
	}
	
	private class GetMediaAsListArguments extends GetMediaCommonArguments {
		private final BigDecimal minLatitude;
		private final BigDecimal minLongitude;
		private final BigDecimal maxLatitude;
		private final BigDecimal maxLongitude;
		
		private GetMediaAsListArguments(final String currentUser, final String title, final MediaType type, final String user, final Date createdFrom,
				final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude,
				final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude) {
			super(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik);
			this.minLatitude = minLatitude;
			this.minLongitude = minLongitude;
			this.maxLatitude = maxLatitude;
			this.maxLongitude = maxLongitude;
		}
		
		@Override
		public boolean equals(final Object object) {
			if (object instanceof GetMediaAsListArguments) {
				final GetMediaAsListArguments arguments = (GetMediaAsListArguments) object;
				return super.equals(arguments) &&
						((minLatitude == null) ? (arguments.minLatitude == null) : minLatitude.equals(arguments.minLatitude)) &&
						((minLongitude == null) ? (arguments.minLongitude == null) : minLongitude.equals(arguments.minLongitude)) &&
						((maxLatitude == null) ? (arguments.maxLatitude == null) : maxLatitude.equals(arguments.maxLatitude)) &&
						((maxLongitude == null) ? (arguments.maxLongitude == null) : maxLongitude.equals(arguments.maxLongitude));
			} else
				return false;

		}
		
		@Override
		public int hashCode() {
			return super.hashCode() + 
					((minLatitude == null) ? 0 : minLatitude.hashCode()) + 
					((minLongitude == null) ? 0 : minLongitude.hashCode()) +
					((maxLatitude == null) ? 0 : maxLatitude.hashCode()) +
					((maxLongitude == null) ? 0 : maxLongitude.hashCode());
		}
	}
	
	private final ExtendedMediaService mediaService;
	private final Map<GetMediaAsMediaResultArguments, MediaResult> getMediaAsMediaResultCache;
	private final Map<GetMediaAsListArguments, List<Media>> getMediaAsListCache;
	private final Map<String, Media> getMediaAsMediaCache;

	public MediaServiceProxy(final ExtendedMediaService mediaService) {
		this.mediaService = mediaService;
		getMediaAsMediaResultCache = Collections.synchronizedMap(new HashMap<GetMediaAsMediaResultArguments, MediaResult>());
		getMediaAsListCache = Collections.synchronizedMap(new HashMap<GetMediaAsListArguments, List<Media>>());
		getMediaAsMediaCache = Collections.synchronizedMap(new HashMap<String, Media>());
	}

	@Override
	public MediaResult getMedia(final String currentUser, final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending) throws MediaServiceException {
		final GetMediaAsMediaResultArguments arguments = new GetMediaAsMediaResultArguments(currentUser, title, type, user, createdFrom,
				createdTo, editedFrom, editedTo, publik, start, length, orderField, ascending);
		MediaResult result = getMediaAsMediaResultCache.get(arguments);
		if (result == null) {
			//Den borei na einai null, giati akoma kai an den vrei kati, tha epistrepsei to adeio (alla oxi null) apotelesma
			result = mediaService.getMedia(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik, start,
					length, orderField, ascending);
			getMediaAsMediaResultCache.put(arguments, result);
		}
		return result;
	}

	@Override
	public List<Media> getMedia(final String currentUser, final String title, final MediaType type, final String user, final Date createdFrom,
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude,
			final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude) throws MediaServiceException {
		final GetMediaAsListArguments arguments = new GetMediaAsListArguments(currentUser, title, type, user, createdFrom, createdTo, editedFrom,
				editedTo, publik, minLatitude, minLongitude, maxLatitude, maxLongitude);
		List<Media> result = getMediaAsListCache.get(arguments);
		if (result == null) {
			result = mediaService.getMedia(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik, minLatitude,
					minLongitude, maxLatitude, maxLongitude);
			getMediaAsListCache.put(arguments, result);
		}
		return result;
	}

	@Override
	public Media getMedia(final String id) throws MediaServiceException {
		Media media = getMediaAsMediaCache.get(id);
		if (media == null) {
			media = mediaService.getMedia(id);
			if (media != null)
				getMediaAsMediaCache.put(id, media);
		}
		return media;
	}

	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException {
		mediaService.getMedia(id, response);
	}

	@Override
	public void addMedia(final Media media, final InputStream input) throws MediaServiceException {
		mediaService.addMedia(media, input);
		//Katharismos twn 2 cache giati ta apotelesmata pou exoun meta tin prosthikh enos media einai 
		//palia kai den boroume na xeroume pws tha ta ananewsoume logw twn periplokwn sunthikwn (theoritika ginetai
		//alla tha ekane tin douleia enos media service kai tha htan para polu xronovoro opote protimame na ta 
		//petaxoume ola kai na ta xanaferoume)
		getMediaAsMediaResultCache.clear();
		getMediaAsListCache.clear();
		//Einai palia ta dedomena, alla gia kathe antikeimeno xexwrista ginetai enhmerwsh grhgora tis cache
		getMediaAsMediaCache.put(media.getId(), media);
	}
	
	@Override
	public void editMedia(final Media media) throws MediaServiceException {
		mediaService.editMedia(media);
		getMediaAsMediaResultCache.clear();
		getMediaAsListCache.clear();
		getMediaAsMediaCache.put(media.getId(), media);
	}
	
	@Override
	public void deleteMedia(final String id) throws MediaServiceException {
		mediaService.deleteMedia(id);
		getMediaAsMediaResultCache.clear();
		getMediaAsListCache.clear();
		getMediaAsMediaCache.remove(id);
	}
}
