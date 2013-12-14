package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.i18n.client.Messages;

public interface MobileMediaShareUrls extends Messages {
	@DefaultMessage("./mediaServlet?id={0}")
	public String download(final String id);
	
	@DefaultMessage("./edit.html?id={0}")
	public String edit(final String id);
	
	//sensor: an exei sensora h suskeuh pou fortwnetai o xarths
	@DefaultMessage("sensor=true&language={0}")
	public String googleMapsOptions(final String locale);
	
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
	
	@DefaultMessage("./images/markersSmall/{0}.svg")
	public String markerImage(final String name);
	
	@DefaultMessage("./images/{0}.svg")
	public String markerUpload(final String name);
	
	@DefaultMessage("./myAccount.html?locale={0}")
	public String myAccount(final String locale);
	
	@DefaultMessage("./newPhoto.html?locale={0}")
	public String newPhoto(final String locale);
	
	@DefaultMessage("./newUser.html?locale={0}")
	public String newUser(final String locale);
	
	@DefaultMessage("./newVideo.html?locale={0}")
	public String newVideo(final String locale);
	
	@DefaultMessage("./images/markersBig/{0}.svg")
	public String selectedImage(final String name);

	@DefaultMessage("./upload.html?locale={0}")
	public String upload(final String locale);
}
