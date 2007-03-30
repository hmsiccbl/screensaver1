<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>


<%-- 
TODO:
 - add lead screener, lab head to Screen Summary
 - paged, sortable cherry pick tables
--%>

<f:subview id="cherryPickRequestViewer">

	<h:form id="cherryPickRequestViewerForm">

		<t:div styleClass="sectionHeader">
			<t:outputText value="Screen Summary" styleClass="sectionHeader" />
		</t:div>

		<%-- following screen summary should be shared with screenAndResultViewer.jspf --%>
		<t:panelGrid id="screenSummaryTable" columns="2"
			styleClass="standardTable" rowClasses="row1,row2"
			columnClasses="keyColumn,column">

			<t:outputText value="Screen&nbsp;ID" escape="false"
				visibleOnUserRole="developer" />
			<t:outputText id="screenId"
				value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenId}"
				styleClass="dataText" visibleOnUserRole="developer" />

			<t:outputText value="Screen&nbsp;Number" escape="false" />
			<t:commandLink id="screenCommand"
				action="#{cherryPickRequestViewer.viewScreen}">
				<t:outputText id="screenNumber"
					value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenNumber}"
					styleClass="dataText" />
			</t:commandLink>

			<t:outputText value="Title" escape="false" />
			<t:outputText id="title"
				value="#{cherryPickRequestViewer.cherryPickRequest.screen.title}"
				styleClass="dataText" />

			<t:outputText value="Screen&nbsp;Type" escape="false" />
			<t:outputText id="screenType"
				value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenType}"
				converter="ScreenTypeConverter" styleClass="dataText" />
		</t:panelGrid>

		<t:div styleClass="sectionHeader">
			<t:outputText value="Cherry Pick Request" styleClass="sectionHeader" />
		</t:div>

		<t:panelGrid columns="1">
			<t:panelGroup id="commandPanel"
				rendered="#{cherryPickRequestViewer.editable}"
				styleClass="commandPanel">
				<t:commandButton id="editCommand" value="Edit"
					action="#{cherryPickRequestViewer.setEditMode}"
					styleClass="command"
					rendered="#{!cherryPickRequestViewer.editMode}" />
				<t:commandButton id="deleteCommand" value="Delete"
					action="#{cherryPickRequestViewer.deleteCherryPickRequest}"
					onclick="javascript: return confirm('Delete this cherry pick request and all of its cherry picks permanently?');"
					styleClass="command"
					rendered="#{cherryPickRequestViewer.editable && !cherryPickRequestViewer.editMode}" 
					disabled="#{cherryPickRequestViewer.cherryPickRequest.allocated}" />
				<t:commandButton id="saveCommand" value="Save"
					action="#{cherryPickRequestViewer.save}" styleClass="command"
					rendered="#{cherryPickRequestViewer.editMode}" />
				<h:commandButton id="cancelEditCommand" value="Cancel"
					rendered="#{cherryPickRequestViewer.editMode}"
					action="#{cherryPickRequestViewer.cancelEdit}" immediate="true"
					styleClass="command" />
			</t:panelGroup>

			<t:panelGrid id="cherryPickRequestInfoTable" columns="2"
				styleClass="standardTable" rowClasses="row1,row2"
				columnClasses="keyColumn,column">

				<t:outputText value="Cherry Pick Request ID" />
				<t:outputText id="cherryPickRequestEntityId"
					value="#{cherryPickRequestViewer.cherryPickRequest.entityId}"
					styleClass="dataText" />

				<t:outputText value="Date Requested" />
				<t:inputDate id="dateRequestedEditable"
					value="#{cherryPickRequestViewer.cherryPickRequest.dateRequested}"
					popupCalendar="true" rendered="#{cherryPickRequestViewer.editMode}"
					styleClass="inputText" />
				<t:outputText id="dateRequested"
					value="#{cherryPickRequestViewer.cherryPickRequest.dateRequested}"
					rendered="#{!cherryPickRequestViewer.editMode}"
					styleClass="dataText" />

				<t:outputText value="Requested&nbsp;By" escape="false" />
				<h:panelGroup rendered="#{!cherryPickRequestViewer.editMode}">
					<t:commandLink id="requestedBy"
						value="#{cherryPickRequestViewer.cherryPickRequest.requestedBy.fullNameLastFirst}"
						action="#{cherryPickRequestViewer.viewLeadScreener}"
						styleClass="dataText entityLink" style="margin-right: 4px" />
					<t:outputText value=" (" styleClass="dataText" />
					<h:outputLink
						value="mailto:#{cherryPickRequestViewer.cherryPickRequest.requestedBy.email}">
						<t:outputText id="requestedByEmail"
							value="#{cherryPickRequestViewer.cherryPickRequest.requestedBy.email}"
							styleClass="dataText" />
					</h:outputLink>
					<t:outputText value=")" styleClass="dataText" />
				</h:panelGroup>
				<t:selectOneMenu id="requestedByEditable"
					value="#{cherryPickRequestViewer.requestedBy.value}"
					rendered="#{cherryPickRequestViewer.editMode}"
					styleClass="inputText">
					<f:selectItems
						value="#{cherryPickRequestViewer.requestedBy.selectItems}" />
				</t:selectOneMenu>

				<t:outputText value="Requested&nbsp;Volume&nbsp;(&#181;L)"
					escape="false" />
				<t:inputText id="requestedVolume"
					value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellRequested}"
					displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.allocated}"
					size="5" styleClass="inputText"
					displayValueOnlyStyleClass="dataText" />

				<t:outputText value="Approved&nbsp;Volume&nbsp;(&#181;L)"
					escape="false" />
				<t:inputText id="approvedVolume"
					value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellApproved}"
					displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.allocated}"
					size="5" styleClass="inputText"
					displayValueOnlyStyleClass="dataText" />

				<t:outputText value="Cherry&nbsp;Pick&nbsp;Plate&nbsp;Type"
					escape="false" />
				<t:outputText
					value="#{cherryPickRequestViewer.cherryPickRequest.assayPlateType}"
					styleClass="dataText" />

				<t:outputText value="Random&nbsp;plate&nbsp;well&nbsp;layout"
					escape="false" />
				<t:selectBooleanCheckbox
					value="#{cherryPickRequestViewer.cherryPickRequest.randomizedAssayPlateLayout}"
					displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.mapped}"
					styleClass="command" displayValueOnlyStyleClass="dataText" />

				<t:outputText value="Empty&nbsp;columns&nbsp;on&nbsp;plate"
					escape="false" />
				<t:outputText
					value="#{cherryPickRequestViewer.emptyColumnsOnAssayPlateAsString}"
					rendered="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.mapped}"
					styleClass="dataText" />
				<t:selectManyListbox
					value="#{cherryPickRequestViewer.emptyColumnsOnAssayPlate.value}"
					size="20"
					rendered="#{cherryPickRequestViewer.editMode && !cherryPickRequestViewer.cherryPickRequest.mapped}"
					styleClass="input">
					<f:selectItems
						value="#{cherryPickRequestViewer.emptyColumnsOnAssayPlate.selectItems}" />
				</t:selectManyListbox>

				<t:outputText value="Comments" escape="false" />
				<t:inputTextarea id="comments"
					value="#{cherryPickRequestViewer.cherryPickRequest.comments}"
					rows="10" cols="80"
					displayValueOnly="#{!cherryPickRequestViewer.editMode}"
					styleClass="inputText" displayValueOnlyStyleClass="dataText" />

			</t:panelGrid>
		</t:panelGrid>

		<t:div styleClass="sectionHeader">
			<t:outputText value="Screener Cherry Picks" styleClass="sectionHeader" />
		</t:div>

		<t:panelStack id="screenerCherryPicksPanelStack"
			selectedPanel="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks ? (cherryPickRequestViewer.editable ? \"addCherryPicks\" : \"noCherryPicks\") : \"viewCherryPicks\"}">

			<t:panelGroup id="noCherryPicks">
				<t:outputText value="Cherry picks have not yet been specified." />
			</t:panelGroup>

			<t:panelGrid id="addCherryPicks" columns="1"
				rendered="#{cherryPickRequestViewer.editable}">
				<t:outputLabel for="cherryPicksInput"
					value="Specify cherry picks as plate/well pairs:"
					styleClass="label" />
				<t:inputTextarea id="cherryPicksInput" rows="20"
					value="#{cherryPickRequestViewer.cherryPicksInput}"
					styleClass="inputText" />
				<%--t:commandButton id="addCherryPicksCommand" value="Add Cherry Picks"
					action="#{cherryPickRequestViewer.addCompoundCherryPicks}"
					rendered="#{cherryPickRequestViewer.cherryPickRequest.screenType == ScreenType.SMALL_COMPOUND}"
					styleClass="command" /--%>
				<t:commandButton id="addPoolCherryPicksCommand" value="Add Cherry Picks (Pool Wells)"
					action="#{cherryPickRequestViewer.addPoolCherryPicks}"
					styleClass="command" />
				<t:commandButton id="addDuplexCherryPicksCommand" value="Add Cherry Picks (Duplex Wells)"
					action="#{cherryPickRequestViewer.addDuplexCherryPicks}"
					styleClass="command" />
			</t:panelGrid>

			<t:panelGrid id="viewCherryPicks" columns="1">

				<t:panelGroup id="screenerCherryPicksCommandPanel"
					rendered="#{cherryPickRequestViewer.editable}">
					<t:commandButton id="deleteCherryPicks" value="Delete All"
						action="#{cherryPickRequestViewer.deleteAllCherryPicks}"
						disabled="#{cherryPickRequestViewer.cherryPickRequest.allocated}"
						styleClass="command" />
				</t:panelGroup>

				<t:dataTable id="screenerCherryPicksTable" var="cherryPickRow"
					value="#{cherryPickRequestViewer.screenerCherryPicksDataModel}"
					styleClass="standardTable" columnClasses="column"
					rowClasses="row1,row2" headerClass="tableHeader"
					sortColumn="#{cherryPickRequestViewer.screenerCherryPicksSortManager.currentSortColumnName}"
					sortAscending="#{cherryPickRequestViewer.screenerCherryPicksSortManager.sortAscending}">
					<t:columns
						value="#{cherryPickRequestViewer.screenerCherryPicksSortManager.columnModel}"
						var="columnName" styleClass="column">
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
						<t:outputText value="#{cherryPickRow[columnName]}" />
					</t:columns>
				</t:dataTable>

				<t:panelGroup id="labCherryPicksCommandPanel"
					rendered="#{cherryPickRequestViewer.editable}">
					<t:commandButton id="allocateCherryPicks" value="Reserve #{cherryPickRequestViewer.liquidTerm}"
						action="#{cherryPickRequestViewer.allocateCherryPicks}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks || cherryPickRequestViewer.cherryPickRequest.allocated}"
						styleClass="command" />
					<t:commandButton id="deallocateCherryPicks"
						value="Cancel Reservation"
						action="#{cherryPickRequestViewer.deallocateCherryPicks}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command" />
					<t:commandButton id="plateMapCherryPicks" value="Map to Plates"
						action="#{cherryPickRequestViewer.plateMapCherryPicks}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command" />
					<h:commandButton id="createCherryPickRequestForUnfulfilled" value="New Cherry Pick Request for Unfulfilled"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated}"
						action="#{cherryPickRequestViewer.createNewCherryPickRequestForUnfulfilledCherryPicks}"
						styleClass="command" />
				</t:panelGroup>

				<t:dataTable id="labCherryPicksTable" var="cherryPickRow"
					value="#{cherryPickRequestViewer.labCherryPicksDataModel}"
					styleClass="standardTable" columnClasses="column"
					rowClasses="row1,row2" headerClass="tableHeader"
					sortColumn="#{cherryPickRequestViewer.labCherryPicksSortManager.currentSortColumnName}"
					sortAscending="#{cherryPickRequestViewer.labCherryPicksSortManager.sortAscending}">
					<t:columns
						value="#{cherryPickRequestViewer.labCherryPicksSortManager.columnModel}"
						var="columnName" styleClass="column">
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
						<t:outputText value="#{cherryPickRow[columnName]}" />
					</t:columns>
				</t:dataTable>

			</t:panelGrid>

		</t:panelStack>


		<t:div styleClass="sectionHeader">
			<t:outputText value="Cherry Pick Plates" styleClass="sectionHeader" />
		</t:div>

		<t:outputText value="<none>" styleClass="label"
			rendered="#{!cherryPickRequestViewer.cherryPickRequest.mapped}" />

		<t:panelGroup id="selectedAssayPlatesCommandPanel"
			rendered="#{cherryPickRequestViewer.editable && cherryPickRequestViewer.cherryPickRequest.mapped}">
			<t:div>
				<t:outputText styleClass="label" value="For selected plates:"/>
			</t:div>
			<t:commandButton id="downloadPlateMappingFiles"
				value="Download Files"
				action="#{cherryPickRequestViewer.downloadPlateMappingFilesForSelectedAssayPlates}"
				disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
				styleClass="command" />
			<t:commandButton id="recordLiquidTransfer"
				value="Created"
				action="#{cherryPickRequestViewer.recordLiquidTransferForSelectedAssayPlates}"
				disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
				styleClass="command" />
			<h:commandButton id="recordFailureOfAssayPlates" value="Failed"
				disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
				action="#{cherryPickRequestViewer.recordFailureOfAssayPlates}"
				styleClass="command" />
		</t:panelGroup>

		<t:dataTable id="assayPlatesTable" var="assayPlateRow"
			value="#{cherryPickRequestViewer.assayPlatesDataModel}"
			styleClass="standardTable" columnClasses="column"
			rowClasses="row1,row2" headerClass="tableHeader"
			rendered="#{cherryPickRequestViewer.cherryPickRequest.mapped}">
			<t:columns value="#{cherryPickRequestViewer.assayPlatesColumnModel}"
				var="columnName" styleClass="column">
				<f:facet name="header">
					<t:outputText value="#{columnName}" />
				</f:facet>
				<t:outputText value="#{assayPlateRow.values[columnName]}" />
			</t:columns>
			<t:column>
				<f:facet name="header">
					<t:selectBooleanCheckbox id="selectAll" value="#{cherryPickRequestViewer.selectAllAssayPlates}"
						onclick="document.getElementById('selectAllAssayPlatesButton').click()" />
				</f:facet>
				<t:selectBooleanCheckbox value="#{assayPlateRow.selected}" />
			</t:column>
		</t:dataTable>

		<t:commandButton id="selectAllAssayPlatesButton" forceId="true"
			action="#{cherryPickRequestViewer.selectAllAssayPlates}"
			rendered="#{cherryPickRequestViewer.cherryPickRequest.mapped}"
			style="display:none" />

	</h:form>

</f:subview>
