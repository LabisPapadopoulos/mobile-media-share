package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource;

//interface gia tis glwsses (ola einai strings). Den ulopoieitai pouthena,
//ftiaxnei mono tou to GWT ulopoihsh.
@LocalizableResource.DefaultLocale("en") //oti akolouthei einai agglika
public interface MobileMediaShareConstants extends Constants {
	@DefaultStringValue("Cancel")
	public String cancel();
	
	@DefaultStringValue("Confirm Password")
	public String confirmPassword();
	
	@DefaultStringValue("Email")
	public String email();

	@DefaultStringValue("Forgot Password?")
	public String forgotPassword_(); //logw periergou xarakthra (?)
	
	@DefaultStringValue("Login")
	public String login();

	@DefaultStringValue("New User")
	public String newUser();
	
	@DefaultStringValue("OK")
	public String ok();

	@DefaultStringValue("Password")
	public String password();
	
	@DefaultStringValue("Passwords do not match")
	public String passwordsDoNotMatch();
	
	@DefaultStringValue("Place Map Here")
	public String placeMapHere();
	
	@DefaultStringValue("Reset")
	public String reset();
}
