package gr.uoa.di.std08169.mobile.media.share.client.services.media;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;

/**
 * Asunxrono service metaxu javascript kai java
 * @author labis
 *
 */
public interface MediaServiceAsync {
	public void getMedia(final String currentUser, final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo,
			final Boolean publik, final Integer start, final Integer length, final String orderField,
			final boolean ascending, final AsyncCallback<MediaResult> callback);
	public void getMedia(final String currentUser, final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude, 
			final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude, final AsyncCallback<List<Media>> callback);
	public void getMedia(final String id, final AsyncCallback<Media> callback);
	public void editMedia(final Media media, final AsyncCallback<Void> callback);
	public void deleteMedia(final String id, final AsyncCallback<Void> callback);
}
