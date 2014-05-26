package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewUser extends Composite implements ClickHandler, EntryPoint, KeyUpHandler, RequestCallback {
	
	protected static interface NewUserUiBinder extends UiBinder<Widget, NewUser>{}
	
	private static final NewUserUiBinder NEW_USER_UI_BINDER = 
			GWT.create(NewUserUiBinder.class);
	
	//H create dhmiougrei dunamika resource (instance) tupou UserService
	//Sundeei ta duo interfaces kai paragei ena service
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	
	@UiField
	protected DivElement form;
	@UiField
	protected TextBox email;
	@UiField
	protected PasswordTextBox password;
	@UiField
	protected PasswordTextBox password2;
	@UiField
	protected Button ok;
	@UiField
	protected Button reset;
	@UiField
	protected Button cancel;
	@UiField
	protected DivElement info;
	
	public NewUser() {
		initWidget(NEW_USER_UI_BINDER.createAndBindUi(this));
		email.addKeyUpHandler(this);
		password.addKeyUpHandler(this);
		password2.addKeyUpHandler(this);
		ok.setEnabled(false);
		ok.addClickHandler(this);
		reset.addClickHandler(this);
		cancel.addClickHandler(this);
		info.getStyle().setDisplay(Style.Display.NONE);
	}

	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == ok) {
			//Ta dedomena pros apostolh bainoun sto swma alla san url
			//Klhsh tou UserServlet me ajax, me tin methodo put (Ylopoihsh protokolou REST)
			//Oi Browsers den upostirizoun PUT kai kat' epektash formes opote kanoun GET kai vazoun ta dedomena sto url.
			//Gi' auto ginetai xrhsh tou RequestBuilder
			final RequestBuilder request = new RequestBuilder(RequestBuilder.PUT, MOBILE_MEDIA_SHARE_URLS.userServletRegister(
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					URL.encodeQueryString(email.getValue()), URL.encodeQueryString(password.getValue()),
					URL.encodeQueryString(password2.getValue())));
			request.setCallback(this);
			try {
				request.send();
			} catch (final RequestException e) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCreatingUser(e.getMessage()));
				//redirect sto map
				Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
						//me to antistoixo locale 
						LocaleInfo.getCurrentLocale().getLocaleName())));
			}
		} else if (clickEvent.getSource() == reset) {
			email.setValue(null);
			password.setValue(null);
			password2.setValue(null);
			ok.setEnabled(false);
		} else if (clickEvent.getSource() == cancel) {
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
		}
	}
	
	@Override
	public void onError(final Request request, final Throwable throwable) {
		Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCreatingUser(throwable.getMessage()));
		//redirect sto map
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
				//me to antistoixo locale 
				LocaleInfo.getCurrentLocale().getLocaleName())));
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		if ((keyUpEvent.getSource() == email) || (keyUpEvent.getSource() == password) ||
				(keyUpEvent.getSource() == password2))
			//trim(): petaei ta kena
			ok.setEnabled(!(email.getValue().trim().isEmpty() || password.getValue().isEmpty() ||
					password2.getValue().isEmpty()));
	}
	
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
	}
	
	//apantish tou server
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		//Error apantish
		if (response.getStatusCode() != Response.SC_OK) {
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCreatingUser(response.getStatusText()));
			//redirect sto map
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
			return;
		}
		form.getStyle().setDisplay(Style.Display.NONE);
		email.setValue(null);
		password.setValue(null);
		password2.setValue(null);
		ok.setEnabled(false);
		reset.setEnabled(false);
		cancel.setEnabled(false);
		info.getStyle().setDisplay(Style.Display.BLOCK);
	}
}
