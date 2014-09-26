package gr.uoa.di.std08169.mobile.media.share.client.services.download;

public class DownloadServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	public DownloadServiceException(final String message, final Throwable cause) {
		super(message, cause);
		if (message == null)
			throw new IllegalArgumentException(DownloadServiceException.class.getName() + " message can not be null");
		if (message.isEmpty())
			throw new IllegalArgumentException(DownloadServiceException.class.getName() + " message can not be empty");
		if (cause == null)
			throw new IllegalArgumentException(DownloadServiceException.class.getName() + " cause can not be null");
	}
}
