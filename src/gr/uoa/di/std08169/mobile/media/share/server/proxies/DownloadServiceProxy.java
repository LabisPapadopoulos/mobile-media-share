package gr.uoa.di.std08169.mobile.media.share.server.proxies;

import java.util.Date;

import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadService;
import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.download.Download;

//To DownloadServiceProxy (pou dhmiourgeitai apo to Spring) anathetei douleia sto
//DownloadService gia na xrhsimopoiei analoga 'h to jdbc 'h to gcd (analoga ti 
//diavase apo to property file).
public class DownloadServiceProxy implements DownloadService {
	private final DownloadService downloadService;
	
	public DownloadServiceProxy(final DownloadService downloadService) {
		this.downloadService = downloadService;
	}

	@Override
	public Download getDownload(final String token) throws DownloadServiceException {
		return downloadService.getDownload(token);
	}

	@Override
	public void addDownload(final Download download) throws DownloadServiceException {
		downloadService.addDownload(download);
	}

	@Override
	public void deleteDownloads(final Date timestampTo) throws DownloadServiceException {
		downloadService.deleteDownloads(timestampTo);
	}
}
