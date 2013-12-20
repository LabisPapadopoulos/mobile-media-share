package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;

import java.math.BigDecimal;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.MarkerImage;
import com.google.maps.gwt.client.MarkerOptions;
import com.google.maps.gwt.client.MouseEvent;

public class NewPhoto implements ClickHandler, EntryPoint, GoogleMap.ClickHandler, KeyUpHandler, /*RequestCallback,*/ Runnable {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	
	private final FormPanel form;
	private final Button capture;
	private final Hidden photo;
	private final TextBox title;
	private final CheckBox publik;
	private final Hidden latitude;
	private final Hidden longitude;
	private final Button ok;
	private final Button reset;
	private VideoElement video;
	private CanvasElement canvas;
	private Marker marker;
		
	public NewPhoto() {
		//gia na min anoixei kainourio parathuro
		form = new FormPanel(new NamedFrame("_self"));
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setAction("./mediaServlet");
		int i = 0;
		capture = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.capturePhoto());
		capture.addClickHandler(this);
		capture.setEnabled(false);
		photo = new Hidden("photo");
		title = new TextBox();
		title.setName("title");
		title.addKeyUpHandler(this);
		title.getElement().addClassName("field");
//		title.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;"); TODO
		publik = new CheckBox();
		publik.setName("public");
		publik.getElement().addClassName("field");
//		publik.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;"); TODO
		//onomata pou tha pane pisw sto servlet
		latitude = new Hidden("latitude");
		longitude = new Hidden("longitude");
		i++;
		int j = 0;
		ok = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.ok());
		ok.addClickHandler(this);
		ok.setEnabled(false);
//		ok.getElement().setAttribute("style", TODO 
//				"top: " + (i * (TOP_STEP * i) + (TOP_STEP * i) + (TOP_STEP * i) + TOP_STEP) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
		reset = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.reset());
		reset.addClickHandler(this);
//		reset.getElement().setAttribute("style", TODO 
//				"top: " + (i * (TOP_STEP * i) + (TOP_STEP * i) + (TOP_STEP * i) + TOP_STEP) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
	}
	
	//Apo ton ClickListener tou GoogleMap (GoogleMap.ClickHandler) 
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
		if (clickEvent.getSource() == capture) {
			//zwgrafizei se sugkekrimenes diastaseis, analoga me tin kamera tou xrhsth
			canvas.setWidth(video.getVideoWidth());
			canvas.setHeight(video.getVideoHeight());
			//Sto hidden photo orizetai to photo data url (by default .png)
			photo.setValue(capture());
			//apokrupsh video
			video.setAttribute("style", "display: none;");
			//emfanish canvas
			canvas.setAttribute("style", "display: block;");
			ok.setEnabled(!title.getValue().trim().isEmpty());
		} else if (clickEvent.getSource() == ok) {
			form.submit();
		} else if (clickEvent.getSource() == reset) {
			video.setAttribute("style", "display: block;");
			canvas.setAttribute("style", "display: none;");
			//katharizei to canvas
			canvas.setWidth(0);
			canvas.setHeight(0);
			photo.setValue(null);
			title.setValue(null);
			publik.setValue(false);//default private
			ok.setEnabled(false);
		} 
	}

/*	//Apo interface RequestCallback (apo tin sendRequest)
	@Override
	public void onError(final Request request, final Throwable throwable) {
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
				//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
				URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
				//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
				URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.newPhoto(
						URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
	}*/
	
	@Override
	public void onKeyUp(final KeyUpEvent _) {
		ok.setEnabled((!photo.getValue().trim().isEmpty()) && (!title.getValue().trim().isEmpty()));		
	}
	
	@Override
	public void onModuleLoad() {
		//Ajax loader: fortwnei pragmata mesw ajax
		//Ruthmiseis gia to google maps
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		AjaxLoader.loadApi(Map.GOOGLE_MAPS_API, Map.GOOGLE_MAPS_VERSION, this, options);
		
/*		try {
			//RequestBuilder gia na kanoume ena GET request sto servlet login gia na paroume
			//to session mas. RequestCallback (this) einai auto pou tha parei tin apantish asunxrona
			//Meta phgenei stin onResponseReceived.
			new RequestBuilder(RequestBuilder.GET, "./userServlet").sendRequest(null, this);
		} catch (final RequestException _) {
			//otidhpote paei strava, xana gurnaei stin login
			//url pou theloume na mas paei
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					//an petuxei to login paei sto list html
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.list(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		}*/
		
	}

/*	//Apo interface RequestCallback (apo tin sendRequest)
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		//an den einai logged in o xrhsths
		if ((response.getStatusCode() != 200) || (response.getText().isEmpty())) {
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.newPhoto(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
			return;
		}
		
		//Ajax loader: fortwnei pragmata mesw ajax
		//Ruthmiseis gia to google maps
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		AjaxLoader.loadApi(Map.GOOGLE_MAPS_API, Map.GOOGLE_MAPS_VERSION, this, options);
	}*/

	@Override
	public void run() {
		final MapOptions options = MapOptions.create(); //Dhmiourgeia antikeimenou me Factory (xwris constructor)
		//Default na fenetai xarths apo dorhforo (Hybrid)
		options.setMapTypeId(MapTypeId.HYBRID);
		options.setZoom(Map.GOOGLE_MAPS_ZOOM);
		final DivElement mapDiv = Document.get().createDivElement();
		mapDiv.addClassName("map");
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		final GoogleMap googleMap = GoogleMap.create(mapDiv, options);
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
		Document.get().getBody().addClassName("bodyClass");
		//Apo to DOM prosthetei komvo (to header me olous tous upokomvous pou exei mesa)
		Document.get().getBody().appendChild(Header.newHeader());
		final FlowPanel flowPanel = new FlowPanel();
		int i = 0;
		//Video element: oti tha deixnei h camera
		video = Document.get().createVideoElement();
		video.setAttribute("style", "display: block;");
		flowPanel.getElement().appendChild(video);
		//Canvas: Gia na fenetai proswrina h eikona molis pathsei o xrhsths capture
		canvas = Document.get().createCanvasElement();
		canvas.setAttribute("style", "display: none;");
		flowPanel.getElement().appendChild(canvas);
		flowPanel.add(photo);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		flowPanel.add(capture);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		final InlineLabel titleLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.title());
		titleLabel.getElement().addClassName("label");
//		titleLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;"); TODO
		flowPanel.add(titleLabel);
		flowPanel.add(title);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		final InlineLabel publicLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.publik());
		publicLabel.getElement().addClassName("label");
//		publicLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;"); TODO
		flowPanel.add(publicLabel);
		flowPanel.add(publik);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		final InlineLabel latitudeLongitudeLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.latitude() + 
				" / " + MOBILE_MEDIA_SHARE_CONSTANTS.longitude());
		latitudeLongitudeLabel.getElement().addClassName("label");
//		latitudeLongitudeLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;"); TODO
		flowPanel.add(latitudeLongitudeLabel);
		final ParagraphElement paragraphElement = Document.get().createPElement();
		paragraphElement.setInnerHTML("&nbsp;");
		flowPanel.getElement().appendChild(paragraphElement);
		flowPanel.getElement().appendChild(mapDiv);
		flowPanel.add(latitude);
		flowPanel.add(longitude);
		flowPanel.add(new Hidden("locale", LocaleInfo.getCurrentLocale().getLocaleName()));
		flowPanel.add(ok);
		flowPanel.add(reset);
		form.add(flowPanel); //giati h forma pairnei ena pragma
		RootPanel.get().add(form);
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
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaError(Ljava/lang/String;)(null);
			return;
		}
		if ($wnd.URL == null)
			$wnd.URL = $wnd.webkitURL;
		if ($wnd.URL == null) {
			this.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaError(Ljava/lang/String;)(null);
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
				that.@gr.uoa.di.std08169.mobile.media.share.client.html.NewPhoto::userMediaError(Ljava/lang/String;)(error.name);
			});
	}-*/;
	
	private void userMediaError(final String error) {
		if (error == null)
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCapturingPhoto(MOBILE_MEDIA_SHARE_CONSTANTS.notSupported()));
		else 
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorCapturingPhoto(MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
	}
	
	private void userMediaSuccess(final String url) {
		video.setAttribute("src", url);
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
