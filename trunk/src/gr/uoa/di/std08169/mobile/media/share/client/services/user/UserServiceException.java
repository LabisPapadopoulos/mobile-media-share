package gr.uoa.di.std08169.mobile.media.share.client.services.user;

public class UserServiceException extends Exception {
	//Epeidh einai serializable h klassh kai dhlwnei tin ekdosh logismikou
	private static final long serialVersionUID = 1L;
		
	//logos tou exception
	public UserServiceException(final String message, final Throwable cause) {
		super(message, cause);
		if (message == null)
			throw new IllegalArgumentException(UserServiceException.class.getName() + " message can not be null");
		if (message.isEmpty())
			throw new IllegalArgumentException(UserServiceException.class.getName() + " message can not be empty");
		if (cause == null)
			throw new IllegalArgumentException(UserServiceException.class.getName() + " cause can not be null");
	}
}
