<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>

<%! private static final String UTF_8 = "UTF-8"; %>
<%! private static final String DEFAULT_LOCALE = "en"; %>

<% if (request.getSession().getAttribute("email") == null) {
	final String locale = (request.getParameter("locale") == null) ? DEFAULT_LOCALE : request.getParameter("locale");
	response.sendRedirect("./login.html?locale=" + URLEncoder.encode(locale, UTF_8) + 
		"&url=" + URLEncoder.encode("./map.jsp?locale=" + URLEncoder.encode(locale, UTF_8), UTF_8));
} else { %>
	<!DOCTYPE html>
	<html>
		<head>
			<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
			<script type="text/javascript" src="./myAccount/myAccount.nocache.js"></script>
			<title>Mobile Media Share</title>
		</head>
		<body>
			<input id="email" type="hidden" value="<%= ((String) request.getSession().getAttribute("email")) %>" />
			<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position: absolute; width: 0; height: 0; border: 0;"></iframe>
		</body>
	</html>
<% } %>
