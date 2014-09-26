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
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
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

public class NewVideo extends Composite implements ClickHandler, EntryPoint, GoogleMap.ClickHandler, KeyUpHandler, Runnable {
	protected static interface NewVideoUiBinder extends UiBinder<Widget, NewVideo> {}
	
	private static final NewVideoUiBinder NEW_VIDEO_UI_BINDER = GWT.create(NewVideoUiBinder.class);
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
	protected Button startRecording;
	@UiField
	protected Button stopRecording;
	@UiField
	protected InlineLabel elapsedTime;
	@UiField
	protected TextBox title;
	@UiField
	protected CheckBox publik;
	@UiField
	protected InlineLabel latitudeLongitude;
	@UiField
	protected DivElement map;
	@UiField
	protected SubmitButton ok;
	@UiField
	protected ResetButton reset;
	private BigDecimal defaultLatitude;
	private BigDecimal defaultLongitude;
	private GoogleMap googleMap;
	private Marker marker;
	private BigDecimal latitude;
	private BigDecimal longitude;
	
	public NewVideo() {
		//Arxikopoihsh tou grafikou me ton Ui Binder
		initWidget(NEW_VIDEO_UI_BINDER.createAndBindUi(this));
		startRecording.addClickHandler(this);
		startRecording.setEnabled(false);
		stopRecording.addClickHandler(this);
		stopRecording.setEnabled(false);
		title.addKeyUpHandler(this);
		ok.setEnabled(false);
		ok.addClickHandler(this);
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
		latitude = new BigDecimal(event.getLatLng().lat());
		longitude = new BigDecimal(event.getLatLng().lng());
	}

	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == startRecording) {
			startRecording.setEnabled(false);
			stopRecording.setEnabled(true);
			canvas.setWidth(video.getVideoWidth());
			canvas.setHeight(video.getVideoHeight());
			updateElapsedTime(0);
			startRecording();
		} else if (clickEvent.getSource() == stopRecording) {
			stopRecording();
			canvas.setWidth(video.getVideoWidth());
			canvas.setHeight(video.getVideoHeight());
			stopRecording.setEnabled(false);
			startRecording.setEnabled(true);
							//na uparxoun frames (dedomena) & titlos
			ok.setEnabled((countFrames() > 0) && (!title.getValue().trim().isEmpty()));
		} else if (clickEvent.getSource() == ok) {
			ok.setEnabled(false);
			submit(title.getValue(), publik.getValue());
		} else if (clickEvent.getSource() == reset) {
			resetRecording();
			video.getStyle().setDisplay(Display.BLOCK);
			canvas.getStyle().setDisplay(Display.NONE);
			//katharizei to canvas
			canvas.setWidth(0);
			canvas.setHeight(0);
			title.setValue(null);
			latitudeLongitude.setText("(" + List.formatLatitude(defaultLatitude) + ", " + List.formatLongitude(defaultLongitude) + ")");
			final LatLng latLng = LatLng.create(defaultLatitude.doubleValue(), defaultLongitude.doubleValue()); 
			googleMap.setCenter(latLng);
			googleMap.setZoom(Map.GOOGLE_MAPS_ZOOM);
			marker.setPosition(latLng);
			//orismos timwn twn latitude and longitude
			latitude = defaultLatitude;
			longitude = defaultLongitude;
			stopRecording.setEnabled(false);
			ok.setEnabled(false);
			startRecording.setEnabled(true);
		}
	}
	
	@Override
	public void onKeyUp(final KeyUpEvent _) {
		//na uparxoun frames (dedomena) & titlos
		ok.setEnabled((countFrames() > 0) && (!title.getValue().trim().isEmpty()));
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
		if (navigator.getUserMedia == null)
			navigator.getUserMedia = navigator.mozGetUserMedia;
		if (navigator.getUserMedia == null)
			navigator.getUserMedia = navigator.msGetUserMedia;
		if (navigator.getUserMedia == null)
			navigator.getUserMedia = navigator.webkitGetUserMedia;
		if (navigator.getUserMedia == null) {
			//Klhsh tis sunartishs (::...) userMediaError apo tin klash (@...NewVideo)
			//(Ljava/lang/String;): gia overloading stin sunartish userMediaError
			//(casting to null se String gia tin Java)
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::userMediaError(Ljava/lang/Integer;)(null);
			return;
		}
		if ($wnd.URL == null)
			$wnd.URL = $wnd.webkitURL;
		if ($wnd.URL == null) {
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::userMediaError(Ljava/lang/Integer;)(null);
			return;
		}
		//An den uparxei to requestAnimationFrame (vazei callback gia otan zwgrafistei to epomeno kare) 
		//psaxnei na to vrei me tin seira apo extension
		// webkit	-> Safari, Chrome
		// moz		-> Firefox
		// ms		-> Internet Explorer
		// o		-> Opera
		if ($wnd.requestAnimationFrame == null)
			$wnd.requestAnimationFrame = $wnd.webkitRequestAnimationFrame;
		if ($wnd.requestAnimationFrame == null)
			$wnd.requestAnimationFrame = $wnd.mozRequestAnimationFrame;
		if ($wnd.requestAnimationFrame == null)
			$wnd.requestAnimationFrame = $wnd.msRequestAnimationFrame;
		if ($wnd.requestAnimationFrame == null)
			$wnd.requestAnimationFrame = $wnd.oRequestAnimationFrame;
		if ($wnd.requestAnimationFrame == null) {
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::userMediaError(Ljava/lang/Integer;)(null);
			return;
		}
		if ($wnd.cancelAnimationFrame == null)
			$wnd.cancelAnimationFrame = $wnd.webkitCancelAnimationFrame;
		if ($wnd.cancelAnimationFrame == null)
			$wnd.cancelAnimationFrame = $wnd.mozCancelAnimationFrame;
		if ($wnd.cancelAnimationFrame == null)
			$wnd.cancelAnimationFrame = $wnd.msCancelAnimationFrame;
		if ($wnd.cancelAnimationFrame == null)
			$wnd.cancelAnimationFrame = $wnd.oCancelAnimationFrame;
		if ($wnd.cancelAnimationFrame == null) {
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::userMediaError(Ljava/lang/Integer;)(null);
			return;
		}
		//katharismos tou recording
		this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::resetRecording()();
		
		//mono gia video
		//this: to newVideo java antikeimeno
		var that = this;
		navigator.getUserMedia({video: true},
		 	//Sto on Success kalei tin userMediaSuccess kai pernaei sto video pou travaei h getUserMedia
			function (stream) {
				//that: klish epanw sto newVideo java object (to instance tin klashs)
				that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::userMediaSuccess(Ljava/lang/String;)($wnd.URL.createObjectURL(stream));
			},
			//Sto on Error kalei tin userMediaError kai pernaei to error event h getUserMedia
			function (error) {
				that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::userMediaError(Ljava/lang/Integer;)(error.code);
			});
	}-*/;
	
	private void userMediaError(final Integer error) {
		if ((error != null) && (error == 1))
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCapturingVideo(MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
		else
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCapturingVideo(MOBILE_MEDIA_SHARE_CONSTANTS.notSupported()));
	}
	
	 //TODO remove
	private void updateElapsedTime(final Integer elapsedTime) {
Window.alert("Elapsed time: " + elapsedTime);
		this.elapsedTime.setText(List.formatDuration(elapsedTime));
	}
	
	private void userMediaSuccess(final String url) {
		video.setSrc(url);
		video.play();
		startRecording.setEnabled(true);
	}
	
	private native void startRecording() /*-{
		this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::resetRecording()();
		$wnd.video.isRecording = true;
		$wnd.video.frameId = $wnd.requestAnimationFrame($wnd.video.recording);
	}-*/;
	
	private native void stopRecording() /*-{
		$wnd.video.isRecording = false;
		$wnd.cancelAnimationFrame($wnd.video.frameId);
		$wnd.video.elapsedTime = Math.floor((Date.now() - $wnd.video.startTime) / 1000);
console.log('$wnd.video.elapsedTime = ' + $wnd.video.elapsedTime); 
		this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::updateElapsedTime(Ljava/lang/Integer;)($wnd.video.elapsedTime);
	}-*/;
	
	private native void resetRecording() /*-{
		if (($wnd.video != null) && ($wnd.video.frameId != null))
			$wnd.cancelAnimationFrame($wnd.video.frameId);
		//arxikopoihsh dedomenwn pou tha xreiastei h javascript
		$wnd.video = new Object();
		$wnd.video.fps = 30;
		$wnd.video.frames = new Array();
		$wnd.video.startTime = Date.now();
		$wnd.video.elapsedTime = 0;
		$wnd.video.frameId = null;
		$wnd.video.isRecording = false;
		var that = this;
console.log('this: ' + this);
		var canvas = this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::canvas;
		var video = this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::video;
		$wnd.video.recording = function (time) {
			//zwgrafizei to kathe kare san fwtografeia s' ena canvas gia na boresei na parei fwtografeies apo to video pou trexei 
			canvas.getContext('2d').drawImage(video, 0, 0, video.videoWidth, video.videoHeight);
			//apothikeush tou kathe kare (frame) s' ena pinaka (me poiothta eikonas 1.0)
			$wnd.video.frames.push(canvas.toDataURL('image/webp', 1.0));
			//metraei ton xrono pou exei perasei
			$wnd.video.elapsedTime = Math.floor((Date.now() - $wnd.video.startTime) / 1000);
console.log('$wnd.video.elapsedTime = ' + $wnd.video.elapsedTime);
console.log('that: ' + that);
			//klish ths updateElapsedTime gia na ananewnei to xronometro
			that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::updateElapsedTime(Ljava/lang/Integer;)($wnd.video.elapsedTime);
			//an grafei akoma video, zhtaei na klithei xana (asugxrona) se 1000 / fps millisecons h requestAnimationFrame h opoia 
			//tha kalesei (asugxrona) tin video.recording otan einai na zwgrafistei to epomeno kare
			if ($wnd.video.isRecording) {
				setTimeout(function() {
					$wnd.video.frameId = requestAnimationFrame($wnd.video.recording);
				}, 1000 / $wnd.video.fps);
			}
//			$wnd.video.frameId = requestAnimationFrame($wnd.video.recording); //anadromh ouras (apla mazeuei to stack kai oti ektelei, to ektelei stin arxh)
		};
	}-*/;
	
	private native int countFrames() /*-{
		return $wnd.video.frames.length;
	}-*/;
	
	private native void submit(final String title, final boolean publik) /*-{
		//Ajax Request epeidh to blob (to video) dhmiourgeitai dunamika se javascript (dedomena pou 
		//paragei o idios o browser monos tou) kai den eisagetai apo ton xrhsth
		var request = new XMLHttpRequest();
		//tha kanei post sto mediaServlet, asugxrona (true)
		request.open('post', '../mediaServlet', true);
		var that = this;
		//otan ginei opoiadhpote kinhsh sxetika me to request, tha kaleitai asugxrona h onreadystatechange
		request.onreadystatechange = function () {
			//an exei oloklhrwthei to request
			if (request.readyState == XMLHttpRequest.DONE) {
				if (request.status == 200) //an exei teleiwsei epituxws
					that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::requestSuccess()();
				else //kaleitai h sunarthsh gia sfalmatos
					that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::requestError(Ljava/lang/String;)(request.statusText);
			}
		};
		var latitude = this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::latitude;
		var longitude = this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewVideo::longitude;
		//gemisma pediwn gia apostolh sto servlet
		//Dedomena pou tha pane san multipart/form-data
		var formData = new FormData();
		formData.append('title', title);
		formData.append('public', publik);
		formData.append('latitude', latitude);
		formData.append('longitude', longitude);
		var blob = $wnd.whammy.fromImageArray($wnd.video.frames, $wnd.video.frames.length / $wnd.video.elapsedTime);
		formData.append('video', blob);
		formData.append('duration', $wnd.video.elapsedTime);
		request.send(formData);
	}-*/;
	
	private void requestError(final String error) {
		Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorUploadingVideo(error));
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(GWT.getHostPageBaseURL(), URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
	}
	
	private void requestSuccess() {
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(GWT.getHostPageBaseURL(), URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())));
	}
}
