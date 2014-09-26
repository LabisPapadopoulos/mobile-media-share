package gr.uoa.di.std08169.mobile.media.share.android.media;

import gr.uoa.di.std08169.mobile.media.share.android.R;

/**
 * Created by labis on 7/14/14.
 */
public enum MediaType {
    APPLICATION("application/", R.drawable.application, R.drawable.application_large),
    AUDIO("audio/", R.drawable.audio, R.drawable.audio_large),
    IMAGE("image/", R.drawable.image, R.drawable.image_large),
    TEXT("text/", R.drawable.text, R.drawable.text_large),
    VIDEO("video/", R.drawable.video, R.drawable.video_large);

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

    public int getLargeDrawable() {
        return largeDrawable;
    }

    private final String mimeTypePrefix;

    private final int drawable;

    private final int largeDrawable;

    private MediaType(final String mimeTypePrefix, final int drawable, final int largeDrawable) {
        this.mimeTypePrefix = mimeTypePrefix;
        this.drawable = drawable;
        this.largeDrawable = largeDrawable;
    }
}
