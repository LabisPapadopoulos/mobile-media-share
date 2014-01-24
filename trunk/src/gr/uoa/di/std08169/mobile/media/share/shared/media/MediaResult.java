package gr.uoa.di.std08169.mobile.media.share.shared.media;

import java.util.List;

public class MediaResult {
	private final List<Media> media;
	private final int total;
	
	public MediaResult(final List<Media> media, final int total) {
		this.media = media;
		this.total = total;
	}

	public List<Media> getMedia() {
		return media;
	}

	public int getTotal() {
		return total;
	}
}
