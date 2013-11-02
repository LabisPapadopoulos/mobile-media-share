package gr.uoa.di.std08169.mobile.media.share.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.context.support.WebApplicationContextUtils;

import gr.uoa.di.std08169.mobile.media.share.client.services.MediaService;
import gr.uoa.di.std08169.mobile.media.share.client.services.MediaServiceException;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.User;

public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(UploadServlet.class.getName());
	private static final int BUFFER_SIZE = 1024;
	//tmp directory me to opoio exei xekinhsei h JVM kai ekei tha dexetai o tomcat ta arxeia pou anevazoun oi xrhstes
	private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	private int maxFileSize;
	//Pou tha apothikeutei monima to arxeio
	private File mediaDir;
	private MediaService mediaService; //Java Bean
	private UserService userService;
	
	@Override
	public void init() {
		maxFileSize  = (Integer) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("maxFileSize", Integer.class);
		mediaDir = (File) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
					getBean("mediaDir", File.class);
		mediaService = (MediaService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("mediaService", MediaServiceImpl.class);
		userService = (UserService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).
				getBean("userService", UserServiceImpl.class);
	}
	
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		if ((String) request.getSession().getAttribute("email") == null) {
			LOGGER.log(Level.WARNING, "Authentication required");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required"); //401 Unauthorized
			return;
		}
		try {
			final User user = userService.getUser((String) request.getSession().getAttribute("email"));
			if (user == null) {
				LOGGER.log(Level.WARNING, "Access denied");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
				return;
			}
			if (!ServletFileUpload.isMultipartContent(request)) {
				LOGGER.log(Level.WARNING, "Request content type is not multipart/form-data");
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
			for (FileItem fileItem : new ServletFileUpload(new DiskFileItemFactory(maxFileSize, TMP_DIR)).parseRequest(request)) {
				//an einai arxeio
				if ((!fileItem.isFormField()) && fileItem.getFieldName().equals("file")) {
					//vrethike ena arxeio
					final InputStream input = fileItem.getInputStream();
					try {
						//Monadiko anagnwristiko gia otidhpote theloume
						id = UUID.randomUUID().toString();
						final File file = new File(mediaDir, id);
						file.createNewFile();
						try {
							final FileOutputStream output = new FileOutputStream(file);
							try {
								final byte[] buffer = new byte[BUFFER_SIZE];
								int read = 0;
								while((read = input.read(buffer)) > 0)
									output.write(buffer, 0, read);								
							} finally {
								output.close();
							}
						//otidhpote borei na ginei throw (akoma kai error)
						} catch (final IOException e) {
							file.delete();
							throw e;
						}
						type = fileItem.getContentType();
						size = fileItem.getSize();
					} finally {
						input.close();
					}
					//diagrafh apo ton disko
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("title")) {
					//vrethike to title
					title = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("public")) {
					publik = "on".equals(fileItem.getString());
					fileItem.delete();
				}
			}
			latitude = new BigDecimal(0);
			longitude = new BigDecimal(0);
			
			
			//Den anevase o xrhsths arxeio
			if ((id == null) || (type == null) || (size == 0)) {
				LOGGER.log(Level.WARNING, "No file specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file specified"); //400 Bad Request
				return;	
			}
			if (title == null) {
				LOGGER.log(Level.WARNING, "No title specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No title specified"); //400 Bad Request
				return;
			}
			if((latitude == null) || (longitude == null)) {
				LOGGER.log(Level.WARNING, "No location specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No location specified"); //400 Bad Request
				return;
			}
			mediaService.addMedia(new Media(id, type, size, duration, user, created, edited, title, latitude,
					longitude, publik));			
		} catch (final FileUploadException e) {
			LOGGER.log(Level.WARNING, "Error parsing request");
			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
		} catch (final MediaServiceException e) {
			LOGGER.log(Level.WARNING, "Error executing request");
			throw new ServletException("Error executing request", e); //Epistrefei 500 ston client
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Access denied");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); //403 Forbidden
		}
	}
}
