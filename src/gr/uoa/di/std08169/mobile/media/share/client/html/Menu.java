package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Menu extends Composite {
	protected static interface MenuUiBinder extends UiBinder<Widget, Menu> {}

	private static final MenuUiBinder MENU_UI_BINDER = GWT.create(MenuUiBinder.class); 
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = GWT.create(MobileMediaShareUrls.class);
	
	@UiField
	protected AnchorElement mapLink; //<a></a>
	
	@UiField
	protected AnchorElement listLink; //<a></a>
	
	@UiField
	protected AnchorElement newPhotoLink; //<a></a>
	
	@UiField
	protected AnchorElement newVideoLink; //<a></a>
	
	@UiField
	protected AnchorElement uploadLink; //<a></a>
	
	@UiField
	protected AnchorElement myAccountLink; //<a></a>
		
	@UiField
	protected AnchorElement logoutLink; //<a></a>

	
	protected Menu() {
		//sto this fernei to header (to div) pou molis eftiaxe
		initWidget(MENU_UI_BINDER.createAndBindUi(this));
		mapLink.setHref(MOBILE_MEDIA_SHARE_URLS.map(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		listLink.setHref(MOBILE_MEDIA_SHARE_URLS.list(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		newPhotoLink.setHref(MOBILE_MEDIA_SHARE_URLS.newPhoto(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		newVideoLink.setHref(MOBILE_MEDIA_SHARE_URLS.newVideo(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		uploadLink.setHref(MOBILE_MEDIA_SHARE_URLS.upload(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		myAccountLink.setHref(MOBILE_MEDIA_SHARE_URLS.myAccount(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		logoutLink.setHref(MOBILE_MEDIA_SHARE_URLS.logout(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
	}
}
