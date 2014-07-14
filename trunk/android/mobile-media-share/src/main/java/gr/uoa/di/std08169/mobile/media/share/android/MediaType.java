package gr.uoa.di.std08169.mobile.media.share.android;

/**
 * Created by labis on 7/14/14.
 */
public enum MediaType {
    APPLICATION("application/", R.drawable.application),
    AUDIO("audio/", R.drawable.audio),
    IMAGE("image/", R.drawable.image),
    TEXT("text/", R.drawable.text),
    VIDEO("video/", R.drawable.video);

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

    public int getDrawable() {
        return drawable;
    }

    private final String mimeTypePrefix;

    private final int drawable;

    private MediaType(final String mimeTypePrefix, final int drawable) {
        this.mimeTypePrefix = mimeTypePrefix;
        this.drawable = drawable;
    }
}
