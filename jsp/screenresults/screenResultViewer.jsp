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

<%-- 
TODO:
- add well type, exclude columns to data table
- link screen number field
- make 'isShareable' property read-only, or add a "Save" button (or make update into an immediate submit)
- sectionHeader style is needing to be applied to 3 elements; should figure out what's going on w/css
--%>

<f:subview id="screenResultViewer">

  <%-- Note: we are sharing screenViewer's screenSearchResults, as ScreenResultViewer 
       and ScreenViewer are synchronized w.r.t. the current Screen. --%>
	<t:aliasBean alias="#{navigator}" value="#{screenViewer.screenSearchResults}">
		<%@ include file="../searchResultsNavPanel.jspf"%>
	</t:aliasBean>

  <h:form id="commandForm" >

		<t:panelGroup>
			<t:commandButton action="#{screenResultViewer.viewScreen}"
				value="View Screen" styleClass="command" />
			<t:commandButton action="#{screenResultViewer.download}"
				value="Download"
				rendered="#{!empty screenResultViewer.screenResult}"
				styleClass="command" />
			<t:commandButton action="#{screenResultViewer.delete}" value="Delete"
				onclick="javascript: return confirm('Delete this screen result permanently?');"
				styleClass="command"
				rendered="#{! screenResultViewer.readOnly && !empty screenResultViewer.screenResult}" />
		</t:panelGroup>

	</h:form>

	<t:panelGroup rendered="#{!screenResultViewer.readOnly}">
		<%@include file="admin/screenResultUploader.jspf"%>
		<%--@ include file="admin/cherryPickUploader.jspf" --%>
	</t:panelGroup>

	<t:panelGroup rendered="#{!screenResultViewer.readOnly && empty screenResultViewer.screenResult}">
		<t:outputText value="No screen result loaded." styleClass="sectionHeader"/>
	</t:panelGroup>
	
	<h:form id="dataForm">

		<t:panelGrid columns="1"
			rendered="#{!empty screenResultViewer.screenResult}">
			<t:outputLabel for="dataHeadersList"
				value="Show selected data headers:" />
			<t:selectManyListbox id="dataHeadersList"
				value="#{screenResultViewer.selectedDataHeaderNames}"
				valueChangeListener="#{screenResultViewer.selectedDataHeadersListener}"
				styleClass="input">
				<f:selectItems id="dataHeaders"
					value="#{screenResultViewer.dataHeaderSelectItems}" />
			</t:selectManyListbox>
			<t:panelGroup>
				<t:commandButton id="updateButton1" value="Update"
					action="#{screenResultViewer.update}" styleClass="command" />
				<t:commandButton id="allDataHeadersButton" value="All"
					action="#{screenResultViewer.showAllDataHeaders}"
					styleClass="command" />
			</t:panelGroup>
		</t:panelGrid>

		<t:panelGrid columns="1">
			<t:collapsiblePanel id="summaryPanel"
				value="#{screenResultViewer.collapsablePanelsState['summary']}"
				title="Summary" var="state" titleVar="title">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:panelGrid columns="2" styleClass="standardTable">
					<t:outputLabel for="screenNumber" value="Screen Number"
						styleClass="keyColumn" />
					<t:outputText id="screenNumber"
						value="#{screenResultViewer.screen.screenNumber}"
						styleClass="dataText" />

					<t:outputLabel for="screenTitle" value="Screen Title"
						styleClass="keyColumn" />
					<t:outputText id="screenTitle"
						value="#{screenResultViewer.screen.title}" styleClass="dataText" />

					<t:outputLabel for="screenResultDateCreated"
						value="Screen Result Created" styleClass="keyColumn"
						rendered="#{!empty screenResultViewer.screenResult}" />
					<t:outputText id="screenResultDateCreated"
						value="#{screenResultViewer.screenResult.dateCreated}"
						styleClass="dataText"
						rendered="#{!empty screenResultViewer.screenResult}" />

					<t:outputLabel for="screenResultReplicateCount" value="Replicates"
						styleClass="keyColumn"
						rendered="#{!empty screenResultViewer.screenResult}" />
					<t:outputText id="screenResultReplicateCount"
						value="#{screenResultViewer.screenResult.replicateCount}"
						styleClass="dataText"
						rendered="#{!empty screenResultViewer.screenResult}" />

					<t:outputLabel for="screenResultDateCreated" value="Shareable"
						styleClass="keyColumn"
						rendered="#{!empty screenResultViewer.screenResult}" />
					<t:selectBooleanCheckbox id="screenResultIsShareable"
						value="#{screenResultViewer.screenResult.shareable}"
						rendered="#{screenResultViewer.readOnlyAdmin || screenResultViewer.editable}"
						displayValueOnly="#{screenResultViewer.readOnly}"
						displayValueOnlyStyleClass="dataText"
						rendered="#{!empty screenResultViewer.screenResult}" />
				</t:panelGrid>
			</t:collapsiblePanel>

			<t:collapsiblePanel id="dataHeadersPanel"
				value="#{screenResultViewer.collapsablePanelsState['datHeadersTable']}"
				title="Data Headers" var="state" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:dataTable id="metadataTable"
					value="#{screenResultViewer.metadata}" var="row"
					styleClass="standardTable" headerClass="tableHeader"
					rowClasses="row1,row2">
					<t:column styleClass="keyColumn">
						<f:facet name="header">
							<t:outputText value="Property" />
						</f:facet>
						<t:outputText value="#{row.rowLabel}" />
					</t:column>
					<t:columns value="#{screenResultViewer.dataHeaderColumnModel}"
						var="columnName" styleClass="column">
						<f:facet name="header">
							<t:outputText value="#{columnName}" />
						</f:facet>
						<t:outputText value="#{screenResultViewer.metadataCellValue}" />
					</t:columns>
				</t:dataTable>
			</t:collapsiblePanel>

			<t:collapsiblePanel id="dataTablePanel"
				value="#{screenResultViewer.collapsablePanelsState['dataTable']}"
				title="Data" var="state" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:dataTable id="rawDataTable"
					binding="#{screenResultViewer.dataTable}"
					value="#{screenResultViewer.rawData}" var="row" rows="10"
					styleClass="standardTable" headerClass="tableHeader"
					rowClasses="row1,row2">
					<t:column styleClass="keyColumn">
						<f:facet name="header">
							<t:outputText value="Plate" />
						</f:facet>
						<t:outputText value="#{row.well.plateNumber}" />
					</t:column>
					<t:column styleClass="keyColumn">
						<f:facet name="header">
							<t:outputText value="Well" />
						</f:facet>
						<t:commandLink action="#{screenResultViewer.showWell}">
							<f:param name="wellIdParam" value="#{row.well.wellName} " />
							<t:outputText value="#{row.well.wellName}" />
						</t:commandLink>
					</t:column>
					<t:columns value="#{screenResultViewer.dataHeaderColumnModel}"
						var="columnName" styleClass="column">
						<f:facet name="header">
							<t:outputText value="#{columnName}" />
						</f:facet>
						<t:outputText value="#{screenResultViewer.rawDataCellValue}" />
					</t:columns>
				</t:dataTable>

				<t:outputLabel for="plateNumber" value="Plate:" />
				<t:selectOneMenu id="plateNumber"
					value="#{screenResultViewer.plateNumber}"
					binding="#{screenResultViewer.plateNumberInput}"
					onchange="javascript:submit()"
					valueChangeListener="#{screenResultViewer.plateNumberListener}"
					converter="IntegerConverter" styleClass="input">
					<f:selectItems value="#{screenResultViewer.plateSelectItems}" />
				</t:selectOneMenu>
				<t:commandButton id="prevPageCommand"
					action="#{screenResultViewer.prevPage}" value="Prev"
					image="/images/arrow-previous.gif" styleClass="command" />
				<t:commandButton id="nextPageCommand"
					action="#{screenResultViewer.nextPage}" value="Next"
					image="/images/arrow-next.gif" styleClass="command" />
				<t:outputLabel id="rowLabel" value="Row"
					for="firstDisplayedRowNumber" />
				<t:inputText id="firstDisplayedRowNumber"
					value="#{screenResultViewer.firstDisplayedRowNumber}"
					binding="#{screenResultViewer.firstDisplayedRowNumberInput}"
					valueChangeListener="#{screenResultViewer.firstDisplayedRowNumberListener}"
					size="6" styleClass="input">
					<f:validateLongRange minimum="1"
						maximum="#{screenResultViewer.rawDataSize}" />
				</t:inputText>
				<t:outputLabel id="rowRange"
					value="#{screenResultViewer.rowRangeText}"
					for="firstDisplayedRowNumber" />
			</t:collapsiblePanel>

			<t:collapsiblePanel id="heatMapsPanel"
				value="#{screenResultViewer.collapsablePanelsState['heatMaps']}"
				title="Heat Maps" var="state" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>
				<%@ include file="heatMapViewer.jspf"%>
			</t:collapsiblePanel>

		</t:panelGrid>
	</h:form>

</f:subview>
