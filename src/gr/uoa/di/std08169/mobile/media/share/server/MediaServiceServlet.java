package gr.uoa.di.std08169.mobile.media.share.server;

import java.util.Date;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.MediaType;

//* extends RemoteServiceServlet: gia na borei na kaleitai mesw diktuou 
//(Gia na ulopoihthei to UserServiceAsync)
//* implements UserService: gia na sumperiferetai san userService
//GWT servlet
public class MediaServiceServlet extends RemoteServiceServlet implements MediaService {
	private static final long serialVersionUID = 1L;

	private MediaService mediaService;
	

	//init gia to servlet
	@Override
	public void init() {
		//pairnei to xml tou spring
		//WebApplicationContextUtils: gia na pairnei application contexts
		//getServletContext(): pairnei to web.xml
		//getWebApplicationContext: pairnei to context pou orizetai sto applicationContext.xml
		//ftiaxnei ena aplication context sumfwna me auta pou orizontai sto web.xml
		//Pairnei ena pragma (bean) pou to lene mediaService gia na kanei douleies gia media
		//(to opoio to mediaService mhlaei me tin bash).
		mediaService = (MediaService) WebApplicationContextUtils.
				getWebApplicationContext(getServletContext()).getBean("mediaService", MediaServiceImpl.class);
	}
	
	@Override
	public MediaResult getMedia(final String title, final MediaType type, final String user,
			final Date createdFrom, final Date createdTo, final Date editedFrom, final Date editedTo,
			final Boolean publik, final Integer start, final Integer length, final String orderField,
			final boolean ascending) throws MediaServiceException {
		return mediaService.getMedia(title, type, user, createdFrom, createdTo, editedFrom, editedTo, 
				publik, start, length, orderField, ascending);
	}


	@Override
	public void addMedia(final Media media) throws MediaServiceException {
		mediaService.addMedia(media);
	}	
}
