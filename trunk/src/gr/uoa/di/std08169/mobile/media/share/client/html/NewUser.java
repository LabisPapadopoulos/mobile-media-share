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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewUser extends Composite implements ClickHandler, EntryPoint, KeyUpHandler {
	
	protected static interface NewUserUiBinder extends UiBinder<Widget, NewUser>{}
	
	private static final NewUserUiBinder NEW_USER_UI_BINDER = 
			GWT.create(NewUserUiBinder.class);
	
	//H create dhmiougrei dunamika resource (instance) tupou UserService
	//Sundeei ta duo interfaces kai paragei ena service
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	
	@UiField
	protected TextBox email;
	@UiField
	protected PasswordTextBox password;
	@UiField
	protected PasswordTextBox password2;
	@UiField
	protected SubmitButton ok;
	@UiField
	protected Button cancel;
	
	public NewUser() {
		initWidget(NEW_USER_UI_BINDER.createAndBindUi(this));
		email.addKeyUpHandler(this);
		password.addKeyUpHandler(this);
		password2.addKeyUpHandler(this);
		ok.setEnabled(false);
		cancel.addClickHandler(this);
	}

	@Override
	public void onClick(final ClickEvent clickEvent) { /* patwntas to cancel */
		//paei stin ketrikh selida (map.html)
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(
				//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
				URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
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
