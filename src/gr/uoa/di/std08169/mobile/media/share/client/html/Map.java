package gr.uoa.di.std08169.mobile.media.share.client.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Map implements EntryPoint, RequestCallback {

	@Override
	public void onModuleLoad() {
		try {
			//RequestBuilder gia na kanoume ena GET request sto servlet login gia na paroume
			//to session mas. RequestCallback (this) einai auto pou tha parei tin apantish asunxrona
			new RequestBuilder(RequestBuilder.GET, "./login").sendRequest(null, this);
		} catch (final RequestException _) {
			//otidhpote paei strava, xana gurnaei stin login
			Window.Location.assign("./login.html");
		}
	}

	@Override
	public void onError(final Request _, final Throwable __) {
		Window.Location.assign("./login.html");
	}

	//molis phre epituxws tin apantish
	@Override
	public void onResponseReceived(final Request request, final Response response) {
		if ((response.getStatusCode() != 200) || (response.getText().isEmpty()))
			Window.Location.assign("./login.html");
		RootPanel.get().add(new Label("Edw tha bei enas Xarths"));
	}
}
