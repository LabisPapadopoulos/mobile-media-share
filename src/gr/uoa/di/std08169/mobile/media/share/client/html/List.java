package gr.uoa.di.std08169.mobile.media.share.client.html;

import java.util.Collections;
import java.util.Date;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MediaTypeConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceAsync;
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
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

//AsyncDataProvider<Media>: fernei dedomena (Media)  
public class List extends AsyncDataProvider<Media> implements ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler, SelectionChangeEvent.Handler, SelectionHandler<SuggestOracle.Suggestion>, ValueChangeHandler<Date> {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS =
			//kanei automath ulopoihsh to GWT tou interface
			GWT.create(MobileMediaShareConstants.class);
	private static final MediaTypeConstants MEDIA_TYPE_CONSTANTS =
			//kanei automath ulopoihsh to GWT tou interface
			GWT.create(MediaTypeConstants.class);
	//Media Service se front-end
	private static final MediaServiceAsync MEDIA_SERVICE =
			GWT.create(MediaService.class);
	private static final int MIN_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int PAGE_SIZE_STEP = 10;
//	private final User user;
//	private final Date created;
//	private Date edited;
//	private String title;
//	private BigDecimal latitude;
//	private BigDecimal longitude;
//	private boolean publik;
	private static final TextColumn<Media> TITLE = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			return media.getTitle();
		}
	};
	private static final TextColumn<Media> TYPE = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			return media.getType();
		}
	};
	private static final TextColumn<Media> SIZE = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			return Long.toString(media.getSize());
		}
	};
	private static final TextColumn<Media> DURATION = new TextColumn<Media>() {
		@Override
		public String getValue(final Media media) {
			return Integer.toString(media.getDuration());
		}
	};
//	
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
//	private static final TextColumn TYPE = new TextColumn<Media>() {
//		@Override
//		public String getValue(final Media media) {
//			return media.getType();
//		}
//	};
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
		//TODO
	}
	

	
	//Search
	private final TextBox title;
	private final ListBox type; //dropdown
	private final SuggestBox user;
	private final DatePicker createdFrom;
	private final DatePicker createdTo;
	private final DatePicker editedFrom;
	private final DatePicker editedTo;
	private final CheckBox publik;
	private final ListBox pageSize;
	private final Button download;
	private final Button edit;
	private final Button delete;
	private final SimplePager pager; //Gia pagination
	private final SingleSelectionModel<Media> selectionModel; //gia highlight kai epilogh grammhs
	private final CellTable<Media> mediaTable; //Pinakas gia emfanish twn Media
	
	public List() {
		title = new TextBox();
		title.addKeyUpHandler(this);
		type = new ListBox();
		//(To ti fainetai, timh tou ti fainetai)
		type.addItem(MOBILE_MEDIA_SHARE_CONSTANTS.anyType(), "");
		//Prosthikh twn dunatwn tupwn arxeiwn
		for (MediaType mediaType : MediaType.values())
			type.addItem(MEDIA_TYPE_CONSTANTS.getString(mediaType.name()), mediaType.name());
		type.addChangeHandler(this); //otan tou allaxei timh
		user = new SuggestBox(); //TODO
		user.addKeyUpHandler(this);
		user.addSelectionHandler(this); //otan epilexei kati apo ta proteinomena
		createdFrom = new DatePicker();
		createdFrom.addValueChangeHandler(this); //otan tha allaxei timh
		createdTo = new DatePicker();
		createdTo.addValueChangeHandler(this);
		editedFrom = new DatePicker();
		editedFrom.addValueChangeHandler(this);
		editedTo = new DatePicker();
		editedTo.addValueChangeHandler(this);
		publik = new CheckBox();
		publik.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(final ValueChangeEvent<Boolean> _) { // o xrhsths allaxe thn timh tou publik
				onRangeChanged(mediaTable);
			}
		});
		pageSize = new ListBox();
		for(int i = MIN_PAGE_SIZE; i <= MAX_PAGE_SIZE; i += PAGE_SIZE_STEP)
			pageSize.addItem(Integer.toString(i), Integer.toString(i));
		pageSize.addChangeHandler(this);
		download = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.download());
		download.setEnabled(false);
		download.addClickHandler(this);
		edit = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.edit());
		edit.setEnabled(false);
		edit.addClickHandler(this);
		delete = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.delete());
		delete.setEnabled(false);
		delete.addClickHandler(this);
		pager = new SimplePager();
		pager.setPageSize(MIN_PAGE_SIZE);
		selectionModel = new SingleSelectionModel<Media>();
		selectionModel.addSelectionChangeHandler(this);
		mediaTable = new CellTable<Media>();
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
		//TODO sthles
		mediaTable.getColumnSortList().setLimit(1); //1 sthlh thn fora tha einai taxinomhmenh
		//by default taxinomhse kata titlo (leitourgei san stoiva - taxinomei panda tin teleutaia sthlh)
		mediaTable.getColumnSortList().push(TITLE);
		//o pager selidopoiei tin lista
		pager.setDisplay(mediaTable);
		//O AsyncDataProvider tha vazei dedomena stin lista
		addDataDisplay(mediaTable);
	}

	//Gia allagh ston typo h sto megethos selidas apo ton xrhsth
	@Override
	public void onChange(final ChangeEvent changeEvent) {
		onRangeChanged(mediaTable);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) { // clicking on download, edit or delete
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyUp(final KeyUpEvent keyUpEvent) { // typing into title or user
		onRangeChanged(mediaTable); // TODO for user
		
	}
	
	@Override
	public void onModuleLoad() { // set up HTML
		Document.get().getBody().addClassName("bodyClass");
		Document.get().getBody().appendChild(Header.newHeader());
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.title()));
		RootPanel.get().add(title);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.type()));
		RootPanel.get().add(type);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.user()));
		RootPanel.get().add(user);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.createdFrom()));
		RootPanel.get().add(createdFrom);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.createdTo()));
		RootPanel.get().add(createdTo);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.editedFrom()));
		RootPanel.get().add(editedFrom);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.editedTo()));
		RootPanel.get().add(editedTo);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.publik()));
		RootPanel.get().add(publik);
		RootPanel.get().add(new InlineLabel(MOBILE_MEDIA_SHARE_CONSTANTS.pageSize()));
		RootPanel.get().add(pageSize);
		RootPanel.get().add(download);
		RootPanel.get().add(edit);
		RootPanel.get().add(delete);
		RootPanel.get().add(pager);
		RootPanel.get().add(mediaTable);
	}
	
	//AsyncDataProvider<Media> fernei dedomena.
	//Kaleitai otan xreiazetai na erthoun nea dedomena (p.x SortHandler, ChangeHandler)
	@Override
	protected void onRangeChanged(final HasData<Media> hasData) { // update list rows
		final String title = this.title.getValue().trim().isEmpty() ? null : this.title.getValue().trim();
		//MediaType epeidh einai ENUM me ena string epistrefetai ena instance
		final MediaType type = this.type.getValue(this.type.getSelectedIndex()).isEmpty() ? null :
				MediaType.valueOf(this.type.getValue(this.type.getSelectedIndex()));
		final User user = null;
		final Date createdFrom = this.createdFrom.getValue();
		final Date createdTo = this.createdTo.getValue();
		final Date editedFrom = this.editedFrom.getValue();
		final Date editedTo =  this.editedTo.getValue();
		final boolean publik = this.publik.getValue();
		pager.setPageSize(Integer.valueOf(pageSize.getValue(pageSize.getSelectedIndex())));
		final int start = pager.getPageStart();
		final int length = pager.getPageSize();
		//Sortarisma ws pros to 1o stoixeio tis listas (an uparxei) opws legetai mesa sthn vash (getDataStoreName)
		final String orderField = (mediaTable.getColumnSortList().size() == 0) ? null : 
			mediaTable.getColumnSortList().get(0).getColumn().getDataStoreName();
		final boolean ascending = (mediaTable.getColumnSortList().size() == 0) ? false : 
			mediaTable.getColumnSortList().get(0).isAscending();
		MEDIA_SERVICE.getMedia(title, type, user, createdFrom, createdTo, editedFrom, editedTo, publik, start, length, orderField, ascending,
				new AsyncCallback<MediaResult>() {
			@Override
			public void onFailure(final Throwable throwable) {//se front-end ston browser
				//Afou egine Failure, bainei adeia Lista apo Media
				updateRowData(0, Collections.<Media>emptyList());
				updateRowCount(0, false);
				selectionModel.clear();
				onSelection(null);
			}

			@Override
			public void onSuccess(final MediaResult result) {
Window.alert("Found " + result + " media");
for (Media media : result.getMedia())
Window.alert(media.getTitle());
				//deixnei ta kainouria dedomena pou efere h onRangeChanged
				updateRowData(start, result.getMedia()); //h selida pou efere
				updateRowCount(result.getTotal(), true); //ananewnei sumfwna me tis sunolikes grammes pou sigoura gnwrizei ton arithmo (true)
				selectionModel.clear(); //katharizei tin highlight grammh tou xrhsth
				onSelectionChange(null); //kanei disable ta download, edit kai delete afou den einai kamia grammh epilegmenh
			}
		});
	}
	
	@Override
	public void onSelection(final SelectionEvent<SuggestOracle.Suggestion> selectionEvent) { // selecting a user
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onSelectionChange(final SelectionChangeEvent selectionChangeEvent) { // selecting a row
		//analoga me to epilegmeno pou edwse o xrhsths (se mia grammh)
		final Media media = selectionModel.getSelectedObject();
		//enable ta download, edit kai delete
		download.setEnabled(media != null);
		edit.setEnabled(media != null);
		delete.setEnabled(media != null);
	}

	@Override
	public void onValueChange(final ValueChangeEvent<Date> valueChangeEvent) { // changing created from, created to, edited from or edited to values
		onRangeChanged(mediaTable);
	}
}
