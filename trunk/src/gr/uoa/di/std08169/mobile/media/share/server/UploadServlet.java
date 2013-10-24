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

import gr.uoa.di.std08169.mobile.media.share.shared.Media;
import gr.uoa.di.std08169.mobile.media.share.shared.User;

public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(UploadServlet.class.getName());
	private static final int BUFFER_SIZE = 1024;

	public static void main(final String[] arguments) {
		throw new Error();
	}
	
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			LOGGER.log(Level.WARNING, "Request content type is not multipart/form-data");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request content type is not multipart/form-data"); //400 Bad Request
			return;
		}
		//DiskFileItemFactoryapothikeuei oti erthei se multipart form data sto disko
		//ServletFileUpload: parsarei to request gia na xexwrisei ta fileItems kai ta apothikeuei sto DiskFile..
		//FileItem: To kathe part tou multi part
		try {
			// file
			String id = null;
			String type = null;
			long size = 0;
			int duration = 0;
// TODO		final User user = userService.getUser(request.getSession().getAttribute("email"));
			final User user = null;
			final Date created = new Date();
			final Date edited = created;
			String title = null;
			BigDecimal latitude = null;
			BigDecimal longitude = null;
			boolean publik = false;
			for (FileItem fileItem : new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request)) { // TODO size from config
																												// dir = tmp
				//an einai arxeio
				if ((!fileItem.isFormField()) && fileItem.getFieldName().equals("file")) {
					//vrethike ena arxeio
					final InputStream input = fileItem.getInputStream();
					try {
						//Monadiko anagnwristiko gia otidhpote theloume
						id = UUID.randomUUID().toString();
						final File file = new File("/var/media", id); // TODO dir from config
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
			
			// TODO mediaService.addMedia(new Media(id, type, size, duration, user, created, edited, title, latitude,
			// longitude, publik));
			
		} catch (final FileUploadException e) {
			LOGGER.log(Level.WARNING, "Error parsing request");
			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
		}
	}
}
