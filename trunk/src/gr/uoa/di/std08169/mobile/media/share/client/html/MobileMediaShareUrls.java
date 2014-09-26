package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.i18n.client.Messages;

public interface MobileMediaShareUrls extends Messages {
	@DefaultMessage("{0}images/defaultUser.png")
	public String defaultUser(final String baseUrl);

	@DefaultMessage("http://www.di.uoa.gr/{0}/")
	public String di(final String locale);
	
	@DefaultMessage("{0}mediaServlet?action=downloadMedia&id={1}")
	public String download(final String baseUrl, final String id);
	
	@DefaultMessage("{0}editMedia.jsp?locale={1}&id={2}")
	public String editMedia(final String baseUrl, final String locale, final String id);
	
	@DefaultMessage("{0}forgotPassword.html?locale={1}")
	public String forgotPassword(final String baseUrl, final String locale);
	
	//sensor: an exei sensora h suskeuh pou fortwnetai o xarths
	@DefaultMessage("sensor=true&language={0}")
	public String googleMapsOptions(final String locale);
	
	@DefaultMessage("{0}images/locales/{1}.png")
	public String localeImage(final String baseUrl, final String locale);

	@DefaultMessage("{0}list.jsp?locale={1}")
	public String list(final String baseUrl, final String locale);
	
	@DefaultMessage("{0}login.html?locale={1}&url={2}")
	public String login(final String baseUrl, final String locale, final String url);
	
	@DefaultMessage("{0}logout.jsp?locale={1}")
	public String logout(final String baseUrl, final String locale);
	
	@DefaultMessage("{0}map.jsp?locale={1}")
	public String map(final String baseUrl, final String locale);
	
	@DefaultMessage("{0}images/markersSmall/{1}.svg")
	public String markerImage(final String baseUrl, final String name);
	
	@DefaultMessage("{0}myAccount.jsp?locale={1}")
	public String myAccount(final String baseUrl, final String locale);
	
	@DefaultMessage("{0}newPhoto.jsp?locale={1}")
	public String newPhoto(final String baseUrl, final String locale);
	
	@DefaultMessage("{0}newUser.html?locale={1}")
	public String newUser(final String baseUrl, final String locale);
	
	@DefaultMessage("{0}newVideo.jsp?locale={1}")
	public String newVideo(final String baseUrl, final String locale);
	
	@DefaultMessage("http://www.di.uoa.gr/~std08169/")
	public String personalWebSite();
	
	@DefaultMessage("{0}images/markersBig/{1}.svg")
	public String selectedImage(final String baseUrl, final String name);

	@DefaultMessage("{0}upload.jsp?locale={1}")
	public String upload(final String baseUrl, final String locale);

	@DefaultMessage("{0}userServlet?locale={1}&email={2}&action=forgot")
	public String userServletForgot(final String baseUrl, final String locale, final String email);
	
	@DefaultMessage("{0}userServlet?locale={1}&email={2}&password={3}&password2={4}")
	public String userServletRegister(final String baseUrl, final String locale, final String email, final String password, final String password2);

	@DefaultMessage("{0}userServlet?locale={1}&token={2}&password={3}&password2={4}&action=reset")
	public String userServletReset(final String baseUrl, final String locale, final String token, final String password, final String password2);

	@DefaultMessage("{0}viewMedia.jsp?locale={1}&id={2}")
	public String viewMedia(final String baseUrl, final String locale, final String id);
}
