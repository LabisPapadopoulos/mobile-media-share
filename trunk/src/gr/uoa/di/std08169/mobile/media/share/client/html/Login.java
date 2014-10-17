package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
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
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

//EntryPoint: Edw borei na xekinaei ena module (mia selida)
//ClickHandler: Enas ActionListener gia to click panw se kapoio button
//KeyPressHandler: Listener gia to otan paththei kapoio plhktro
public class Login extends Composite implements ClickHandler, EntryPoint, KeyUpHandler {
	
	protected static interface LoginUiBinder extends UiBinder<Widget, Login> {}
	
	private static final LoginUiBinder LOGIN_UI_BINDER = 
			GWT.create(LoginUiBinder.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	
	@UiField
	protected Hidden url;
	@UiField
	protected Hidden locale;
	@UiField
	protected TextBox email;
	@UiField
	protected PasswordTextBox password;
	@UiField
	protected SubmitButton login;
	@UiField
	protected Button newUser;
	@UiField
	protected AnchorElement forgotPassword; //<a></a>
	@UiField
	protected AnchorElement playstore;
	
	public Login() {
		//Arxikopoihsh tou grafikou me ton Ui Binder
		initWidget(LOGIN_UI_BINDER.createAndBindUi(this));
		url.setValue(MOBILE_MEDIA_SHARE_URLS.map(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		locale.setValue(LocaleInfo.getCurrentLocale().getLocaleName());
		email.addKeyUpHandler(this);
		password.addKeyUpHandler(this);
		newUser.addClickHandler(this);
		forgotPassword.setHref(MOBILE_MEDIA_SHARE_URLS.forgotPassword(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		playstore.setHref(MOBILE_MEDIA_SHARE_URLS.googlePlay());
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) { // click sto new user
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.newUser(GWT.getHostPageBaseURL(), 
			//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
			URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		if ((keyUpEvent.getSource() == email) || (keyUpEvent.getSource() == password))
			//energopoish tou Login an einai kai password an den einai kena
			login.setEnabled(!(email.getValue().isEmpty() || password.getValue().isEmpty()));
	}
	
	@Override
	public void onModuleLoad() { //molis fortwthei to module (h javascript), molis to zhthsei kapoia selida
		RootPanel.get().add(this);
		email.setFocus(true);
	}
}
