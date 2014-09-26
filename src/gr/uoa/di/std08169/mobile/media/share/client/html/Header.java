package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Composite {
	//Interface: gia na boresoume na to xrhsimopoihsmoume me GWT create
	//UiBinder: klassh gia na psareuei templates.
	//1o Orisma: Ti einai to template mou (div, eikona) h widget gia na einai oloklhrh html pou borei na xrhsimopoihsei meta to GWT
	//2o Orisma: San ti klash thelw na to psarepsw (ws header)
	//protected: gia na borei na xrhsimopoihthei apo tin klash paidi pou tha ftiaxei to GWT
	//static: gia na anoikei s' olh tin klash kai na borei na xrhsimopoihthei san 
	//Header.HeaderUiBinder anti gia new Header().HeaderUiBinder
	//H HeaderUiBinder orizetai molis fortwthei (apo ton class loader) kai h Header.
	protected static interface HeaderUiBinder extends UiBinder<Widget, Header> {}

	private static final HeaderUiBinder HEADER_UI_BINDER = GWT.create(HeaderUiBinder.class); 
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = GWT.create(MobileMediaShareUrls.class);
	
	//DOM element
	//To GWT tha vrei UI field me onoma logo kai tha to valei stin metavlhth logo
	//protected giati tha ftiaxei ulopoihsh kai tha prepei na ta piraxei kai to GWT
	@UiField
	protected DivElement banner;
	
	@UiField
	protected AnchorElement di; //<a></a>

	@UiField
	protected AnchorElement startPage; //<a></a>
		
	protected Header() {
		//sto this fernei to header (to div) pou molis eftiaxe
		initWidget(HEADER_UI_BINDER.createAndBindUi(this));
		//Gia pragmata pou theloun periexomeno
		//redirect sto map kai krataei kai tin glwssa
		di.setHref(MOBILE_MEDIA_SHARE_URLS.di(LocaleInfo.getCurrentLocale().getLocaleName()));
		startPage.setHref(MOBILE_MEDIA_SHARE_URLS.map(GWT.getHostPageBaseURL(), LocaleInfo.getCurrentLocale().getLocaleName()));
		//Gia kathe diathesimo locale (apo to MobileMediaShare.gwt.xml)
		for (String locale : LocaleInfo.getAvailableLocaleNames()) {
			if (!locale.equals("default")) {
				final AnchorElement localeAnchor = Document.get().createAnchorElement();
				localeAnchor.setHref(Window.Location.createUrlBuilder().setParameter("locale", locale).buildString());
				localeAnchor.setTitle(LocaleInfo.getLocaleNativeDisplayName(locale));
				localeAnchor.setClassName("locale_" + locale);
				final ImageElement localeImage = Document.get().createImageElement();
				localeImage.setSrc(MOBILE_MEDIA_SHARE_URLS.localeImage(GWT.getHostPageBaseURL(), locale));
									//Emfanizei to locale sti glwssa tou
				localeImage.setAlt(LocaleInfo.getLocaleNativeDisplayName(locale));
				localeAnchor.appendChild(localeImage);
				banner.appendChild(localeAnchor);
			}
		}
	}
}
