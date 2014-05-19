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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserService;
import gr.uoa.di.std08169.mobile.media.share.client.services.user.UserServiceException;
import gr.uoa.di.std08169.mobile.media.share.shared.user.User;
import gr.uoa.di.std08169.mobile.media.share.shared.user.UserStatus;

/**
 * Diaxeirish xrhstwn mesw REST. 
 * @author labis
 * @see <a href="http://en.wikipedia.org/wiki/Representational_state_transfer>REST</a>
 * @see <a href="https://javamail.java.net/nonav/docs/api/">JavaMail</a>
 */
public class UserServlet extends HttpServlet {
	//Klash pou epistrefei ena username kai ena password se morfh 
	//PasswordAuthentication gia na borei na sundethei s' ena smtp server
	//Ginetai se xexwristh klash to PasswordAuthenticator gia logous asfaleias giati to
	//password borei na to pairnoume dunamika apo ton xrhsth xwris na apothikeuetai kapou.
	private class PasswordAuthenticator extends Authenticator {
		//Ylopoiei mia getPasswordAuthentication pou prepei na vrei me kapoio tropo username
		//kai password. Edw diavazontai apo property giati einai hdh gnwsta. Alla borei kai
		//o xrhsths m' ena p.x pop-up parathuro na ta pliktrologei.
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(smtpUser, smtpPassword);
		}
	}
	
	private static final long serialVersionUID = 1L;
	private static final String LOGIN_URL = "./login.html?locale=%s&url=%s";
	private static final String MAP_URL = "./map.jsp?locale=%s";
	private static final String UTF_8 = "UTF-8";
	private static final String HTML_MIME_TYPE = "text/html;charset=UTF-8";
	private static final Logger LOGGER = Logger.getLogger(UserServlet.class.getName()); 
	
	private UserService userService; //Java Bean
	private Session session;
	private String smtpUser;
	private String smtpPassword;
	private String smtpFrom;
	private String smtpReplyTo;
	private String smtpSubject;
	private String smtpContent;

		
	//init gia to servlet
	@Override
	public void init() {
		//pairnei to xml tou spring
		//WebApplicationContextUtils: gia na pairnei application contexts (perivallon pou trexei h efarmogh)
		//getServletContext(): pairnei to web.xml
		//getWebApplicationContext: pairnei to context pou orizetai sto applicationContext.xml
		//ftiaxnei ena aplication context sumfwna me auta pou orizontai sto web.xml
		//Pairnei ena pragma (bean) pou to lene userService gia na kanei douleies gia xrhstes
		//(to opoio to userService mhlaei me tin bash).
		final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext()); 
		userService = (UserService) webApplicationContext.getBean("userService", UserService.class);
		// mail				
		final Properties properties = new Properties();
		final String smtpHost = (String) webApplicationContext.getBean("smtpHost", String.class);
		properties.setProperty("mail.smtp.host", smtpHost);
		final Integer smtpPort = (Integer) webApplicationContext.getBean("smtpPort", Integer.class);
		properties.setProperty("mail.smtp.port", smtpPort.toString());
		//Gia na rwtaei an xreiazetai login (authentication)
		final Boolean smtpAuth = (Boolean) webApplicationContext.getBean("smtpAuth", Boolean.class);
		properties.setProperty("mail.smtp.auth", smtpAuth.toString());
		//Xrhsh protokolou asfaleias gia asfalhs sundesh
		final Boolean smtpSSL = (Boolean) webApplicationContext.getBean("smtpSSL", Boolean.class);
		properties.setProperty("mail.smtp.ssl.enable", smtpSSL.toString());
		//Xrhsths pou tha steilei to mail
		smtpUser = (String) webApplicationContext.getBean("smtpUser", String.class);
		properties.setProperty("mail.smtp.user", smtpUser);
		smtpPassword = (String) webApplicationContext.getBean("smtpPassword", String.class);
		smtpFrom = (String) webApplicationContext.getBean("smtpFrom", String.class);
		smtpReplyTo = (String) webApplicationContext.getBean("smtpReplyTo", String.class);
		smtpSubject = (String) webApplicationContext.getBean("smtpSubject", String.class);
		smtpContent = (String) webApplicationContext.getBean("smtpContent", String.class);
		//Sundesh me SMTP server pou orizetai apo ta properties kai 
		//me ton PasswordAuthenticator exei kai username kai password
		session = Session.getInstance(properties, new PasswordAuthenticator());
	}
	
	//Get ginetai patwntas to link gia energopoihsh logariasmou (mono gia mail)
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		final String token = request.getParameter("token");
		final String locale = request.getParameter("locale");
		if (token == null) {
			LOGGER.warning("No token specified");
			//Auta pou zhtaei o xrhsths einai akura
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No token specified"); //400 Bad Request
			return;
		}
		try {
			final User user = userService.getUserByToken(token);
			if (user == null) {
				LOGGER.warning("Invalid token " + token);
				//Den anagnwrizei to token pou dinei o xrhsths (den uparxei 'h exei lhxei) - 401
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
				return;
			}
			user.setStatus(UserStatus.NORMAL);
			userService.editUser(user, null);
			LOGGER.info("User " + user.getEmail() + " activated their account");
			//teleiwnei tin apanthsh
			response.sendRedirect(String.format(MAP_URL, URLEncoder.encode(locale, UTF_8)));
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error retrieving user with token " + token, e);
			//Internal Server Error (500)
			throw new ServletException("Error retrieving user", e);
		}
	}
	
	@Override
	public void doPut(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		final String email = request.getParameter("email");
		final String password = request.getParameter("password");
		final String password2 = request.getParameter("password2");
		final String locale = request.getParameter("locale");
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
		if (password2 == null) {
			LOGGER.warning("No confirm password specified");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No confirm password specified"); //400 Bad Request
			return;
		}
		if (!password.equals(password2)) {
			LOGGER.warning("Passwords do not match");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Passwords do not match"); //400 Bad Request
			return;
		}
		if (locale == null) {
			LOGGER.warning("No locale specified");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No locale specified"); //400 Bad Request
			return;
		}
		try {
			final String token = userService.addUser(email, password);
			if (token == null) {
				LOGGER.info("User " + email + " already exists");
				response.sendError(HttpServletResponse.SC_CONFLICT, "User already exists");
				return;
			}
			//Message me polla parts giati borei na perilamvanei attachments
			//Borei na einai kai plain-text, alla den exei attachments (html, fotos)
			final MimeMessage message = new MimeMessage(session);
			//Logariasmos pou tha fainetai ston paralhpth ws apostoleas
			//InternetAddress, suntaktikos elegnxos e-mail
			message.setFrom(new InternetAddress(smtpFrom));
			message.setReplyTo(new InternetAddress[] {new InternetAddress(smtpReplyTo)});
			//Prosthikh parallhpth. TO: autos pou tha parei to mhnuma
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject(smtpSubject, UTF_8);
			//Lista me ta kommatia tou mhnumatos. Gia apostolh otidhpote allo ektos apo plain-text p.x apostolh html
			final Multipart multipart = new MimeMultipart();
			//Kurio meros tou mhnumatos
	        final MimeBodyPart mimeBodyPart = new MimeBodyPart();
	        mimeBodyPart.setContent(String.format(smtpContent, email, token, locale), HTML_MIME_TYPE);
	        multipart.addBodyPart(mimeBodyPart);
	        message.setContent(multipart);
	        //apostolh mhmunatos
			Transport.send(message);
			//OK apanta o server xwris na grapsei kati allo
			response.getWriter().flush();
		} catch (final AddressException e) {
			LOGGER.log(Level.WARNING, "Error adding user " + email, e);
			throw new ServletException("Error adding user", e); //Epistrefei 500 ston client
		} catch (final MessagingException e) {
			LOGGER.log(Level.WARNING, "Error adding user " + email, e);
			throw new ServletException("Error adding user", e);
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error adding user " + email, e);
			throw new ServletException("Error adding user", e);
		}
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
