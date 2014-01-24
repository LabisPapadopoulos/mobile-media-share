package gr.uoa.di.std08169.mobile.media.share.server.proxies;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;

//Ylopoiei ena mediaservice xrhsimopoiwntas ena allo mediaService. (Wrapper)
//Tha kaleitai o proxy kai to spring tha dialegei jdbcMediaService 'h gcdMediaService
public class MediaServiceProxy implements ExtendedMediaService {
	
	private final ExtendedMediaService mediaService;

	public MediaServiceProxy(final ExtendedMediaService mediaService) {
		this.mediaService = mediaService;
	}

	@Override
	public MediaResult getMedia(final String currentUser, final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending) throws MediaServiceException {
		return mediaService.getMedia(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik, start, length, orderField, ascending);
	}

	@Override
	public List<Media> getMedia(final String currentUser, final String title, final MediaType type, final String user, final Date createdFrom,
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude,
			final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude) throws MediaServiceException {
		return mediaService.getMedia(currentUser, title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik, minLatitude, minLongitude, maxLatitude, maxLongitude);
	}

	@Override
	public Media getMedia(final String id) throws MediaServiceException {
		return mediaService.getMedia(id);
	}

	@Override
	public void deleteMedia(final String id) throws MediaServiceException {
		mediaService.deleteMedia(id);
	}

	@Override
	public void addMedia(final Media media, final InputStream input) throws MediaServiceException {
		mediaService.addMedia(media, input);
	}

	@Override
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException {
		mediaService.getMedia(id, response);
	}
}
