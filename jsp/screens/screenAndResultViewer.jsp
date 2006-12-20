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
- sectionHeader style is needing to be applied to 3 elements; should figure out what's going on w/css
--%>

<f:subview id="screenAndResultViewer">

	<t:aliasBean alias="#{navigator}" value="#{screenViewer.screenSearchResults}">
		<%@ include file="../searchResultsNavPanel.jspf"%>
	</t:aliasBean>

	<t:panelGroup rendered="#{!screenResultViewer.readOnly}">
		<%--@ include file="screenresults/admin/cherryPickUploader.jspf" --%>
	</t:panelGroup>

	<h:form id="dataForm">
		<t:panelGrid columns="1">
			<t:collapsiblePanel id="screenPanel"
				value="#{screenResultViewer.collapsablePanelsState['screenSummary']}"
				title="Screen Summary" var="state" titleVar="title">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="Screen Summary" styleClass="sectionHeader"
								rendered="#{state}" />
							<h:outputText value="Screen Details" styleClass="sectionHeader"
								rendered="#{!state}" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<f:facet name="closedContent">
					<t:panelGrid columns="2">
						<t:outputText value="#{screenResultViewer.screen.screenNumber}: \"#{screenResultViewer.screen.title}\""
							styleClass="dataText" />
						<t:div>
						<t:outputText value="Lab: " styleClass="inputLabel"/>
						<t:commandLink
							value="#{screenViewer.screen.labHead.labName}"
							action="#{screenViewer.viewLabHead}"
							styleClass="dataText entityLink"/>
						<t:outputText value="&nbsp;&nbsp;Screener: " styleClass="inputLabel" escape="false"/>
						<t:commandLink
							value="#{screenViewer.screen.leadScreener.fullNameLastFirst}"
							action="#{screenViewer.viewLeadScreener}"
							styleClass="dataText entityLink"/>
						</t:div>
					</t:panelGrid>
				</f:facet>

				<%@ include file="../screens/screenViewer.jspf"%>
			</t:collapsiblePanel>

			<t:collapsiblePanel id="screenResultPanel"
				value="#{screenResultViewer.collapsablePanelsState['screenResultSummary']}"
				title="Screen Result Summary" var="state" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="Screen Result Summary"
								styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:panelGrid columns="2">
					<t:outputLabel for="screenResultDateCreated"
						value="First data deposition" styleClass="keyColumn" />
					<t:outputText id="screenResultDateCreated"
						value="#{screenResultViewer.screenResult.dateCreated}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultLastImported" value="Last Imported"
						styleClass="keyColumn" />
					<t:outputText id="screenResultLastImported"
						value="#{screenResultViewer.screenResult.dateLastImported}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultIsShareable" value="Shareable"
						styleClass="keyColumn" />
					<%--h:form id="screenResultIsShareableForm"--%> 
						<%-- TODO: make cancel response undo click; the following is not working: onclick="javascript:if (confirm('Make screen result #{screenResultViewer.screenResult.shareable ? \"unshared\" : \"shared\"}?')) submit(); else this.value=#{screenResultViewer.screenResult.shareable};" --%>
						<t:selectBooleanCheckbox id="screenResultIsShareable"
							value="#{screenResultViewer.screenResult.shareable}"
							displayValueOnly="#{screenResultViewer.readOnly}"
							displayValueOnlyStyleClass="dataText"
							onclick="javascript:submit()" />
					<%--/h:form--%>
					
					<t:outputLabel for="platesCount" value="Plates"
						styleClass="keyColumn" />
					<t:outputText id="screenResultPlateCount"
						value="#{screenResultViewer.screenResult.plateNumberCount}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultReplicateCount" value="Replicates"
						styleClass="keyColumn" />
					<t:outputText id="screenResultReplicateCount"
						value="#{screenResultViewer.screenResult.replicateCount}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultExperimentalWellsCount"
						value="Experimental Wells" styleClass="keyColumn" />
					<t:outputText id="screenResultExperimentWellCount"
						value="#{screenResultViewer.screenResult.experimentalWellCount}"
						styleClass="dataText"/>

				</t:panelGrid>
			</t:collapsiblePanel>

			<%--h:form id="commandForm" --%>

				<t:panelGroup>
					<t:commandButton action="#{screenResultViewer.download}"
						value="Download"
						rendered="#{!empty screenResultViewer.screenResult}"
						styleClass="command" />
					<t:commandButton action="#{screenResultViewer.delete}"
						value="Delete"
						onclick="javascript: return confirm('Delete this screen result permanently?');"
						styleClass="command"
						rendered="#{!screenResultViewer.readOnly && !empty screenResultViewer.screenResult}" />
				</t:panelGroup>

			<%--/h:form--%>

			<t:panelGrid columns="1"
				rendered="#{!empty screenResultViewer.screenResult && !(screenResultViewer.collapsablePanelsState['dataHeadersTable'] && screenResultViewer.collapsablePanelsState['dataTable'])}">
				<t:outputLabel for="dataHeadersList"
					value="Show selected data headers:" styleClass="inputLabel" />
				<t:selectManyListbox id="dataHeadersList"
					value="#{screenResultViewer.selectedResultValueTypes.value}"
					styleClass="input">
					<f:selectItems id="dataHeaders"
						value="#{screenResultViewer.selectedResultValueTypes.selectItems}" />
				</t:selectManyListbox>
				<t:panelGroup>
					<t:commandButton id="updateDataHeadersButton" forceId="true" value="Update"
						action="#{screenResultViewer.updateDataHeaders}" styleClass="command" />
					<t:commandButton id="allDataHeadersButton" value="All"
						action="#{screenResultViewer.showAllDataHeaders}"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:collapsiblePanel id="dataHeadersPanel"
				value="#{screenResultViewer.collapsablePanelsState['dataHeadersTable']}"
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

				<t:dataTable id="dataHeadersTable"
					value="#{screenResultViewer.dataHeaders}" var="row"
					styleClass="standardTable" headerClass="tableHeader"
					rowClasses="row1,row2">
					<t:column styleClass="keyColumn">
						<f:facet name="header">
							<t:outputText value="Property" />
						</f:facet>
						<t:outputText value="#{row.rowLabel}" />
					</t:column>
					<t:columns value="#{screenResultViewer.dataHeadersColumnModel}"
						var="columnName" styleClass="column">
						<f:facet name="header">
							<t:outputText value="#{columnName}" />
						</f:facet>
						<t:outputText value="#{screenResultViewer.dataHeadersCellValue}" />
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
					value="#{screenResultViewer.rawData}" var="row" rows="24"
					styleClass="standardTable" headerClass="tableHeader"
					rowClasses="row1,row2">
					<t:columns value="#{screenResultViewer.sortManager.columnModel}"
						var="columnName" styleClass="numericColumn">
						<f:facet name="header">
							<t:commandLink
								action="#{screenResultViewer.sortManager.sortOnColumn}"
								disabled="#{columnName == \"Excluded\"}">
								<t:outputText value="#{columnName}" />
							</t:commandLink>
						</f:facet>
						<t:outputText value="#{row[columnName]}" rendered="#{columnName != \"Well\"}"/>
						<t:commandLink action="#{screenResultViewer.viewWell}"
							rendered="#{columnName == \"Well\"}">
							<t:outputText value="#{row[columnName]}" />
						</t:commandLink>
					</t:columns>
				</t:dataTable>

				<h:commandButton id="firstPageCommand"
					action="#{screenResultViewer.firstPage}" value="First"
					image="/images/arrow-first.gif" styleClass="command" />
				<t:commandButton id="prevPageCommand"
					action="#{screenResultViewer.prevPage}" value="Prev"
					image="/images/arrow-previous.gif" styleClass="command" />
				<t:commandButton id="nextPageCommand"
					action="#{screenResultViewer.nextPage}" value="Next"
					image="/images/arrow-next.gif" styleClass="command" />
				<h:commandButton id="lastPageCommand"
					action="#{screenResultViewer.lastPage}" value="Last"
					image="/images/arrow-last.gif" styleClass="command" />

				<t:outputLabel id="rowRange"
					value="#{screenResultViewer.rowRangeText}" for="rowNumber"
					styleClass="inputLabel" />
				<t:inputText id="rowNumber" value="#{screenResultViewer.rowNumber}"
					binding="#{screenResultViewer.rowNumberInput}"
					valueChangeListener="#{screenResultViewer.rowNumberListener}"
					size="6" styleClass="input">
					<f:validateLongRange minimum="1"
						maximum="#{screenResultViewer.rawDataSize}" />
				</t:inputText>
				<t:commandButton id="updateDataTableRowsButton" forceId="true" value="Go"
					action="#{screenResultViewer.updateDataTableRows}"
					styleClass="command" />

				<t:panelGroup id="showHitsOnlyCommandPanel"
					rendered="#{!empty screenResultViewer.hitsForDataHeader.selectItems}">
					<t:selectBooleanCheckbox id="showHitsOnly"
						value="#{screenResultViewer.showHitsOnly}" immediate="true"
						onchange="javascript:document.getElementById('updateDataTableRowsButton').click()"/>
					<t:outputLabel value="Show only hits for" for="showHitsOnly"
						styleClass="inputLabel" />
					<t:selectOneMenu id="hitsForDataHeaderList"
						value="#{screenResultViewer.hitsForDataHeader.value}"
						onchange="javascript:document.getElementById('updateDataTableRowsButton').click()"
						immediate="true" styleClass="input">
						<f:selectItems id="hitsForDataHeader"
							value="#{screenResultViewer.hitsForDataHeader.selectItems}" />
					</t:selectOneMenu>
				</t:panelGroup>

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
				<%@ include file="screenresults/heatMapViewer.jspf"%>
			</t:collapsiblePanel>

		</t:panelGrid>
	</h:form>

	<%-- Warning: screenResultUploader.jspf must be included outside of h:form elements --%> 
	<t:panelGroup rendered="#{empty screenResultViewer.screenResult}">
		<t:outputText value="Screen result not available" styleClass="sectionHeader"/>
	</t:panelGroup>
	<t:panelGroup rendered="#{!screenResultViewer.readOnly && empty screenResultViewer.screenResult}">
		<%@include file="screenresults/admin/screenResultUploader.jspf"%>
	</t:panelGroup>

</f:subview>
