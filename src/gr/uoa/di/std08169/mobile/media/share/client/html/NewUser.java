package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceAsync;

public class NewUser extends Composite implements ClickHandler, EntryPoint, KeyUpHandler {
	
	protected static interface NewUserUiBinder extends UiBinder<Widget, NewUser>{}
	
	private static final NewUserUiBinder NEW_USER_UI_BINDER = 
			GWT.create(NewUserUiBinder.class);
	
	//H create dhmiougrei dunamika resource (instance) tupou UserService
	//Sundeei ta duo interfaces kai paragei ena service
	private static final UserServiceAsync USER_SERVICE = GWT.create(UserService.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES = 
			GWT.create(MobileMediaShareMessages.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	
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
	
	public NewUser() {
		initWidget(NEW_USER_UI_BINDER.createAndBindUi(this));
		email.addKeyUpHandler(this);
		password.addKeyUpHandler(this);
		password2.addKeyUpHandler(this);
		ok.addClickHandler(this);
		ok.setEnabled(false);
		reset.addClickHandler(this);
		cancel.addClickHandler(this);
	}

	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == ok) {
			if (!password.getValue().equals(password2.getValue())) {
				Window.alert(MOBILE_MEDIA_SHARE_CONSTANTS.passwordsDoNotMatch());
				password.setValue(null);
				password2.setValue(null);
				ok.setEnabled(false);
			}
			//Xekinaei na kalesei to web service kai otan klhthei tha kanei ta onSuccess 'h onFailure
			USER_SERVICE.addUser(email.getValue().trim(), password.getValue(), new AsyncCallback<Boolean>() {
				//Kaleitai molis petaxei exception h emailExists
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCreatingUser(throwable.getMessage()));
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(
							//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
				}
				
				@Override
				public void onSuccess(final Boolean result) {
					if (result)
						Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(
								//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
								URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
					else {
						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.userAlreadyExists(email.getValue()));
						email.setValue(null);
						password.setValue(null);
						password2.setValue(null);
						ok.setEnabled(false);
					}
				}
			});
		} else if (clickEvent.getSource() == reset) {
			email.setValue(null);
			password.setValue(null);
			password2.setValue(null);
			ok.setEnabled(false);
		} else if (clickEvent.getSource() == cancel) {
			//paei stin ketrikh selida (map.html)
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
		}
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
}
