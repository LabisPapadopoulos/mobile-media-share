package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.util.HashMap;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;

public class Header /*implements EventListener*/ {
	//Interface: gia na boresoume na to xrhsimopoihsmoume me GWT create
	//UiBinder: klassh gia na psareuei templates.
	//1o Orisma: Ti einai to template mou (div, eikona)
	//2o Orisma: San ti klash thelw na to psarepsw (ws header)
	//protected: gia na borei na xrhsimopoihthei apo tin klash paidi pou tha ftiaxei to GWT
	//static: gia na anoikei s' olh tin klash kai na borei na xrhsimopoihthei san 
	//Header.HeaderUiBinder anti gia new Header().HeaderUiBinder
	//H HeaderUiBinder orizetai molis fortwthei (apo ton class loader) kai h Header.
	protected static interface HeaderUiBinder extends UiBinder<DivElement, Header> {}

	private static final HeaderUiBinder HEADER_UI_BINDER = GWT.create(HeaderUiBinder.class); 
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS =
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	
	protected DivElement header;
	protected java.util.Map<ImageElement, String> locales;
	
	//DOM element
	//To GWT tha vrei UI field me onoma logo kai tha to valei stin metavlhth logo
	//protected giati tha ftiaxei ulopoihsh kai tha prepei na ta piraxei kai to GWT
	@UiField
	protected ImageElement logo;
	
	@UiField
	protected HeadingElement title;

	@UiField
	protected AnchorElement mapLink; //<a></a>
	
	@UiField
	protected SpanElement map;
	
	@UiField
	protected AnchorElement listLink; //<a></a>
	
	@UiField
	protected SpanElement list;
	
	@UiField
	protected AnchorElement newPhotoLink; //<a></a>
	
	@UiField
	protected SpanElement newPhoto;
	
	@UiField
	protected AnchorElement newVideoLink; //<a></a>
	
	@UiField
	protected SpanElement newVideo;
	
	@UiField
	protected AnchorElement uploadLink; //<a></a>
	
	@UiField
	protected SpanElement upload;
	
	@UiField
	protected AnchorElement myAccountLink; //<a></a>
	
	@UiField
	protected SpanElement myAccount;
	
	@UiField
	protected AnchorElement logoutLink; //<a></a>
	
	@UiField
	protected SpanElement logout;
	
	//Factory: xrhsh tis methodou gia tin dhmiourgia tou instance tou header
	//(logw protected constructor)
	public static DivElement newHeader() {
		return new Header().header;
	}
	
	protected Header() {
		//sto this fernei to header (to div) pou molis eftiaxe
		header = HEADER_UI_BINDER.createAndBindUi(this);
		locales = new HashMap<ImageElement, String>();
		logo.setAlt(MOBILE_MEDIA_SHARE_CONSTANTS.mobileMediaShare()); //alt=""
		//Gia pragmata pou theloun periexomeno
		title.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.mobileMediaShare());
		//redirect sto map kai krataei kai tin glwssa
		mapLink.setHref(MOBILE_MEDIA_SHARE_URLS.map(LocaleInfo.getCurrentLocale().getLocaleName()));
		mapLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.map());
		map.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.map());
		listLink.setHref(MOBILE_MEDIA_SHARE_URLS.list(LocaleInfo.getCurrentLocale().getLocaleName()));
		listLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.list());
		list.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.list());
		newPhotoLink.setHref(MOBILE_MEDIA_SHARE_URLS.newPhoto(LocaleInfo.getCurrentLocale().getLocaleName()));
		newPhotoLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.newPhoto());
		newPhoto.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.newPhoto());
		newVideoLink.setHref(MOBILE_MEDIA_SHARE_URLS.newVideo(LocaleInfo.getCurrentLocale().getLocaleName()));
		newVideoLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.newVideo());
		newVideo.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.newVideo());
		uploadLink.setHref(MOBILE_MEDIA_SHARE_URLS.upload(LocaleInfo.getCurrentLocale().getLocaleName()));
		uploadLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.upload());
		upload.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.upload());
		myAccountLink.setHref(MOBILE_MEDIA_SHARE_URLS.myAccount(LocaleInfo.getCurrentLocale().getLocaleName()));
		myAccountLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.myAccount());
		myAccount.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.myAccount());
		logoutLink.setHref(MOBILE_MEDIA_SHARE_URLS.logout(LocaleInfo.getCurrentLocale().getLocaleName()));
		logoutLink.setTitle(MOBILE_MEDIA_SHARE_CONSTANTS.logout());
		logout.setInnerText(MOBILE_MEDIA_SHARE_CONSTANTS.logout());
		
		
		//Gia kathe diathesimo locale (apo to MobileMediaShare.gwt.xml)
		for (String locale : LocaleInfo.getAvailableLocaleNames()) {
			if (!locale.equals("default")) {
				final AnchorElement localeAnchor = Document.get().createAnchorElement();
				localeAnchor.setHref(Window.Location.createUrlBuilder().setParameter("locale", locale).buildString());
				localeAnchor.setTitle(LocaleInfo.getLocaleNativeDisplayName(locale));
				
				final ImageElement localeImage = Document.get().createImageElement();
				localeImage.setSrc(MOBILE_MEDIA_SHARE_URLS.localeImage(locale));
									//Emfanizei to locale sti glwssa tou
				localeImage.setAlt(LocaleInfo.getLocaleNativeDisplayName(locale));
				localeAnchor.appendChild(localeImage);
				
				header.appendChild(localeAnchor);
//				locales.put(localeImage, locale);
//				//Poia event tha stelnei sto logo me onClick
//				Event.sinkEvents(localeImage, Event.ONCLICK);
//				//Ti tha kanei otan ginei to event (tha trexei tin onBrowserEvent)
//				Event.setEventListener(localeImage, this);
			}
		}
	}

//	@Override
//	public void onBrowserEvent(final Event event) {
//		//Redirect sto url pou prokuptei apo to trexon url, an allaxtei to locale me auto pou prepei
//		Window.Location.assign(Window.Location.createUrlBuilder().setParameter("locale",
//				//getEventTarget: se poio element egine epanw to click (localeImage)
//				locales.get(event.getEventTarget())).buildString());
//	}
}
