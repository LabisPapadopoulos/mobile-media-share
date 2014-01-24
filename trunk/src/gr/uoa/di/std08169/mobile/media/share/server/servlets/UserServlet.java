package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;

import java.io.IOException;
import java.net.URLEncoder;
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

//Login Servlet
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LOGIN_URL = "./login.html?locale=%s&url=%s";
	private static final String UTF_8 = "UTF-8"; 
	private static final Logger LOGGER = Logger.getLogger(UserServlet.class.getName()); 

	private UserService userService; //Java Bean
	
	//init gia to servlet
	@Override
	public void init() {
		//pairnei to xml tou spring
		//WebApplicationContextUtils: gia na pairnei application contexts
		//getServletContext(): pairnei to web.xml
		//getWebApplicationContext: pairnei to context pou orizetai sto applicationContext.xml
		//ftiaxnei ena aplication context sumfwna me auta pou orizontai sto web.xml
		//Pairnei ena pragma (bean) pou to lene userService gia na kanei douleies gia xrhstes
		//(to opoio to userService mhlaei me tin bash).
		userService = (UserService) WebApplicationContextUtils.
				getWebApplicationContext(getServletContext()).getBean("userService", UserService.class);
	}
	
	/**
	 * Log in users.
	 */
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		//Elegxos an to request einai multipart form data
		if (!ServletFileUpload.isMultipartContent(request)) {
			LOGGER.warning("Request content type is not multipart/form-data");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request content type is not multipart/form-data"); //400 Bad Request
			return;
		}
		//DiskFileItemFactoryapothikeuei oti erthei se multipart form data sto disko
		//ServletFileUpload: parsarei to request gia na xexwrisei ta fileItems kai ta apothikeuei sto DiskFile..
		//FileItem: To kathe part tou multi part
		try {
			String email = null;
			String password = null;
			String locale = null;
			String url = null;
			for (FileItem fileItem : new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request)) {
				//an einai pedio formas kai oxi arxeia
				if (fileItem.isFormField() && fileItem.getFieldName().equals("email")) {
					//vrethike to e-mail
					email = fileItem.getString();
					//diagrafh apo ton disko
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("password")) {
					//vrethike to password
					password = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("locale")) {
					locale = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("url")) {
					url = fileItem.getString();
					fileItem.delete();
				}
			}
			if (email == null) {
				LOGGER.warning("No email specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No email specified"); //400 Bad Request
				return;
			}
			if (password == null) {
				LOGGER.warning("No password specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No password specified"); //400 Bad Request
				return;
			}
			try {
				if (userService.isValidUser(email, password)) {
					//apothikeush tou xrhsth sto session pou einai ston server
					request.getSession().setAttribute("email", email);
					LOGGER.info("User " + email + " logged in successfully");
					response.sendRedirect(url);
				} else {
					LOGGER.info("User " + email + " entered invalid credentials");
					response.sendRedirect(String.format(LOGIN_URL, URLEncoder.encode(locale, UTF_8),
							URLEncoder.encode(url, UTF_8)));
				}
			} catch (final UserServiceException e) {
				LOGGER.log(Level.WARNING, "Error validating user " + email, e);
				throw new ServletException("Error validating user " + email, e); //Epistrefei 500 ston client
			}
		} catch (final FileUploadException e) {
			LOGGER.log(Level.WARNING, "Error parsing request", e);
			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
		}
	}
}
