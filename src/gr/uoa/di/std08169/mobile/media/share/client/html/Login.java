package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

//EntryPoint: Edw borei na xekinaei ena module (mia selida)
//ClickHandler: Enas ActionListener gia to click panw se kapoio button
//KeyPressHandler: Listener gia to otan paththei kapoio plhktro
public class Login implements ClickHandler, EntryPoint, KeyUpHandler {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS =
			//kanei automath ulopoihsh to GWT tou interface
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	
	//Ta textBoxes/buttons pou xreiazomaste gia tin arxikh login othonh
	private final FormPanel form;
	private final TextBox email;
	private final PasswordTextBox password;
	private final Button login;
	private final Button newUser;
	private final Button forgotPassword;
	
	public Login() {
		//To apotelesma tou submit (to action tis formas)
		//tha fortwthei sto idio parathiro (_self)
		form = new FormPanel(new NamedFrame("_self"));
		//methodos (post) apostolhs plhroforiwn
		form.setMethod(FormPanel.METHOD_POST);
		//To request tha einai me polla merh kai mesa
		//ekei tha einai kai oi parametroi anti gia to url
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		//url gia to servlet login 
		form.setAction("./login");
		email = new TextBox();
		email.setName("email");
		email.addKeyUpHandler(this);
		password = new PasswordTextBox();
		password.setName("password");
		password.addKeyUpHandler(this);
		//string login apo to interface
		login = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.login());
		login.addClickHandler(this);
		login.setEnabled(false);
		newUser = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.newUser());
		newUser.addClickHandler(this);
		forgotPassword = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.forgotPassword_());
		forgotPassword.addClickHandler(this);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		//Apo pou hrthe to click event
		if (clickEvent.getSource() == login)
			//apostolh dedomenwn sto servlet
			form.submit();
		else if (clickEvent.getSource() == newUser)
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.newUser(
				//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
				URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
		else if (clickEvent.getSource() == forgotPassword)
			Window.alert("As prosexes!");
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		if ((keyUpEvent.getSource() == email) || (keyUpEvent.getSource() == password))
			//energopoish tou Login an einai kai password an den einai kena
			login.setEnabled(!(email.getValue().isEmpty() || password.getValue().isEmpty()));
	}
	
	@Override
	public void onModuleLoad() { //molis fortwthei to module (h javascript), molis to zhthsei kapoia selida
		Document.get().getBody().appendChild(Header.newHeader());
		//Travaei olh tin selida: RootPanel.get()
		//InlineLabel gia na fainetai stin idia grammh
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.email()));
		flowPanel.add(email);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		flowPanel.add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.password()));
		flowPanel.add(password);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		flowPanel.add(login);
		flowPanel.add(newUser);
		flowPanel.add(forgotPassword);
		//Ta locale, url pernountai sto body tis formas, opote den xreiazetai url encode
		flowPanel.add(new Hidden("locale", LocaleInfo.getCurrentLocale().getLocaleName()));
		flowPanel.add(new Hidden("url", Window.Location.getParameter("url")));
		form.add(flowPanel);
		RootPanel.get().add(form);
	}
}
