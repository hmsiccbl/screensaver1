<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="screenAndResultViewer">

	<t:saveState value="#{screenViewer.showNavigationBar}" />

	<t:aliasBean alias="#{navigator}" value="#{screensBrowser}">
		<h:form id="navPanelForm">
			<%@include file="../searchResultsNavPanel.jspf"%>
		</h:form>
	</t:aliasBean>

	<%--t:panelGroup rendered="#{!screenResultViewer.readOnly}">
		<%@ include file="screenresults/admin/cherryPickUploader.jspf">
	</t:panelGroup--%>

	<t:panelGrid columns="1" width="100%">
		<%@include file="../screens/screenViewer.jspf"%>
		<%@include file="screenresults/annotationViewer.jspf"%>
		<%@include file="screenresults/screenResultViewer.jspf"%>
	</t:panelGrid>

</f:subview>
