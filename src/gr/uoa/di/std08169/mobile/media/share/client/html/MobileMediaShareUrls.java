package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.i18n.client.Messages;

public interface MobileMediaShareUrls extends Messages {
	@DefaultMessage("./login.html?locale={0}&url={1}")
	public String login(final String locale, final String url);
	
	@DefaultMessage("./map.html?locale={0}")
	public String map(final String locale);
	
	@DefaultMessage("./newUser.html?locale={0}")
	public String newUser(final String locale);
}
