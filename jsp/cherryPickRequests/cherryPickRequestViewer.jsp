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
--%>

<f:subview id="cherryPickRequestViewer">

	<h:form id="cherryPickRequestViewerForm">

		<%-- following screen summary should be shared with studyViewer.jspf --%>
		<t:collapsiblePanel id="screenSummaryPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['screenSummary']}"
			title="Screen Summary" var="isCollapsed" titleVar="title">
			<f:facet name="header">
				<t:div styleClass="sectionHeader">
					<t:headerLink immediate="true" styleClass="sectionHeader">
						<t:graphicImage
							value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<t:outputText value="#{title}" styleClass="sectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:panelGrid id="screenSummaryTable" columns="2"
				styleClass="standardTable" rowClasses="row1,row2"
				columnClasses="keyColumn,column">

				<t:outputText value="Screen&nbsp;ID" escape="false"
					visibleOnUserRole="developer"
					title="The database ID for the screen. This is an implementation-level detail displayed only for development purposes" />
				<t:outputText id="screenId"
					value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenId}"
					styleClass="dataText" visibleOnUserRole="developer" />

				<t:outputText value="Screen&nbsp;Number" escape="false"
					title="The number used to uniquely identify the screen" />
				<t:commandLink id="screenCommand"
					action="#{cherryPickRequestViewer.viewScreen}">
					<t:outputText id="screenNumber"
						value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenNumber}"
						styleClass="dataText" />
				</t:commandLink>

				<t:outputText value="Title" escape="false"
					title="The title of the screen" />
				<t:outputText id="title"
					value="#{cherryPickRequestViewer.cherryPickRequest.screen.title}"
					styleClass="dataText" />

				<t:outputText value="Screen&nbsp;Type" escape="false"
					title="'Small Molecule' or 'RNAi'" />
				<t:outputText id="screenType"
					value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenType}"
					converter="ScreenTypeConverter" styleClass="dataText" />
			</t:panelGrid>
		</t:collapsiblePanel>

		<t:collapsiblePanel id="cherryPickRequestDetailsPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['cherryPickRequestDetails']}"
			title="Cherry Pick Request Details" var="isCollapsed"
			titleVar="title">
			<f:facet name="header">
				<t:div styleClass="sectionHeader">
					<t:headerLink immediate="true" styleClass="sectionHeader">
						<t:graphicImage
							value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<t:outputText value="#{title}" styleClass="sectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:panelGrid columns="1">
				<t:panelGroup id="cherryPickRequestCommandPanel"
					styleClass="commandPanel">
					<t:panelGroup id="adminCommandPanel"
						rendered="#{cherryPickRequestViewer.editable}">
						<t:commandButton id="editCommand" value="Edit"
							action="#{cherryPickRequestViewer.setEditMode}"
							styleClass="command"
							rendered="#{!cherryPickRequestViewer.editMode}"
							title="Enter edit mode for the cherry pick request" />
						<t:commandButton id="deleteCommand" value="Delete"
							action="#{cherryPickRequestViewer.deleteCherryPickRequest}"
							onclick="javascript: return confirm('Delete this cherry pick request and all of its cherry picks permanently?');"
							styleClass="command"
							rendered="#{cherryPickRequestViewer.editable && !cherryPickRequestViewer.editMode}"
							disabled="#{cherryPickRequestViewer.cherryPickRequest.allocated}"
							title="Delete this cherry pick request" />
						<t:commandButton id="saveCommand" value="Save"
							action="#{cherryPickRequestViewer.save}" styleClass="command"
							rendered="#{cherryPickRequestViewer.editMode}"
							title="Save your changes and leave edit mode" />
						<t:commandButton id="cancelEditCommand" value="Cancel"
							rendered="#{cherryPickRequestViewer.editMode}"
							action="#{cherryPickRequestViewer.cancelEdit}" immediate="true"
							styleClass="command"
							title="Discard your changes and leave edit mode" />
					</t:panelGroup>
					<t:commandButton id="downloadCherryPickRequestCommand"
						value="Download"
						action="#{cherryPickRequestViewer.downloadCherryPickRequest}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks}"
						styleClass="command"
						title="Download the cherry pick request to a file" />
				</t:panelGroup>

				<t:panelGrid id="cherryPickRequestInfoTable" columns="2"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="keyColumn,column">

					<t:outputText value="Cherry Pick Request #"
						title="The cherry pick request number" />
					<t:outputText id="cherryPickRequestEntityNumber"
						value="#{cherryPickRequestViewer.cherryPickRequest.cherryPickRequestNumber}"
						styleClass="dataText" />

					<t:outputText value="Date Requested"
						title="The date the cherry pick request was made by the screener" />
					<t:inputDate id="dateRequestedEditable"
						value="#{cherryPickRequestViewer.cherryPickRequest.dateRequested}"
						popupCalendar="true"
						rendered="#{cherryPickRequestViewer.editMode}"
						styleClass="inputText" />
					<t:outputText id="dateRequested"
						value="#{cherryPickRequestViewer.cherryPickRequest.dateRequested}"
						rendered="#{!cherryPickRequestViewer.editMode}"
						styleClass="dataText" />

					<t:outputText value="Requested&nbsp;By" escape="false"
						title="The screener that made the request" />
					<t:panelGroup rendered="#{!cherryPickRequestViewer.editMode}">
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
					</t:panelGroup>
					<t:selectOneMenu id="requestedByEditable"
						value="#{cherryPickRequestViewer.requestedBy.value}"
						rendered="#{cherryPickRequestViewer.editMode}"
						styleClass="inputText">
						<f:selectItems
							value="#{cherryPickRequestViewer.requestedBy.selectItems}" />
					</t:selectOneMenu>

					<t:outputText value="Requested&nbsp;Volume&nbsp;(&#181;L)"
						escape="false"
						title="The volume per well that the screener requested" />
					<t:inputText id="requestedVolume"
						value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellRequested}"
						displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.allocated}"
						size="5" styleClass="inputText"
						displayValueOnlyStyleClass="dataText" />

					<t:outputText value="Approved&nbsp;Volume&nbsp;(&#181;L)"
						escape="false"
						title="The volume per well approved by the screening room" />
					<t:outputText
						value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellApproved}"
						rendered="#{(!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.allocated) && empty cherryPickRequestViewer.cherryPickRequest.volumeApprovedBy}"
						styleClass="dataText" />
					<t:outputText
						value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellApproved} (approved by #{cherryPickRequestViewer.cherryPickRequest.volumeApprovedBy.fullNameFirstLast} on #{cherryPickRequestViewer.dateVolumeApproved})"
						rendered="#{(!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.allocated) && !empty cherryPickRequestViewer.cherryPickRequest.volumeApprovedBy}"
						styleClass="dataText" />
					<t:panelGroup
						rendered="#{cherryPickRequestViewer.editMode && !cherryPickRequestViewer.cherryPickRequest.allocated}">
						<t:div styleClass="nowrap">
							<t:inputText id="approvedVolume"
								value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellApproved}"
								size="5" styleClass="inputText" />
						</t:div>
						<t:div styleClass="nowrap">
							<t:outputText value="Approved&nbsp;By" escape="false"
								styleClass="label"
								title="The screening room staff member that approved this volume" />
							<t:selectOneMenu id="volumeApprovedByEditable"
								value="#{cherryPickRequestViewer.volumeApprovedBy.value}"
								rendered="#{cherryPickRequestViewer.editMode}"
								styleClass="inputText">
								<f:selectItems
									value="#{cherryPickRequestViewer.volumeApprovedBy.selectItems}" />
							</t:selectOneMenu>
						</t:div>
						<t:div styleClass="nowrap">
							<t:outputText value="Date Approved" styleClass="label"
								title="The date the cherry pick volume was approved" />
							<t:inputDate id="dateVolumeApprovedEditable"
								value="#{cherryPickRequestViewer.cherryPickRequest.dateVolumeApproved}"
								popupCalendar="true" styleClass="inputText" />
						</t:div>
					</t:panelGroup>

					<t:outputText value="Cherry&nbsp;Pick&nbsp;Plate&nbsp;Type"
						escape="false"
						title="The plate type, e.g., 'Eppendorf', 'Genetix', etc." />
					<t:outputText
						value="#{cherryPickRequestViewer.cherryPickRequest.assayPlateType}"
						styleClass="dataText" />

					<t:outputText value="Random&nbsp;plate&nbsp;well&nbsp;layout"
						escape="false"
						title="True when screener requested a random layout for the cherry pick plates" />
					<t:selectBooleanCheckbox
						value="#{cherryPickRequestViewer.cherryPickRequest.randomizedAssayPlateLayout}"
						displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command" displayValueOnlyStyleClass="dataText" />

					<t:outputText
						value="Screener-requested&nbsp;empty&nbsp;wells&nbsp;on&nbsp;plate"
						escape="false"
						title="The wells the screener requested to be left empty" />
					<t:panelGroup>
						<t:inputText id="emptyWells"
							value="#{cherryPickRequestViewer.cherryPickRequest.requestedEmptyWellsOnAssayPlate}"
							converter="#{cherryPickRequestViewer.emptyWellsConverter}"
							displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.mapped}"
							displayValueOnlyStyleClass="dataText" styleClass="inputText"
							size="40" />
						<t:outputText value="(e.g. \"Col:3, Row:H, B2, N18\")"
						  styleClass="label" rendered="#{cherryPickRequestViewer.editMode}"/>
					</t:panelGroup>

					<t:outputText value="Comments" escape="false"
						title="Comments made by screening room staff" />
					<t:inputTextarea id="cherryPickRequestComments"
						value="#{cherryPickRequestViewer.cherryPickRequest.comments}"
						rows="10" cols="80"
						displayValueOnly="#{!cherryPickRequestViewer.editMode}"
						styleClass="inputText" displayValueOnlyStyleClass="dataText" />

					<t:outputText value="# Screener&nbsp;cherry&nbsp;picks"
						escape="false" title="The number of screener cherry picks" />
					<t:outputText id="screenerCherryPickCount"
						value="#{cherryPickRequestViewer.screenerCherryPickCount}"
						styleClass="dataText" />

					<t:outputText value="# Lab&nbsp;cherry&nbsp;picks" escape="false"
						title="The number of lab cherry picks" />
					<t:outputText id="labCherryPickCount"
						value="#{cherryPickRequestViewer.labCherryPickCount}"
						styleClass="dataText" />

					<t:outputText value="#&nbsp;Unfulfilled&nbsp;Lab&nbsp;Cherry&nbsp;Picks" escape="false"
						title="The number of unfulfilled lab cherry picks" />
					<t:outputText id="unfulfilledLabCherryPicksCount"
						value="#{cherryPickRequestViewer.cherryPickRequest.numberUnfulfilledLabCherryPicks}"
						styleClass="dataText" />

					<t:outputText value="#&nbsp;Cherry&nbsp;Pick&nbsp;Plates&nbsp;Completed" escape="false"
						title="The number of completed cherry pick plates" />
					<t:outputText id="assayPlatesCompletedCount"
						value="#{cherryPickRequestViewer.completedCherryPickPlatesCount} of #{cherryPickRequestViewer.activeCherryPickPlatesCount}"
						styleClass="dataText" />

					<t:outputText value="Completed" escape="false"
						title="Has the cherry pick request been completed, such that all cherry pick plates have been plated" />
					<t:outputText id="isCompleted"
						value="#{cherryPickRequestViewer.cherryPickRequest.plated}"
						styleClass="dataText" />

				</t:panelGrid>
			</t:panelGrid>
		</t:collapsiblePanel>

	</h:form>

	<h:form id="screenCherryPicksPanelForm">

		<t:collapsiblePanel id="screenerCherryPicksPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['screenerCherryPicks']}"
			title="Screener Cherry Picks" var="isCollapsed" titleVar="title">
			<f:facet name="header">
				<t:div styleClass="sectionHeader">
					<t:headerLink immediate="true" styleClass="sectionHeader">
						<t:graphicImage
							value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<t:outputText value="#{title}" styleClass="sectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:outputText value="Cherry picks have not yet been specified."
				styleClass="label"
				rendered="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks && !cherryPickRequestViewer.editable}" />

			<t:panelGrid id="addCherryPicksAndHelpPanels" columns="2"
				columnClasses="column"
				rendered="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks && cherryPickRequestViewer.editable}">
				<t:panelGrid id="addCherryPicksPanel" columns="1">
					<t:outputLabel for="cherryPicksInput"
						value="Specify cherry picks as plate/well pairs:"
						styleClass="label" />
					<t:inputTextarea id="cherryPicksInput" rows="30" cols="30"
						value="#{cherryPickRequestViewer.cherryPicksInput}"
						styleClass="inputText" />
					<t:commandButton id="addPoolCherryPicksCommand"
						value="Add Cherry Picks (Pool Wells)"
						action="#{cherryPickRequestViewer.addCherryPicksForPoolWells}"
						rendered="#{cherryPickRequestViewer.rnaiScreen}"
						styleClass="command"
						title="Add cherry picks, mapping from pool wells to duplex wells" />
					<t:commandButton id="addCherryPicksCommand"
						value="Add Cherry Picks"
						action="#{cherryPickRequestViewer.addCherryPicksForWells}"
						styleClass="command"
						title="Add cherry picks for the specified wells" />
				</t:panelGrid>
				<%@ include file="../help/libraries/wellFinderInputHelp.jsp"%>
			</t:panelGrid>

			<t:panelGrid id="viewScreenerCherryPicks" columns="1"
				rendered="#{!empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks}">

				<t:outputText
					value="WARNING: Cherry pick allowance has been exceeded! (#{cherryPickRequestViewer.cherryPickRequest.cherryPickAllowanceUsed} > #{cherryPickRequestViewer.cherryPickRequest.cherryPickAllowance})"
					rendered="#{!cherryPickRequestViewer.cherryPickRequest.mapped && cherryPickRequestViewer.cherryPickRequest.cherryPickAllowanceUsed > cherryPickRequestViewer.cherryPickRequest.cherryPickAllowance}"
					styleClass="errorMessage" />

				<t:panelGroup id="screenerCherryPicksCommandPanel"
					styleClass="commandPanel"
					rendered="#{cherryPickRequestViewer.editable}">
					<t:commandButton id="deleteCherryPicks" value="Delete All"
						action="#{cherryPickRequestViewer.deleteAllCherryPicks}"
						disabled="#{cherryPickRequestViewer.cherryPickRequest.allocated}"
						styleClass="command"
						title="Delete all the screener cherry picks, so you can enter them again" />
				</t:panelGroup>

				<%-- Render table into a buffer, allowing dataScrollers to be positioned above table. See http://wiki.apache.org/myfaces/Buffer. --%>
				<t:buffer into="#{screenerCherryPicksTableRenderBuffer}">
					<t:dataTable id="screenerCherryPicksTable" var="cherryPickRow"
						binding="#{cherryPickRequestViewer.screenerCherryPicksDataTable.dataTableUIComponent}"
						value="#{cherryPickRequestViewer.screenerCherryPicksDataTable.dataModel}"
						styleClass="standardTable" columnClasses="column"
						rows="#{cherryPickRequestViewer.screenerCherryPicksDataTable.rowsPerPageSelector.selection}"
						rowClasses="row1,row2" headerClass="tableHeader"
						sortColumn="#{cherryPickRequestViewer.screenerCherryPicksDataTable.sortManager.sortColumnName}"
						sortAscending="#{cherryPickRequestViewer.screenerCherryPicksDataTable.sortManager.sortAscending}">
						<t:columns
							value="#{cherryPickRequestViewer.screenerCherryPicksDataTable.sortManager.columnModel}"
							var="column" styleClass="column">
							<f:facet name="header">
								<%-- immediate="false" needed to allow UISelectMany components to be updated when sort is changed via clicking on table header --%>
								<t:commandSortHeader columnName="#{column.name}" arrow="false"
									immediate="false">
									<f:facet name="ascending">
										<t:graphicImage value="/images/ascending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<f:facet name="descending">
										<t:graphicImage value="/images/descending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<t:outputText value="#{column.name}" />
								</t:commandSortHeader>
							</f:facet>
							<t:outputText
								value="#{cherryPickRequestViewer.screenerCherryPicksDataTable.cellValue}" />
						</t:columns>
					</t:dataTable>
				</t:buffer>

				<t:aliasBean alias="#{dataTable}"
					value="#{cherryPickRequestViewer.screenerCherryPicksDataTable}">
					<%@ include file="../dataTableNavigator.jspf"%>
				</t:aliasBean>

				<t:div>
					<t:outputText value="#{screenerCherryPicksTableRenderBuffer}"
						escape="false" />
				</t:div>

			</t:panelGrid>

		</t:collapsiblePanel>

	</h:form>

	<h:form id="labCherryPicksPanelForm">

		<t:collapsiblePanel id="labCherryPicksPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['labCherryPicks']}"
			title="Lab Cherry Picks" var="isCollapsed" titleVar="title">
			<f:facet name="header">
				<t:div styleClass="sectionHeader">
					<t:headerLink immediate="true" styleClass="sectionHeader">
						<t:graphicImage
							value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<t:outputText value="#{title}" styleClass="sectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:panelGrid columns="1">

				<t:outputText value="Cherry picks have not yet been specified."
					styleClass="label"
					rendered="#{empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}" />

				<t:panelGroup id="labCherryPicksCommandPanel"
					styleClass="commandPanel"
					rendered="#{cherryPickRequestViewer.editable && !empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}">
					<t:commandButton id="viewCherryPickRequestWellVolumes"
						value="View Well Volumes"
						action="#{cherryPickRequestViewer.viewCherryPickRequestWellVolumes}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks}"
						styleClass="command"
						title="View the  available #{cherryPickRequestViewer.liquidTerm} volumes for the cherry picks on the cherry pick copy plates" />
					<t:commandButton
						id="viewCherryPickRequestWellVolumesForUnfulfilled"
						value="View Unfulfilled Well Volumes"
						action="#{cherryPickRequestViewer.viewCherryPickRequestWellVolumesForUnfulfilled}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks}"
						styleClass="command"
						title="View the  available #{cherryPickRequestViewer.liquidTerm} volumes for the unfulfilled cherry picks on the cherry pick copy plates" />
					<t:commandButton id="allocateCherryPicks"
						value="Reserve #{cherryPickRequestViewer.liquidTerm}"
						action="#{cherryPickRequestViewer.allocateCherryPicks}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks || cherryPickRequestViewer.cherryPickRequest.allocated}"
						styleClass="command"
						title="Reserve #{cherryPickRequestViewer.liquidTerm} for the cherry picks from the cherry pick copy plates" />
					<t:commandButton id="deallocateCherryPicks"
						value="Cancel Reservation"
						action="#{cherryPickRequestViewer.deallocateCherryPicks}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command"
						title="Cancel the #{cherryPickRequestViewer.liquidTerm} reservations from the cherry pick copy plates" />
					<t:commandButton id="plateMapCherryPicks" value="Map to Plates"
						action="#{cherryPickRequestViewer.plateMapCherryPicks}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command"
						title="Choose plate number and destination well for the cherry picks" />
					<t:commandButton id="createCherryPickRequestForUnfulfilled"
						value="New Cherry Pick Request for Unfulfilled"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.numberUnfulfilledLabCherryPicks == 0}"
						action="#{cherryPickRequestViewer.createNewCherryPickRequestForUnfulfilledCherryPicks}"
						styleClass="command"
						title="Create a new cherry pick request consisting of the unfulfilled cherry picks" />
					<t:outputLabel for="showFailedLabCherryPicks" value="Show failed:"
						styleClass="label" title="Show or hide lab cherry picks on failed assay plate attempts" />
					<t:selectBooleanCheckbox id="showFailedLabCherryPicks"
						value="#{cherryPickRequestViewer.showFailedLabCherryPicks}"
						valueChangeListener="#{cherryPickRequestViewer.toggleShowFailedLabCherryPicks}"
						onchange="javascript:document.getElementById('toggleShowFailedLabCherryPicksCommand').click()"
						immediate="true" styleClass="command"
						title="Show or hide failed lab cherry picks" />
					<t:commandButton id="toggleShowFailedLabCherryPicksCommand"
						immediate="true" forceId="true" style="display:none" />

				</t:panelGroup>

				<%-- Render table into a buffer, allowing dataScrollers to be positioned above table. See http://wiki.apache.org/myfaces/Buffer. --%>
				<t:buffer into="#{labCherryPicksTableRenderBuffer}">
					<t:dataTable id="labCherryPicksTable"
						binding="#{cherryPickRequestViewer.labCherryPicksDataTable.dataTableUIComponent}"
						var="row"
						value="#{cherryPickRequestViewer.labCherryPicksDataTable.dataModel}"
						styleClass="standardTable" columnClasses="column"
						rows="#{cherryPickRequestViewer.labCherryPicksDataTable.rowsPerPageSelector.selection}"
						rowClasses="row1,row2" headerClass="tableHeader"
						sortColumn="#{cherryPickRequestViewer.labCherryPicksDataTable.sortManager.sortColumnName}"
						sortAscending="#{cherryPickRequestViewer.labCherryPicksDataTable.sortManager.sortAscending}"
						rendered="#{!empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}">
						<t:columns
							value="#{cherryPickRequestViewer.labCherryPicksDataTable.sortManager.columnModel}"
							var="column" styleClass="column">
							<f:facet name="header">
								<%-- immediate="false" needed to allow UISelectMany components to be updated when sort is changed via clicking on table header --%>
								<t:commandSortHeader columnName="#{column.name}" arrow="false"
									immediate="false">
									<f:facet name="ascending">
										<t:graphicImage value="/images/ascending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<f:facet name="descending">
										<t:graphicImage value="/images/descending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<t:outputText value="#{column.name}" />
								</t:commandSortHeader>
							</f:facet>
							<t:outputText
								value="#{cherryPickRequestViewer.labCherryPicksDataTable.cellValue}" />
						</t:columns>
					</t:dataTable>
				</t:buffer>

				<t:aliasBean alias="#{dataTable}"
					value="#{cherryPickRequestViewer.labCherryPicksDataTable}">
					<%@ include file="../dataTableNavigator.jspf"%>
				</t:aliasBean>

				<t:div>
					<t:outputText value="#{labCherryPicksTableRenderBuffer}"
						escape="false" />
				</t:div>

			</t:panelGrid>

		</t:collapsiblePanel>

		<t:collapsiblePanel id="cherryPickPlatesPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['cherryPickPlates']}"
			title="Cherry Pick Plates" var="isCollapsed" titleVar="title">
			<f:facet name="header">
				<t:div styleClass="sectionHeader">
					<t:headerLink immediate="true" styleClass="sectionHeader">
						<t:graphicImage
							value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<t:outputText value="#{title}" styleClass="sectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:panelGrid columns="1">
				<t:outputText value="<none>" styleClass="label"
					rendered="#{!cherryPickRequestViewer.cherryPickRequest.mapped}" />

				<t:panelGroup styleClass="commandPanel"
					rendered="#{cherryPickRequestViewer.editable && cherryPickRequestViewer.cherryPickRequest.mapped}">
					<t:commandButton id="downloadPlateMappingFiles"
						value="Download Files for Selected Plates"
						action="#{cherryPickRequestViewer.downloadPlateMappingFilesForSelectedAssayPlates}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command"
						title="Download the input files for the liquid transfer equipment" />
					<t:outputLabel for="showFailedAssayPlates"
						value="Show all failed plate attempts:" styleClass="label"
						title="Show or hide the assay plate attempts that failed" />
					<t:selectBooleanCheckbox id="showFailedAssayPlates"
						value="#{cherryPickRequestViewer.showFailedAssayPlates}"
						valueChangeListener="#{cherryPickRequestViewer.toggleShowFailedAssayPlates}"
						onchange="javascript:document.getElementById('toggleShowFailedAssayPlatesCommand').click()"
						immediate="true" styleClass="command"
						title="Show or hide the failed assay plates" />
					<t:commandButton id="toggleShowFailedAssayPlatesCommand"
						forceId="true" immediate="true" style="display:none" />
				</t:panelGroup>

				<t:outputText
					value="WARNING: Some cherry pick plates will be created from the same source plate!  Be aware that you will need to reload one or more source plates."
					rendered="#{cherryPickRequestViewer.cherryPickRequest.sourcePlateReloadRequired}"
					styleClass="errorMessage" />

				<t:message for="assayPlatesTable" styleClass="errorMessage" />
				<t:dataTable id="assayPlatesTable" forceId="true"
					var="assayPlateRow"
					value="#{cherryPickRequestViewer.assayPlatesDataModel}"
					styleClass="standardTable" columnClasses="column"
					rowClasses="row1,row2" headerClass="tableHeader"
					rendered="#{cherryPickRequestViewer.cherryPickRequest.mapped}">
					<t:column>
						<f:facet name="header">
							<t:selectBooleanCheckbox id="selectAll"
								value="#{cherryPickRequestViewer.selectAllAssayPlates}"
								onclick="document.getElementById('selectAllAssayPlatesButton').click()" />
						</f:facet>
						<t:selectBooleanCheckbox value="#{assayPlateRow.selected}" />
					</t:column>
					<t:columns
						value="#{cherryPickRequestViewer.assayPlatesColumnModel}"
						var="columnName" styleClass="column">
						<f:facet name="header">
							<t:outputText value="#{columnName}" />
						</f:facet>
						<t:outputText
							value="#{assayPlateRow.columnName2Value[columnName]}" />
					</t:columns>
				</t:dataTable>

				<t:panelGrid id="createCherryPickLiquidTransferCommandPanel"
					columns="1" styleClass="commandPanel groupingPanel"
					rendered="#{cherryPickRequestViewer.editable && cherryPickRequestViewer.cherryPickRequest.mapped}">

					<t:panelGroup>
						<t:outputLabel for="liquidTransferPerformedBy"
							value="Performed by:" styleClass="label"
							title="The screening lab staff member who performed the transfer" />
						<t:selectOneMenu id="liquidTransferPerformedBy"
							value="#{cherryPickRequestViewer.liquidTransferPerformedBy.value}"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText">
							<f:selectItems
								value="#{cherryPickRequestViewer.liquidTransferPerformedBy.selectItems}" />
						</t:selectOneMenu>
						<t:outputLabel for="dateOfLiquidTransfer" value="Date:"
							styleClass="label"
							title="The date the #{cherryPickRequestViewer.liquidTerm} transfer took place" />
						<t:inputDate id="dateOfLiquidTransfer"
							value="#{cherryPickRequestViewer.dateOfLiquidTransfer}"
							popupCalendar="true"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText" />
						<t:outputLabel for="liquidTransferComments" value="Comments:"
							styleClass="label"
							title="Screening room staff comments for the #{cherryPickRequestViewer.liquidTerm} transfer" />
						<t:inputText id="liquidTransferComments"
							value="#{cherryPickRequestViewer.liquidTransferComments}"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText" />
					</t:panelGroup>

					<t:panelGroup>
						<t:commandButton id="recordLiquidTransfer"
							value="Record Selected Plates as 'Plated'"
							action="#{cherryPickRequestViewer.recordLiquidTransferForSelectedAssayPlates}"
							disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
							styleClass="command"
							title="Record successful #{cherryPickRequestViewer.liquidTerm} transfer" />
						<t:commandButton id="recordFailureOfAssayPlates"
							value="Record Selected Plates as 'Failed'"
							disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
							action="#{cherryPickRequestViewer.recordFailureOfAssayPlates}"
							styleClass="command"
							title="Record unsuccessful #{cherryPickRequestViewer.liquidTerm} transfer" />
						<t:commandButton id="cancelAssayPlates"
							value="Cancel Selected Plates"
							disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
							action="#{cherryPickRequestViewer.deallocateCherryPicksByPlate}"
							styleClass="command"
							title="Deallocate cherry picks for selected plates" />
					</t:panelGroup>

				</t:panelGrid>

				<t:commandButton id="selectAllAssayPlatesButton" forceId="true"
					action="#{cherryPickRequestViewer.selectAllAssayPlates}"
					rendered="#{cherryPickRequestViewer.cherryPickRequest.mapped}"
					style="display:none" />

			</t:panelGrid>
		</t:collapsiblePanel>

	</h:form>

</f:subview>