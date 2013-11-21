package gr.uoa.di.std08169.mobile.media.share.client.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.uoa.di.std08169.mobile.media.share.shared.User;
import gr.uoa.di.std08169.mobile.media.share.shared.UserResult;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class UserOracle extends SuggestOracle {
	
	private static final UserServiceAsync USER_SERVICE = 
			GWT.create(UserService.class);
	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		
		USER_SERVICE.getUsers(request.getQuery(), request.getLimit(), new AsyncCallback<UserResult>() {
			//Den borese na psaxei gia xrhstes
			@Override
			public void onFailure(final Throwable throwable) {
				final Response response = new Response();
				//gemizei me kenh lista tin apantish
				response.setSuggestions(Collections.<UserSuggestion>emptyList());
				//den exei alles protaseis
				response.setMoreSuggestionsCount(0);
				callback.onSuggestionsReady(request, response);
			}
			
			@Override
			public void onSuccess(final UserResult result) {
				//apantish pou tha tou sthleis gi' auto pou rwthse
				final Response response = new Response();
				final List<UserSuggestion> users = new ArrayList<UserSuggestion>();
				for(User user : result.getUsers())
					users.add(new UserSuggestion(user));
				//dinei tin lista me tous proteinomenous xrhstes
				response.setSuggestions(users);
				//dinei posa epipleon apotelesmata uparxoun
				response.setMoreSuggestionsCount(result.getTotal() - result.getUsers().size());
				//Dinei apantish sto erwthma mesw tou callback kai gemizei to suggestBox
				callback.onSuggestionsReady(request, response);
			}
		});
		
		
	}

}
