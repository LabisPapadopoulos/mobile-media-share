package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.i18n.client.Messages;

public interface MobileMediaShareUrls extends Messages {
	@DefaultMessage("./uploadServlet?id={0}")
	public String download(final String id);
	
	@DefaultMessage("./images/locales/{0}.png")
	public String localeImage(final String locale);

	@DefaultMessage("./list.html?locale={0}")
	public String list(final String locale);
	
	@DefaultMessage("./login.html?locale={0}&url={1}")
	public String login(final String locale, final String url);
	
	@DefaultMessage("./logout.html?locale={0}")
	public String logout(final String locale);
	
	@DefaultMessage("./map.html?locale={0}")
	public String map(final String locale);
	
	@DefaultMessage("./myAccount.html?locale={0}")
	public String myAccount(final String locale);
	
	@DefaultMessage("./newPhoto.html?locale={0}")
	public String newPhoto(final String locale);
	
	@DefaultMessage("./newUser.html?locale={0}")
	public String newUser(final String locale);
	
	@DefaultMessage("./newVideo.html?locale={0}")
	public String newVideo(final String locale);
	
	@DefaultMessage("./upload.html?locale={0}")
	public String upload(final String locale);
}
