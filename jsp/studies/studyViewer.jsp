<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="studyViewer" rendered="#{! empty studyViewer.study}">

	<t:panelGrid columns="1" width="100%">
		<%@include file="studyDetailViewer.jspf"%>
		<%@include file="annotationViewer.jspf"%>
	</t:panelGrid>

</f:subview>
