package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ResetButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SubmitButton;
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

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;

public class Upload extends Composite implements ChangeHandler, ClickHandler, EntryPoint, GoogleMap.ClickHandler, KeyUpHandler, Runnable {
	
	protected static interface UploadUiBinder extends UiBinder<Widget, Upload> {}
	
	public static final String MARKER_URL = "./images/uploadMarker.svg";
	
	private static final UploadUiBinder UPLOAD_UI_BINDER = 
			GWT.create(UploadUiBinder.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	
	@UiField
	protected FileUpload file;
	@UiField
	protected TextBox title;
	@UiField
	protected Hidden latitude;
	@UiField
	protected Hidden longitude;
	@UiField
	protected Hidden locale;
	@UiField
	protected DivElement mapContainer;
	@UiField
	protected SubmitButton ok;
	@UiField
	protected ResetButton reset;
	
	private Marker marker;
	
	public Upload() {
		initWidget(UPLOAD_UI_BINDER.createAndBindUi(this));
		//enhmerwnei otan allaxei h timh tou
		file.addChangeHandler(this);
		title.addKeyUpHandler(this);
		ok.setEnabled(false);
		reset.addClickHandler(this);
	}
	
	//Otan o xrhsths pataei panw ston xarth
	@Override
	public void handle(final MouseEvent event) {
		//orismos theshs marker
		marker.setPosition(event.getLatLng());
		//orismos timwn twn latitude and longitude
		latitude.setValue(new BigDecimal(event.getLatLng().lat()).toString());
		longitude.setValue(new BigDecimal(event.getLatLng().lng()).toString());
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
			ok.setEnabled(false);
	}

	//Otan allaxei timh tou file field
	@Override
	public void onChange(final ChangeEvent _) {
														//to onoma tou arxeiou na mhn einai keno
		ok.setEnabled((file.getFilename() != null) && (!file.getFilename().trim().isEmpty()) &&
				(!title.getValue().trim().isEmpty()));		
	}

	@Override
	public void onKeyUp(final KeyUpEvent _) {
		ok.setEnabled((file.getFilename() != null) && (!file.getFilename().trim().isEmpty()) &&
				(!title.getValue().trim().isEmpty()));		
	}

	@Override
	public void onModuleLoad() {
		locale.setValue(LocaleInfo.getCurrentLocale().getLocaleName());
		//Prosthikh tou widget stin selida
		RootPanel.get().add(this);
		//Ajax loader: fortwnei pragmata mesw ajax
		//Ruthmiseis gia to google maps
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		AjaxLoader.loadApi(Map.GOOGLE_MAPS_API, Map.GOOGLE_MAPS_VERSION, this, options);
	}

	//Molis fortwthei o xarths kaleitai h run
	@Override
	public void run() {
		final MapOptions options = MapOptions.create(); //Dhmiourgeia antikeimenou me Factory (xwris constructor)
		//Default na fenetai xarths apo dorhforo (Hybrid)
		options.setMapTypeId(MapTypeId.HYBRID);
		options.setZoom(Map.GOOGLE_MAPS_ZOOM);
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		final GoogleMap googleMap = GoogleMap.create(mapContainer, options);
		googleMap.addClickListener(this);
		final MarkerOptions markerOptions = MarkerOptions.create();
		markerOptions.setMap(googleMap);
		markerOptions.setIcon(MarkerImage.create(MARKER_URL));
		marker = Marker.create(markerOptions);
		//Statikh javascript klash pou elenxei an o browser upostirizei geografiko prosdiorismo theshs (san Window.alert)
		Geolocation.getIfSupported().getCurrentPosition(new Callback<Position, PositionError>() {
			@Override
			public void onFailure(final PositionError error) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingYourLocation(error.getMessage()));
				final LatLng latLng = LatLng.create(Map.GOOGLE_MAPS_LATITUDE, Map.GOOGLE_MAPS_LONGITUDE); 
				googleMap.setCenter(latLng);
				marker.setPosition(latLng);
				//orismos timwn twn latitude and longitude
				latitude.setValue(new BigDecimal(latLng.lat()).toString());
				longitude.setValue(new BigDecimal(latLng.lng()).toString());
			}

			@Override
			public void onSuccess(final Position position) {
				//Kentrarei o xarths sto shmeio pou vrethike o xrhsths (Latitude, Longitude)
										//Coordinates: pairnei tis suntetagmenes tis theshs
				final LatLng latLng = LatLng.create(position.getCoordinates().getLatitude(),
						position.getCoordinates().getLongitude());
				googleMap.setCenter(latLng);
				marker.setPosition(latLng);
				//orismos timwn twn latitude and longitude
				latitude.setValue(new BigDecimal(latLng.lat()).toString());
				longitude.setValue(new BigDecimal(latLng.lng()).toString());
			}
		});
	}
}
