
<%-- Redirect to the start page, running within JSF --%>
<%-- Note: if we use <jsp:forward/>, the page being forwarded to will be forwarded to WITHOUT an authentication/authorization check! --%>
<% response.sendRedirect("main.jsf"); %>
