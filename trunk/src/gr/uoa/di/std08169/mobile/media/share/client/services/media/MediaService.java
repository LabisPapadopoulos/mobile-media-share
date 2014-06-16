package gr.uoa.di.std08169.mobile.media.share.client.services.media;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

//Service gia diaxeirish arxeiwn
@RemoteServiceRelativePath("../mediaService")
public interface MediaService extends RemoteService {
	//GetMedia gia tin Lista (pinaka)
	public MediaResult getMedia(final User currentUser, final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending) throws MediaServiceException;
	//GetMedia gia ton Xarth
	public List<Media> getMedia(final User currentUser, final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude, 
			final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude) throws MediaServiceException;
	public Media getMedia(final String id) throws MediaServiceException;
	public void editMedia(final Media media) throws MediaServiceException;
	public void deleteMedia(final String id) throws MediaServiceException;
}
