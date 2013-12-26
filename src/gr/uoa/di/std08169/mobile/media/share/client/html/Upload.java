package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.ParagraphElement;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.InlineLabel;
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

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;

public class Upload implements ChangeHandler, ClickHandler, EntryPoint, GoogleMap.ClickHandler, KeyUpHandler, Runnable {
	public static final String MARKER_URL = "./images/uploadMarker.svg";
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	private static final int TOP = 5;
	private static final int TOP_STEP = 30;
	private static final int LEFT_OFFSET = 430;
	private static final int LEFT_STEP = 100;
	
	private final FormPanel form;
	private final FileUpload file;
	private final TextBox title;
	private final CheckBox publik;
	private final Hidden latitude;
	private final Hidden longitude;
	private final Button ok;
	private final Button reset;
	private Marker marker;
	
	public Upload() {
		//gia na min anoixei kainourio parathuro
		form = new FormPanel(new NamedFrame("_self"));
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setAction("./mediaServlet");
		form.getElement().setClassName("uploadForm");
		int i = 0;
		file = new FileUpload();
		file.setName("file");
		//enhmerwnei otan allaxei h timh tou
		file.addChangeHandler(this);
		file.getElement().addClassName("field");
		file.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		title = new TextBox();
		title.setName("title");
		title.addKeyUpHandler(this);
		title.getElement().addClassName("field");
		title.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		publik = new CheckBox();
		publik.setName("public");
		publik.getElement().addClassName("field");
		publik.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;");
		//onomata pou tha pane pisw sto servlet
		latitude = new Hidden("latitude");
		longitude = new Hidden("longitude");
		i++;
		int j = 0;
		ok = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.ok());
		ok.addClickHandler(this);
		ok.setEnabled(false);
		ok.getElement().setAttribute("style", 
				"top: " + (i * (TOP_STEP * i) + (TOP_STEP * i) + (TOP_STEP * i) + TOP_STEP + TOP_STEP) + "px; " +
						"left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
		reset = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.reset());
		reset.addClickHandler(this);
		reset.getElement().setAttribute("style", 
				"top: " + (i * (TOP_STEP * i) + (TOP_STEP * i) + (TOP_STEP * i) + TOP_STEP + TOP_STEP) + "px; " +
						"left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
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
		if (clickEvent.getSource() == ok) {
			form.submit();
		} else if (clickEvent.getSource() == reset) {
			//kanei reset tin forma kai ara katharizei to file
			form.reset();
			title.setValue(null);
			publik.setValue(false);//default private
			ok.setEnabled(false);
		} 
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
		final DivElement mapDiv = Document.get().createDivElement();
		mapDiv.addClassName("map");
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		final GoogleMap googleMap = GoogleMap.create(mapDiv, options);
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
		Document.get().getBody().addClassName("bodyClass");
		//Apo to DOM prosthetei komvo (to header me olous tous upokomvous pou exei mesa)
		Document.get().getBody().appendChild(Header.newHeader());
		final ImageElement uploadImage = Document.get().createImageElement();
		uploadImage.setSrc("./images/uploadLogo.png");
		uploadImage.setClassName("uploadImage");
		uploadImage.setAlt("Upload Image");
		Document.get().getBody().appendChild(uploadImage);
		final FlowPanel flowPanel = new FlowPanel();
		int i = 0;
		final InlineLabel fileLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.file());
		fileLabel.getElement().addClassName("label");
		fileLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;");
		flowPanel.add(fileLabel);
		flowPanel.add(file);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		final InlineLabel titleLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.title());
		titleLabel.getElement().addClassName("label");
		titleLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;");
		flowPanel.add(titleLabel);
		flowPanel.add(title);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		final InlineLabel publicLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.publik());
		publicLabel.getElement().addClassName("label");
		publicLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;");
		flowPanel.add(publicLabel);
		flowPanel.add(publik);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		final InlineLabel latitudeLongitudeLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.latitude() + 
				" / " + MOBILE_MEDIA_SHARE_CONSTANTS.longitude());
		latitudeLongitudeLabel.getElement().addClassName("label");
		latitudeLongitudeLabel.getElement().setAttribute("style", "top: " + (TOP + TOP_STEP * (i++)) + "px;");
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
	}
}
