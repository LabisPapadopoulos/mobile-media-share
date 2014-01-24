package gr.uoa.di.std08169.mobile.media.share.server;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;

/**
 * ExtendedMediaService: Epekteinei to MediaService me methodous pou einai diathesimes
 * mono sto back-end. To InputStream kai to OutputStream den metaglwtizontai me to GWT.
 * @author labis
 *
 */
public interface ExtendedMediaService extends MediaService {
	public void addMedia(final Media media, final InputStream input) throws MediaServiceException;
	public void getMedia(final String id, final HttpServletResponse response) throws MediaServiceException;
}
