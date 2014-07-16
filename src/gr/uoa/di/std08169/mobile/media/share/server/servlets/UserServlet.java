package gr.uoa.di.std08169.mobile.media.share.server.servlets;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
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

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.Gson;

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
	private static final String LOGIN_ACTION = "login";
	private static final String FORGOT_ACTION = "forgot";
	private static final String RESET_ACTION = "reset";
	private static final String LOGIN_URL = "./login.html?locale=%s&url=%s";
	private static final String MAP_URL = "./map.jsp?locale=%s";
	private static final String RESET_PASSWORD_URL = "./resetPassword.html?locale=%s&token=%s";
	private static final String UTF_8 = "utf-8";
	private static final String HTML_MIME_TYPE = "text/html;charset=utf-8";
	private static final Logger LOGGER = Logger.getLogger(UserServlet.class.getName());
	private static final String APPLICATION_JSON = "application/json";
	
	private UserService userService; //Java Bean
	private Session session;
	private String smtpUser;
	private String smtpPassword;
	private String smtpFrom;
	private String smtpReplyTo;
	private String smtpResourceBundle;
			
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
		smtpResourceBundle = (String) webApplicationContext.getBean("smtpResourceBundle", String.class);
		//Sundesh me SMTP server pou orizetai apo ta properties kai 
		//me ton PasswordAuthenticator exei kai username kai password
		session = Session.getInstance(properties, new PasswordAuthenticator());
	}
	
	//Get ginetai patwntas to link gia energopoihsh logariasmou (mono gia mail)
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		final String token = request.getParameter("token");
		final String locale = request.getParameter("locale");
		try {
			if ((token == null) || (locale == null)) { //klhsh apo kinito gia apostolh twn plhroforiwn tou trexontos xrhsth
				final String email = (String) request.getSession().getAttribute("email");
				if (email == null) {
					LOGGER.warning("No user is logged in");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required"); //401
					return;
				}
				final User user = userService.getUser(email);
				if (user == null) {
					LOGGER.warning("User " + email + " does not exist");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required"); //401
					return;
				}
				//return se json ton user
				response.setCharacterEncoding(UTF_8);
				//tupos apantishs
				response.setContentType(APPLICATION_JSON);
				//Egrafh stin apantish tin lista me ta Media ws JSON (me xrhsh tou Google Gson)
				final Writer writer = response.getWriter();
				try {
					writer.write(new Gson().toJson(user));
				} finally {
					writer.close();
				}
			} else { //klhsh apo e-mail energopoihsh 'h epanafora logariasmou
				final User user = userService.getUserByToken(token);
				if (user == null) {
					LOGGER.warning("Invalid token " + token);
					//Den anagnwrizei to token pou dinei o xrhsths (den uparxei 'h exei lhxei) - 404
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid token");
					return;
				}
				if (user.getStatus() == UserStatus.PENDING) { //action register
					user.setStatus(UserStatus.NORMAL);
					userService.editUser(user, null);
					LOGGER.info("User " + user.getEmail() + " activated their account");
					//teleiwnei tin apanthsh
					response.sendRedirect(String.format(MAP_URL, URLEncoder.encode(locale, UTF_8)));
				} else if (user.getStatus() == UserStatus.FORGOT) { //action reset
					LOGGER.info("User " + user.getEmail() + " initialized password reset");
					//teleiwnei tin apanthsh
					response.sendRedirect(String.format(RESET_PASSWORD_URL, URLEncoder.encode(locale, UTF_8),
							URLEncoder.encode(token, UTF_8)));
				} else { //periptwsh pou exei xrhsimopoihthei token apo mail pou den einai egkuro
					LOGGER.warning("Invalid status " + user.getStatus() + " for user " + user.getEmail());
					//O xrhsths den einai pending h forgot
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid status");
				}
			}
		} catch (final UserServiceException e) {
			LOGGER.log(Level.WARNING, "Error updating user with token " + token, e);
			//Internal Server Error (500)
			throw new ServletException("Error updating user", e);
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
			//Pairnei to bundle ths antistoixhs glwssas
			final ResourceBundle bundle = ResourceBundle.getBundle(smtpResourceBundle, new Locale(locale));
			//apo to bundle pairnei to string me key: register.subject
			
			message.setSubject(bundle.getString("register.subject"), UTF_8);
			//Lista me ta kommatia tou mhnumatos. Gia apostolh otidhpote allo ektos apo plain-text p.x apostolh html
			final Multipart multipart = new MimeMultipart();
			//Kurio meros tou mhnumatos
	        final MimeBodyPart mimeBodyPart = new MimeBodyPart();
	        mimeBodyPart.setContent(String.format(bundle.getString("register.content"), email, token), HTML_MIME_TYPE);
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
	 * Log in users, forgot password, reset password
	 */
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		final String action = request.getParameter("action");
		final String email = request.getParameter("email");
		final String password = request.getParameter("password");
		final String password2 = request.getParameter("password2");
		final String locale = request.getParameter("locale");
		final String url = request.getParameter("url");
		final String token = request.getParameter("token");
		
		if (LOGIN_ACTION.equals(action)) { //login stin forma
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
			if (locale == null) {
				LOGGER.warning("No locale specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No locale specified"); //400 Bad Request
				return;
			}
			if (url == null) {
				LOGGER.warning("No URL specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No URL specified"); //400 Bad Request
				return;
			}
			try {
				final User user = userService.getUser(email);
				if (user == null) {
					LOGGER.warning("User " + email + " does not exist");
					//O xrhsths den einai normal h admin
					response.sendRedirect(String.format(LOGIN_URL, URLEncoder.encode(locale, UTF_8),
							URLEncoder.encode(url, UTF_8)));
					return;
				}
				if ((user.getStatus() != UserStatus.NORMAL) && (user.getStatus() != UserStatus.ADMIN)) {
					LOGGER.warning("Invalid status " + user.getStatus() + " for user " + user.getEmail());
					//O xrhsths den einai normal h admin
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid status");
					return;
				}
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
				throw new ServletException("Error validating user", e); //Epistrefei 500 ston client
			}
		} else if (FORGOT_ACTION.equals(action)) { //sto ok ths formas tou forgot password
			if (email == null) {
				LOGGER.warning("No email specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No email specified"); //400 Bad Request
				return;
			}
			try {
				final User user = userService.getUser(email);
				if (user == null) {
					LOGGER.warning("User not found");
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found"); //404 Not Found
					return;
				}
				if ((user.getStatus() != UserStatus.NORMAL) && (user.getStatus() != UserStatus.ADMIN)) {
					LOGGER.warning("Invalid status " + user.getStatus() + " for user " + user.getEmail());
					//O xrhsths den einai normal h admin
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid status");
					return;
				}
				user.setStatus(UserStatus.FORGOT);
				final String newToken = userService.editUser(user, null);
				//Message me polla parts giati borei na perilamvanei attachments
				//Borei na einai kai plain-text, alla den exei attachments (html, fotos)
				final MimeMessage message = new MimeMessage(session);
				//Logariasmos pou tha fainetai ston paralhpth ws apostoleas
				//InternetAddress, suntaktikos elegnxos e-mail
				message.setFrom(new InternetAddress(smtpFrom));
				message.setReplyTo(new InternetAddress[] {new InternetAddress(smtpReplyTo)});
				//Prosthikh parallhpth. TO: autos pou tha parei to mhnuma
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
				//Pairnei to bundle ths antistoixhs glwssas
				final ResourceBundle bundle = ResourceBundle.getBundle(smtpResourceBundle, new Locale(locale));
				//apo to bundle pairnei to string me key: forgot.subject	
				message.setSubject(bundle.getString("forgot.subject"), UTF_8);
				//Lista me ta kommatia tou mhnumatos. Gia apostolh otidhpote allo ektos apo plain-text p.x apostolh html
				final Multipart multipart = new MimeMultipart();
				//Kurio meros tou mhnumatos
		        final MimeBodyPart mimeBodyPart = new MimeBodyPart();
		        mimeBodyPart.setContent(String.format(bundle.getString("forgot.content"),
		        		((user.getName() == null) || user.getName().isEmpty()) ? email : user.getName(), newToken), HTML_MIME_TYPE);
		        multipart.addBodyPart(mimeBodyPart);
		        message.setContent(multipart);
		        //apostolh mhmunatos
				Transport.send(message);
				//OK apanta o server xwris na grapsei kati allo
				response.getWriter().flush();
				return;
			} catch (final AddressException e) {
				LOGGER.log(Level.WARNING, "Error resetting password for user " + email, e);
				throw new ServletException("Error resetting password", e); //Epistrefei 500 ston client (provlima ston server)
			} catch (final MessagingException e) {
				LOGGER.log(Level.WARNING, "Error resetting password for user " + email, e);
				throw new ServletException("Error resetting password", e); //Epistrefei 500 ston client
			} catch (final UserServiceException e) {
				LOGGER.log(Level.WARNING, "Error resetting password for user " + email, e);
				throw new ServletException("Error resetting password", e); //Epistrefei 500 ston client				
			}
		} else if (RESET_ACTION.equals(action)) { //sti forma resetPassword
			if (token == null) {
				LOGGER.warning("No token specified");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No token specified"); //400 Bad Request
				return;
			}
			try {
				final User user = userService.getUserByToken(token);
				if (user == null) {
					LOGGER.warning("User not found");
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found"); //404 Not Found
					return;
				}
				if (user.getStatus() != UserStatus.FORGOT) {
					LOGGER.warning("Invalid status " + user.getStatus() + " for user with token " + token);
					//O xrhsths den einai normal h admin
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid status");
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
				user.setStatus(UserStatus.NORMAL);
				userService.editUser(user, password);
				LOGGER.info("User " + user.getEmail() + " reset their password succesfully");
			} catch (final UserServiceException e) {
				LOGGER.log(Level.WARNING, "Error resetting password for user with token " + token, e);
				throw new ServletException("Error resetting password", e); //Epistrefei 500 ston client
			}
		} else { // sumperilamvanomenou tou null
			LOGGER.warning("Unknown action " + action);
			//den katalavenei ti phre
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action " + action); //400 Bad Request
			return;
		}
	}
}
