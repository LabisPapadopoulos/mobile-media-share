package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;

public class Upload implements ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler, RequestCallback {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	private static final int TOP_STEP = 30;
	private static final int LEFT_OFFSET = 425;
	private static final int LEFT_STEP = 100;
	
	private final FormPanel form;
	private final FileUpload file;
	private final TextBox title;
//	private final GoogleMap map; //[Latitude, Longitude]
	private final CheckBox publik;
	private final Button ok;
	private final Button reset;
	
	public Upload() {
		//gia na min anoixei kainourio parathuro
		form = new FormPanel(new NamedFrame("_self"));
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setAction("./uploadServlet");
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
		publik.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		// anamesa sto publik kai sto ok tha bei allo field to opoio tha kanei
		i++;
		int j = 0;
		ok = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.ok());
		ok.addClickHandler(this);
		ok.setEnabled(false);
		ok.getElement().setAttribute("style", "top: " + (TOP_STEP * i) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
		reset = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.reset());
		reset.addClickHandler(this);
		reset.getElement().setAttribute("style", "top: " + (TOP_STEP * i) + "px; left: " + (LEFT_OFFSET + LEFT_STEP * j++) + "px;");
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

	@Override
	public void onChange(final ChangeEvent _) {
														//to onoma tou arxeiou na mhn einai keno
		ok.setEnabled((file.getFilename() != null) && (!file.getFilename().trim().isEmpty()) &&
				(!title.getValue().trim().isEmpty()));		
	}

	@Override
	public void onError(final Request _, final Throwable __) {
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
				//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
				URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
				//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
				URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.upload(
						URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
	}

	@Override
	public void onKeyUp(final KeyUpEvent _) {
		ok.setEnabled((file.getFilename() != null) && (!file.getFilename().trim().isEmpty()) &&
				(!title.getValue().trim().isEmpty()));		
	}

	@Override
	public void onModuleLoad() {
		try {
			//RequestBuilder gia na kanoume ena GET request sto servlet login gia na paroume
			//to session mas. RequestCallback (this) einai auto pou tha parei tin apantish asunxrona
			new RequestBuilder(RequestBuilder.GET, "./loginServlet").sendRequest(null, this);
		} catch (final RequestException _) {
			//otidhpote paei strava, xana gurnaei stin login
			//url pou theloume na mas paei
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.upload(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		}
	}
	
	//molis phre epituxws tin apantish
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		//an den einai logged in o xrhsths
		if ((response.getStatusCode() != 200) || (response.getText().isEmpty()))
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.upload(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));	
		Document.get().getBody().addClassName("bodyClass");
		//Apo to DOM prosthetei komvo (to header me olous tous upokomvous pou exei mesa)
		Document.get().getBody().appendChild(Header.newHeader());
		final FlowPanel flowPanel = new FlowPanel();
		int i = 0;
		final InlineLabel fileLabel = new InlineLabel("File");
		fileLabel.getElement().addClassName("label");
		fileLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(fileLabel);
		flowPanel.add(file);
		flowPanel.getElement().appendChild(Document.get().createBRElement()); //<br />
		final InlineLabel titleLabel = new InlineLabel("Title");
		titleLabel.getElement().addClassName("label");
		titleLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(titleLabel);
		flowPanel.add(title);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		final InlineLabel publicLabel = new InlineLabel("Public");
		publicLabel.getElement().addClassName("label");
		publicLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(publicLabel);
		flowPanel.add(publik);
		flowPanel.getElement().appendChild(Document.get().createBRElement());
		final InlineLabel latitudeLongitudeLabel = new InlineLabel("Latitude/Longitude");
		latitudeLongitudeLabel.getElement().addClassName("label");
		latitudeLongitudeLabel.getElement().setAttribute("style", "top: " + (TOP_STEP * (i++)) + "px;");
		flowPanel.add(latitudeLongitudeLabel);
		flowPanel.add(new Hidden("locale", LocaleInfo.getCurrentLocale().getLocaleName()));
		flowPanel.add(ok);
		flowPanel.add(reset);
		form.add(flowPanel); //giati h forma pairnei ena pragma
		RootPanel.get().add(form);
	}
}
