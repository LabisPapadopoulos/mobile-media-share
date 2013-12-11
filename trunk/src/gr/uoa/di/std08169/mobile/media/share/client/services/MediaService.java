package gr.uoa.di.std08169.mobile.media.share.client.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaType;

//Service gia diaxeirish arxeiwn
@RemoteServiceRelativePath("../mediaService")
public interface MediaService extends RemoteService {
	//GetMedia gia tin Lista (pinaka)
	public MediaResult getMedia(final String currentUser, final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending) throws MediaServiceException;
	//GetMedia gia ton Xarth
	public List<Media> getMedia(final String currentUser, final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik, final BigDecimal minLatitude, 
			final BigDecimal minLongitude, final BigDecimal maxLatitude, final BigDecimal maxLongitude) throws MediaServiceException;
	public Media getMedia(final String id) throws MediaServiceException;
	public void addMedia(final Media media) throws MediaServiceException;
//TODO	public void editMedia(final Media media) throws MediaServiceException;
	public void deleteMedia(final String id) throws MediaServiceException;
}
