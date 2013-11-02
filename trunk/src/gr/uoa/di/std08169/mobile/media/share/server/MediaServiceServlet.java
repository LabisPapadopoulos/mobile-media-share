package gr.uoa.di.std08169.mobile.media.share.server;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.Media;

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
	public void addMedia(final Media media) throws MediaServiceException {
		mediaService.addMedia(media);
	}
	
}
