package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Map implements EntryPoint, RequestCallback {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	
	@Override
	public void onModuleLoad() {
		try {
			//RequestBuilder gia na kanoume ena GET request sto servlet login gia na paroume
			//to session mas. RequestCallback (this) einai auto pou tha parei tin apantish asunxrona
			new RequestBuilder(RequestBuilder.GET, "./login").sendRequest(null, this);
		} catch (final RequestException _) {
			//otidhpote paei strava, xana gurnaei stin login
			//url pou theloume na mas paei
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.map(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		}
	}

	@Override
	public void onError(final Request _, final Throwable __) {
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
				//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
				URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
				//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
				URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.map(
						URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
	}

	//molis phre epituxws tin apantish
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		//an den einai logged in o xrhsths
		if ((response.getStatusCode() != 200) || (response.getText().isEmpty()))
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.map(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		Document.get().getBody().addClassName("bodyClass");
		//Apo to DOM prosthetei komvo (to header me olous tous upokomvous pou exei mesa)
		Document.get().getBody().appendChild(Header.newHeader());
		RootPanel.get().add(new Label(MOBILE_MEDIA_SHARE_CONSTANTS.map()));
	}
}
