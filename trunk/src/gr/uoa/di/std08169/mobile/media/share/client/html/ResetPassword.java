package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;

public class ResetPassword extends Composite implements ClickHandler, EntryPoint, KeyUpHandler, RequestCallback {
	
	protected static interface ResetPasswordUiBinder extends UiBinder<Widget, ResetPassword>{}
	
	private static final ResetPasswordUiBinder RESET_PASSWORD_UI_BINDER = 
			GWT.create(ResetPasswordUiBinder.class);
	
	//H create dhmiougrei dunamika resource (instance) tupou UserService
	//Sundeei ta duo interfaces kai paragei ena service
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS =
			GWT.create(MobileMediaShareConstants.class);
	
	@UiField
	protected PasswordTextBox password;
	@UiField
	protected PasswordTextBox password2;
	@UiField
	protected Button ok;
	@UiField
	protected Button reset;
	private String token;
	
	public ResetPassword() {
		initWidget(RESET_PASSWORD_UI_BINDER.createAndBindUi(this));
		password.addKeyUpHandler(this);
		password2.addKeyUpHandler(this);
		ok.setEnabled(false);
		ok.addClickHandler(this);
		reset.addClickHandler(this);
	}

	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == ok) {
			//Ta dedomena pros apostolh bainoun sto swma alla san url
			//Klhsh tou UserServlet me ajax, me tin methodo put (Ylopoihsh protokolou REST)
			//Oi Browsers den upostirizoun PUT kai kat' epektash formes opote kanoun GET kai vazoun ta dedomena sto url.
			//Gi' auto ginetai xrhsh tou RequestBuilder
			final RequestBuilder request = new RequestBuilder(RequestBuilder.POST, MOBILE_MEDIA_SHARE_URLS.userServletReset(
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//apostolh kai tou token sto url anti gia hidden pedio
					URL.encodeQueryString(token), URL.encodeQueryString(password.getValue()),
					URL.encodeQueryString(password2.getValue())));
			//xana asxoleitai h idia h klash (this) me tin apantish tou request (onError, onResponseReceived) 
			request.setCallback(this);
			try {
				//stelnei to request
				request.send();
			} catch (final RequestException e) { //den borese na steilei to request
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorResettingPassword(e.getMessage()));
				//redirect sto map
				Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
						//me to antistoixo locale 
						LocaleInfo.getCurrentLocale().getLocaleName())));
			}
		} else if (clickEvent.getSource() == reset) {
			password.setValue(null);
			password2.setValue(null);
			ok.setEnabled(false);
		}
	}
	
	@Override
	public void onError(final Request request, final Throwable throwable) {
		Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorResettingPassword(throwable.getMessage()));
		//redirect sto map
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
				//me to antistoixo locale 
				LocaleInfo.getCurrentLocale().getLocaleName())));
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		if ((keyUpEvent.getSource() == password) || (keyUpEvent.getSource() == password2))
			//trim(): petaei ta kena
			ok.setEnabled(!(password.getValue().isEmpty() || password2.getValue().isEmpty()));
	}
	
	@Override
	public void onModuleLoad() {
		token = Window.Location.getParameter("token");
		if (token == null) {
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorResettingPassword(
					MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
			//redirect sto map
			Window.Location.assign(GWT.getHostPageBaseURL() + MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
			return;
		}
		RootPanel.get().add(this);
	}
	
	//apantish tou server
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		//Error apantish
		if (response.getStatusCode() != Response.SC_OK) {
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorResettingPassword(response.getStatusText()));
			//redirect sto map
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
			return;
		}
		password.setValue(null);
		password2.setValue(null);
		ok.setEnabled(false);
		reset.setEnabled(false);
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
				//me to antistoixo locale 
				LocaleInfo.getCurrentLocale().getLocaleName())));
	}
}
