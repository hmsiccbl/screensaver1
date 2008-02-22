<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="wellSearchResultsViewer">

	<h:form id="wellSearchResultsViewerForm">
		<t:aliasBean alias="#{searchResults}" value="#{wellsBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

	<t:panelGroup rendered="#{wellsBrowser.entityView}">
		<%@ include file="wellViewer.jsp"%>
	</t:panelGroup>

</f:subview>


