<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentSearchResultsViewer">

	<h:form id="reagentSearchResultsViewerForm">

		<t:popup id="showHideAnnotationDialog" closePopupOnExitingPopup="true"
			closePopupOnExitingElement="false" styleClass="popupDialog">
			<t:commandLink>
				<t:outputText value="Show/Hide Annotations >>" styleClass="label" />
			</t:commandLink>
			<f:facet name="popup">
				<t:panelGrid columns="1" styleClass="popupDialog">
					<t:outputLabel for="annotationsList"
						value="Show selected annotations:" styleClass="label" />
					<t:selectManyCheckbox id="annotationsList" layout="lineDirection"
						layoutWidth="#{reagentsBrowser.annotationTypeSelector.size}"
						value="#{reagentsBrowser.annotationTypeSelector.value}"
						valueChangeListener="#{reagentsBrowser.annotationTypesTable.selectionListener}"
						binding="#{reagentsBrowser.annotationTypesTable.selectManyUIComponent}"
						styleClass="label" style="vertical-align: top">
						<f:selectItems id="annotations"
							value="#{reagentsBrowser.annotationTypeSelector.selectItems}" />
					</t:selectManyCheckbox>
					<t:panelGroup>
						<t:commandButton id="updateAnnotationTypesButton" forceId="true"
							value="Update" styleClass="command"
							title="Update the annotations selection" />
						<t:commandButton id="allAnnotationsButton" value="All"
							action="#{reagentsBrowser.annotationTypesTable.selectAll}"
							styleClass="command" title="Show all annotations" />
						<t:commandButton id="noAnnotationsButton" value="First"
							action="#{reagentsBrowser.annotationTypesTable.selectNone}"
							styleClass="command" title="Show the first annotation" />
					</t:panelGroup>
				</t:panelGrid>
			</f:facet>
		</t:popup>

		<t:collapsiblePanel id="annotationTypesPanel"
			value="#{reagentsBrowser.isPanelCollapsedMap['annotationTypes']}"
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
				value="#{reagentsBrowser.annotationTypesTable.dataModel}" var="row"
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
					value="#{reagentsBrowser.annotationTypesTable.columnModel}"
					var="columnName" styleClass="column">
					<f:facet name="header">
						<t:outputText value="#{columnName}" />
					</f:facet>
					<t:outputText
						value="#{reagentsBrowser.annotationTypesTable.cellValue}" />
				</t:columns>
			</t:dataTable>
		</t:collapsiblePanel>

		<t:collapsiblePanel id="reagentsDataPanel"
		value="#{reagentsBrowser.isPanelCollapsedMap['reagentsData']}"
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

			<t:aliasBean alias="#{searchResults}" value="#{reagentsBrowser}">
				<%@include file="../searchResults.jspf"%>
			</t:aliasBean>
		</t:collapsiblePanel>
	</h:form>

	<t:panelGroup rendered="#{reagentsBrowser.entityView && !reagentsBrowser.isPanelCollapsedMap['annotationValues']}">
		<%@ include file="reagentViewer.jsp"%>
	</t:panelGroup>
</f:subview>


