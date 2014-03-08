package gr.uoa.di.std08169.mobile.media.share.client.services.media;

public class MediaServiceException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public MediaServiceException(final String message, final Throwable cause) {
		super(message, cause);
		if (message == null)
			throw new IllegalArgumentException(MediaServiceException.class.getName() + " message can not be null");
		if (message.isEmpty())
			throw new IllegalArgumentException(MediaServiceException.class.getName() + " message can not be empty");
		if (cause == null)
			throw new IllegalArgumentException(MediaServiceException.class.getName() + " cause can not be null");
	}
	
	public MediaServiceException(final String message) {
		super(message);
		if (message == null)
			throw new IllegalArgumentException(MediaServiceException.class.getName() + " message can not be null");
		if (message.isEmpty())
			throw new IllegalArgumentException(MediaServiceException.class.getName() + " message can not be empty");
	}
}
