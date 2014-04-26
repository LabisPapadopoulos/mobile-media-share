package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.context.support.WebApplicationContextUtils;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;

public class MediaServlet extends HttpServlet {
	private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));
	private static final long serialVersionUID = 1L;
	private static final String MAP_URL = "./map.jsp?locale=%s";
	private static final String UTF_8 = "UTF-8";
	private static final String PHOTO_PREFIX = "data:image/png;base64,";
	private static final Logger LOGGER = Logger.getLogger(MediaServlet.class.getName());
	//tmp directory me to opoio exei xekinhsei h JVM kai ekei tha dexetai o tomcat ta arxeia pou anevazoun oi xrhstes

	//Service gia apostolh twn dedomenwn tou media
	private ExtendedMediaService mediaService; //Java Bean
	private int bufferSize;
	private UserService userService;
	
	/**
	 * Arxikopoiei ta java beans me xrhsh Spring
	 * Einai dunaton na peiraxtoun exwterika mesw enos property file.
	 */
	@Override
	public void init() {
		mediaService = (ExtendedMediaService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("mediaService", MediaService.class);
		bufferSize = (Integer) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("bufferSize", Integer.class);
		userService = (UserService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("userService", UserService.class);
	}
	
	/**
	 * Katevazei ena arxeio
	 */
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		//elenxos oti uparxei email sto session
		if ((String) request.getSession().getAttribute("email") == null) {
			LOGGER.warning("Authentication required");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"); //401 Unauthorized
			return;
		}
		try {
			//elenxos oti o xrhsths einai gnwstos (oti uparxei stin vash)  
			final User user = userService.getUser((String) request.getSession().getAttribute("email"));
			if (user == null) {
				LOGGER.warning("Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;
			}
			final String id = request.getParameter("id");
			if (id == null) {
				LOGGER.warning("Bad request");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
				return;
			}
			mediaService.getMedia(id, response);
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Internal server error", e); //den borese na psaxei gia to arxeio
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error"); //500 Internal server error
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Access denied", e);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
		}
	}
	
	/**
	 * Anevazei ena arxeio kai kanei redirect sto map.jsp
	 */
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		if ((String) request.getSession().getAttribute("email") == null) {
			LOGGER.warning("Authentication required");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"); //401 Unauthorized
			return;
		}
		try {
			final User user = userService.getUser((String) request.getSession().getAttribute("email"));
			if (user == null) {
				LOGGER.warning("Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;
			}
			if (!ServletFileUpload.isMultipartContent(request)) {
				LOGGER.warning("Request content type is not multipart/form-data");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request content type is not multipart/form-data"); //400 Bad Request
				return;
			}
			//DiskFileItemFactory apothikeuei oti erthei se multipart form data sto disko
			//ServletFileUpload: parsarei to request gia na xexwrisei ta fileItems kai ta apothikeuei sto DiskFile..
			//FileItem: To kathe part tou multi part
			// file
			String id = null;
			String type = null;
			long size = 0;
			int duration = 0;
			final Date created = new Date();
			final Date edited = created;
			String title = null;
			BigDecimal latitude = null;
			BigDecimal longitude = null;
			boolean publik = false;
			String locale = null;
			InputStream input = null;
			//Diathetei mnhmh gia metafora arxeiou mexri kai BUFFER_SIZE bytes. Apo ekei kai panw xrhsimopoiei disko.
			final List<FileItem> fileItems = new ServletFileUpload(new DiskFileItemFactory(bufferSize, TMP_DIR)).parseRequest(request); 
			for (FileItem fileItem : fileItems) {
				//an einai arxeio
				if ((!fileItem.isFormField()) && fileItem.getFieldName().equals("file")) {
					//vrethike ena arxeio
					input = fileItem.getInputStream();
					id = UUID.randomUUID().toString();
					type = fileItem.getContentType();
					size = fileItem.getSize();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("photo")) {
					if (!fileItem.getString(UTF_8).startsWith(PHOTO_PREFIX)) {
						LOGGER.warning("Photo is not image/png");
						response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Photo is not image/png"); //415 UNSUPPORTED MEDIA TYPE
						return;
					}
					//apokwdikopoihsh tis eikonas
					final byte[] data = Base64.decodeBase64(fileItem.getString(UTF_8).substring(PHOTO_PREFIX.length()));
					//antigrafh ths eikonas apo enan pinaka apo bytes
					input = new ByteArrayInputStream(data);
					id = UUID.randomUUID().toString();
					type = "image/png";
					size = data.length;
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("title")) {
					//vrethike to title
					title = fileItem.getString(UTF_8);
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("public")) {
					publik = "on".equals(fileItem.getString(UTF_8));
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("latitude")) {
					latitude = new BigDecimal(fileItem.getString(UTF_8));
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("longitude")) {
					longitude = new BigDecimal(fileItem.getString(UTF_8));
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("locale")) {
					locale = fileItem.getString(UTF_8);
				}
			}
			//Den anevase o xrhsths arxeio
			if ((id == null) || (type == null) || (size == 0) || (input == null)) {
				LOGGER.warning("No file specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file specified"); //400 Bad Request
				if (input != null)
					input.close();
				for (FileItem fileItem : fileItems)
					fileItem.delete();
				return;
			}
			if (title == null) {
				LOGGER.warning("No title specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No title specified"); //400 Bad Request
				if (input != null)
					input.close();
				for (FileItem fileItem : fileItems)
					fileItem.delete();
				return;
			}
			if((latitude == null) || (longitude == null)) {
				LOGGER.warning("No location specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No location specified"); //400 Bad Request
				if (input != null)
					input.close();
				for (FileItem fileItem : fileItems)
					fileItem.delete();
				return;
			}
			try {
				mediaService.addMedia(new Media(id, type, size, duration, user, created, edited, title, latitude,
						longitude, publik), input);
			} finally {
				input.close();
				for (FileItem fileItem : fileItems)
					fileItem.delete();
			}
			LOGGER.info("User " + user + " uploaded media " + id);
			response.sendRedirect(String.format(MAP_URL, URLEncoder.encode(locale, UTF_8)));
		} catch (final FileUploadException e) {
			LOGGER.log(Level.WARNING, "Error parsing request", e);
			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Access denied", e);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
		}
	}	
}
