package gr.uoa.di.std08169.mobile.media.share.client.services;

import gr.uoa.di.std08169.mobile.media.share.shared.Media;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

//Service gia diaxeirish arxeiwn
@RemoteServiceRelativePath("../mediaService")
public interface MediaService extends RemoteService {
//	public Media[] getMedia(/* kritiria anazhthshs */) throws MediaServiceException;
//	public Media getMedia(final String id) throws MediaServiceException;
	public void addMedia(final Media media) throws MediaServiceException;
//	public void editMedia(final Media media) throws MediaServiceException;
//	public void deleteMedia(final String id) throws MediaServiceException;
}
