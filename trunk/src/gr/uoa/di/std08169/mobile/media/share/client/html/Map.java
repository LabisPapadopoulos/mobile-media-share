package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ParagraphElement;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MediaTypeConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserOracle;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserSuggestion;
import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.User;

public class Map implements ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler,
		RequestCallback, SelectionHandler<Suggestion>, ValueChangeHandler<Date>, Runnable {
	public static final String GOOGLE_MAPS_API = "maps";
	public static final String GOOGLE_MAPS_VERSION = "3";
	public static final double GOOGLE_MAPS_ZOOM = 8.0;
	public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
	public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
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
	private static final int TOP = 25;
	private static final int TOP_STEP = 5;
	private static final int LEFT_OFFSET = 100;
	
	private final TextBox title;
	private final ListBox type; //dropdown
	private final SuggestBox user;
	private final DateBox createdFrom;
	private final DateBox createdTo;
	private final DateBox editedFrom;
	private final DateBox editedTo;
	private final ListBox publik;
	private final Button download;
	private final Button edit;
	private final Button delete;
	private User selectedUser;
	private Request userRequest;
	private String currentUser;
	private GoogleMap googleMap;
	
	public Map() {
		int i = 1;
		int j = 0;
		title = new TextBox();
		title.addKeyUpHandler(this);
		title.getElement().addClassName("listInputCol1");
		title.getElement().setAttribute("style",
				"top: " + (TOP * i) + "px;"); //25px
		
		user = new SuggestBox(new UserOracle());
		user.addKeyUpHandler(this);
		user.addSelectionHandler(this); //otan epilexei kati apo ta proteinomena
		user.getElement().addClassName("listInputCol2");
		user.getElement().setAttribute("style",
				"top: " + (TOP * i) + "px;"); //25px
		
		type = new ListBox();
		//(To ti fainetai, timh tou ti fainetai)
		type.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		//Prosthikh twn dunatwn tupwn arxeiwn
		for (MediaType mediaType : MediaType.values())
			type.addItem(MEDIA_TYPE_CONSTANTS.getString(mediaType.name()), mediaType.name());
		type.addChangeHandler(this); //otan tou allaxei timh
		type.getElement().addClassName("listInputType");
		type.getElement().setAttribute("style",
				"top: " + (TOP_STEP + TOP * i++) + "px;"); //25px
	
		// i = 2
		j = 0;
		final DateBox.Format dateBoxFormat = new DateBox.DefaultFormat(DATE_FORMAT);
		createdFrom = new DateBox();
		createdFrom.setFormat(dateBoxFormat);
		//epistrefei null gia times pou den katalavainei
		createdFrom.setFireNullValues(true);
		createdFrom.addValueChangeHandler(this); //otan tha allaxei timh
		//me kathe allagh tou textBox, enhmerwnetai o pinakas
		createdFrom.getTextBox().addKeyUpHandler(this);
		createdFrom.getElement().addClassName("listInputCol1");
		createdFrom.getElement().setAttribute("style",
				"top: " + (i * (TOP + TOP_STEP) + TOP_STEP) + "px;"); //65px
		
		createdTo = new DateBox();
		createdTo.setFormat(dateBoxFormat);
		createdTo.setFireNullValues(true);
		createdTo.addValueChangeHandler(this); //otan tha allaxei timh
		createdTo.getTextBox().addKeyUpHandler(this);
		createdTo.getElement().addClassName("listInputCol2");
		createdTo.getElement().setAttribute("style",
				"top: " + (i * (TOP + TOP_STEP) + TOP_STEP) + "px;"); //35px
		
		// i = 2
		j = 0;
		editedFrom = new DateBox();
		editedFrom.setFormat(dateBoxFormat);
		editedFrom.setFireNullValues(true);
		editedFrom.addValueChangeHandler(this); //otan tha allaxei timh
		editedFrom.getTextBox().addKeyUpHandler(this);
		editedFrom.getElement().addClassName("listInputCol1");
		editedFrom.getElement().setAttribute("style",
				"top: " + (i * (TOP + TOP) + TOP_STEP + TOP_STEP) + "px;"); //110px
		
		editedTo = new DateBox();
		editedTo.setFormat(dateBoxFormat);
		editedTo.setFireNullValues(true);
		editedTo.addValueChangeHandler(this); //otan tha allaxei timh
		editedTo.getTextBox().addKeyUpHandler(this);
		editedTo.getElement().addClassName("listInputCol2");
		editedTo.getElement().setAttribute("style",
				"top: " + (i * (TOP + TOP) + TOP_STEP + TOP_STEP) + "px;"); //110px
		
		// i = 2
		j = 0;
		publik = new ListBox();
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.publik(), Boolean.TRUE.toString());
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS._private(), Boolean.FALSE.toString());
		publik.addChangeHandler(this);
		publik.getElement().addClassName("listInputCol1");
		publik.getElement().setAttribute("style",
				"top: " + (i * (TOP + TOP + TOP) - TOP_STEP) + "px;"); //145px
		
		//i = 3
		j = 1;
		i++;
		download = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.download());
		download.setEnabled(false);
		download.addClickHandler(this);
		download.getElement().addClassName("listField");
		download.getElement().addClassName("listButtons");
		download.getElement().setAttribute("style",
				//180px
				"top: " + ((TOP * i) + (TOP * i) + TOP + TOP_STEP) + "px; left: " + (LEFT_OFFSET * j++) + "px;"); //100px
		
		
		edit = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.edit());
		edit.setEnabled(false);
		edit.addClickHandler(this);
		edit.getElement().addClassName("listField");
		edit.getElement().addClassName("listButtons");
		edit.getElement().setAttribute("style",
				//180px
				"top: " + ((TOP * i) + (TOP * i) + TOP + TOP_STEP) + "px; left: " + (LEFT_OFFSET * j++) + "px;"); //200px
		
		delete = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.delete());
		delete.setEnabled(false);
		delete.addClickHandler(this);
		delete.getElement().addClassName("listField");
		delete.getElement().addClassName("listButtons");
		delete.getElement().setAttribute("style",
				//180px
				"top: " + ((TOP * i) + (TOP * i++) + TOP + TOP_STEP) + "px; left: " + (LEFT_OFFSET * j++) + "px;"); //300px
	}
	
	//Gia allagh ston typo apo ton xrhsth
	@Override
	public void onChange(final ChangeEvent _) {
		updateMap();
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) { // clicking on download, edit or delete // TODO
		if (clickEvent.getSource() == download)
			//Anoigei neo tab pou tha trexei tin doGet gia na katevei to arxeio
{/* TODO			Window.open(MOBILE_MEDIA_SHARE_URLS.download(URL.encodeQueryString(selectionModel.getSelectedObject().getId())), "_blank", ""); */}
		else if (clickEvent.getSource() == edit)
{/* TODO			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.edit(URL.encodeQueryString(selectionModel.getSelectedObject().getId()))); */}
		else if ((clickEvent.getSource() == delete) &&
				Window.confirm(MOBILE_MEDIA_SHARE_CONSTANTS.areYouSureYouWantToDeleteThisMedia())) {
// TODO {			
//			//Diagrafh tou arxeiou apo tin vash
//			MEDIA_SERVICE.deleteMedia(selectionModel.getSelectedObject().getId(),
//					new AsyncCallback<Void>() {
//				@Override
//				public void onFailure(final Throwable throwable) {
//					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorDeletingMedia(throwable.getMessage()));
//				}
//				
//				@Override
//				public void onSuccess(final Void _) {
//					//delete file apo to file systhma
//					try {
//						//encode url se periptwsh pou exei periergous xarakthres
//						new RequestBuilder(RequestBuilder.DELETE, "./mediaServlet?id=" + URL.encodeQueryString(selectionModel.getSelectedObject().getId())).
//								sendRequest(null, Map.this);
//					} catch (final RequestException e) {
//						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorDeletingMedia(e.getMessage()));
//					}
//					//katharizei to highlight tou pinaka
//					selectionModel.clear();
//					//apenergopoiountai ta koubia (download, edit, delete)
//					onSelectionChange(null);
//					//fernei nea dedomena ston pinaka
//					onRangeChanged(null);
//				}
//			});
//}TODO
		}
	}
	
	//Se sfalma sto RequestCallback
	@Override
	public void onError(final Request request, final Throwable __) {
		if(request == userRequest) {
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.map(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
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
		try {
			//RequestBuilder gia na kanoume ena GET request sto servlet login gia na paroume
			//to session mas. RequestCallback (this) einai auto pou tha parei tin apantish asunxrona
			//Meta phgenei stin onResponseReceived.
			userRequest = new RequestBuilder(RequestBuilder.GET, "./userServlet").sendRequest(null, this);
		} catch (final RequestException _) {
			//otidhpote paei strava, xana gurnaei stin login
			//url pou theloume na mas paei
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					//an petuxei to login paei sto list html
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.map(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		}
	}
	
	//molis phre epituxws tin apantish
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		if(request == userRequest) {
			//an den einai logged in o xrhsths
			if ((response.getStatusCode() != 200) || (response.getText().isEmpty())) {
				Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
						//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
						URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
						//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
						URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.list(
								URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
				return;
			}
			currentUser = response.getText();
			//Ajax loader: fortwnei pragmata mesw ajax
			//Ruthmiseis gia to google maps
			final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
			options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
			AjaxLoader.loadApi(GOOGLE_MAPS_API, GOOGLE_MAPS_VERSION, this, options);
		}
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
		final DivElement mapDiv = Document.get().createDivElement();
		mapDiv.addClassName("map");
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		googleMap = GoogleMap.create(mapDiv, options);
		//Statith javascript kalsh pou elenxei an o browser upostirizei geografiko prosdiorismo theshs (san Window.alert)
		Geolocation.getIfSupported().getCurrentPosition(new Callback<Position, PositionError>() {
			@Override
			public void onFailure(final PositionError error) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingYourLocation(error.getMessage()));
				googleMap.setCenter(LatLng.create(GOOGLE_MAPS_LATITUDE, GOOGLE_MAPS_LONGITUDE));

			}

			@Override
			public void onSuccess(final Position position) {
				//Kentrarei o xarths sto shmeio pou vrethike o xrhsths (Latitude, Longitude)
										//Coordinates: pairnei tis suntetagmenes tis theshs
				googleMap.setCenter(LatLng.create(position.getCoordinates().getLatitude(),
						position.getCoordinates().getLongitude()));
			}
		});
		
		
		Document.get().getBody().addClassName("bodyClass");
		Document.get().getBody().appendChild(Header.newHeader());
		int i = 1;
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.getElement().addClassName("search-filter");
		final InlineLabel titleLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.title());
		titleLabel.getElement().addClassName("listLabelCol1");
		titleLabel.getElement().setAttribute("style", 
				"top: " + (TOP + TOP_STEP + TOP_STEP * i) + "px;"); //35px
		flowPanel.add(titleLabel);
		flowPanel.add(title);
		final InlineLabel userLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.user());
		userLabel.getElement().addClassName("listLabelCol2");
		userLabel.getElement().setAttribute("style", 
				"top: " + (TOP + TOP_STEP + TOP_STEP * i) + "px;");
		flowPanel.add(userLabel);
		flowPanel.add(user);
		final InlineLabel typeLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.type());
		typeLabel.getElement().addClassName("listLabelType");
		typeLabel.getElement().setAttribute("style", 
				"top: " + (TOP + TOP_STEP + TOP_STEP * i++) + "px;");
		flowPanel.add(typeLabel);
		flowPanel.add(type);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		//i = 2
		final InlineLabel createdFromLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.createdFrom());
		createdFromLabel.getElement().addClassName("listLabelCol1");
		createdFromLabel.getElement().setAttribute("style", 
				"top: " + (TOP + TOP * i) + "px;"); //75px
		flowPanel.add(createdFromLabel);
		flowPanel.add(createdFrom);
		final InlineLabel createdToLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.createdTo());
		createdToLabel.getElement().addClassName("listLabelCol2");
		createdToLabel.getElement().setAttribute("style", 
				"top: " + (TOP + TOP * i++) + "px;"); //75px
		flowPanel.add(createdToLabel);
		flowPanel.add(createdTo);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		//i = 3
		final InlineLabel editedFromLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.editedFrom());
		editedFromLabel.getElement().addClassName("listLabelCol1");
		editedFromLabel.getElement().setAttribute("style", 
				"top: " + ((TOP + TOP * i) + (TOP_STEP * i) + TOP_STEP) + "px;"); //120px
		flowPanel.add(editedFromLabel);
		flowPanel.add(editedFrom);
		final InlineLabel editedToLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.editedTo());
		editedToLabel.getElement().addClassName("listLabelCol2");
		editedToLabel.getElement().setAttribute("style",
				"top: " + ((TOP + TOP * i) + (TOP_STEP * i++) + TOP_STEP) + "px;"); //120px
		flowPanel.add(editedToLabel);
		flowPanel.add(editedTo);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		final InlineLabel publicLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.publik());
		publicLabel.getElement().addClassName("listLabelCol1");
		publicLabel.getElement().setAttribute("style", 
				"top: " + (TOP + TOP + TOP * i) + "px;"); //150px
		flowPanel.add(publicLabel);
		flowPanel.add(publik);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		flowPanel.add(download);
		flowPanel.add(edit);
		flowPanel.add(delete);
		final ParagraphElement paragraphElement = Document.get().createPElement();
		paragraphElement.setAttribute("style", "padding-bottom: " + (TOP_STEP + TOP_STEP + TOP_STEP + TOP + TOP * i) + "px;"); //140px
		paragraphElement.setInnerHTML("&nbsp;");
		flowPanel.getElement().appendChild(paragraphElement);
		flowPanel.getElement().appendChild(mapDiv);
		RootPanel.get().add(flowPanel);
		updateMap();
	}
	
	private void updateMap() {
		if (currentUser == null)
			return;
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
		
/*		MEDIA_SERVICE.getMedia(currentUser, title, type, (selectedUser == null) ? null : selectedUser.getEmail(), TODO
				createdFrom, createdTo, editedFrom, editedTo, publik, start, length, orderField, ascending, 
				new AsyncCallback<MediaResult>() {
			@Override
			public void onFailure(final Throwable throwable) {//se front-end ston browser
				//Afou egine Failure, bainei adeia Lista apo Media
				TODO
				updateRowData(0, Collections.<Media>emptyList());
				updateRowCount(0, false);
				selectionModel.clear();
				onSelection(null);
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingMedia(throwable.getMessage()));
				throw new RuntimeException(throwable);
			}

			@Override
			public void onSuccess(final MediaResult result) {
				TODO
				//deixnei ta kainouria dedomena pou efere h onRangeChanged
				updateRowData(start, result.getMedia()); //h selida pou efere
				//ananewnei sumfwna me tis sunolikes grammes pou sigoura gnwrizei ton arithmo (true)
				updateRowCount(result.getTotal(), true);
				selectionModel.clear(); //katharizei tin highlight grammh tou xrhsth
				//kanei disable ta download, edit kai delete afou den einai kamia grammh epilegmenh
				onSelectionChange(null);
			}
		}); */
	}
}
