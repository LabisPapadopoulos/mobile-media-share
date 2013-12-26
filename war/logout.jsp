<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>

<%! private static final String UTF_8 = "UTF-8"; %>
<%! private static final String DEFAULT_LOCALE = "en"; %>

<% request.getSession().removeAttribute("email");
final String locale = (request.getParameter("locale") == null) ? DEFAULT_LOCALE : request.getParameter("locale");
response.sendRedirect("./login.html?locale=" + URLEncoder.encode(locale, UTF_8) + 
			"&url=" + URLEncoder.encode("./map.jsp?locale=" + URLEncoder.encode(locale, UTF_8), UTF_8)); %>
