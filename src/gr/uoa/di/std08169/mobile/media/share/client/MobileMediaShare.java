package gr.uoa.di.std08169.mobile.media.share.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class MobileMediaShare implements EntryPoint {

	@Override
	public void onModuleLoad() {
		RootPanel.get().add(new Label("Hello world!"));
	}
}
