package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
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
		login = new Button("Login");
		login.addClickHandler(this);
		login.setEnabled(false);
		newUser = new Button("New User");
		newUser.addClickHandler(this);
		forgotPassword = new Button("Forgot Password?");
		forgotPassword.addClickHandler(this);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		//Apo pou hrthe to click event
		if (clickEvent.getSource() == login)
			//apostolh dedomenwn sto servlet
			form.submit();
		else if (clickEvent.getSource() == newUser)
			Window.Location.assign("./newUser.html");
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
		//Travaei olh tin selida: RootPanel.get()
		//InlineLabel gia na fainetai stin idia grammh
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new InlineLabel("Email"));
		flowPanel.add(email);
		flowPanel.add(new InlineHTML("<br />"));
		flowPanel.add(new InlineLabel("Password"));
		flowPanel.add(password);
		flowPanel.add(new InlineHTML("<br />"));
		flowPanel.add(login);
		flowPanel.add(newUser);
		flowPanel.add(forgotPassword);
		form.add(flowPanel);
		RootPanel.get().add(form);
	}
}
