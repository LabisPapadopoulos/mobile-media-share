package gr.uoa.di.std08169.mobile.media.share.client.services;

import java.util.Date;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaType;

//Service gia diaxeirish arxeiwn
@RemoteServiceRelativePath("../mediaService")
public interface MediaService extends RemoteService {
	public MediaResult getMedia(final String title, final MediaType type, final String user, final Date createdFrom, 
			final Date createdTo, final Date editedFrom, final Date editedTo, final Boolean publik,
			final Integer start, final Integer length, final String orderField, final boolean ascending) throws MediaServiceException;
//	public Media getMedia(final String id) throws MediaServiceException;
	public void addMedia(final Media media) throws MediaServiceException;
//	public void editMedia(final Media media) throws MediaServiceException;
//	public void deleteMedia(final String id) throws MediaServiceException;
}
