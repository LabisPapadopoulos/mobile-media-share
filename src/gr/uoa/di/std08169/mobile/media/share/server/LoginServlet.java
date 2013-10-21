package gr.uoa.di.std08169.mobile.media.share.server;

import gr.uoa.di.std08169.mobile.media.share.client.services.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.UserServiceException;

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
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LOGIN_URL = "./login.html?locale=%s&url=%s";
	private static final String UTF_8 = "UTF-8"; 
	private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName()); 

	private UserService userService;
	
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
				getWebApplicationContext(getServletContext()).getBean("userService", UserServiceImpl.class);
	}
	
	//Tin doGet tha tin kaloun oles oi othones ektos apo tin login gia na doun an o xrhsths einai
	//'hdh sundedemenos. H apantish tha einai to email tou xrhsth 'h tipota an den einai sundedemenos
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		//gia na parsaristei to string tis apantishs se html
 		response.setContentType("text/html");
		final String email = (String) request.getSession().getAttribute("email");
		if(email != null)
			response.getWriter().println(email);
		//teleiwse h apantish
		response.getWriter().close();
	}
	
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		//Elegxos an to request einai multipart form data
		if (!ServletFileUpload.isMultipartContent(request)) {
			LOGGER.log(Level.WARNING, "Request content type is not multipart/form-data");
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
				LOGGER.log(Level.WARNING, "No email specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No email specified"); //400 Bad Request
				return;
			}
			if (password == null) {
				LOGGER.log(Level.WARNING, "No password specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No password specified"); //400 Bad Request
				return;
			}
			try {
				if (userService.isValidUser(email, password)) {
					//apothikeush tou xrhsth sto session pou einai ston server
					request.getSession().setAttribute("email", email);
					LOGGER.log(Level.INFO, "User " + email + " logged in successfully");
					response.sendRedirect(url);
				} else {
					LOGGER.log(Level.INFO, "User " + email + " entered invalid credentials");
					response.sendRedirect(String.format(LOGIN_URL, URLEncoder.encode(locale, UTF_8),
							URLEncoder.encode(url, UTF_8)));
				}
			} catch (final UserServiceException e) {
				LOGGER.log(Level.WARNING, "Error validating user " + email);
				throw new ServletException("Error validating user " + email, e); //Epistrefei 500 ston client
			}
		} catch (final FileUploadException e) {
			LOGGER.log(Level.WARNING, "Error parsing request");
			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
		}
	}
}
