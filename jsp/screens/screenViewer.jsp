<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="screenViewer">

	<t:saveState value="#{screenViewer.showNavigationBar}" />

	<t:aliasBean alias="#{navigator}" value="#{screensBrowser}">
		<h:form id="navPanelForm">
			<%@include file="../searchResultsNavPanel.jspf"%>
		</h:form>
	</t:aliasBean>

	<t:panelGrid columns="1" width="100%">
		<%@include file="screenDetailViewer.jspf"%>
		<%@include file="../studies/annotationViewer.jspf"%>
		<%@include file="screenresults/screenResultViewer.jspf"%>
	</t:panelGrid>

</f:subview>
