package gr.uoa.di.std08169.mobile.media.share.client.html;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

//Widget xwris Ui Binder (ui xml). Den einai GWT module (EntryPoint) dhladh selida mono tou.
//O PhotoSelector xrhsimopoieitai apo tin MyAccount (selida-module) gia na epilegei o xrhsths photo 
public class PhotoSelector extends Composite {
	private static final int ROWS = 3;
	private static final int COLUMNS = 4;
	private static final MediaServiceAsync MEDIA_SERVICE = GWT.create(MediaService.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = GWT.create(MobileMediaShareUrls.class);
	
	private final Image[][] images;
	private final String[][] ids;
	private String value;
	
	//Arxkopoihsh tou plegmatos eikonwn
	public PhotoSelector() {
		images = new Image[ROWS][COLUMNS];
		ids = new String[ROWS][COLUMNS];
		value = null;
		final VerticalPanel rows = new VerticalPanel();
		// TODO na exei kapoy panw ena X na kleinei		
		for (int i = 0; i < ROWS; i++) {
			final HorizontalPanel columns = new HorizontalPanel();
			for (int j = 0; j < COLUMNS; j++) {
				final int row = i;
				final int column = j;
				images[i][j] = new Image();
				images[i][j].setWidth(MyAccount.IMAGE_WIDTH);
				images[i][j].setHeight(MyAccount.IMAGE_HEIGHT);
				//clickHandler gia kathe mia apo tis eikones
				//(gia na dwsei to value se opoia eikona pathsei o xrhsths)
				images[i][j].addClickHandler(new ClickHandler() {
					@Override
					public void onClick(final ClickEvent _) {
						value = ids[row][column];
						PhotoSelector.this.setVisible(false);
					}
				});
				ids[i][j] = null;
				columns.add(images[i][j]);
			}
			rows.add(columns);
		}
		// TODO na exei kapoy katw ena pager
		initWidget(rows);
	}
	
	public void init(final String email) {
		MEDIA_SERVICE.getMedia(email, null, MediaType.IMAGE, email, null, null, null, null, null, 0, 
								//eikones taxinomhmenes kata titlo
				ROWS * COLUMNS, List.TITLE.getDataStoreName(), true, new AsyncCallback<MediaResult>() {
			@Override
			public void onFailure(final Throwable _) {
				Window.alert("Error retrieving photos");
				// TODO na exei panta mia photo (th default)
			}

			@Override
			public void onSuccess(final MediaResult result) {
				// TODO na exei panta mia photo (th default) prwth
				for (int i = 0; i < ROWS; i++) {
					for (int j = 0; j < COLUMNS; j++) {
						final Media media = result.getMedia().get(i * COLUMNS + j);
						images[i][j].setUrl(MOBILE_MEDIA_SHARE_URLS.download(media.getId()));
						images[i][j].setTitle(media.getTitle());
						images[i][j].setAltText(media.getTitle());
						ids[i][j] = media.getId();
					}
				}
			}
		});
	}
	
	public void setValue(final String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
