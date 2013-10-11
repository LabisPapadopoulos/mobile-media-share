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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import gr.uoa.di.std08169.mobile.media.share.client.i18n.MobileMediaShareConstants;

public class Upload implements ChangeHandler, ClickHandler, EntryPoint, KeyUpHandler, RequestCallback {
	private static final MobileMediaShareConstants MOBILE_MEDIA_SHARE_CONSTANTS = 
			GWT.create(MobileMediaShareConstants.class);
	private static final MobileMediaShareUrls MOBILE_MEDIA_SHARE_URLS = 
			GWT.create(MobileMediaShareUrls.class);
	
	private final FormPanel form;
	private final FileUpload file;
	private final TextBox title;
//	private final GoogleMap map; //[Latitude, Longitude]
	private final CheckBox publik;
	private final Button ok;
	private final Button reset;
	
	public Upload() {
		form = new FormPanel();
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setAction("./upload");
		file = new FileUpload();
		file.setName("file");
		//enhmerwnei otan allaxei h timh tou
		file.addChangeHandler(this);
		title = new TextBox();
		title.setName("title");
		title.addKeyUpHandler(this);
		publik = new CheckBox();
		publik.setName("public");
		ok = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.ok());
		ok.addClickHandler(this);
		ok.setEnabled(false);
		reset = new Button(MOBILE_MEDIA_SHARE_CONSTANTS.reset());
		reset.addClickHandler(this);
	}
	
	@Override
	public void onClick(final ClickEvent clickEvent) {
		Window.alert("on click");
		if (clickEvent.getSource() == ok) {
			
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
		Window.alert("on change");
														//to onoma tou arxeiou na mhn einai keno
		ok.setEnabled((file.getFilename() != null) && (!file.getFilename().trim().isEmpty()) &&
				(!title.getValue().trim().isEmpty()));		
	}

	@Override
	public void onError(final Request _, final Throwable __) {
		Window.alert("on error");
		Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
				//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
				URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
				//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
				URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.upload(
						URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
	}

	@Override
	public void onKeyUp(final KeyUpEvent _) {
		Window.alert("on key up");
		ok.setEnabled((file.getFilename() != null) && (!file.getFilename().trim().isEmpty()) &&
				(!title.getValue().trim().isEmpty()));		
	}

	@Override
	public void onModuleLoad() {
		Window.alert("on module load");
		try {
			//RequestBuilder gia na kanoume ena GET request sto servlet login gia na paroume
			//to session mas. RequestCallback (this) einai auto pou tha parei tin apantish asunxrona
			new RequestBuilder(RequestBuilder.GET, "./login").sendRequest(null, this);
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
		Window.alert("on response received (status: " + response.getStatusCode() + ", text: " + response.getText() + ")");
		//an den einai logged in o xrhsths
		if ((response.getStatusCode() != 200) || (response.getText().isEmpty()))
			Window.Location.assign(MOBILE_MEDIA_SHARE_URLS.login(
					//encodeQueryString: Kwdikopoiei to localeName san parametro gia queryString enos url
					URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName()),
					//kwdikopoieitai to url map epeidh pernaei san parametros (meta apo ?)
					URL.encodeQueryString(MOBILE_MEDIA_SHARE_URLS.upload(
							URL.encodeQueryString(LocaleInfo.getCurrentLocale().getLocaleName())))));
		//Apo to DOM prosthetei komvo (to header me olous tous upokomvous pou exei mesa)
		Document.get().getBody().appendChild(Header.newHeader());
		
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new Label("File"));
		flowPanel.add(file);
		flowPanel.add(new Label("Title"));
		flowPanel.add(title);
		flowPanel.add(new Label("Public"));
		flowPanel.add(publik);
		flowPanel.add(new Label("Latitude/Longitude"));
		flowPanel.add(ok);
		flowPanel.add(reset);
		
		form.add(flowPanel); //giati h forma pairnei ena pragma
		RootPanel.get().add(form);
	}
}
