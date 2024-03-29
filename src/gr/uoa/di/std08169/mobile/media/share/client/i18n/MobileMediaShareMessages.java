package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

//Messages: Gia mhnumata me parametrous 
@LocalizableResource.DefaultLocale("en")
public interface MobileMediaShareMessages extends Messages {
	@DefaultMessage("{0}h {1}m {2}s")
	public String durationFormatHoursMinutesSeconds(final int hours, final int minutes, final int seconds);
	
	@DefaultMessage("{0}m {1}s")
	public String durationFormatMinutesSeconds(final int minutes, final int seconds);
	
	@DefaultMessage("{0}s")
	public String durationFormatSeconds(final int seconds);
	
	@DefaultMessage("{0}@...")
	public String emailFormat(final String email);
	
	@DefaultMessage("Error capturing photo: {0}")
	public String errorCapturingPhoto(final String message);
	
	@DefaultMessage("Error capturing video: {0}")
	public String errorCapturingVideo(final String message);
	
	//{0} to prwto orisma (message)
	@DefaultMessage("Error creating user: {0}")
	public String errorCreatingUser(final String message);
	
	@DefaultMessage("Error deleting media: {0}")
	public String errorDeletingMedia(final String message);
	
	@DefaultMessage("Error editing media: {0}")
	public String errorEditingMedia(final String message);
	
	@DefaultMessage("Error editing user: {0}")
	public String errorEditingUser(final String message);
	
	@DefaultMessage("Error resetting password: {0}")
	public String errorResettingPassword(final String message);
	
	@DefaultMessage("Error retrieving media: {0}")
	public String errorRetrievingMedia(final String message);
	
	@DefaultMessage("Error retrieving medium: {0}")
	public String errorRetrievingMedium(final String message);
	
	@DefaultMessage("Error retrieving user: {0}")
	public String errorRetrievingUser(final String message);
	
	@DefaultMessage("Error retrieving your location: {0}")
	public String errorRetrievingYourLocation(final String message);
	
	@DefaultMessage("Error uploading video: {0}")
	public String errorUploadingVideo(final String message);
	
	@DefaultMessage("Error viewing media: {0}")
	public String errorViewingMedia(final String message);
	
	@DefaultMessage("{0}° {1}′ {2}″ N")
	public String latitudeFormatNorth(final int degrees, final int minutes, final int seconds);

	@DefaultMessage("{0}° {1}′ {2}″ S")
	public String latitudeFormatSouth(final int degrees, final int minutes, final int seconds);
	
	@DefaultMessage("{0}° {1}′ {2}″ E")
	public String longitudeFormatEast(final int degrees, final int minutes, final int seconds);
	
	@DefaultMessage("{0}° {1}′ {2}″ W")
	public String longitudeFormatWest(final int degrees, final int minutes, final int seconds);
	
	@DefaultMessage("Page {0}")
	public String page(final int page);
	
	@DefaultMessage("Page {0} of {1}, displaying photos {2} to {3} of {4}")
	public String pageOfDisplayingPhotosToOf(final int currentPage, final int totalPages, 
			final int startPhoto, final int endPhoto, final int totalPhotos);
	
	@DefaultMessage("User {0} already exists")
	public String userAlreadyExists(final String email);
	
	@DefaultMessage("{0} ({1}@...)")
	public String userFormat(final String name, final String email);
}
