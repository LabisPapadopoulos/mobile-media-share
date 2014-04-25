package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;
import java.util.Date;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.MarkerImage;
import com.google.maps.gwt.client.MarkerOptions;
import com.google.maps.gwt.client.MouseEvent;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;

public class EditMedia extends Composite implements ClickHandler, EntryPoint, GoogleMap.ClickHandler, KeyUpHandler, Runnable {
	//To interface ftiaxnei ena widget me vash to EditMedia
	protected static interface EditMediaUiBinder extends UiBinder<Widget, EditMedia> {}
	
	private static final EditMediaUiBinder EDIT_MEDIA_UI_BINDER = GWT.create(EditMediaUiBinder.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	
	private static final MediaServiceAsync MEDIA_SERVICE = GWT.create(MediaService.class);
	
	@UiField
	protected TextBox title;
	@UiField
	protected CheckBox publik;
	@UiField
	protected InlineLabel latitudeLongitude;
	@UiField
	protected DivElement map;
	@UiField
	protected Button ok;
	@UiField
	protected Button reset;
	private GoogleMap googleMap;
	private Marker marker;
	private Media media;
	
	public EditMedia() {
		//Dhmiourgeia grafikou tou widget
		initWidget(EDIT_MEDIA_UI_BINDER.createAndBindUi(this));
		title.addKeyUpHandler(this);
		ok.addClickHandler(this);
		reset.addClickHandler(this);
	}
	
	@Override
	public void handle(final MouseEvent event) { // click sto xarth
		latitudeLongitude.setText("(" + List.formatLatitude(new BigDecimal(event.getLatLng().lat())) + ", " + 
				List.formatLongitude(new BigDecimal(event.getLatLng().lng())) + ")");
		googleMap.setCenter(event.getLatLng());
		marker.setPosition(event.getLatLng());
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == ok) {
			media.setTitle(title.getValue());
			media.setPublic(publik.getValue());
			//apo ton marker thetei tin nea thesh tou antikeimenou
			media.setLatitude(new BigDecimal(marker.getPosition().lat()));
			media.setLongitude(new BigDecimal(marker.getPosition().lng()));
			media.setEdited(new Date());
			MEDIA_SERVICE.editMedia(media, new AsyncCallback<Void>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorEditingMedia(throwable.getMessage()));
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							//me to antistoixo locale 
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}

				@Override
				public void onSuccess(final Void _) {
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							//me to antistoixo locale 
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}
			});
		} else if (clickEvent.getSource() == reset) {
			title.setValue(media.getTitle());
			publik.setValue(media.isPublic());
			latitudeLongitude.setText("(" + List.formatLatitude(media.getLatitude()) + ", " + List.formatLongitude(media.getLongitude()) + ")");
			//Antikeimeno (javascript) pou periexei suntetagmenes xarth
			final LatLng latLng = LatLng.create(media.getLatitude().doubleValue(), media.getLongitude().doubleValue());
			googleMap.setCenter(latLng);
			googleMap.setZoom(Map.GOOGLE_MAPS_ZOOM);
			marker.setPosition(latLng);
		}
		
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		ok.setEnabled(!title.getValue().isEmpty());
	}

	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
		//options gia to fortwma tis vivliothikhs
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		//Xekinaei na fortwnei Google Maps me ta options kai otan teleiwsei trexei tin run tou this (EditMedia)
		AjaxLoader.loadApi(Map.GOOGLE_MAPS_API, Map.GOOGLE_MAPS_VERSION, this, options);
	}

	@Override
	public void run() {
		//options gia ton sugkekrimeno xarth
		final MapOptions options = MapOptions.create(); //Dhmiourgeia antikeimenou me Factory (xwris constructor)
		//Default na fenetai xarths apo doruforo (Hybrid)
		options.setMapTypeId(MapTypeId.HYBRID);
		options.setZoom(Map.GOOGLE_MAPS_ZOOM);
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		googleMap = GoogleMap.create(map, options);
		//Otan o xrhsths kanei click epanw ston xarth
		googleMap.addClickListener(this);
		final MarkerOptions markerOptions = MarkerOptions.create();
		markerOptions.setMap(googleMap);
		markerOptions.setIcon(MarkerImage.create(Upload.MARKER_URL));
		marker = Marker.create(markerOptions);
		//psaxnei antikeimeno gia na kentrarei o xarths kai na fortwsei h forma
		final String id = Window.Location.getParameter("id");
		if (id == null) {
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingMedium(
					MOBILE_MEDIA_SHARE_CONSTANTS.noMediaIdSpecified()));
			//redirect sto map
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
		} else {
			//Klhsh tou MEDIA_SERVICE gia na paroume to antikeimeno
			MEDIA_SERVICE.getMedia(id, new AsyncCallback<Media>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingMedium(throwable.getMessage()));
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							//me to antistoixo locale 
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}

				@Override
				public void onSuccess(final Media media) {
					if (media == null) {
						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorEditingMedia(
								MOBILE_MEDIA_SHARE_CONSTANTS.mediaNotFound()));
						//redirect sto map
						Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
								//me to antistoixo locale 
								LocaleInfo.getCurrentLocale().getLocaleName())));
					//Vrethike to media kai anhkei ston sundedemeno xrhsth 
					} else if (media.getUser().getEmail().equals(InputElement.as(Document.get().getElementById("email")).getValue())) {
						//Topika apothikeush tou media pou tha ginei edit
						EditMedia.this.media = media;
						//Click sto reset gia na parei times forma, xarths kai marker
						reset.click();
					} else { // to media vrethike alla den anhkei sto xrhsth
						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorEditingMedia(
								MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
						//redirect sto map
						Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
								//me to antistoixo locale 
								LocaleInfo.getCurrentLocale().getLocaleName())));
					}
				}
			});
		}
	}
}
