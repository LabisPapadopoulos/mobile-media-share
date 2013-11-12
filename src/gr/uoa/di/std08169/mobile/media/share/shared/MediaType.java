package gr.uoa.di.std08169.mobile.media.share.shared;

public enum MediaType {
	APPLICATION("application/"),
	AUDIO("audio/"),
	IMAGE("image/"),
	TEXT("text/"),
	VIDEO("video/");
	
	public static MediaType getMediaType(final String mimeType) {
		if (mimeType == null)
			return null;
		for (MediaType mediaType : MediaType.values()) {
			if (mimeType.startsWith(mediaType.mimeTypePrefix))
				return mediaType;
		}
		return null;
	}
	
	public String getMimeTypePrefix() {
		return mimeTypePrefix;
	}
	
	private final String mimeTypePrefix;
	
	private MediaType(final String mimeTypePrefix) {
		this.mimeTypePrefix = mimeTypePrefix;
	}
}
