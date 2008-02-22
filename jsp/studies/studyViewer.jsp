<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="studyViewer" rendered="#{! empty studyViewer.study}">

	<t:panelGrid columns="1" width="100%">
		<%@include file="studyDetailViewer.jspf"%>

		<h:form id="studyAnnotationsPanelForm">
			<t:collapsiblePanel id="annotationTypesPanel"
				value="#{studyViewer.isPanelCollapsedMap['annotationTypes']}"
				title="Annotation Types" var="isCollapsed" titleVar="title">
				<f:facet name="header">
					<t:div styleClass="subsectionHeader">
						<t:headerLink immediate="true" styleClass="subsectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="subsectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:dataTable id="annotationTypesTable"
					value="#{studyViewer.annotationTypesTable.dataModel}" var="row"
					rendered="#{!isCollapsed}" styleClass="standardTable"
					headerClass="tableHeader" rowClasses="row1,row2">
					<t:column styleClass="keyColumn">
						<f:facet name="header">
							<t:outputText value="Annotation Name" />
						</f:facet>
						<t:outputText value="#{row.rowLabel}" escape="false"
							title="#{row.rowDescription}" />
					</t:column>
					<t:columns
						value="#{studyViewer.annotationTypesTable.columnModel}"
						var="columnName" styleClass="column">
						<f:facet name="header">
							<t:outputText value="#{columnName}" />
						</f:facet>
						<t:outputText
							value="#{studyViewer.annotationTypesTable.cellValue}" />
					</t:columns>
				</t:dataTable>
			</t:collapsiblePanel>

			<t:collapsiblePanel id="reagentsDataPanel"
				value="#{studyViewer.isPanelCollapsedMap['reagentsData']}"
				title="Reagents" var="isCollapsed" titleVar="title">
				<f:facet name="header">
					<t:div styleClass="subsectionHeader">
						<t:headerLink immediate="true" styleClass="subsectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="subsectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>
			</t:collapsiblePanel>

		</h:form>

		<t:div
			rendered="#{! empty studyViewer.study && ! studyViewer.isPanelCollapsedMap['reagentsData']}">
			<t:aliasBean alias="#{reagentsBrowser}"
				value="#{studyViewer.reagentSearchResults}">
				<%@include file="../libraries/reagentSearchResults.jsp"%>
			</t:aliasBean>
		</t:div>
		
	</t:panelGrid>

</f:subview>
