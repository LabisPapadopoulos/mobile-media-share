package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource;

@LocalizableResource.DefaultLocale("en")
public interface UserStatusConstants extends ConstantsWithLookup {
	@DefaultStringValue("Administrator")
	public String ADMIN();
	
	@DefaultStringValue("Forgot Password")
	public String FORGOT();
	
	@DefaultStringValue("Locked")
	public String LOCKED();
	
	@DefaultStringValue("Normal")
	public String NORMAL();
	
	@DefaultStringValue("Pending")
	public String PENDING();
}
