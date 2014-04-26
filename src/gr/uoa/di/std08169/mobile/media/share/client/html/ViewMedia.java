package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SourceElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.MarkerImage;
import com.google.maps.gwt.client.MarkerOptions;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;
import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareMessages;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceAsync;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;

public class ViewMedia extends Composite implements ClickHandler, EntryPoint, Runnable {
	//To interface ftiaxnei ena widget me vash to EditMedia
	protected static interface ViewMediaUiBinder extends UiBinder<Widget, ViewMedia> {}
	
	private static final ViewMediaUiBinder VIEW_MEDIA_UI_BINDER = GWT.create(ViewMediaUiBinder.class);
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS =
			GWT.create(MobileMediaShareUrls.class);
	private static final MobileMediaShareMessages MOBILE_MEDIA_SHARE_MESSAGES =
			GWT.create(MobileMediaShareMessages.class);
	private static final MediaServiceAsync MEDIA_SERVICE = GWT.create(MediaService.class);
	private static final double CONTENT_WIDTH = 512.0;
	private static final double CONTENT_HEIGHT = 512.0;
	
	@UiField
	protected DivElement content;
	@UiField
	protected Button download;
	@UiField
	protected Button edit;
	@UiField
	protected Button delete;
	@UiField
	protected InlineLabel title;
	//Gia prosthikh safeHTML sti selida
	@UiField
	protected InlineHTML type;
	@UiField
	protected InlineLabel size;
	@UiField
	protected InlineLabel duration;
	@UiField
	protected InlineLabel user;
	@UiField
	protected InlineLabel created;
	@UiField
	protected InlineLabel edited;
	@UiField
	protected InlineHTML publik;
	@UiField
	protected InlineLabel latitudeLongitude;
	@UiField
	protected DivElement map;
	
	private GoogleMap googleMap;
	private Marker marker;
	
	public ViewMedia() {
		//Dhmiourgeia grafikou tou widget
		initWidget(VIEW_MEDIA_UI_BINDER.createAndBindUi(this));
		download.addClickHandler(this);
		edit.setEnabled(false);
		edit.addClickHandler(this);
		delete.setEnabled(false);
		delete.addClickHandler(this);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		if (clickEvent.getSource() == download) { // TODO
		} else if (clickEvent.getSource() == edit) { // TODO
		} else if (clickEvent.getSource() == delete) { // TODO
		}
	}
	
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
		//options gia to fortwma tis vivliothikhs
		final AjaxLoader.AjaxLoaderOptions options = AjaxLoader.AjaxLoaderOptions.newInstance();
		options.setOtherParms(MOBILE_MEDIA_SHARE_URLS.googleMapsOptions(LocaleInfo.getCurrentLocale().getLocaleName()));
		//Xekinaei na fortwnei Google Maps me ta options kai otan teleiwsei trexei tin run tou this (EditMedia)
		AjaxLoader.loadApi(Map.GOOGLE_MAPS_API, Map.GOOGLE_MAPS_VERSION, this, options);
	}

	@Override
	public void run() {
		//options gia ton sugkekrimeno xarth
		final MapOptions options = MapOptions.create(); //Dhmiourgeia antikeimenou me Factory (xwris constructor)
		//Default na fenetai xarths apo doruforo (Hybrid)
		options.setMapTypeId(MapTypeId.HYBRID);
		options.setZoom(Map.GOOGLE_MAPS_ZOOM);
		//Dhmiourgei ton xarth me tis panw ruthmiseis kai to vazei sto mapDiv
		googleMap = GoogleMap.create(map, options);
		//Otan o xrhsths kanei click epanw ston xarth
		final MarkerOptions markerOptions = MarkerOptions.create();
		markerOptions.setMap(googleMap);
		marker = Marker.create(markerOptions);
		//psaxnei antikeimeno gia na kentrarei o xarths kai na fortwsei h forma
		final String id = Window.Location.getParameter("id");
		if (id == null) {
			Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingMedium(
					MOBILE_MEDIA_SHARE_CONSTANTS.noMediaIdSpecified()));
			//redirect sto map
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
					//me to antistoixo locale 
					LocaleInfo.getCurrentLocale().getLocaleName())));
		} else {
			//Klhsh tou MEDIA_SERVICE gia na paroume to antikeimeno (metadedomena)
			MEDIA_SERVICE.getMedia(id, new AsyncCallback<Media>() {
				@Override
				public void onFailure(final Throwable throwable) {
					Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorRetrievingMedium(throwable.getMessage()));
					//redirect sto map
					Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
							//me to antistoixo locale 
							LocaleInfo.getCurrentLocale().getLocaleName())));
				}

				@Override
				public void onSuccess(final Media media) {
					if (media == null) {
						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorViewingMedia(
								MOBILE_MEDIA_SHARE_CONSTANTS.mediaNotFound()));
						//redirect sto map
						Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
								//me to antistoixo locale 
								LocaleInfo.getCurrentLocale().getLocaleName())));
					//o xrhsths vlepei to media giati einai diko tou 'h public
					} else if (media.getUser().getEmail().equals(InputElement.as(Document.get().getElementById("email")).getValue()) ||
							media.isPublic()) {
						
						//Gemisma tou div content analoga ton tupo
						switch (MediaType.getMediaType(media.getType())) {
						case APPLICATION:
							final ImageElement application = Document.get().createImageElement();
							application.setSrc(MOBILE_MEDIA_SHARE_URLS.selectedImage(MediaType.APPLICATION.name().toLowerCase()));
							application.setAlt(media.getTitle());
							application.getStyle().setWidth(CONTENT_WIDTH, Style.Unit.PX);
							application.getStyle().setHeight(CONTENT_HEIGHT, Style.Unit.PX);
							content.appendChild(application);
							break;
						case AUDIO:
							final AudioElement audio = Document.get().createAudioElement();
							audio.setControls(true);
							audio.setPreload(AudioElement.PRELOAD_AUTO);
							//url sto opoio vriskontai ta dedomena tou antikeimenou. Ta travaei o browser
							//me xrhsh tou media servlet
							final SourceElement audioSource = Document.get().createSourceElement();
							audioSource.setSrc(MOBILE_MEDIA_SHARE_URLS.download(media.getId()));
							audioSource.setType(media.getType());
							audio.appendChild(audioSource);
							content.appendChild(audio);
							break;
						case IMAGE:
							final ImageElement image = Document.get().createImageElement();
							image.setSrc(MOBILE_MEDIA_SHARE_URLS.download(media.getId()));
							image.setAlt(media.getTitle());
							image.getStyle().setWidth(CONTENT_WIDTH, Style.Unit.PX);
							image.getStyle().setHeight(CONTENT_HEIGHT, Style.Unit.PX);
							content.appendChild(image);
							break;
						case TEXT:
							/**
							 * @see http://www.gwtproject.org/doc/latest/tutorial/JSON.html#http
							 */
							final ParagraphElement text = Document.get().createPElement();
							text.getStyle().setWidth(CONTENT_WIDTH, Style.Unit.PX);
							text.getStyle().setHeight(CONTENT_HEIGHT, Style.Unit.PX);
							//scrollbar gia to text
							text.getStyle().setOverflow(Style.Overflow.SCROLL);
							content.appendChild(text);
							//Zhtaei asugxrona to periexomeno enos url
							final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET,
									MOBILE_MEDIA_SHARE_URLS.download(media.getId()));
							try {
								requestBuilder.sendRequest(null, new RequestCallback() {
									@Override
									public void onError(final Request request, final Throwable throwable) {
										Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorViewingMedia(throwable.getMessage()));
										//redirect sto map
										Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
												//me to antistoixo locale 
												LocaleInfo.getCurrentLocale().getLocaleName())));
									}
									
									@Override
									public void onResponseReceived(final Request request, final Response response) {
										if (response.getStatusCode() == Response.SC_OK) {
											//selida pou fernei to response se me to keimeno pros anazhthsh
											text.setInnerText(response.getText());
										} else {
											//selida pou efere to response se periptwsh sfalmatos (getText())
											Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorViewingMedia(response.getText()));
											//redirect sto map
											Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
													//me to antistoixo locale 
													LocaleInfo.getCurrentLocale().getLocaleName())));
										}
									}
								});
							} catch (final RequestException e) {
								Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorViewingMedia(e.getMessage()));
								//redirect sto map
								Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
										//me to antistoixo locale 
										LocaleInfo.getCurrentLocale().getLocaleName())));
							}
							
							
							break;
						case VIDEO:
							final VideoElement video = Document.get().createVideoElement();
							video.setPoster(MOBILE_MEDIA_SHARE_URLS.selectedImage(MediaType.VIDEO.name().toLowerCase()));
							video.setControls(true);
							video.setPreload(VideoElement.PRELOAD_AUTO);
							video.setWidth(Double.valueOf(CONTENT_WIDTH).intValue());
							video.setHeight(Double.valueOf(CONTENT_HEIGHT).intValue());
							final SourceElement videoSource = Document.get().createSourceElement();
							videoSource.setSrc(MOBILE_MEDIA_SHARE_URLS.download(media.getId()));
							videoSource.setType(media.getType());
							video.appendChild(videoSource);
							content.appendChild(video);
							break;
						}
						if (media.getUser().getEmail().equals(InputElement.as(Document.get().getElementById("email")).getValue())) {
							edit.setEnabled(true);
							delete.setEnabled(true);
						}
						title.setText(media.getTitle());
						//prosthikh html (keimeno kai eikona) stin selida
						type.setHTML(List.TYPE.getValue(media));
						//analoga ton tupo dialegetai to katallhlo eikonidio
						marker.setIcon(MarkerImage.create(MOBILE_MEDIA_SHARE_URLS.selectedImage(
								MediaType.getMediaType(media.getType()).name().toLowerCase())));
						size.setText(List.SIZE.getValue(media));
						duration.setText(List.DURATION.getValue(media));
						user.setText(List.USER.getValue(media));
						created.setText(List.CREATED.getValue(media));
						edited.setText(List.EDITED.getValue(media));
						publik.setHTML(List.PUBLIC.getValue(media));
						latitudeLongitude.setText("(" + List.LATITUDE.getValue(media) + ", " + List.LONGITUDE.getValue(media) + ")");
						final LatLng latLng = LatLng.create(media.getLatitude().doubleValue(), media.getLongitude().doubleValue());
						googleMap.setCenter(latLng);
						marker.setPosition(latLng);
					} else { //Vrethike to media alla einai private allounou
						Window.alert(MOBILE_MEDIA_SHARE_MESSAGES.errorViewingMedia(
								MOBILE_MEDIA_SHARE_CONSTANTS.accessDenied()));
						//redirect sto map
						Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.map(URL.encodeQueryString(
								//me to antistoixo locale 
								LocaleInfo.getCurrentLocale().getLocaleName())));
					}
				}
			});
		}
	}
}
