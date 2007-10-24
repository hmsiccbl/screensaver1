<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentViewer">

	<t:aliasBean alias="#{nameValueTable}"
		value="#{reagentViewer.nameValueTable}">
		<%@ include file="../nameValueTable.jspf"%>
	</t:aliasBean>

	<t:panelGrid style="margin-top: 10px;">
		<t:panelGroup rendered="#{! empty reagentViewer.compounds}">
			<h:form id="reagentContentsDownloadForm">
				<h:commandButton value="Dowload SD File"
					action="#{reagentViewer.downloadSDFile}" styleClass="command"
					title="Download the contents of the reagent as an SD File" />
			</h:form>
		</t:panelGroup>
	</t:panelGrid>

	<t:panelGroup rendered="#{! empty reagentViewer.compounds}">
		<%@ include file="structureImageNotice.jspf"%>
	</t:panelGroup>

	<t:div style="margin-top: 15px; margin-left: 20px;"
		rendered="#{reagentViewer.annotationNameValueTable.numRows > 0}">
		<t:outputText styleClass="subsectionHeader" value="Annotations" />
		<t:aliasBean alias="#{nameValueTable}"
			value="#{reagentViewer.annotationNameValueTable}">
			<%@ include file="../nameValueTable.jspf"%>
		</t:aliasBean>
	</t:div>

</f:subview>
