package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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

import com.google.gson.Gson;

import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.media.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.server.ExtendedMediaService;
import gr.uoa.di.std08169.mobile.media.share.shared.media.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaResult;
import gr.uoa.di.std08169.mobile.media.share.shared.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

public class MediaServlet extends HttpServlet {
	private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));
	private static final long serialVersionUID = 1L;
	private static final String MAP_URL = "./map.jsp?locale=%s";
	private static final String UTF_8 = "UTF-8";
	private static final String APPLICATION_JSON = "application/json";
	private static final String PHOTO_PREFIX = "data:image/png;base64,";
	private static final String ON = "on";
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
			//agnwstos xrhsths (den exei kanei login)
			LOGGER.warning("Authentication required");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"); //401 Unauthorized
			return;
		}
		try {
			//elenxos oti o xrhsths einai gnwstos (oti uparxei stin vash)  
			final User currentUser = userService.getUser((String) request.getSession().getAttribute("email"));
			if (currentUser == null) {
				//agnwstos xrhsths (den uparxei)
				LOGGER.warning("Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;
			}
			final String action = request.getParameter("action");
			if (action == null) {
				LOGGER.warning("Bad request");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
				return;
			}
			if (action.equals("getList")) { //Get list gia otan to android xtupaei mesw REST gia tin Map
				final String title = request.getParameter("title");
				final MediaType type = (request.getParameter("type") == null) ? null :
					MediaType.values()[Integer.parseInt(request.getParameter("type"))];
				final String user = request.getParameter("user");
				final Date createdFrom = (request.getParameter("createdFrom") == null) ? null :
					new Date(Long.parseLong(request.getParameter("createdFrom")));
				final Date createdTo = (request.getParameter("createdTo") == null) ? null :
					new Date(Long.parseLong(request.getParameter("createdTo")));
				final Date editedFrom = (request.getParameter("editedFrom") == null) ? null :
					new Date(Long.parseLong(request.getParameter("editedFrom")));
				final Date editedTo = (request.getParameter("editedTo") == null) ? null :
					new Date(Long.parseLong(request.getParameter("editedTo")));
				final Boolean publik = (request.getParameter("public") == null) ? null :
					Boolean.parseBoolean(request.getParameter("public"));
				final BigDecimal minLatitude = (request.getParameter("minLatitude") == null) ? null :
					new BigDecimal(request.getParameter("minLatitude"));
				final BigDecimal minLongitude = (request.getParameter("minLongitude") == null) ? null :
					new BigDecimal(request.getParameter("minLongitude"));
				final BigDecimal maxLatitude = (request.getParameter("maxLatitude") == null) ? null :
					new BigDecimal(request.getParameter("maxLatitude"));
				final BigDecimal maxLongitude = (request.getParameter("maxLongitude") == null) ? null :
					new BigDecimal(request.getParameter("maxLongitude"));

				final List<Media> media = mediaService.getMedia(currentUser, title, type, user, createdFrom, 
						createdTo, editedFrom, editedTo, publik, minLatitude, minLongitude, maxLatitude, maxLongitude);
				response.setCharacterEncoding(UTF_8);
				//tupos apantishs
				response.setContentType(APPLICATION_JSON);
				//Egrafh stin apantish tin lista me ta Media ws JSON (me xrhsh tou Google Gson)
				final Writer writer = response.getWriter();
				try {
					writer.write(new Gson().toJson(media));
				} finally {
					writer.close();
				}
			} else if (action.equals("getResult")) { //Get result gia otan to android xtupaei mesw REST gia tin List
				final String title = request.getParameter("title");
				final MediaType type = (request.getParameter("type") == null) ? null :
					MediaType.values()[Integer.parseInt(request.getParameter("type"))];
				final String user = request.getParameter("user");
				final Date createdFrom = (request.getParameter("createdFrom") == null) ? null :
					new Date(Long.parseLong(request.getParameter("createdFrom")));
				final Date createdTo = (request.getParameter("createdTo") == null) ? null :
					new Date(Long.parseLong(request.getParameter("createdTo")));
				final Date editedFrom = (request.getParameter("editedFrom") == null) ? null :
					new Date(Long.parseLong(request.getParameter("editedFrom")));
				final Date editedTo = (request.getParameter("editedTo") == null) ? null :
					new Date(Long.parseLong(request.getParameter("editedTo")));
				final Boolean publik = (request.getParameter("public") == null) ? null :
					Boolean.parseBoolean(request.getParameter("public"));
				
				final Integer start = (request.getParameter("start") == null) ? null :
					Integer.parseInt(request.getParameter("start"));
				final Integer length = (request.getParameter("length") == null) ? null :
					Integer.parseInt(request.getParameter("length"));
				final String orderField = (request.getParameter("orderField") == null) ? null : 
					request.getParameter("orderField");
				final boolean ascending = (request.getParameter("ascending") == null) ? false :
					Boolean.parseBoolean(request.getParameter("ascending"));
				
				final MediaResult mediaResult = mediaService.getMedia(currentUser, title, type, user, createdFrom,
						createdTo, editedFrom, editedTo, publik, start, length, orderField, ascending);
				response.setCharacterEncoding(UTF_8);
				response.setContentType(APPLICATION_JSON);
				
				final Writer writer = response.getWriter();
				try {
					writer.write(new Gson().toJson(mediaResult));
				} finally {
					writer.close();
				}

			} else if (action.equals("getMedia")) { //Get enos media gia otan to android xtupaei mesw REST gia tin View Media
				final String id = request.getParameter("id");
				if (id == null) {
					LOGGER.warning("Bad request");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
					return;
				}
				final Media media = mediaService.getMedia(id);
				
				response.setCharacterEncoding(UTF_8);
				response.setContentType(APPLICATION_JSON);
				
				final Writer writer = response.getWriter();
				try {
					writer.write(new Gson().toJson(media));
				} finally {
					writer.close();
				}
			} else if (action.equals("downloadMedia")) { //gia download enos media
				final String id = request.getParameter("id");
				if (id == null) {
					LOGGER.warning("Bad request");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
					return;
				}
				final Media media = mediaService.getMedia(id);
				if ((currentUser.getStatus() == UserStatus.ADMIN) || currentUser.equals(media.getUser()) || media.isPublic())
					mediaService.getMedia(id, response);
				else {
					//Den exei dikaioma autos o xrhsths na katevasei auto to media
					LOGGER.warning("Access denied");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
					return;				
				}
			}
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Error retrieving media", e); //den borese na psaxei gia to arxeio
			throw new ServletException("Error retrieving media", e); //Internal Server Error 500
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
			final String action = request.getParameter("action");
			if ("editMedia".equals(action)) { //TODO edit media gia post apo android
				try {
					final String id = request.getParameter("id");
					final String title = request.getParameter("title");
					final Boolean publik = (request.getParameter("public") == null) ? null :
						Boolean.parseBoolean(request.getParameter("public"));
					final BigDecimal latitude = (request.getParameter("latitude") == null) ? null : 
						new BigDecimal(request.getParameter("latitude"));
					final BigDecimal longitude = (request.getParameter("longitude") == null) ? null : 
						new BigDecimal(request.getParameter("longitude"));
					if (id == null) {
						LOGGER.warning("Bad request");
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request"); //400 Bad request
						return;
					}
					final Media media = mediaService.getMedia(id);
					if (user.getEmail().equals(media.getUser().getEmail()) || user.getStatus() == UserStatus.ADMIN) {
						if (title != null)
							media.setTitle(title);
						if (publik != null)
							media.setPublic(publik);
						if ((latitude != null) && (longitude != null)) {
							media.setLatitude(latitude);
							media.setLongitude(longitude);
						}
						media.setEdited(new Date());
						mediaService.editMedia(media);
						
						//return se json ton user
						response.setCharacterEncoding(UTF_8);
						//tupos apantishs
						response.setContentType(APPLICATION_JSON);
						//Egrafh stin apantish tin lista me ta Media ws JSON (me xrhsh tou Google Gson)
						final Writer writer = response.getWriter();
						try {
							writer.write(new Gson().toJson("Media " + media.getTitle() + " edited succesfully"));
						} finally {
							writer.close();
						}	
					} else {
						//Den exei dikaioma autos o xrhsths na epexergastei auto to media
						LOGGER.warning("Access denied");
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
						return;	
					}
				} catch (final MediaServiceException e) {
					LOGGER.log(Level.WARNING, "Error retrieving media", e); //den borese na psaxei gia to arxeio
					throw new ServletException("Error retrieving media", e); //Internal Server Error 500
				}
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
				} else if ((!fileItem.isFormField()) && fileItem.getFieldName().equals("video")) { //vrethike video blob
					input = fileItem.getInputStream();
					id = UUID.randomUUID().toString();
					type = fileItem.getContentType();
					size = fileItem.getSize();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("title")) {
					//vrethike to title
					title = fileItem.getString(UTF_8);
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("duration")) {
					duration = Integer.parseInt(fileItem.getString(UTF_8));
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("public")) {
					publik = Boolean.parseBoolean(fileItem.getString(UTF_8)) ||
							//Html gia checkboxes, radiobuttons, images:
							//otan einai tsekarismena epistrefei to value.
							//otan den einai, epistrefei null.
							//an einai value="timh" to value einai h timh
							//an den uparxei value, default einai to "on"
							ON.equalsIgnoreCase(fileItem.getString(UTF_8));
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
			if (locale == null)
				response.getWriter().flush(); //apantaei me 200 OK
			else
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
	
	/**
	 * Delete enos media apo android
	 */
	@Override
	public void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		//elenxos oti uparxei email sto session
		if ((String) request.getSession().getAttribute("email") == null) {
			//agnwstos xrhsths (den exei kanei login)
			LOGGER.warning("Authentication required");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"); //401 Unauthorized
			return;
		}
		try {
			//elenxos oti o xrhsths einai gnwstos (oti uparxei stin vash)  
			final User currentUser = userService.getUser((String) request.getSession().getAttribute("email"));
			if (currentUser == null) {
				//agnwstos xrhsths (den uparxei)
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
			final Media media = mediaService.getMedia(id);
			if (currentUser.getEmail().equals(media.getUser().getEmail()) || (currentUser.getStatus() == UserStatus.ADMIN)) {
				mediaService.deleteMedia(id);
				//return se json ton user
				response.setCharacterEncoding(UTF_8);
				//tupos apantishs
				response.setContentType(APPLICATION_JSON);
				//den exoume tipota na poume, apla paei 200 OK
				response.getWriter().close();
			} else {
				//Den exei dikaioma autos o xrhsths na diagrapsei auto to media
				LOGGER.warning("Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;	
			}
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request", e);
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Access denied", e);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
		}
	}
}
