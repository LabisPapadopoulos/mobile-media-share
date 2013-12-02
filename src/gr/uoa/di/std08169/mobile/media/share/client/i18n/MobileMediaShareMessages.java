package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

//Messages: Gia mhnumata me parametrous 
@LocalizableResource.DefaultLocale("en")
public interface MobileMediaShareMessages extends Messages {
	@DefaultMessage("{0}@...")
	public String emailFormat(final String email);
	
	//{0} to prwto orisma (message)
	@DefaultMessage("Error creating user: {0}")
	public String errorCreatingUser(final String message);
	
	@DefaultMessage("Error deleting media: {0}")
	public String errorDeletingMedia(final String message);
	
	@DefaultMessage("Error retrieving media: {0}")
	public String errorRetrievingMedia(final String message);
	
	@DefaultMessage("Error retrieving your location: {0}")
	public String errorRetrievingYourLocation(final String message);
	
	@DefaultMessage("{0}° {1}′ {2}″ N")
	public String latitudeFormatNorth(final int degrees, final int minutes, final int seconds);

	@DefaultMessage("{0}° {1}′ {2}″ S")
	public String latitudeFormatSouth(final int degrees, final int minutes, final int seconds);
	
	@DefaultMessage("{0}° {1}′ {2}″ E")
	public String longitudeFormatEast(final int degrees, final int minutes, final int seconds);
	
	@DefaultMessage("{0}° {1}′ {2}″ W")
	public String longitudeFormatWest(final int degrees, final int minutes, final int seconds);
	
	@DefaultMessage("User {0} already exists")
	public String userAlreadyExists(final String email);
	
	@DefaultMessage("{0} ({1}@...)")
	public String userFormat(final String name, final String email);
}
