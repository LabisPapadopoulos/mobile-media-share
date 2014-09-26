package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadService;
import gr.uoa.di.std08169.mobile.media.share.client.services.download.DownloadServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.server.Utilities;
import gr.uoa.di.std08169.mobile.media.share.shared.download.Download;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationContextUtils;

public class DownloadServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(DownloadServlet.class.getName());
	private static final long serialVersionUID = 1L;

	private DownloadService downloadService;
	private UserService userService;
	private ExtendedMediaService mediaService;
	
	@Override
	public void init() {
		//arxikopoihsh twn services gia to sugkekrimeno servlet
		//Pairnei beans apo to spring
		downloadService = (DownloadService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("downloadService", DownloadService.class);
		mediaService = (ExtendedMediaService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("mediaService", MediaService.class);
		userService = (UserService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("userService", UserService.class);
	}
	
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		try {
			final String token = request.getParameter("token");
			if (token == null) {
				//den edwse token
				LOGGER.warning("Bad request");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
				return;
			}
			final Download download = downloadService.getDownload(token);
			if (download == null) {
				//den exei bei sta downloads gia katevasma
				LOGGER.warning("Unauthorized");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"); //401 Unauthorized
				return;
			}
			final String id = download.getMedia();
			final Media media = mediaService.getMedia(id);
			if (media == null) {
				//zhthse pragma pou den uparxei
				LOGGER.warning("Not found");
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found"); //404 Not found
				return;
			}
			final String email = download.getUser();
			final User user = userService.getUser(email);
			if (!((user != null) && //o xrhsths uparxei
					(media.isPublic() || //to media einai public
							(user.getStatus() == UserStatus.ADMIN) || //o xrhsths einai admin
											//o xrhsths einai aplos xrhsths kai tou anhkei to media
							((user.getStatus() == UserStatus.NORMAL) &&  media.getUser().equals(user))))) {
				//ton gnwrizoume alla den tou to dinoume
				LOGGER.warning("Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;
			}
			//apostolh tou media sto response
			mediaService.getMedia(id, response);
		} catch (final DownloadServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		}
	}
	
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		try {
			final String email = (String) request.getSession().getAttribute("email"); //elegxos oti o user einai hdh sundedemenos
			if (email == null) {
				LOGGER.warning("Authentication required");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"); //401 Unauthorized
				return;
			}
			final String id = request.getParameter("media");
			if (id == null) {
				//den edwse media
				LOGGER.warning("Bad request");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
				return;
			}
			final Media media = mediaService.getMedia(id);
			if (media == null) {
				//zhthse pragma pou den uparxei
				LOGGER.warning("Not found");
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found"); //404 Not found
				return;
			}
			final User user = userService.getUser(email);
			if (!((user != null) && //o xrhsths uparxei
					(media.isPublic() || //to media einai public
							(user.getStatus() == UserStatus.ADMIN) || //o xrhsths einai admin
											//o xrhsths einai aplos xrhsths kai tou anhkei to media
							((user.getStatus() == UserStatus.NORMAL) &&  media.getUser().equals(user))))) {
				//ton gnwrizoume alla den tou to dinoume
				LOGGER.warning("Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;
			}
			final String client = request.getRemoteAddr(); //pairnei tin IP tou client
			final Date timestamp = new Date();
			//dhmiourgeia token sumfwna me ta upoloipa dedomena pou phre.
			final String token = Utilities.generateToken(id, email, client, timestamp);
			downloadService.addDownload(new Download(token, id, email, client, timestamp));
			//apantaei me to token gia na katevasei to media o xrhsths
			response.getWriter().write(token);
			response.getWriter().close();
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final DownloadServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		}
	}
}
