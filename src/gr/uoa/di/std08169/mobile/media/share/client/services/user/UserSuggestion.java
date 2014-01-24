package gr.uoa.di.std08169.mobile.media.share.client.services.user;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.SuggestOracle;

public class UserSuggestion implements SuggestOracle.Suggestion {
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES = 
			GWT.create(MobileMediaShareMessages.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final char EMAIL_DELIMITER = '@';
	private final User user;
	
	public UserSuggestion(final User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	//Methodoi gia emfanish xrhstwn
	//ti tha deixei ston xrhsth gia na dialexei (lista pou tha fainetai katw apo to suggestBox)
	@Override
	public String getDisplayString() {
		// Labis (labis@...)
		return MOBILE_MEDIA_SHARE_MESSAGES.userFormat(
				(user.getName() == null) ? MOBILE_MEDIA_SHARE_CONSTANTS._anonymous_() : user.getName(), 
				user.getEmail().substring(0, user.getEmail().indexOf(EMAIL_DELIMITER)));
	}

	//ti tha deixnei ston xrhsth afou dialexei (panw sto suggestBox)
	@Override
	public String getReplacementString() {
		return MOBILE_MEDIA_SHARE_MESSAGES.emailFormat(user.getEmail().substring(0, user.getEmail().indexOf(EMAIL_DELIMITER)));
	}
}
