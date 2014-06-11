package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.UserStatusConstants;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class MyAccount extends Composite implements ClickHandler, EntryPoint, KeyUpHandler, Runnable {
	public static final String IMAGE_WIDTH = "256px";
	public static final String IMAGE_HEIGHT = "256px";
	//To interface ftiaxnei ena widget me vash to MyAccount
	protected static interface MyAccountUiBinder extends UiBinder<Widget, MyAccount> {}
	//metatrepei to Ui xml se java antikeimeno
	private static final MyAccountUiBinder MY_ACCOUNT_UI_BINDER = GWT.create(MyAccountUiBinder.class);
	private static final UserServiceAsync USER_SERVICE = GWT.create(UserService.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	private static final UserStatusConstants USER_STATUS_CONSTANTS = 
			GWT.create(UserStatusConstants.class);

	@UiField
	protected Image photo;
	//constructor tou annotation me orismata
	//Den tha to ftiaxei to ui binder to photoSelector, alla emeis
	@UiField(provided = true)
	protected PhotoSelector photoSelector;
	@UiField
	protected TextBox name;
	@UiField
	protected InlineLabel status;
	@UiField
	protected InlineLabel email;
	@UiField
	protected PasswordTextBox password;
	@UiField
	protected PasswordTextBox password2;
	@UiField
	protected Button ok;
	@UiField
	protected Button reset;
	private User user;

	public MyAccount() {
		//ylopoihsh ths run() apo to Runnable
		photoSelector = new PhotoSelector(this);
		initWidget(MY_ACCOUNT_UI_BINDER.createAndBindUi(this));
		photo.setWidth(IMAGE_WIDTH);
		photo.setHeight(IMAGE_HEIGHT);
		photo.addClickHandler(this);
		photoSelector.setVisible(false);
		name.addKeyUpHandler(this);
		password.addKeyUpHandler(this);
		password2.addKeyUpHandler(this);
		ok.addClickHandler(this);
		ok.setEnabled(false);
		reset.addClickHandler(this);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == photo) {
			//emfanish panta tis 1hs selidas
			photoSelector.showPage(0);
			photoSelector.setVisible(true);
		} else if (clickEvent.getSource() == ok) {
			user.setName(name.getValue());
			user.setPhoto(photoSelector.getValue());
			//toulaxiston ena password den einai keno (keno string) ara o xrhsths hthele na ta allaxei kai
			//epeidh den sumfwnoun metaxu tous ta egrapse lathos
			if ((!(password.getValue().isEmpty() && password2.getValue().isEmpty())) && 
					(!password.getValue().equals(password2.getValue()))) {
				ok.setEnabled(false);
				Window.alert(MOBILE_MEDIA_SHARE_CONSTANTS.passwordsDoNotMatch());
				return;
			}
			USER_SERVICE.editUser(user, password.getValue().isEmpty() ? null : password.getValue(), 
					new AsyncCallback<String>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorEditingUser(throwable.getMessage()));
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}

				@Override
				public void onSuccess(final String _) {
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}
			});
		} else if (clickEvent.getSource() == reset) {
			photo.setUrl((user.getPhoto() == null) ? MOBILE_MEDIA_SHARE_URLS.defaultUser() : MOBILE_MEDIA_SHARE_URLS.download(user.getPhoto()));
			photoSelector.setValue(user.getPhoto());
			name.setValue(user.getName());
			status.setText(USER_STATUS_CONSTANTS.getString(user.getStatus().name()));
			email.setText(user.getEmail());
			password.setValue(null);
			password2.setValue(null);
			ok.setEnabled(false);
		}
	}

	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		//exei peiraxei o xrhsths to onoma
		ok.setEnabled(((keyUpEvent.getSource() == name) &&
				//to neo onoma diaferei apo to palio
				(!((user.getName() == null) ? name.getValue().isEmpty() : name.getValue().equals(user.getName())))) ||
		//peiraxe to password kai ta password sumfwnoun (arkei na mhn einai kena)
		((keyUpEvent.getSource() == password) && (!password.getValue().isEmpty()) && (password.getValue().equals(password2.getValue()))) ||
		//peiraxe to password2 kai ta password sumfwnoun (arkei na mhn einai kena)
		((keyUpEvent.getSource() == password2) && (!password.getValue().isEmpty()) && (password.getValue().equals(password2.getValue()))));
	}
	
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
		final String email = InputElement.as(Document.get().getElementById("email")).getValue();
		USER_SERVICE.getUser(email, new AsyncCallback<User>() {
			@Override
			public void onFailure(final Throwable throwable) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingUser(throwable.getMessage()));
				//redirect sto map
				Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
						//me to antistoixo locale 
						LocaleInfo.getCurrentLocale().getLocaleName())));
			}

			@Override
			public void onSuccess(final User user) {
				if (user == null) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingUser(MOBILE_MEDIA_SHARE_CONSTANTS.userNotFound()));
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							//me to antistoixo locale 
							LocaleInfo.getCurrentLocale().getLocaleName())));
					return;
				}
				MyAccount.this.user = user;
				photoSelector.init(user.getEmail());
				reset.click();
			}
		});
	}
	
	//tha klithei molis dialexei o xrhsths fwtografia 
	@Override
	public void run() { // TODO einai panta to null default?
					//an dialexe th default fwtografia o xrhsths
		photo.setUrl((photoSelector.getValue() == null) ?
				// tote th default
				MOBILE_MEDIA_SHARE_URLS.defaultUser() :
					//alliws auth pou dialexe
					MOBILE_MEDIA_SHARE_URLS.download(photoSelector.getValue()));
		ok.setEnabled(true);
	}
}
