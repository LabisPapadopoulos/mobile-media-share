package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MediaTypeConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserOracle;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserSuggestion;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

//Composite: Einai widget pou einai ftiagmeno me ui xml kai periexei allous widget mesa tou (g:..., gg:..., ...)
public class List extends Composite implements ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler, 
		SelectionChangeEvent.Handler, SelectionHandler<SuggestOracle.Suggestion>, ValueChangeHandler<Date> {
	
	//metatrepei to Ui xml se java antikeimeno
	protected static interface ListUiBinder extends UiBinder<Widget, List> {}

	private class ListDataProvider extends AsyncDataProvider<Media> {
		//AsyncDataProvider<Media> fernei dedomena.
		//Kaleitai otan xreiazetai na erthoun nea dedomena (p.x SortHandler, ChangeHandler)
		@Override
		protected void onRangeChanged(final HasData<Media> _) { // update list rows
			final String title = List.this.title.getValue().trim().isEmpty() ? null : List.this.title.getValue().trim();
			//MediaType epeidh einai ENUM me ena string epistrefetai ena instance
			final MediaType type = List.this.type.getValue(List.this.type.getSelectedIndex()).isEmpty() ? null :
					MediaType.valueOf(List.this.type.getValue(List.this.type.getSelectedIndex()));
			final Date createdFrom = List.this.createdFrom.getValue();
			final Date createdTo = List.this.createdTo.getValue();
			final Date editedFrom = List.this.editedFrom.getValue();
			final Date editedTo =  List.this.editedTo.getValue();
			final Boolean publik = List.this.publik.getValue(List.this.publik.getSelectedIndex()).isEmpty() ? null : 
				Boolean.valueOf(List.this.publik.getValue(List.this.publik.getSelectedIndex()));
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
					onSelectionChange(null);
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
	}

	public static final Column<Media, SafeHtml> TYPE = new Column<Media, SafeHtml>(new SafeHtmlCell()) {
		@Override
		public SafeHtml getValue(final Media media) {
			final SafeHtmlBuilder html = new SafeHtmlBuilder();
			if (MediaType.getMediaType(media.getType()) != null) {
				html.append(SafeHtmlUtils.fromTrustedString("<span class=\"" + MediaType.getMediaType(media.getType()).name() + "\">"));
				html.appendEscaped(MEDIA_TYPE_CONSTANTS.getString(MediaType.getMediaType(media.getType()).name()));
				html.appendHtmlConstant("</span>");
			}
			return html.toSafeHtml();
		}
	};
	public static final TextColumn<Media> SIZE = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			//megethos arxeiou (B, KB, MB)
			if (media.getSize() < 1024l)
				return SIZE_BYTES_FORMAT.format(media.getSize());
			else if (media.getSize() < 1024l * 1024l)
				return SIZE_KILOBYTES_FORMAT.format(media.getSize() / 1024.0f);
			else
				return SIZE_MEGABYTES_FORMAT.format(media.getSize() / 1024.0f / 1024.0f);
		}
	};
	public static final TextColumn<Media> DURATION = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			final MediaType mediaType = MediaType.getMediaType(media.getType());
			return ((mediaType == MediaType.AUDIO) || (mediaType == MediaType.VIDEO)) ?
					formatDuration(media.getDuration()) : "-";
		}
	};
	public static final TextColumn<Media> USER = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			return MOBILE_MEDIA_SHARE_MESSAGES.userFormat(
					(media.getUser().getName() == null) ? MOBILE_MEDIA_SHARE_CONSTANTS._anonymous_() :
						media.getUser().getName(), media.getUser().getEmail().substring(0,
								media.getUser().getEmail().indexOf('@')));
		}
	};
	public static final TextColumn<Media> CREATED = new TextColumn<Media>() { //Date created
		@Override
		public String getValue(final Media media) {
			return DATE_TIME_FORMAT.format(media.getCreated());
		}
	};
	public static final TextColumn<Media> EDITED = new TextColumn<Media>() { //Date edited
		@Override
		public String getValue(final Media media) {
			return DATE_TIME_FORMAT.format(media.getEdited());
		}
	};
	public static final TextColumn<Media> LATITUDE = new TextColumn<Media>() { //BigDecimal latitude
		@Override
		public String getValue(final Media media) {
			return formatLatitude(media.getLatitude());
		}
	};
	public static final TextColumn<Media> LONGITUDE = new TextColumn<Media>() { //BigDecimal longitude
		@Override
		public String getValue(final Media media) {
			return formatLongitude(media.getLongitude());
		}
	};
	public static final Column<Media, SafeHtml> PUBLIC = new Column<Media, SafeHtml>(new SafeHtmlCell()) { //boolean publik
		@Override
		public SafeHtml getValue(final Media media) {
			final SafeHtmlBuilder html = new SafeHtmlBuilder();
			if (media.isPublic()) {
				html.appendHtmlConstant("<span class=\"public\">");
				html.appendEscaped(MOBILE_MEDIA_SHARE_CONSTANTS.publik());
			} else {
				html.appendHtmlConstant("<span class=\"private\">");
				html.appendEscaped(MOBILE_MEDIA_SHARE_CONSTANTS._private());
			}
			html.appendHtmlConstant("</span>");
			return html.toSafeHtml();
		}
	};
	private static final ListUiBinder LIST_UI_BINDER = GWT.create(ListUiBinder.class);
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
	private static final UserServiceAsync USER_SERVICE =
			GWT.create(UserService.class);
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
	private static final int DURATION_BASE = 60;
	private static final BigDecimal DEGREES_BASE = new BigDecimal(60);
	//Ena geniko column pou pairnei Media kai deixnei SafeHtml. Ws orisma pairnei ena Cell gia SafeHtmlCell
	public static final Column<Media, SafeHtml> TITLE = new Column<Media, SafeHtml>(new SafeHtmlCell()) {
		@Override
		public SafeHtml getValue(final Media media) {
			//Dhmiourgeitai Builder pou kanei append pragmata. (San StringBuilder)
			final SafeHtmlBuilder html = new SafeHtmlBuilder();
			//* Oti einai hdh safe (me to trusted string 'h me allo builder), to vazei opws einai
			html.append(SafeHtmlUtils.fromTrustedString("<a href=\"" +
					MOBILE_MEDIA_SHARE_URLS.viewMedia(LocaleInfo.getCurrentLocale().getLocaleName(), media.getId()) + "\">"));
			//* Oti den einai safe, to kanei upoxrewtika escape gia na mhn exei tags, p.x. <br /> -> &lt;br /&gt;
			html.appendEscaped(media.getTitle());
			//Append mia aplh stathera pou elegxetai se compile time
			html.appendHtmlConstant("</a>");
			return html.toSafeHtml();
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
	//Protected kai oxi private gia na borei na tis xeiristei kai o UI Binder
	//Oute final boroun na einai gia na tis arxikopoiei (UiField)
	@UiField
	protected TextBox title;
	@UiField
	protected ListBox type; //dropdown
	//Parametros pros to annotation,
	//auto to UiField den tha ftiaxtei apo to UiBinder
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
	protected ListBox pageSize;
	@UiField
	protected Button download;
	@UiField
	protected Button edit;
	@UiField
	protected Button delete;
	@UiField
	protected SimplePager pager; //Gia pagination
	@UiField
	protected CellTable<Media> mediaTable; //Pinakas gia emfanish twn Media
	
	private final SingleSelectionModel<Media> selectionModel; //gia highlight kai epilogh grammhs
	private final ListDataProvider dataProvider;
	private User currentUser;
	private User selectedUser;
	
	public static String formatDuration(final int duration) {
		final int seconds = duration % DURATION_BASE;
		final int minutes = (duration / DURATION_BASE) % DURATION_BASE; //p.x se 63 lepta -> krataei 3 lepta
		final int hours = duration / DURATION_BASE / DURATION_BASE;
		return (hours > 0) ? MOBILE_MEDIA_SHARE_MESSAGES.durationFormatHoursMinutesSeconds(hours, minutes, seconds) :
				((minutes > 0) ? MOBILE_MEDIA_SHARE_MESSAGES.durationFormatMinutesSeconds(minutes, seconds) :
				MOBILE_MEDIA_SHARE_MESSAGES.durationFormatSeconds(seconds));
	}
	
	public static String formatLatitude(final BigDecimal latitude) {
		// 1 moira = 60 prwta lepta
		// 1 prwto lepto = 60 deutera
		// to divideAndRemainder kanei tin diairesh kai epistefei:
		// temp1[0]: phliko
		// temp1[1]: upoloipo
		final BigDecimal[] temp1 = latitude.multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
		final int seconds = temp1[1].intValue();
		final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
		final int minutes = temp2[1].intValue();
		final int degrees = temp2[0].intValue();
		//an einai arnhtiko-> einai notia, alliws voreia
		return (latitude.compareTo(BigDecimal.ZERO) < 0) ?
				MOBILE_MEDIA_SHARE_MESSAGES.latitudeFormatSouth(-degrees, -minutes, -seconds) :
				MOBILE_MEDIA_SHARE_MESSAGES.latitudeFormatNorth(degrees, minutes, seconds);
	}
	
	public static String formatLongitude(final BigDecimal longitude) {
		final BigDecimal[] temp1 = longitude.multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
		final int seconds = temp1[1].intValue();
		final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
		final int minutes = temp2[1].intValue();
		final int degrees = temp2[0].intValue();
		//an einai arnhtiko-> einai dutika, alliws anatolika
		return (longitude.compareTo(BigDecimal.ZERO) < 0) ?
				MOBILE_MEDIA_SHARE_MESSAGES.longitudeFormatWest(-degrees, -minutes, -seconds) :
				MOBILE_MEDIA_SHARE_MESSAGES.longitudeFormatEast(degrees, minutes, seconds);
	}

	
	public List() {
		//Den afhnoume ton UiBinder na dhmiourghsei to SuggestBox giati den xerei ti na valei gia UserOracle
		//Prin na xekinhsei na stinei tin selida (me to initWidget) dhmiourgoume ola ta UiFields pou exoun provided = true
		user = new SuggestBox(new UserOracle());
		initWidget(LIST_UI_BINDER.createAndBindUi(this));
		title.addKeyUpHandler(this);
		user.addKeyUpHandler(this);
		user.addSelectionHandler(this); //otan epilexei kati apo ta proteinomena
		//(To ti fainetai, timh tou ti fainetai)
		type.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		//Prosthikh twn dunatwn tupwn arxeiwn
		for (MediaType mediaType : MediaType.values())
			type.addItem(MEDIA_TYPE_CONSTANTS.getString(mediaType.name()), mediaType.name());
		type.addChangeHandler(this); //otan tou allaxei timh
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
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.publik(), Boolean.TRUE.toString());
		publik.addItem(MOBILE_MEDIA_SHARE_CONSTANTS._private(), Boolean.FALSE.toString());
		publik.addChangeHandler(this);
		for(int p = MIN_PAGE_SIZE; p <= MAX_PAGE_SIZE; p += PAGE_SIZE_STEP)
			pageSize.addItem(Integer.toString(p), Integer.toString(p));
		pageSize.addChangeHandler(this);
		download.setEnabled(false);
		download.addClickHandler(this);
		edit.setEnabled(false);
		edit.addClickHandler(this);
		delete.setEnabled(false);
		delete.addClickHandler(this);
		pager.setPageSize(MIN_PAGE_SIZE);
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
		selectionModel = new SingleSelectionModel<Media>();
		selectionModel.addSelectionChangeHandler(this);
		mediaTable.setSelectionModel(selectionModel);
		//O AsyncDataProvider tha vazei dedomena stin lista (table)
		dataProvider = new ListDataProvider();
		selectedUser = null;
	}

	//Gia allagh ston typo h sto megethos selidas apo ton xrhsth
	@Override
	public void onChange(final ChangeEvent _) {
		dataProvider.onRangeChanged(null);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) { // clicking on download, edit or delete
		if (clickEvent.getSource() == download)
			//Anoigei neo tab pou tha trexei tin doGet gia na katevei to arxeio
			Window.open(MOBILE_MEDIA_SHARE_URLS.download(URL.encodeQueryString(selectionModel.getSelectedObject().getId())), "_blank", "");
		else if (clickEvent.getSource() == edit)
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.editMedia(URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					URL.encodeQueryString(selectionModel.getSelectedObject().getId())));
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
					//katharizei to highlight tou pinaka
					selectionModel.clear();
					//apenergopoiountai ta koubia (download, edit, delete)
					onSelectionChange(null);
					//fernei nea dedomena ston pinaka
					dataProvider.onRangeChanged(null);
				}
			});
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
		dataProvider.onRangeChanged(null);
	}
	
	@Override
	public void onModuleLoad() { // set up HTML
		RootPanel.get().add(this); //prosthikh sth selida (tin jsp) tou widget
		USER_SERVICE.getUser(InputElement.as(Document.get().getElementById("email")).getValue(), new AsyncCallback<User>() {
			@Override
			public void onFailure(final Throwable throwable) {
				Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingUser(
						MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
				//redirect sto map
				Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString( 
						LocaleInfo.getCurrentLocale().getLocaleName())));
			}

			@Override
			public void onSuccess(final User user) {
				if (user == null) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingUser(
							MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString( 
							LocaleInfo.getCurrentLocale().getLocaleName())));
					return;
				}
				currentUser = user;
				//O data provider tha provalei ta dedomena tou ston pinaka
				dataProvider.addDataDisplay(mediaTable);
//				dataProvider.onRangeChanged(null);
			}
		});
	}
	
	//Otan dialegei o xrhsths sugkekrimenh protash
	@Override
	public void onSelection(final SelectionEvent<SuggestOracle.Suggestion> selectionEvent) { // selecting a user
		selectedUser = (selectionEvent.getSelectedItem() instanceof UserSuggestion) ? 
				((UserSuggestion) selectionEvent.getSelectedItem()).getUser() : null;
		dataProvider.onRangeChanged(null); //fernei dedomena gi' auton (epilegmeno) ton xrhsth
	}
	
	@Override
	public void onSelectionChange(final SelectionChangeEvent _) { // selecting a row		
		//analoga me to epilegmeno pou edwse o xrhsths (se mia grammh)
		final Media media = selectionModel.getSelectedObject();
		//enable ta download, edit kai delete
		download.setEnabled(media != null);
		//edit kai delete kapoio arxeio an anhkei ston trexonta xrhsth
		if ((media != null) && (currentUser.equals(media.getUser()) || (currentUser.getStatus() == UserStatus.ADMIN))) {
			edit.setEnabled(true);
			delete.setEnabled(true);
		}
	}

	@Override
	public void onValueChange(final ValueChangeEvent<Date> _) { // changing created from, created to, edited from or edited to values
		dataProvider.onRangeChanged(null);
	}
}
