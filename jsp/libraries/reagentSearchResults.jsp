<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentSearchResultsViewer">
	<h:form id="reagentSearchResultsViewerForm">
		<t:aliasBean alias="#{searchResults}" value="#{reagentsBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

	<t:panelGroup rendered="#{reagentsBrowser.entityView}">
		<%@ include file="reagentViewer.jsp"%>
	</t:panelGroup>

</f:subview>


