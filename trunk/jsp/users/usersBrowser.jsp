<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="usersBrowser">
	<h:form id="usersBrowserForm">
		<t:aliasBean alias="#{searchResults}" value="#{usersBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

	<t:panelGroup rendered="#{usersBrowser.entityView}">
		<%--@ include file="usrViewer.jsp"--%>
	</t:panelGroup>
</f:subview>


