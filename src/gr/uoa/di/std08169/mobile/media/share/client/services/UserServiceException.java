package gr.uoa.di.std08169.mobile.media.share.client.services;

public class UserServiceException extends Exception {
	//Epeidh einai serializable h klassh kai dhlwnei tin ekdosh logismikou
	private static final long serialVersionUID = 1L;
		
	//logos tou exception
	public UserServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
