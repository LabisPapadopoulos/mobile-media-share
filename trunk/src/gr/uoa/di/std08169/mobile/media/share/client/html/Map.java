package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MediaTypeConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserOracle;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserSuggestion;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.GoogleMap.CenterChangedHandler;
import com.google.maps.gwt.client.GoogleMap.ZoomChangedHandler;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.MarkerImage;
import com.google.maps.gwt.client.MarkerOptions;
import com.google.maps.gwt.client.MouseEvent;

public class Map extends Composite implements CenterChangedHandler, ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler,
		Marker.ClickHandler, SelectionHandler<Suggestion>, ValueChangeHandler<Date>, Runnable,
		ZoomChangedHandler {
	//Epistrefei widget pou mesa tou tha exei ena map
	protected static interface MapUiBinder extends UiBinder<Widget, Map> {}
	
	public static final String GOOGLE_MAPS_API = "maps";
	public static final String GOOGLE_MAPS_VERSION = "3";
	public static final double GOOGLE_MAPS_ZOOM = 8.0;
	public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
	public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
	
	//Diavazei to Ui xml kai sto dinei gia xrhsh
	private static final MapUiBinder MAP_UI_BINDER = 
			GWT.create(MapUiBinder.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MediaTypeConstants MEDIA_TYPE_CONSTANTS =
			//kanei automath ulopoihsh to GWT tou interface
			GWT.create(MediaTypeConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	//Media Service se front-end
	private static final MediaServiceAsync MEDIA_SERVICE =
			GWT.create(MediaService.class);
	private static final DateTimeFormat DATE_FORMAT =
			DateTimeFormat.getFormat(MOBILE_MEDIA_SHARE_CONSTANTS.dateFormat());

	@UiField
	protected TextBox title;
	@UiField
	protected ListBox type; //dropdown
	@UiField(provided = true)
	protected SuggestBox user;
	@UiField
	protected DateBox createdFrom;
	@UiField
	protected DateBox createdTo;
	@UiField
	protected DateBox editedFrom;
	@UiField
	protected DateBox editedTo;
	@UiField
	protected ListBox publik;
	@UiField
	protected Button download;
	@UiField
	protected Button edit;
	@UiField
	protected Button delete;
	//To div pou tha bei o xarths
	@UiField
	protected DivElement mapContainer;
	
	private final java.util.Map<Marker, Media> markers;
	private final java.util.Map<MediaType, MarkerImage> markerImages;
	private final java.util.Map<MediaType, MarkerImage> selectedMarkerImages;
	private User selectedUser;
	private GoogleMap googleMap;
	private Marker selectedMarker;
	
	public Map() {
		user = new SuggestBox(new UserOracle());
		//dhmiourgeia widget gia tin selida map
		initWidget(MAP_UI_BINDER.createAndBindUi(this));
		title.addKeyUpHandler(this);
		user.addKeyUpHandler(this);
		user.addSelectionHandler(this); //otan epilexei kati apo ta proteinomena		
		//(To ti fainetai, timh tou ti fainetai)
		type.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		//Prosthikh twn dunatwn tupwn arxeiwn
		for (MediaType mediaType : MediaType.values())
			type.addItem(MEDIA_TYPE_CONSTANTS.getString(mediaType.name()), mediaType.name());
		type.addChangeHandler(this); //otan tou allaxei timh
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.publik(), Boolean.TRUE.toString());
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS._private(), Boolean.FALSE.toString());
		publik.addChangeHandler(this);
		final DateBox.Format dateBoxFormat = new DateBox.DefaultFormat(DATE_FORMAT);
		createdFrom.setFormat(dateBoxFormat);
		//epistrefei null gia times pou den katalavainei
		createdFrom.setFireNullValues(true);
		createdFrom.addValueChangeHandler(this); //otan tha allaxei timh
		//me kathe allagh tou textBox, enhmerwnetai o pinakas
		createdFrom.getTextBox().addKeyUpHandler(this);
		createdTo.setFormat(dateBoxFormat);
		createdTo.setFireNullValues(true);
		createdTo.addValueChangeHandler(this); //otan tha allaxei timh
		createdTo.getTextBox().addKeyUpHandler(this);
		editedFrom.setFormat(dateBoxFormat);
		editedFrom.setFireNullValues(true);
		editedFrom.addValueChangeHandler(this); //otan tha allaxei timh
		editedFrom.getTextBox().addKeyUpHandler(this);
		editedTo.setFormat(dateBoxFormat);
		editedTo.setFireNullValues(true);
		editedTo.addValueChangeHandler(this); //otan tha allaxei timh
		editedTo.getTextBox().addKeyUpHandler(this);
		download.setEnabled(false);
		download.addClickHandler(this);
		edit.setEnabled(false);
		edit.addClickHandler(this);
		delete.setEnabled(false);
		delete.addClickHandler(this);
		markers = new HashMap<Marker, Media>();
		markerImages = new HashMap<MediaType, MarkerImage>();
		selectedMarkerImages = new HashMap<MediaType, MarkerImage>();
	}
	
	//Handler gia zoomarisma kai allagh kentrou xarth
	@Override
	public void handle() {
		updateMap();
	}
	
	//Handler gia click se marker ston xarth
	@Override
	public void handle(final MouseEvent event) {
		// xedialexe to selected marker
		if (selectedMarker != null) {
			selectedMarker.setIcon(markerImages.get(MediaType.getMediaType(markers.get(selectedMarker).getType())));
		}
		final String currentUser = InputElement.as(Document.get().getElementById("email")).getValue();
		//Se epilegmeno antikeimeno bainei h pio megalh tou eikona 
		for (java.util.Map.Entry<Marker, Media> marker : markers.entrySet()) {
			if (marker.getKey().getPosition().equals(event.getLatLng())) { // autos o marker dialexthke
				marker.getKey().setIcon(selectedMarkerImages.get(MediaType.getMediaType(marker.getValue().getType())));
				selectedMarker = marker.getKey();
				download.setEnabled(true);
				if (marker.getValue().getUser().getEmail().equals(currentUser)) {
					edit.setEnabled(true);
					delete.setEnabled(true);
				}
			}
		}
	}
	
	//Gia allagh ston typo apo ton xrhsth
	@Override
	public void onChange(final ChangeEvent _) {
		updateMap();
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) { // clicking on download, edit or delete
		if (clickEvent.getSource() == download)
			//Anoigei neo tab pou tha trexei tin doGet gia na katevei to arxeio
			Window.open(MOBILE_MEDIA_SHARE_URLS.download(URL.encodeQueryString(markers.get(selectedMarker).getId())), "_blank", "");
		else if (clickEvent.getSource() == edit)
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.editMedia(URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()), 
					//stelnei ws parametro to id tou media pou dialexe o xrhsths
					URL.encodeQueryString(markers.get(selectedMarker).getId())));
		else if ((clickEvent.getSource() == delete) &&
				Window.confirm(MOBILE_MEDIA_SHARE_CONSTANTS.areYouSureYouWantToDeleteThisMedia())) {			
			//Diagrafh tou arxeiou apo tin vash
			MEDIA_SERVICE.deleteMedia(markers.get(selectedMarker).getId(),
					new AsyncCallback<Void>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorDeletingMedia(throwable.getMessage()));
				}
				
				@Override
				public void onSuccess(final Void _) {
					updateMap();
				}
			});
		}
	}
	
	//Otan grafei sto pedio titlo 'h sto xrhsth
	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) {
		//an ta dedomena tou text box den tairiazoun me ton epilegmeno xrhsth,
		//o epilegmenos xrhsths ginetai null
		//new UserSuggestion(selectedUser).getReplacementString(): to proteinomeno gia ton selectedUser, 
		//tha prepei na einai grammeno an einai epilegmenos o selectedUser
		//user.getValue(): to grammeno sto suggest box
		if ((keyUpEvent.getSource() == user) && //paththike plhktro sto suggest box
				(selectedUser != null) && //uphrxe dialegmenos xrhsths
				//h timh tou suggest box de symfwnei me to dialegmeno xrhsth
				(!new UserSuggestion(selectedUser).getReplacementString().equals(user.getValue())))
			selectedUser = null; // dialexe ton kanena
		updateMap();
	}
	
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
		//Ajax loader: fortwnei pragmata mesw ajax
		//Ruthmiseis gia to google maps
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		AjaxLoader.loadApi(GOOGLE_MAPS_API, GOOGLE_MAPS_VERSION, this, options);
	}
	
	//Otan dialegei o xrhsths sugkekrimenh protash (apo tin anazhthsh tou user)
	@Override
	public void onSelection(final SelectionEvent<SuggestOracle.Suggestion> selectionEvent) { // selecting a user
		selectedUser = (selectionEvent.getSelectedItem() instanceof UserSuggestion) ? 
				((UserSuggestion) selectionEvent.getSelectedItem()).getUser() : null;
		updateMap();
	}

	//Otan allazoun ta created from, created to, edited from 'h edited to
	@Override
	public void onValueChange(final ValueChangeEvent<Date> _) {
		updateMap();
	}

	//Molis fortwthei o xarths kaleitai h run
	@Override
	public void run() {
		final MapOptions options = MapOptions.create(); //Dhmiourgeia antikeimenou me Factory (xwris constructor)
		//Default na fenetai xarths apo dorhforo (Hybrid)
		options.setMapTypeId(MapTypeId.HYBRID);
		options.setZoom(GOOGLE_MAPS_ZOOM);
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		googleMap = GoogleMap.create(mapContainer, options);
		//Listener gia otan allaxtei to kentro tou xarth
		googleMap.addCenterChangedListener(this);
		//Listener gia otan zoomaretai o xarths
		googleMap.addZoomChangedListener(this);
		//Statikh javascript klash pou elenxei an o browser upostirizei geografiko prosdiorismo theshs (san Window.alert)
		Geolocation.getIfSupported().getCurrentPosition(new Callback<Position, PositionError>() {
			@Override
			public void onFailure(final PositionError error) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingYourLocation(error.getMessage()));
				googleMap.setCenter(LatLng.create(GOOGLE_MAPS_LATITUDE, GOOGLE_MAPS_LONGITUDE));
				updateMap();
			}

			@Override
			public void onSuccess(final Position position) {
				//Kentrarei o xarths sto shmeio pou vrethike o xrhsths (Latitude, Longitude)
										//Coordinates: pairnei tis suntetagmenes tis theshs
				googleMap.setCenter(LatLng.create(position.getCoordinates().getLatitude(),
						position.getCoordinates().getLongitude()));
				updateMap();
			}
		});
		for (MediaType type : MediaType.values()) {
			//Ftaxnei to markerImage apo to arxiko size (originalSize) se size
			markerImages.put(type, MarkerImage.create(MOBILE_MEDIA_SHARE_URLS.markerImage(type.name().toLowerCase())));
			selectedMarkerImages.put(type, MarkerImage.create(MOBILE_MEDIA_SHARE_URLS.selectedImage(type.name().toLowerCase())));
		}
	}
	
	private void updateMap() {
		//Apo to input hidden pou exei parei timh apo to session mesw tou jsp, vrisketai o currentUser
		//InputElement.as: casting to Element se InputElement gia na paroume to value 
		final String currentUser = InputElement.as(Document.get().getElementById("email")).getValue();
		final String title = this.title.getValue().trim().isEmpty() ? null : this.title.getValue().trim();
		//MediaType epeidh einai ENUM me ena string epistrefetai ena instance
		final MediaType type = this.type.getValue(this.type.getSelectedIndex()).isEmpty() ? null :
				MediaType.valueOf(this.type.getValue(this.type.getSelectedIndex()));
		final Date createdFrom = this.createdFrom.getValue();
		final Date createdTo = this.createdTo.getValue();
		final Date editedFrom = this.editedFrom.getValue();
		final Date editedTo =  this.editedTo.getValue();
		final Boolean publik = this.publik.getValue(this.publik.getSelectedIndex()).isEmpty() ? null : 
			Boolean.valueOf(this.publik.getValue(this.publik.getSelectedIndex()));
		//Epistrefei ta notio-dutika (suntetagmenes tetragwnou pou kaluptei o xarths)
		final BigDecimal minLatitude = new BigDecimal(googleMap.getBounds().getSouthWest().lat());
		final BigDecimal minLongitude = new BigDecimal(googleMap.getBounds().getSouthWest().lng());
		//epistrefei ta voreio-anatolika (suntetagmenes tetragwnou pou kaluptei o xarths)
		final BigDecimal maxLatitude = new BigDecimal(googleMap.getBounds().getNorthEast().lat());
		final BigDecimal maxLongitude = new BigDecimal(googleMap.getBounds().getNorthEast().lng());
		MEDIA_SERVICE.getMedia(currentUser, title, type, (selectedUser == null) ? null : selectedUser.getEmail(),
				createdFrom, createdTo, editedFrom, editedTo, publik, minLatitude, minLongitude, maxLatitude, maxLongitude, 
				new AsyncCallback<List<Media>>() {
			@Override
			public void onFailure(final Throwable throwable) {//se front-end ston browser
				for (java.util.Map.Entry<Marker, Media> marker : markers.entrySet())
					//svinei ton marker apo ton xarth
					marker.getKey().setMap((GoogleMap) null);
				markers.clear(); //adeiasma listas apo markers
				selectedMarker = null;
				download.setEnabled(false);
				edit.setEnabled(false);
				delete.setEnabled(false);
			}

			@Override
			public void onSuccess(final List<Media> result) {
				for (java.util.Map.Entry<Marker, Media> marker : markers.entrySet())
					//svinei ton marker apo ton xarth
					marker.getKey().setMap((GoogleMap) null);
				markers.clear(); //adeiasma tou map apo markers
				//Ruthmiseis gia ta shmeia ston xarth
				final MarkerOptions options = MarkerOptions.create();
				options.setMap(googleMap);
				options.setClickable(true);
				//Shmeia ston xarth gia kathe media
				for (Media media : result) {
					final Marker marker = Marker.create(options);
					//doubleValue: to gurnaei se double apo bigDecimal
					marker.setPosition(LatLng.create(media.getLatitude().doubleValue(), media.getLongitude().doubleValue()));
					marker.setTitle(media.getTitle());
					//vriskei tin katallhlh eikona gia sugkekrimeno tupo antikeimenou apo to hashMap
					marker.setIcon(markerImages.get(MediaType.getMediaType(media.getType())));
					marker.addClickListener(Map.this);
					markers.put(marker, media);
				}
				selectedMarker = null;
				download.setEnabled(false);
				edit.setEnabled(false);
				delete.setEnabled(false);
			}
		});
	}
}
