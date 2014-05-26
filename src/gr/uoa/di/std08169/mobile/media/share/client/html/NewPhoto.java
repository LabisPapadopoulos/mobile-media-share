package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.dom.client.Style.Display;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
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

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;

public class NewPhoto extends Composite implements ClickHandler, EntryPoint, GoogleMap.ClickHandler, KeyUpHandler, Runnable {	
	protected static interface NewPhotoUiBinder extends UiBinder<Widget, NewPhoto> {}
	
	private static final NewPhotoUiBinder NEW_PHOTO_UI_BINDER = GWT.create(NewPhotoUiBinder.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	
	@UiField
	protected VideoElement video;
	@UiField
	protected CanvasElement canvas;
	@UiField
	protected Button capture;
	@UiField
	protected Hidden photo;
	@UiField
	protected TextBox title;
	@UiField
	protected InlineLabel latitudeLongitude;
	@UiField
	protected DivElement map;
	@UiField
	protected Hidden latitude;
	@UiField
	protected Hidden longitude;
	@UiField
	protected Hidden locale;
	@UiField
	protected SubmitButton ok;
	@UiField
	protected ResetButton reset;
	private BigDecimal defaultLatitude;
	private BigDecimal defaultLongitude;
	private GoogleMap googleMap;
	private Marker marker;
		
	public NewPhoto() {		
		//Arxikopoihsh tou grafikou me ton Ui Binder
		initWidget(NEW_PHOTO_UI_BINDER.createAndBindUi(this));
		capture.addClickHandler(this);
		capture.setEnabled(false);
		title.addKeyUpHandler(this);
		locale.setValue(LocaleInfo.getCurrentLocale().getLocaleName());
		ok.setEnabled(false);
		reset.addClickHandler(this);
	}
	
	//Apo ton ClickListener tou GoogleMap (GoogleMap.ClickHandler) 
	@Override
	public void handle(final MouseEvent event) {
		//enhmerwsh etiketas
		latitudeLongitude.setText("(" + List.formatLatitude(new BigDecimal(event.getLatLng().lat())) + ", " +
				List.formatLongitude(new BigDecimal(event.getLatLng().lng())) + ")");
		//enhmerwsh xarth
		googleMap.setCenter(event.getLatLng());
		//orismos theshs marker
		marker.setPosition(event.getLatLng());
		//orismos timwn twn latitude and longitude
		latitude.setValue(new BigDecimal(event.getLatLng().lat()).toString());
		longitude.setValue(new BigDecimal(event.getLatLng().lng()).toString());
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == capture) {
			//zwgrafizei se sugkekrimenes diastaseis, analoga me tin kamera tou xrhsth
			canvas.setWidth(video.getVideoWidth());
			canvas.setHeight(video.getVideoHeight());
			//Sto hidden photo orizetai to photo data url (by default .png)
			photo.setValue(capture());
			//apokrupsh video
			video.getStyle().setDisplay(Display.NONE);
			//emfanish canvas
			canvas.getStyle().setDisplay(Display.BLOCK);
			ok.setEnabled(!title.getValue().trim().isEmpty());
		} else if (clickEvent.getSource() == reset) {
			video.getStyle().setDisplay(Display.BLOCK);
			canvas.getStyle().setDisplay(Display.NONE);
			//katharizei to canvas
			canvas.setWidth(0);
			canvas.setHeight(0);
			photo.setValue(null);
			title.setValue(null);
			latitudeLongitude.setText("(" + List.formatLatitude(defaultLatitude) + ", " + List.formatLongitude(defaultLongitude) + ")");
			final LatLng latLng = LatLng.create(defaultLatitude.doubleValue(), defaultLongitude.doubleValue()); 
			googleMap.setCenter(latLng);
			googleMap.setZoom(Map.GOOGLE_MAPS_ZOOM);
			marker.setPosition(latLng);
			//orismos timwn twn latitude and longitude
			latitude.setValue(defaultLatitude.toString());
			longitude.setValue(defaultLongitude.toString());
			ok.setEnabled(false);
		} 
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent _) {
		ok.setEnabled((!photo.getValue().trim().isEmpty()) && (!title.getValue().trim().isEmpty()));
	}
	
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
		//Ajax loader: fortwnei pragmata mesw ajax
		//Ruthmiseis gia to google maps
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		AjaxLoader.loadApi(Map.GOOGLE_MAPS_API, Map.GOOGLE_MAPS_VERSION, this, options);
	}

	@Override
	public void run() {
		final MapOptions options = MapOptions.create(); //Dhmiourgeia antikeimenou me Factory (xwris constructor)
		//Default na fenetai xarths apo dorhforo (Hybrid)
		options.setMapTypeId(MapTypeId.HYBRID);
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		googleMap = GoogleMap.create(map, options);
		googleMap.addClickListener(this);
		final MarkerOptions markerOptions = MarkerOptions.create();
		markerOptions.setMap(googleMap);
		markerOptions.setIcon(MarkerImage.create(Upload.MARKER_URL));
		marker = Marker.create(markerOptions);
		//Statikh javascript klash pou elenxei an o browser upostirizei geografiko prosdiorismo theshs (san Window.alert)
		Geolocation.getIfSupported().getCurrentPosition(new Callback<Position, PositionError>() {
			@Override
			public void onFailure(final PositionError error) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingYourLocation(error.getMessage()));
				defaultLatitude = new BigDecimal(Map.GOOGLE_MAPS_LATITUDE);
				defaultLongitude = new BigDecimal(Map.GOOGLE_MAPS_LONGITUDE);
				reset.click();
			}

			@Override
			public void onSuccess(final Position position) {
				defaultLatitude = new BigDecimal(position.getCoordinates().getLatitude());
				defaultLongitude = new BigDecimal(position.getCoordinates().getLongitude());
				reset.click();
			}
		});
		initializeVideo();
	}
	
	//Arxikopoihsh video me kwdika Javascript
	//Se java suntaktika einai san abstract (me ulopoihsh javascript, anti gia C) sunarthsh
	//'h san mia dhlwsh sunartish enos interface
	//Native: afou den einai ulopoihmenh se java
	private native void initializeVideo() /*-{
		//navigator: singleton antikeimeno js pou antiproswpeuei ton browser
		//*getUserMedia: ti polhmesa upostirizei o upologisths tou xrhsth
		if (navigator.getUserMedia == null) {
			if (navigator.mozGetUserMedia != null)
				navigator.getUserMedia = navigator.mozGetUserMedia;
			else if (navigator.msGetUserMedia != null)
				navigator.getUserMedia = navigator.msGetUserMedia;
			else if (navigator.webkitGetUserMedia != null)
				navigator.getUserMedia = navigator.webkitGetUserMedia;
		}
		if (navigator.getUserMedia == null) {
			//Klhsh tis sunartishs (::...) userMediaError apo tin klash (@...NewPhoto)
			//(Ljava/lang/String;): gia overloading stin sunartish userMediaError
			//(casting to null se String gia tin Java)
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaError(Ljava/lang/Integer;)(null);
			return;
		}
		if ($wnd.URL == null)
			$wnd.URL = $wnd.webkitURL;
		if ($wnd.URL == null) {
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaError(Ljava/lang/Integer;)(null);
			return;
		}
		//mono gia video
		//this: to newPhoto java antikeimeno
		var that = this;
		navigator.getUserMedia({video: true},
		 	//Sto on Success kalei tin userMediaSuccess kai pernaei sto video pou travaei h getUserMedia
			function (stream) {
				//that: klish epanw sto newPhoto java object (to instance tin klashs)
				that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaSuccess(Ljava/lang/String;)($wnd.URL.createObjectURL(stream));
			},
			//Sto on Error kalei tin userMediaError kai pernaei to error event h getUserMedia
			function (error) {
				that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaError(Ljava/lang/Integer;)(error.code);
			});
	}-*/;
	
	private void userMediaError(final Integer error) {
		if ((error != null) && (error == 1))
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCapturingPhoto(MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
		else
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCapturingPhoto(MOBILE_MEDIA_SHARE_CONSTANTS.notSupported()));
	}
	
	private void userMediaSuccess(final String url) {
		video.setSrc(url);
		video.play();
		capture.setEnabled(true);
	}
	
	private native String capture() /*-{
		var canvas = this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::canvas;
		var video = this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::video;
		canvas.getContext('2d').drawImage(video, 0, 0, video.videoWidth, video.videoHeight);
		return canvas.toDataURL();
	}-*/;
}
