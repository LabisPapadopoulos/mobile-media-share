package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.context.support.WebApplicationContextUtils;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;

/**
 * Diaxeirish xrhstwn mesw REST. 
 * @author labis
 * @see <a href="http://en.wikipedia.org/wiki/Representational_state_transfer>REST</a>
 * @see <a href="https://javamail.java.net/nonav/docs/api/">JavaMail</a>
 */
public class UserServlet extends HttpServlet {
	private class PasswordAuthenticator extends Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
System.out.println("Default user: " + getDefaultUserName());			
System.out.println("Port: " + getRequestingPort());
System.out.println("Prompt: " + getRequestingPrompt());
System.out.println("protocol: " + getRequestingProtocol());
System.out.println("Site: " + getRequestingSite());
			return new PasswordAuthentication(smtpUser, smtpPassword);
		}
	}
	
	private static final long serialVersionUID = 1L;
	private static final String LOGIN_URL = "./login.html?locale=%s&url=%s";
	private static final String UTF_8 = "UTF-8"; 
	private static final Logger LOGGER = Logger.getLogger(UserServlet.class.getName()); 
	
	private UserService userService; //Java Bean
	private Session session;
	private String smtpHost;
	private String smtpPort;
	private String smtpAuth;
	private String smtpUser;
	private String smtpPassword;
	private String smtpSSL;
	private String smtpFrom;
	private String smtpSubject;
	private String smtpContent;

		
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
		
		//TODO fwrtwma apo properties
		smtpHost = "smtp.gmail.com";
		smtpPort = "465";
		smtpAuth = "true";
		smtpUser = "mobile.media.share";
		smtpPassword = "medi@sh@re";
		smtpSSL = "true";
		smtpFrom = "mobile.media.share@gmail.com";
		smtpSubject = "Welcome";
		smtpContent = "Token: %s";
		
		// mail				
		final Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.setProperty("mail.smtp.auth", smtpAuth);
		properties.setProperty("mail.smtp.port", smtpPort);
		properties.setProperty("mail.smtp.user", smtpUser);
		properties.setProperty("mail.smtp.ssl.enable", smtpSSL);
		session = Session.getInstance(properties, new PasswordAuthenticator());
	}
	
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
			String action = null;
			String email = null;
			String password = null;
			String password2 = null;
			String locale = null;
			String url = null;
			for (FileItem fileItem : new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request)) {
				if (fileItem.isFormField() && fileItem.getFieldName().equals("action")) {
					action = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("email")) {
					//vrethike to e-mail
					email = fileItem.getString();
					//diagrafh apo ton disko
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("password")) {
					//vrethike to password
					password = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("password2")) {
					//vrethike to password
					password2 = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("locale")) {
					//vrethike to locale
					locale = fileItem.getString();
					fileItem.delete();
				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("url")) {
					//vrethike to url
					url = fileItem.getString();
					fileItem.delete();
				}
				
			}
			if (action == null) {
				LOGGER.warning("No action specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No action specified"); //400 Bad Request
				return;
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
				if (action.equals("new")) {
					if (!password.equals(password2)) {
						LOGGER.warning("Passwords do not match");
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Passwords do not match"); //400 Bad Request
						return;
					}
					final String token = userService.addUser(email, password);
					if (token == null) {
						LOGGER.info("User " + email + " already exists");
						response.sendError(HttpServletResponse.SC_CONFLICT, "User " + email + " already exists");
						return;
					}
					final MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(smtpFrom));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
					message.setSubject(smtpSubject, "UTF-8");
					final Multipart multipart = new MimeMultipart();
			        final MimeBodyPart mimeBodyPart = new MimeBodyPart();
			        mimeBodyPart.setContent(String.format(smtpContent, email, token), "text/html;charset=UTF-8");
			        multipart.addBodyPart(mimeBodyPart);
			        message.setContent(multipart);
					Transport.send(message);
					response.sendRedirect(String.format(LOGIN_URL, URLEncoder.encode(locale, UTF_8), URLEncoder.encode(url, UTF_8)));
				}
			} catch (final AddressException e) {
				LOGGER.log(Level.WARNING, "Error adding user " + email, e);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email " + email + " is invalid");
				return;
			} catch (final MessagingException e) {
				LOGGER.log(Level.WARNING, "Error adding user " + email, e);
				throw new ServletException("Error adding user " + email, e); //Epistrefei 500 ston client
			} catch (final UserServiceException e) {
				LOGGER.log(Level.WARNING, "Error adding user " + email, e);
				throw new ServletException("Error adding user " + email, e); //Epistrefei 500 ston client
			}
		} catch (final FileUploadException e) {
			LOGGER.log(Level.WARNING, "Error parsing request", e);
			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
		}
	}
	
	/**
	 * Log in users.
	 */
//	@Override
//	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
//		//Elegxos an to request einai multipart form data
//		if (!ServletFileUpload.isMultipartContent(request)) {
//			LOGGER.warning("Request content type is not multipart/form-data");
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request content type is not multipart/form-data"); //400 Bad Request
//			return;
//		}
//		//DiskFileItemFactoryapothikeuei oti erthei se multipart form data sto disko
//		//ServletFileUpload: parsarei to request gia na xexwrisei ta fileItems kai ta apothikeuei sto DiskFile..
//		//FileItem: To kathe part tou multi part
//		try {
//			String email = null;
//			String password = null;
//			String locale = null;
//			String url = null;
//			for (FileItem fileItem : new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request)) {
//				//an einai pedio formas kai oxi arxeia
//				if (fileItem.isFormField() && fileItem.getFieldName().equals("email")) {
//					//vrethike to e-mail
//					email = fileItem.getString();
//					//diagrafh apo ton disko
//					fileItem.delete();
//				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("password")) {
//					//vrethike to password
//					password = fileItem.getString();
//					fileItem.delete();
//				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("locale")) {
//					locale = fileItem.getString();
//					fileItem.delete();
//				} else if (fileItem.isFormField() && fileItem.getFieldName().equals("url")) {
//					url = fileItem.getString();
//					fileItem.delete();
//				}
//			}
//			if (email == null) {
//				LOGGER.warning("No email specified");
//				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No email specified"); //400 Bad Request
//				return;
//			}
//			if (password == null) {
//				LOGGER.warning("No password specified");
//				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No password specified"); //400 Bad Request
//				return;
//			}
//			try {
//				if (userService.isValidUser(email, password)) {
//					//apothikeush tou xrhsth sto session pou einai ston server
//					request.getSession().setAttribute("email", email);
//					LOGGER.info("User " + email + " logged in successfully");
//					response.sendRedirect(url);
//				} else {
//					LOGGER.info("User " + email + " entered invalid credentials");
//					response.sendRedirect(String.format(LOGIN_URL, URLEncoder.encode(locale, UTF_8),
//							URLEncoder.encode(url, UTF_8)));
//				}
//			} catch (final UserServiceException e) {
//				LOGGER.log(Level.WARNING, "Error validating user " + email, e);
//				throw new ServletException("Error validating user " + email, e); //Epistrefei 500 ston client
//			}
//		} catch (final FileUploadException e) {
//			LOGGER.log(Level.WARNING, "Error parsing request", e);
//			throw new ServletException("Error parsing request", e); //Epistrefei 500 ston client
//		}
//	}
}
