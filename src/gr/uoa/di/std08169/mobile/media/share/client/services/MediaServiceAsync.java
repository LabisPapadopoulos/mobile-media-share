package gr.uoa.di.std08169.mobile.media.share.client.services;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;

import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.User;

/**
 * Asunxrono service metaxu javascript kai java
 * @author labis
 *
 */
public interface MediaServiceAsync {
	public void getMedia(final String title, final MediaType type, final User user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending,
			final AsyncCallback<MediaResult> callback);
	public void addMedia(final Media media, final AsyncCallback<Void> callback);
}
