<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="studiesBrowser">
	<h:form id="studiesBrowserForm">
		<t:aliasBean alias="#{searchResults}" value="#{studiesBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

	<t:panelGroup rendered="#{studiesBrowser.entityView}">
		<%@ include file="studyViewer.jsp"%>
	</t:panelGroup>
</f:subview>


