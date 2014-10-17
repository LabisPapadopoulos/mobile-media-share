package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Footer extends Composite {
	protected static interface FooterUiBinder extends UiBinder<Widget, Footer> {}

	private static final FooterUiBinder FOOTER_UI_BINDER = GWT.create(FooterUiBinder.class); 
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);

	@UiField
	protected AnchorElement administrator; //<a></a>
	@UiField
	protected AnchorElement html5Anchor;
	
	protected Footer() {
		//sto this fernei to header (to div) pou molis eftiaxe
		initWidget(FOOTER_UI_BINDER.createAndBindUi(this));
		//Gia pragmata pou theloun periexomeno
		//redirect sto map kai krataei kai tin glwssa
		administrator.setHref(MOBILE_MEDIA_SHARE_URLS.personalWebSite());
		html5Anchor.setHref(MOBILE_MEDIA_SHARE_URLS.html5LogoAnchor());
	}
}
