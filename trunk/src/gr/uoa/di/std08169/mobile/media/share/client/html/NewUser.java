package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceAsync;

public class NewUser implements ClickHandler, EntryPoint, KeyUpHandler {
	//H create dhmiougrei dunamika resource (instance) tupou UserService
	//Sundeei ta duo interfaces kai paragei ena service
	private static final UserServiceAsync USER_SERVICE = GWT.create(UserService.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES = 
			GWT.create(MobileMediaShareMessages.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	private static final int TOP_STEP = 30;
	private static final int LEFT_OFFSET = 360;
	private static final int LEFT_STEP = 100;
	
	private final TextBox email;
	private final PasswordTextBox password;
	private final PasswordTextBox password2;
	private final Button ok;
	private final Button reset;
	private final Button cancel;
	
	public NewUser() {
		int i = 0;
		email = new TextBox();
		email.getElement().addClassName("field");
		email.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		email.addKeyUpHandler(this);
		password = new PasswordTextBox();
		password.getElement().addClassName("field");
		password.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		password.addKeyUpHandler(this);
		password2 = new PasswordTextBox();
		password2.getElement().addClassName("field");
		password2.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		password2.addKeyUpHandler(this);
		int j = 0;
		ok = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.ok());
		ok.getElement().addClassName("newUserButtons");
		ok.getElement().setAttribute("style", 
				"top: " + (TOP_STEP * i) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
		ok.addClickHandler(this);
		ok.setEnabled(false);
		reset = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.reset());
		reset.getElement().addClassName("newUserButtons");
		reset.getElement().setAttribute("style", 
				"top: " + (TOP_STEP * i) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
		reset.addClickHandler(this);
		cancel = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.cancel());
		cancel.getElement().addClassName("newUserButtons");
		cancel.getElement().setAttribute("style", 
				"top: " + (TOP_STEP * i) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
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
		Document.get().getBody().addClassName("bodyClass");
		Document.get().getBody().appendChild(Header.newHeader());
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.getElement().addClassName("newUser");
		int i = 0;
		final InlineLabel emailLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.email());
		emailLabel.getElement().addClassName("label");
		emailLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(emailLabel);
		flowPanel.add(email);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		final InlineLabel passwordLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.password());
		passwordLabel.getElement().addClassName("label");
		passwordLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(passwordLabel);
		flowPanel.add(password);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		final InlineLabel confirmPasswordLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.confirmPassword());
		confirmPasswordLabel.getElement().addClassName("label");
		confirmPasswordLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(confirmPasswordLabel);
		flowPanel.add(password2);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		flowPanel.add(ok);
		flowPanel.add(reset);
		flowPanel.add(cancel);
		
		RootPanel.get().add(flowPanel);
	}
}
