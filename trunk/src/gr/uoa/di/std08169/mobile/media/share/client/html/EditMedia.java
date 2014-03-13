package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EditMedia implements EntryPoint {

	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	
	private static final MediaServiceAsync MEDIA_SERVICE = GWT.create(MediaService.class);
	
	public EditMedia() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onModuleLoad() {
		final String id = Window.Location.getParameter("id");
		if (id == null) {
			Window.alert("Dwse ID enos Media");
			//redirect sto map
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
		} else {
			//Klhsh tou MEDIA_SERVICE gia na paroume to antikeimeno
			MEDIA_SERVICE.getMedia(id, new AsyncCallback<Media>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert("To media den boresa na to psaxw");
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							//me to antistoixo locale 
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}

				@Override
				public void onSuccess(final Media media) {
					if (media == null) {
						Window.alert("To Media den yparxei");
						//redirect sto map
						Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
								//me to antistoixo locale 
								LocaleInfo.getCurrentLocale().getLocaleName())));
					} else {
						
						// elegxoume an to media einai tou current user (den kanei o kathenas edit)
						
						
						// fortwnoume xarth
						
						// Twra ftiaxnoume selida
						
						media.setTitle(media.getTitle() + " " + media.getTitle());
						media.setLatitude(media.getLatitude().multiply(new BigDecimal(2)));
						media.setLongitude(media.getLongitude().multiply(new BigDecimal(2)));
						media.setPublic(!media.isPublic());
						
						Window.alert("Prin to media Service");
						
						MEDIA_SERVICE.editMedia(media, new AsyncCallback<Void>() {

							@Override
							public void onFailure(final Throwable throwable) {
								Window.alert("Den borw na kanw edit");
								//redirect sto map
								Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
										//me to antistoixo locale 
										LocaleInfo.getCurrentLocale().getLocaleName())));
							}

							@Override
							public void onSuccess(final Void _) {
								Window.alert("ZHTW!");
								//redirect sto map
								Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
										//me to antistoixo locale 
										LocaleInfo.getCurrentLocale().getLocaleName())));
							}
						});
					}
				}
			});
		}
	}
}
