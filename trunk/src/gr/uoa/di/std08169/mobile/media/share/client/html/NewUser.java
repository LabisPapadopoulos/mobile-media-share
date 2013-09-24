package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class NewUser implements ClickHandler, EntryPoint, KeyUpHandler {

	private final TextBox email;
	private final PasswordTextBox password;
	private final PasswordTextBox password2;
	private final Button ok;
	private final Button reset;
	private final Button cancel;
	
	public NewUser() {
		email = new TextBox();
		email.addKeyUpHandler(this);
		password = new PasswordTextBox();
		password.addKeyUpHandler(this);
		password2 = new PasswordTextBox();
		password2.addKeyUpHandler(this);
		ok = new Button("OK");
		ok.addClickHandler(this);
		ok.setEnabled(false);
		reset = new Button("Reset");
		reset.addClickHandler(this);
		cancel = new Button("Cancel");
		cancel.addClickHandler(this);
	}

	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == ok) {
			Window.alert("Pathses ok :P");
		} else if (clickEvent.getSource() == reset) {
			email.setValue(null);
			password.setValue(null);
			password2.setValue(null);
			ok.setEnabled(false);
		} else if (clickEvent.getSource() == cancel) {
			//paei stin ketrikh selida (map.html)
			Window.Location.assign("./map.html");
		}
		
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		if ((keyUpEvent.getSource() == email) || (keyUpEvent.getSource() == password) ||
				(keyUpEvent.getSource() == password2))
			ok.setEnabled(!(email.getValue().isEmpty() || password.getValue().isEmpty() ||
					password2.getValue().isEmpty()));
	}
	
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(new Label("Email"));
		RootPanel.get().add(email);
		RootPanel.get().add(new Label("Password"));
		RootPanel.get().add(password);
		RootPanel.get().add(new Label("Retype Password"));
		RootPanel.get().add(password2);
		RootPanel.get().add(new Label());
		RootPanel.get().add(ok);
		RootPanel.get().add(reset);
		RootPanel.get().add(cancel);
	}
}
