<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="screeningRoomActivitiesBrowser">
	<h:form id="screeningRoomActivitiesBrowserForm">
		<t:aliasBean alias="#{searchResults}" value="#{screeningRoomActivitiesBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

<%--
	<t:panelGroup rendered="#{activitiesBrowser.entityView}">
		<%@ include file="activityViewer.jsp"%>
	</t:panelGroup>
--%>
</f:subview>


