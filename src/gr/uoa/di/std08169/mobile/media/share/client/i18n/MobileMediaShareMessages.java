package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

//Messages: Gia mhnumata me parametrous 
@LocalizableResource.DefaultLocale("en")
public interface MobileMediaShareMessages extends Messages {
	//{0} to prwto orisma (message)
	@DefaultMessage("Error creating user: {0}")
	public String errorCreatingUser(final String message);
	
	@DefaultMessage("User {0} already exists")
	public String userAlreadyExists(final String email);
}
