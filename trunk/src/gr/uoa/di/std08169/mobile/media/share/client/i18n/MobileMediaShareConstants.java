package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource;

//interface gia tis glwsses (ola einai strings). Den ulopoieitai pouthena,
//ftiaxnei mono tou to GWT ulopoihsh.
@LocalizableResource.DefaultLocale("en") //oti akolouthei einai agglika
public interface MobileMediaShareConstants extends Constants {
	@DefaultStringValue("<anonymous>")
	public String _anonymous_();
	
	@DefaultStringValue("access denied")
	public String accessDenied();
	
	@DefaultStringValue("Any Type")
	public String anyType();
	
	@DefaultStringValue("Are you sure you want to delete this media?")
	public String areYouSureYouWantToDeleteThisMedia();
	
	@DefaultStringValue("Cancel")
	public String cancel();
	
	@DefaultStringValue("Capture Photo")
	public String capturePhoto();
	
	@DefaultStringValue("Confirm Password")
	public String confirmPassword();
	
	@DefaultStringValue("Created")
	public String created();
	
	@DefaultStringValue("Created From")
	public String createdFrom();
	
	@DefaultStringValue("Created To")
	public String createdTo();
	
	@DefaultStringValue("MM/dd/yyyy")
	public String dateFormat();
	
	@DefaultStringValue("MM/dd/yyyy hh:mm:ss a")
	public String dateTimeFormat();
	
	@DefaultStringValue("Delete")
	public String delete();
	
	@DefaultStringValue("Department of Informatics & Telecommunications, University of Athens")
	public String di();
	
	@DefaultStringValue("Download")
	public String download();
	
	@DefaultStringValue("Duration")
	public String duration();
	
	@DefaultStringValue("Edit")
	public String edit();
	
	@DefaultStringValue("Edited")
	public String edited();
	
	@DefaultStringValue("Edited From")
	public String editedFrom();
	
	@DefaultStringValue("Edited To")
	public String editedTo();
	
	@DefaultStringValue("Edit Media")
	public String editMedia();

	@DefaultStringValue("Email")
	public String email();
	
	@DefaultStringValue("File")
	public String file();

	@DefaultStringValue("Forgot Password?")
	public String forgotPassword_(); //logw periergou xarakthra (?)
	
	@DefaultStringValue("Latitude")
	public String latitude();
	
	@DefaultStringValue("Latitude/Longitude")
	public String latitudeLongitude();
	
	@DefaultStringValue("List")
	public String list();
	
	@DefaultStringValue("Login")
	public String login();
	
	@DefaultStringValue("Logout")
	public String logout();
	
	@DefaultStringValue("Longitude")
	public String longitude();
	
	@DefaultStringValue("Map")
	public String map();
	
	@DefaultStringValue("media not found")
	public String mediaNotFound();
	
	@DefaultStringValue("Mobile Media Share")
	public String mobileMediaShare();
	
	@DefaultStringValue("My Account")
	public String myAccount();

	@DefaultStringValue("Name")
	public String name();
	
	@DefaultStringValue("New Photo")
	public String newPhoto();
	
	@DefaultStringValue("New User")
	public String newUser();
	
	@DefaultStringValue("New Video")
	public String newVideo();
	
	@DefaultStringValue("no media ID specified")
	public String noMediaIdSpecified();
	
	@DefaultStringValue("not supported")
	public String notSupported();
	
	@DefaultStringValue("OK")
	public String ok();

	@DefaultStringValue("Page Size")
	public String pageSize();
	
	@DefaultStringValue("Password")
	public String password();
	
	@DefaultStringValue("Passwords do not match")
	public String passwordsDoNotMatch();

	@DefaultStringValue("Please fill in your email to reset your password")
	public String pleaseFillInYourEmailToResetYourPassword();

	@DefaultStringValue("Private")
	public String _private();
	
	@DefaultStringValue("Public")
	public String publik();
	
	@DefaultStringValue("Reset")
	public String reset();

	@DefaultStringValue("Return to login page")
	public String returnToLoginPage();
	
	@DefaultStringValue("Size")
	public String size();

	//# -> psifio, an einai 0 den to deixnei
	//0 -> an einai 0, tha to deixei
	@DefaultStringValue("0.### B")
	public String sizeBytesFormat();
	
	@DefaultStringValue("0.### KB")
	public String sizeKilobytesFormat();
	
	@DefaultStringValue("0.### MB")
	public String sizeMegabytesFormat();
	
	@DefaultStringValue("Start recording")
	public String startRecording();
	
	@DefaultStringValue("Status")
	public String status();
	
	@DefaultStringValue("Stop recording")
	public String stopRecording();
	
	@DefaultStringValue("Title")
	public String title();

	@DefaultStringValue("Type")
	public String type();
	
	@DefaultStringValue("Upload")
	public String upload();
	
	@DefaultStringValue("User")
	public String user();
	
	@DefaultStringValue("user not found")
	public String userNotFound();

	@DefaultStringValue("View Media")
	public String viewMedia();

	@DefaultStringValue("You are here:")
	public String youAreHere();
	
	@DefaultStringValue("You have received an email with instructions about completing your registration.")
	public String youHaveReceivedAnEmailWithInstructionsAboutCompletingYourRegistration();
	
	@DefaultStringValue("You have received an email with instructions about resetting your password.")
	public String youHaveReceivedAnEmailWithInstructionsAboutResettingYourPassword();
}
