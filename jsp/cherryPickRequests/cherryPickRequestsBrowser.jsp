<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="cherryPickRequestsBrowser">
	<h:form id="cherryPickRequestsBrowserForm">
		<t:aliasBean alias="#{searchResults}" value="#{cherryPickRequestsBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

	<t:panelGroup rendered="#{cherryPickRequestsBrowser.entityView}">
		<%@ include file="cherryPickRequestViewer.jsp"%>
	</t:panelGroup>

</f:subview>


