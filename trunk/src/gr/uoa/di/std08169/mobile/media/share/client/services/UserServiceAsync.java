package gr.uoa.di.std08169.mobile.media.share.client.services;

import gr.uoa.di.std08169.mobile.media.share.shared.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

//Client gia UserService interface
//Async: H apantish borei na arghsei na erthei giati 
//h methodos tha epistrepsei prin teleiwsei to apotelesma tis
public interface UserServiceAsync {
	//AsyncCallback<Boolean>: Otan erthei o Boolean pou perimenoume, tote tha 
	//trexei to callback (kathisterimenh ektelesh logo diktuou)
	//Einai void giati den epistrefoun kati tin stigmh pou kalountai, tha
	//epistrepsoun me to AsyncCallback argotera
	public void getUser(final String email, final AsyncCallback<User> callback);
	public void isValidUser(final String email, final String password, final AsyncCallback<Boolean> callback);
	public void addUser(final String email, final String password, final AsyncCallback<Boolean> callback);
}
