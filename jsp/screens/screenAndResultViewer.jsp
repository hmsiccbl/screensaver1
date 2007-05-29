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

	<t:aliasBean alias="#{navigator}" value="#{screensBrowser.screenSearchResults}">
		<%@ include file="../searchResultsNavPanel.jspf"%>
	</t:aliasBean>

	<%--t:panelGroup rendered="#{!screenResultViewer.readOnly}">
		<%@ include file="screenresults/admin/cherryPickUploader.jspf">
	</t:panelGroup--%>

	<h:form id="dataForm">
		<t:panelGrid columns="1" width="100%">
			<t:collapsiblePanel id="screenPanel"
				value="#{screenResultViewer.isPanelCollapsedMap['screenSummary']}"
				title="Screen Summary" var="isCollapsed" titleVar="title">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="Screen Summary" styleClass="sectionHeader"
								rendered="#{isCollapsed}" />
							<h:outputText value="Screen Details" styleClass="sectionHeader"
								rendered="#{!isCollapsed}" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<f:facet name="closedContent">
					<t:panelGrid columns="1">
						<t:outputText value="#{screenResultViewer.screenResult.screen.screenNumber}: \"#{screenResultViewer.screenResult.screen.title}\""
							styleClass="dataText" />
						<t:div>
							<t:outputText value="Lab:" styleClass="label"/>
							<t:commandLink value="#{screenResultViewer.screenResult.screen.labHead.labName}"
								action="#{screenViewer.viewLabHead}"
								styleClass="dataText entityLink" />
							<t:outputText value="&nbsp;&nbsp;Screener:" styleClass="label" escape="false"/>
							<t:commandLink
								value="#{screenResultViewer.screenResult.screen.leadScreener.fullNameLastFirst}"
								action="#{screenViewer.viewLeadScreener}"
								styleClass="dataText entityLink" />
						</t:div>
					</t:panelGrid>
				</f:facet>

				<%@ include file="../screens/screenViewer.jspf"%>
			</t:collapsiblePanel>

			<t:collapsiblePanel id="screenResultPanel"
				value="#{screenResultViewer.isPanelCollapsedMap['screenResultSummary']}"
				title="Screen Result Summary" var="isCollapsed" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult && !screenResultViewer.screenResult.restricted}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="Screen Result Summary"
								styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:panelGroup>
					<t:commandButton action="#{screenResultViewer.download}"
						value="Download"
						styleClass="command" />
					<t:commandButton action="#{screenResultViewer.delete}"
						value="Delete"
						onclick="javascript: return confirm('Delete this screen result permanently?');"
						styleClass="command" rendered="#{!screenResultViewer.readOnly}" />
				</t:panelGroup>

				<t:panelGrid columns="2" styleClass="standardTable"
					columnClasses="keyColumn,textColumn" rowClasses="row1,row2">
					<%-- as long as screenResult.dateCreated is synonymous with "first screening room activity date", 
				       there's no point in displaying this redundant field, as screening room activities, with their assoicated dates, are 
				       shown in screen panel --%>
					<%--t:outputLabel for="screenResultDateCreated"
						value="Date of first screning room activity" styleClass="keyColumn" />
					<t:outputText id="screenResultDateCreated"
						value="#{screenResultViewer.screenResult.dateCreated}"
						styleClass="dataText" /--%>

					<t:outputLabel for="screenResultLastImported" value="Last Imported" />
					<t:outputText id="screenResultLastImported"
						value="#{screenResultViewer.screenResult.dateLastImported}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultIsShareable" value="Shareable" />
					<t:div>
						<t:selectBooleanCheckbox id="screenResultIsShareable"
							value="#{screenResultViewer.screenResult.shareable}"
							styleClass="label"
							displayValueOnly="#{screenResultViewer.readOnly}"
							displayValueOnlyStyleClass="dataText"
							onclick="javascript:document.getElementById('saveScreenResultButton').click()" />
						<t:commandButton id="saveScreenResultButton" forceId="true"
							action="#{screenResultViewer.saveScreenResult}"
							styleClass="hiddenCommand" />
					</t:div>

					<t:outputLabel for="screenResultPlateCount" value="Plates" />
					<t:outputText id="screenResultPlateCount"
						value="#{screenResultViewer.screenResult.plateNumberCount}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultReplicateCount" value="Replicates" />
					<t:outputText id="screenResultReplicateCount"
						value="#{screenResultViewer.screenResult.replicateCount}"
						styleClass="dataText" />

					<t:outputLabel for="screenResultExperimentalWellCount"
						value="Experimental Wells" />
					<t:outputText id="screenResultExperimentalWellCount"
						value="#{screenResultViewer.screenResult.experimentalWellCount}"
						styleClass="dataText" />

				</t:panelGrid>
			</t:collapsiblePanel>

			<t:panelGrid columns="1"
				rendered="#{!empty screenResultViewer.screenResult && !screenResultViewer.screenResult.restricted && !(screenResultViewer.isPanelCollapsedMap['dataHeadersTable'] && screenResultViewer.isPanelCollapsedMap['dataTable'])}">
				<t:outputLabel for="dataHeadersList"
					value="Show selected data headers:" styleClass="label" />
				<t:selectManyCheckbox id="dataHeadersList" layout="pageDirection" layoutWidth="6" 
					value="#{screenResultViewer.selectedResultValueTypes.value}" 
					valueChangeListener="#{screenResultViewer.resultValueTypesChangeListener}"
					binding="#{screenResultViewer.dataHeadersSelectMany}"
					styleClass="label" style="vertical-align: top">
					<f:selectItems id="dataHeaders" 
						value="#{screenResultViewer.selectedResultValueTypes.selectItems}" />
				</t:selectManyCheckbox>
				<t:panelGroup>
					<t:commandButton id="updateDataHeadersButton" forceId="true"
						value="Update" action="#{screenResultViewer.updateDataHeaders}"
						styleClass="command" />
					<t:commandButton id="allDataHeadersButton" value="All"
						action="#{screenResultViewer.showAllDataHeaders}"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:collapsiblePanel id="dataHeadersPanel"
				value="#{screenResultViewer.isPanelCollapsedMap['dataHeadersTable']}"
				title="Data Headers" var="isCollapsed" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult && !screenResultViewer.screenResult.restricted}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:dataTable id="dataHeadersTable"
					value="#{screenResultViewer.dataHeaders}" var="row"
					rendered="#{!isCollapsed}" styleClass="standardTable"
					headerClass="tableHeader" rowClasses="row1,row2">
					<t:column styleClass="keyColumn">
						<f:facet name="header">
							<t:outputText value="Property" />
						</f:facet>
						<t:outputText value="#{row.rowLabel}" escape="false" />
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
				value="#{screenResultViewer.isPanelCollapsedMap['dataTable']}"
				title="Data" var="isCollapsed" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult && !screenResultViewer.screenResult.restricted}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>

				<t:panelGrid columns="1" style="width: 100%" rendered="#{!isCollapsed}">
					<t:panelGroup id="dataTableCommandPanel" styleClass="commandPanel">
						<h:commandButton id="firstPageCommand"
							action="#{screenResultViewer.firstPage}" value="First"
							image="/images/arrow-first.png" styleClass="command" title="First page" />
						<t:commandButton id="prevPageCommand"
							action="#{screenResultViewer.prevPlate}" value="Prev Plate"
							image="/images/arrow-fastrewind.png" styleClass="command" title="Previous plate"/>
						<t:commandButton id="prevPlateCommand"
							action="#{screenResultViewer.prevPage}" value="Prev" 
							image="/images/arrow-previous.png" styleClass="command" title="Previous page"/>
						<t:commandButton id="nextPageCommand"
							action="#{screenResultViewer.nextPage}" value="Next"
							image="/images/arrow-next.png" styleClass="command" title="Next page"/>
						<t:commandButton id="nextPlateCommand"
							action="#{screenResultViewer.nextPlate}" value="Next Plate"
							image="/images/arrow-fastforward.png" styleClass="command" title="Next plate" />
						<h:commandButton id="lastPageCommand"
							action="#{screenResultViewer.lastPage}" value="Last"
							image="/images/arrow-last.png" styleClass="command" title="Last page" />

						<t:outputLabel id="rowRange"
							value="#{screenResultViewer.rowRangeText}" for="rowNumber"
							styleClass="label" />
						<t:inputText id="rowNumber"
							value="#{screenResultViewer.rowNumber}"
							binding="#{screenResultViewer.rowNumberInput}"
							valueChangeListener="#{screenResultViewer.rowNumberListener}"
							size="6" styleClass="inputText">
							<%--f:validateLongRange minimum="1"
						maximum="#{screenResultViewer.rawDataSize}" /--%>
						</t:inputText>
						<t:commandButton id="updateDataTableRowsButton" forceId="true"
							value="Go" action="#{screenResultViewer.updateDataTableRows}"
							styleClass="command" />

						<t:panelGroup id="showHitsOnlyCommandPanel"
							rendered="#{!empty screenResultViewer.hitsForDataHeader.selectItems}">
							<t:selectBooleanCheckbox id="showHitsOnly" styleClass="label"
								value="#{screenResultViewer.showHitsOnly}" immediate="true"
								onclick="javascript:document.getElementById('updateDataTableRowsButton').click()" />
							<t:outputLabel value="Show only hits for" for="showHitsOnly"
								styleClass="label" />
							<t:selectOneMenu id="hitsForDataHeaderList"
								value="#{screenResultViewer.hitsForDataHeader.value}"
								displayValueOnly="#{screenResultViewer.hitsForDataHeader.size <= 1}"
								onchange="javascript:document.getElementById('updateDataTableRowsButton').click()"
								immediate="true" styleClass="inputText">
								<f:selectItems id="hitsForDataHeader"
									value="#{screenResultViewer.hitsForDataHeader.selectItems}" />
							</t:selectOneMenu>
						</t:panelGroup>
					</t:panelGroup>

					<t:dataTable id="rawDataTable"
						binding="#{screenResultViewer.dataTable}"
						value="#{screenResultViewer.rawData}" var="row" rows="24"
						styleClass="standardTable" headerClass="tableHeader"
						rowClasses="row1,row2"
						sortColumn="#{screenResultViewer.sortManager.currentSortColumnName}"
						sortAscending="#{screenResultViewer.sortManager.sortAscending}">
						<t:columns value="#{screenResultViewer.sortManager.columnModel}"
							var="columnName"
							styleClass="#{(columnName==\"Plate\" || columnName==\"Well\") ? \"keyColumn\" : (screenResultViewer.numericColumn ? \"numericColumn\" : \"textColumn\")} #{screenResultViewer.resultValueCellExcluded ? \"excludedValue\" : \"\"} ">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{columnName}" arrow="false">
									<f:facet name="ascending">
										<t:graphicImage value="/images/ascending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<f:facet name="descending">
										<t:graphicImage value="/images/descending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<h:outputText value="#{columnName}" />
								</t:commandSortHeader>
							</f:facet>
							<t:outputText value="#{row[columnName]}"
								rendered="#{columnName != \"Well\"}" />
							<t:commandLink action="#{screenResultViewer.viewWell}"
								rendered="#{columnName == \"Well\"}">
								<t:outputText value="#{row[columnName]}" />
							</t:commandLink>
						</t:columns>
					</t:dataTable>
				</t:panelGrid>
			</t:collapsiblePanel>

			<t:collapsiblePanel id="heatMapsPanel"
				value="#{screenResultViewer.isPanelCollapsedMap['heatMaps']}"
				title="Heat Maps" var="isCollapsed" titleVar="title"
				rendered="#{!empty screenResultViewer.screenResult && !screenResultViewer.screenResult.restricted}">
				<f:facet name="header">
					<t:div styleClass="sectionHeader">
						<t:headerLink immediate="true" styleClass="sectionHeader">
							<h:graphicImage
								value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
								styleClass="icon" />
							<h:outputText value="#{title}" styleClass="sectionHeader" />
						</t:headerLink>
					</t:div>
				</f:facet>
				<t:panelGroup rendered="#{!isCollapsed}">
					<%@ include file="screenresults/heatMapViewer.jspf"%>
				</t:panelGroup>
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
