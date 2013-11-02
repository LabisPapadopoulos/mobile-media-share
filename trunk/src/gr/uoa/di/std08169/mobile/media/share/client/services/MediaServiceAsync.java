package gr.uoa.di.std08169.mobile.media.share.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import gr.uoa.di.std08169.mobile.media.share.shared.Media;

/**
 * Asunxrono service metaxu javascript kai java
 * @author labis
 *
 */
public interface MediaServiceAsync {
	public void addMedia(final Media media, final AsyncCallback<Void> callback);
}
