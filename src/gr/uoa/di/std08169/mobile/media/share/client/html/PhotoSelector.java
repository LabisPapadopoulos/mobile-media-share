package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

//Widget xwris Ui Binder (ui xml). Den einai GWT module (EntryPoint) dhladh selida mono tou.
//O PhotoSelector xrhsimopoieitai apo tin MyAccount (selida-module) gia na epilegei o xrhsths photo 
public class PhotoSelector extends Composite implements ClickHandler {
	protected static interface PhotoSelectorUiBinder extends UiBinder<Widget, PhotoSelector> {}
	//metatrepei to Ui xml se java antikeimeno
	private static final PhotoSelectorUiBinder PHOTO_SELECTOR_UI_BINDER = GWT.create(PhotoSelectorUiBinder.class);
	
	private static final int ROWS = 3;
	private static final int COLUMNS = 4;
	private static final int PAGER_PAGES = 5;
	private static final MediaServiceAsync MEDIA_SERVICE = GWT.create(MediaService.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = GWT.create(MobileMediaShareUrls.class);
	
	@UiField
	protected Button close;
	@UiField
	protected VerticalPanel photos;
	@UiField
	protected HorizontalPanel pager;
	@UiField
	protected Label pagerText;
	//gia na klhthei otan allaxei o xrhsths eikona (trexei h run()) 
	private final Runnable callback;
	private String email;
	private String value;
	
	//Arxkopoihsh tou plegmatos eikonwn
	public PhotoSelector(final Runnable callback) {
		initWidget(PHOTO_SELECTOR_UI_BINDER.createAndBindUi(this));
		close.addClickHandler(this);
		email = null;
		value = null;
		this.callback = callback;
	}
	
	public void init(final String email) {
		this.email = email;
		showPage(0);
	}
	
	public void setValue(final String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	//click handler tou close
	@Override
	public void onClick(final ClickEvent clickEvent) {
		setVisible(false);
	}
	
	public void showPage(final int page) {
		MEDIA_SERVICE.getMedia(email, null, MediaType.IMAGE, email, null, null, null, null, null, 
				page * ROWS * COLUMNS, ROWS * COLUMNS,
				//eikones taxinomhmenes kata titlo
				List.TITLE.getDataStoreName(), true, new AsyncCallback<MediaResult>() {
			@Override
			public void onFailure(final Throwable _) {
				Window.alert("Error retrieving photos"); // TODO
			}
			
			@Override
			public void onSuccess(final MediaResult result) {
				final int totalPages = (int) Math.ceil((result.getTotal() + 1) / (float) (ROWS * COLUMNS)); //strogulopoihsh pros ta panw (+ 1 gia th default)
				photos.clear();
				HorizontalPanel row = null;
				for (int i = 0; (i < ROWS * COLUMNS) && (i < result.getMedia().size()); i++) {
					final Media media = result.getMedia().get(i);
					final Image image = new Image();
					image.setUrl(MOBILE_MEDIA_SHARE_URLS.download(media.getId()));
					image.setTitle(media.getTitle());
					image.setAltText(media.getTitle());
					image.setWidth(MyAccount.IMAGE_WIDTH);
					image.setHeight(MyAccount.IMAGE_HEIGHT);
					//clickHandler gia kathe mia apo tis eikones
					//(gia na dwsei to value se opoia eikona pathsei o xrhsths)
					image.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(final ClickEvent _) {
							value = media.getId();
							callback.run();
							PhotoSelector.this.setVisible(false);
						}
					});
					if (i % COLUMNS == 0) { //ana COLUMNS photos ginetai allagh grammhs
						row = new HorizontalPanel();
						photos.add(row);
					}
					row.add(image);
				}
				if (page == totalPages - 1) { // sthn teleutaia selida bazei kai th default photo
					final Image image = new Image();
					image.setUrl(MOBILE_MEDIA_SHARE_URLS.defaultUser());
					image.setTitle("Default"); // TODO constants
					image.setAltText("Default");
					image.setWidth(MyAccount.IMAGE_WIDTH);
					image.setHeight(MyAccount.IMAGE_HEIGHT);
					//clickHandler gia kathe mia apo tis eikones
					//(gia na dwsei to value se opoia eikona pathsei o xrhsths)
					image.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(final ClickEvent _) {
							value = null;
							callback.run();
							PhotoSelector.this.setVisible(false);
						}
					});
					if (result.getTotal() % COLUMNS == 0) { //ana COLUMNS photos ginetai allagh grammhs
						row = new HorizontalPanel();
						photos.add(row);
					}
					row.add(image);
				}
				pager.clear();
				//prosthikh:
				// <<
				// <		
				if (page > 0) {
					final Anchor firstPage = new Anchor();
					firstPage.setText("<<");
					firstPage.setTitle("First page");
					firstPage.setHref("#");
					firstPage.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(final ClickEvent _) {
							showPage(0);
						}
					});
					pager.add(firstPage);
					final Anchor previousPage = new Anchor();
					previousPage.setText("<");
					previousPage.setTitle("Previous page");
					previousPage.setHref("#");
					previousPage.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(final ClickEvent _) {
							showPage(page - 1);
						}
					});
					pager.add(previousPage);
				}
				
				//prosthikh numbers
				int start = page;
				int end = page;
				//oso exoun perithorio na mikrinei to start 'h na megalwsei to end (toulaxiston ena apo ta duo na borei na metakinithei)
				//kai na mhn exei sublhrwthei to plhthos twn selidwn pou theloume
				while (((start > 0) || (end < totalPages)) && (end - start < PAGER_PAGES)) {
					//mikrenei to start mexri na ftasei to 0
					if (start > 0)
						start--;
					//megalwnei to end mexri na ftasei to totalPages - 1
					if (end < totalPages)
						end++;
				}
				
				for (int i = start; i < end; i++) {
					if (i == page) {
						final InlineLabel currentPage = new InlineLabel();
						currentPage.setText(Integer.toString(i + 1));
						currentPage.setTitle("Page " + (i + 1)); //TODO
						pager.add(currentPage);
					} else {
						final Anchor currentPage = new Anchor();
						currentPage.setText(Integer.toString(i + 1));
						currentPage.setTitle("Page " + (i + 1)); //TODO
						currentPage.setHref("#");
						final int j = i;
						currentPage.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(final ClickEvent _) {
								showPage(j);
							}
						});
						pager.add(currentPage);
					}
				}
				//prosthikh
				// >
				// >>
				//An h trexousa selida den einai h teleutaia
				if (page < totalPages - 1) {
					final Anchor nextPage = new Anchor();
					nextPage.setText(">");
					nextPage.setTitle("Next page"); //TODO
					nextPage.setHref("#");
					nextPage.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(final ClickEvent _) {
							showPage(page + 1);
						}
					});
					pager.add(nextPage);
					final Anchor previousPage = new Anchor();
					previousPage.setText(">>");
					previousPage.setTitle("Last page");
					previousPage.setHref("#");
					previousPage.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(final ClickEvent _) {
							showPage(totalPages - 1);
						}
					});
					pager.add(previousPage);
				}
				pagerText.setText("Page " + (page + 1) + " of " + totalPages + ", displaying photos " + 
						(page * ROWS * COLUMNS + 1) + " to " + 
						(((page + 1) * ROWS * COLUMNS < result.getTotal() + 1) ? ((page + 1) * ROWS * COLUMNS) : (result.getTotal() + 1)) 
						+ " of " + (result.getTotal() + 1)); //TODO
			}
		});
	}
}
