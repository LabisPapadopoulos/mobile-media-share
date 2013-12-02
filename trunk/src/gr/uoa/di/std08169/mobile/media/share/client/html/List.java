package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

//AsyncDataProvider<Media>: fernei dedomena (Media)  
public class List extends AsyncDataProvider<Media> implements ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler, RequestCallback, 
		SelectionChangeEvent.Handler, SelectionHandler<SuggestOracle.Suggestion>, ValueChangeHandler<Date> {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS =
			//kanei automath ulopoihsh to GWT tou interface
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
	private static final DateTimeFormat DATE_TIME_FORMAT =
			DateTimeFormat.getFormat(MOBILE_MEDIA_SHARE_CONSTANTS.dateTimeFormat());	
	private static final NumberFormat SIZE_BYTES_FORMAT = 
			NumberFormat.getFormat(MOBILE_MEDIA_SHARE_CONSTANTS.sizeBytesFormat());
	private static final NumberFormat SIZE_KILOBYTES_FORMAT = 
			NumberFormat.getFormat(MOBILE_MEDIA_SHARE_CONSTANTS.sizeKilobytesFormat());
	private static final NumberFormat SIZE_MEGABYTES_FORMAT = 
			NumberFormat.getFormat(MOBILE_MEDIA_SHARE_CONSTANTS.sizeMegabytesFormat());
	private static final int MIN_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int PAGE_SIZE_STEP = 10;
	private static final int TOP = 25;
	private static final int TOP_STEP = 5;
	private static final int LEFT_OFFSET = 100;
	private static final BigDecimal DEGREES_BASE = new BigDecimal(60);
	private static final TextColumn<Media> TITLE = new TextColumn<Media>() { // TODO
		@Override
		public String getValue(final Media media) {
			return media.getTitle();
		}
	};
	private static final TextColumn<Media> TYPE = new TextColumn<Media>() { // TODO
		@Override
		public String getValue(final Media media) {
			return (MediaType.getMediaType(media.getType()) == null) ? "" : 
				MEDIA_TYPE_CONSTANTS.getString(MediaType.getMediaType(media.getType()).name());
		}
	};
	private static final TextColumn<Media> SIZE = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			//megathos arxeiou (B, KB, MB)
			if (media.getSize() < 1024l)
				return SIZE_BYTES_FORMAT.format(media.getSize());
			else if (media.getSize() < 1024l * 1024l)
				return SIZE_KILOBYTES_FORMAT.format(media.getSize() / 1024.0f);
			else
				return SIZE_MEGABYTES_FORMAT.format(media.getSize() / 1024.0f / 1024.0f);
		}
	};
	private static final TextColumn<Media> DURATION = new TextColumn<Media>() { // TODO
		@Override
		public String getValue(final Media media) {
			return Integer.toString(media.getDuration());
		}
	};
	private static final TextColumn<Media> USER = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			return MOBILE_MEDIA_SHARE_MESSAGES.userFormat(
					(media.getUser().getName() == null) ? MOBILE_MEDIA_SHARE_CONSTANTS._anonymous_() :
						media.getUser().getName(), media.getUser().getEmail().substring(0,
								media.getUser().getEmail().indexOf('@')));
		}
	};
	private static final TextColumn<Media> CREATED = new TextColumn<Media>() { //Date created
		@Override
		public String getValue(final Media media) {
			return DATE_TIME_FORMAT.format(media.getCreated());
		}
	};
	private static final TextColumn<Media> EDITED = new TextColumn<Media>() { //Date edited
		@Override
		public String getValue(final Media media) {
			return DATE_TIME_FORMAT.format(media.getEdited());
		}
	};
	private static final TextColumn<Media> LATITUDE = new TextColumn<Media>() { //BigDecimal latitude
		@Override
		public String getValue(final Media media) {
			// 1 moira = 60 prwta lepta
			// 1 prwto lepto = 60 deutera
			// to divideAndRemainder kanei tin diairesh kai epistefei:
			// temp1[0]: phliko
			// temp1[1]: upoloipo
			final BigDecimal[] temp1 = media.getLatitude().multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
			final int seconds = temp1[1].intValue();
			final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
			final int minutes = temp2[1].intValue();
			final int degrees = temp2[0].intValue();
			//an einai arnhtiko-> einai notia, alliws voreia
			return (media.getLatitude().compareTo(BigDecimal.ZERO) < 0) ?
					MOBILE_MEDIA_SHARE_MESSAGES.latitudeFormatSouth(degrees, minutes, seconds) :
					MOBILE_MEDIA_SHARE_MESSAGES.latitudeFormatNorth(degrees, minutes, seconds);
		}
	};
	private static final TextColumn<Media> LONGITUDE = new TextColumn<Media>() { //BigDecimal longitude
		@Override
		public String getValue(final Media media) {
			
			final BigDecimal[] temp1 = media.getLongitude().multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
			final int seconds = temp1[1].intValue();
			final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
			final int minutes = temp2[1].intValue();
			final int degrees = temp2[0].intValue();
			//an einai arnhtiko-> einai notia, alliws voreia
			return (media.getLongitude().compareTo(BigDecimal.ZERO) < 0) ?
					MOBILE_MEDIA_SHARE_MESSAGES.longitudeFormatWest(degrees, minutes, seconds) :
					MOBILE_MEDIA_SHARE_MESSAGES.longitudeFormatEast(degrees, minutes, seconds);
		}
	};
	private static final TextColumn<Media> PUBLIC = new TextColumn<Media>() { //boolean publik //TODO
		@Override
		public String getValue(final Media media) {
			return media.isPublic() ? MOBILE_MEDIA_SHARE_CONSTANTS.publik() : MOBILE_MEDIA_SHARE_CONSTANTS._private();
		}
	};

	//static block gia tis sthles gia prosthkh idiothtwn
	static {
		TITLE.setDataStoreName("title");
		TITLE.setSortable(true);
		TITLE.setDefaultSortAscending(true); //Defalt ASC
		TYPE.setDataStoreName("type");
		TYPE.setSortable(true);
		TYPE.setDefaultSortAscending(true);
		SIZE.setDataStoreName("size");
		SIZE.setSortable(true);
		SIZE.setDefaultSortAscending(true);
		DURATION.setDataStoreName("duration");
		DURATION.setSortable(true);
		DURATION.setDefaultSortAscending(true);
		USER.setDataStoreName("\"user\"");
		USER.setSortable(true);
		USER.setDefaultSortAscending(true);
		CREATED.setDataStoreName("created");
		CREATED.setSortable(true);
		CREATED.setDefaultSortAscending(true);
		EDITED.setDataStoreName("edited");
		EDITED.setSortable(true);
		EDITED.setDefaultSortAscending(true);
		LATITUDE.setDataStoreName("latitude");
		LATITUDE.setSortable(true);
		LATITUDE.setDefaultSortAscending(true);
		LONGITUDE.setDataStoreName("longitude");
		LONGITUDE.setSortable(true);
		LONGITUDE.setDefaultSortAscending(true);
		PUBLIC.setDataStoreName("public");
		PUBLIC.setSortable(true);
		PUBLIC.setDefaultSortAscending(true);
	}

	//Search
	private final TextBox title;
	private final ListBox type; //dropdown
	private final SuggestBox user;
	private final DateBox createdFrom;
	private final DateBox createdTo;
	private final DateBox editedFrom;
	private final DateBox editedTo;
	private final ListBox publik;
	private final ListBox pageSize;
	private final Button download;
	private final Button edit;
	private final Button delete;
	private final SimplePager pager; //Gia pagination
	private final SingleSelectionModel<Media> selectionModel; //gia highlight kai epilogh grammhs
	private final CellTable<Media> mediaTable; //Pinakas gia emfanish twn Media
	private User selectedUser;
	private Request userRequest;
	private String currentUser;
	
	public List() {
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
		
		pageSize = new ListBox();
		for(int p = MIN_PAGE_SIZE; p <= MAX_PAGE_SIZE; p += PAGE_SIZE_STEP)
			pageSize.addItem(Integer.toString(p), Integer.toString(p));
		pageSize.addChangeHandler(this);
		pageSize.getElement().addClassName("listInputCol2");
		pageSize.getElement().setAttribute("style",
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
		
		//i = 4
		j = 1;
		pager = new SimplePager();
		pager.setPageSize(MIN_PAGE_SIZE);
		pager.getElement().addClassName("pager");
		pager.getElement().setAttribute("style",
				//220px
				"top: " + ((TOP * i) + (TOP * i) + (i * TOP_STEP)) + "px;");
		
		selectionModel = new SingleSelectionModel<Media>();
		selectionModel.addSelectionChangeHandler(this);
		mediaTable = new CellTable<Media>();
		mediaTable.getElement().setAttribute("style", "padding-top: " + ((TOP + TOP) * i) + "px;"); //200px; TODO
		
		mediaTable.setSelectionModel(selectionModel);
		//gia na allazei selida o xrhsths kai me ta velakia (dexia-aristera)
		mediaTable.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.CHANGE_PAGE);
		//gia na allazei grammh o xrhsths kai me ta velakia (panw-katw)
		mediaTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
		mediaTable.setPageSize(MIN_PAGE_SIZE);
		//gia sort tis sthles kai kanei update ta dedomena
		mediaTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(mediaTable));
		mediaTable.addColumn(TITLE, MOBILE_MEDIA_SHARE_CONSTANTS.title()); //prosthikh sthlhs TITLE ston pinaka
		mediaTable.addColumn(TYPE, MOBILE_MEDIA_SHARE_CONSTANTS.type());
		mediaTable.addColumn(SIZE, MOBILE_MEDIA_SHARE_CONSTANTS.size());
		mediaTable.addColumn(DURATION, MOBILE_MEDIA_SHARE_CONSTANTS.duration());
		mediaTable.addColumn(USER, MOBILE_MEDIA_SHARE_CONSTANTS.user());
		mediaTable.addColumn(CREATED, MOBILE_MEDIA_SHARE_CONSTANTS.created()); //createdFrom/To
		mediaTable.addColumn(EDITED, MOBILE_MEDIA_SHARE_CONSTANTS.edited()); //editedFrom/To
		mediaTable.addColumn(LATITUDE, MOBILE_MEDIA_SHARE_CONSTANTS.latitude());
		mediaTable.addColumn(LONGITUDE, MOBILE_MEDIA_SHARE_CONSTANTS.longitude());
		mediaTable.addColumn(PUBLIC, MOBILE_MEDIA_SHARE_CONSTANTS.publik());
		mediaTable.getColumnSortList().setLimit(1); //1 sthlh thn fora tha einai taxinomhmenh
		//by default taxinomhse kata titlo (leitourgei san stoiva - taxinomei panda tin teleutaia sthlh)
		mediaTable.getColumnSortList().push(TITLE);
		//o pager selidopoiei tin lista
		pager.setDisplay(mediaTable);
		//O AsyncDataProvider tha vazei dedomena stin lista (table)
		addDataDisplay(mediaTable);
		selectedUser = null;
	}

	//Gia allagh ston typo h sto megethos selidas apo ton xrhsth
	@Override
	public void onChange(final ChangeEvent _) {
		onRangeChanged(null);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) { // clicking on download, edit or delete
		if (clickEvent.getSource() == download)
			//Anoigei neo tab pou tha trexei tin doGet gia na katevei to arxeio
			Window.open(MOBILE_MEDIA_SHARE_URLS.download(URL.encodeQueryString(selectionModel.getSelectedObject().getId())), "_blank", "");
		else if (clickEvent.getSource() == edit)
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.edit(URL.encodeQueryString(selectionModel.getSelectedObject().getId())));
		else if ((clickEvent.getSource() == delete) &&
				Window.confirm(MOBILE_MEDIA_SHARE_CONSTANTS.areYouSureYouWantToDeleteThisMedia())) {
			
			//Diagrafh tou arxeiou apo tin vash
			MEDIA_SERVICE.deleteMedia(selectionModel.getSelectedObject().getId(),
					new AsyncCallback<Void>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorDeletingMedia(throwable.getMessage()));
				}
				
				@Override
				public void onSuccess(final Void _) {
					//delete file apo to file systhma
					try {
						//encode url se periptwsh pou exei periergous xarakthres
						new RequestBuilder(RequestBuilder.DELETE, "./mediaServlet?id=" + URL.encodeQueryString(selectionModel.getSelectedObject().getId())).
								sendRequest(null, List.this);
					} catch (final RequestException e) {
						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorDeletingMedia(e.getMessage()));
					}
					//katharizei to highlight tou pinaka
					selectionModel.clear();
					//apenergopoiountai ta koubia (download, edit, delete)
					onSelectionChange(null);
					//fernei nea dedomena ston pinaka
					onRangeChanged(null);
				}
			});
		}
	}
	
	//Apo interface RequestCallback
	@Override
	public void onError(final Request request, final Throwable throwable) {
		if(request == userRequest) {
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.list(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		}
	}

	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) { // typing into title or user
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
		onRangeChanged(null);
	}
	
	@Override
	public void onModuleLoad() { // set up HTML
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
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.list(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		}
	}
	
	//AsyncDataProvider<Media> fernei dedomena.
	//Kaleitai otan xreiazetai na erthoun nea dedomena (p.x SortHandler, ChangeHandler)
	@Override
	protected void onRangeChanged(final HasData<Media> _) { // update list rows
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
		pager.setPageSize(Integer.valueOf(pageSize.getValue(pageSize.getSelectedIndex())));
		final int start = pager.getPageStart();
		final int length = pager.getPageSize();
		//Sortarisma ws pros to 1o stoixeio tis listas (an uparxei) opws legetai mesa sthn vash (getDataStoreName)
		final String orderField = (mediaTable.getColumnSortList().size() == 0) ? null : 
			mediaTable.getColumnSortList().get(0).getColumn().getDataStoreName();
		final boolean ascending = (mediaTable.getColumnSortList().size() == 0) ? false : 
			mediaTable.getColumnSortList().get(0).isAscending();
		MEDIA_SERVICE.getMedia(currentUser, title, type, (selectedUser == null) ? null : selectedUser.getEmail(),
				createdFrom, createdTo, editedFrom, editedTo, publik, start, length, orderField, ascending, 
				new AsyncCallback<MediaResult>() {
			@Override
			public void onFailure(final Throwable throwable) {//se front-end ston browser
				//Afou egine Failure, bainei adeia Lista apo Media
				updateRowData(0, Collections.<Media>emptyList());
				updateRowCount(0, false);
				selectionModel.clear();
				onSelection(null);
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingMedia(throwable.getMessage()));
			}

			@Override
			public void onSuccess(final MediaResult result) {
				//deixnei ta kainouria dedomena pou efere h onRangeChanged
				updateRowData(start, result.getMedia()); //h selida pou efere
				//ananewnei sumfwna me tis sunolikes grammes pou sigoura gnwrizei ton arithmo (true)
				updateRowCount(result.getTotal(), true);
				selectionModel.clear(); //katharizei tin highlight grammh tou xrhsth
				//kanei disable ta download, edit kai delete afou den einai kamia grammh epilegmenh
				onSelectionChange(null);
			}
		});
	}
	
	//Apo interface RequestCallback.
	//H onResponseReceived einai h onSuccess tou RequestCallback
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		if (request == userRequest) {
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
			final InlineLabel pageSizeLabel = new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.pageSize());
			pageSizeLabel.getElement().addClassName("listLabelCol2");
			pageSizeLabel.getElement().setAttribute("style", 
					"top: " + (TOP + TOP + TOP * i) + "px;");
			flowPanel.add(pageSizeLabel);
			flowPanel.add(pageSize);
			flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
			flowPanel.add(download);
			flowPanel.add(edit);
			flowPanel.add(delete);
			flowPanel.add(pager);
			flowPanel.add(mediaTable);
			RootPanel.get().add(flowPanel);
			onRangeChanged(null);
		}
	}
	
	//Otan dialegei o xrhsths sugkekrimenh protash
	@Override
	public void onSelection(final SelectionEvent<SuggestOracle.Suggestion> selectionEvent) { // selecting a user
		selectedUser = (selectionEvent.getSelectedItem() instanceof UserSuggestion) ? 
				((UserSuggestion) selectionEvent.getSelectedItem()).getUser() : null;
		onRangeChanged(null); //fernei dedomena gi' auton (epilegmeno) ton xrhsth
	}
	
	@Override
	public void onSelectionChange(final SelectionChangeEvent _) { // selecting a row		
		//analoga me to epilegmeno pou edwse o xrhsths (se mia grammh)
		final Media media = selectionModel.getSelectedObject();
		//enable ta download, edit kai delete
		download.setEnabled(media != null);
		//edit kai delete kapoio arxeio an anhkei ston trexonta xrhsth
		edit.setEnabled((media != null) && media.getUser().getEmail().equals(currentUser));
		delete.setEnabled((media != null) && media.getUser().getEmail().equals(currentUser));
	}

	@Override
	public void onValueChange(final ValueChangeEvent<Date> _) { // changing created from, created to, edited from or edited to values
		onRangeChanged(null);
	}
}
