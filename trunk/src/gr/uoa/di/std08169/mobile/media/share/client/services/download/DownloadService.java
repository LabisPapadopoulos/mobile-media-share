package gr.uoa.di.std08169.mobile.media.share.client.services.download;

import java.util.Date;

import gr.uoa.di.std08169.mobile.media.share.shared.download.Download;

public interface DownloadService {
	public Download getDownload(final String token) throws DownloadServiceException;
	public void addDownload(final Download download) throws DownloadServiceException;
	public void deleteDownloads(final Date timestampTo) throws DownloadServiceException; //diagrafei downloads prin apo to timestampTo
}
