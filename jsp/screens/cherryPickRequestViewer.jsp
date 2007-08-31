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

		<%-- following screen summary should be shared with screenAndResultViewer.jspf --%>
		<t:collapsiblePanel id="screenSummaryPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['screenSummary']}"
			title="Screen Summary" var="isCollapsed" titleVar="title">
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

				<t:outputText value="Title" escape="false" title="The title of the screen" />
				<t:outputText id="title"
					value="#{cherryPickRequestViewer.cherryPickRequest.screen.title}"
					styleClass="dataText" />

				<t:outputText value="Screen&nbsp;Type" escape="false" title="'Small Molecule' or 'RNAi'" />
				<t:outputText id="screenType"
					value="#{cherryPickRequestViewer.cherryPickRequest.screen.screenType}"
					converter="ScreenTypeConverter" styleClass="dataText" />
			</t:panelGrid>
		</t:collapsiblePanel>

		<t:collapsiblePanel id="cherryPickRequestDetailsPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['cherryPickRequestDetails']}"
			title="Cherry Pick Request Details" var="isCollapsed" titleVar="title">
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

			<t:panelGrid columns="1">
				<t:panelGroup id="cherryPickRequestCommandPanel"
					rendered="#{cherryPickRequestViewer.editable}"
					styleClass="commandPanel">
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
					<h:commandButton id="cancelEditCommand" value="Cancel"
						rendered="#{cherryPickRequestViewer.editMode}"
						action="#{cherryPickRequestViewer.cancelEdit}" immediate="true"
						styleClass="command" title="Discard your changes and leave edit mode" />
					<t:commandButton id="downloadCherryPickRequestCommand"
						value="Download"
						action="#{cherryPickRequestViewer.downloadCherryPickRequest}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks}"
						styleClass="command" title="Download the cherry pick request to a file"/>
				</t:panelGroup>

				<t:panelGrid id="cherryPickRequestInfoTable" columns="2"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="keyColumn,column">

					<t:outputText value="Cherry Pick Request #" title="The cherry pick request number" />
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
						escape="false" title="The volume per well that the screener requested" />
					<t:inputText id="requestedVolume"
						value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellRequested}"
						displayValueOnly="#{!cherryPickRequestViewer.editMode || cherryPickRequestViewer.cherryPickRequest.allocated}"
						size="5" styleClass="inputText"
						displayValueOnlyStyleClass="dataText" />

					<t:outputText value="Approved&nbsp;Volume&nbsp;(&#181;L)"
						escape="false" title="The volume per well approved by the screening room" />
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
						escape="false" title="The plate type, e.g., 'Eppendorf', 'Genetix', etc." />
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

					<t:outputText value="Screener-requested&nbsp;empty columns&nbsp;on&nbsp;plate"
						escape="false" title="The columns the screener requested to leave empty" />
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

					<t:outputText value="Comments" escape="false" title="Comments made by screening room staff" />
					<t:inputTextarea id="cherryPickRequestComments"
						value="#{cherryPickRequestViewer.cherryPickRequest.comments}"
						rows="10" cols="80"
						displayValueOnly="#{!cherryPickRequestViewer.editMode}"
						styleClass="inputText" displayValueOnlyStyleClass="dataText" />

					<t:outputText value="Screener&nbsp;cherry&nbsp;picks"
						escape="false" title="The number of screener cherry picks" />
					<t:outputText id="screenCherryPickCount"
						value="#{cherryPickRequestViewer.screenerCherryPickCount}"
						styleClass="dataText" />

					<t:outputText value="Lab&nbsp;cherry&nbsp;picks" escape="false"
					  title="The number of lab cherry picks" />
					<t:outputText id="labCherryPickCount"
						value="#{cherryPickRequestViewer.labCherryPickCount}"
						styleClass="dataText" />

					<t:outputText value="Cherry&nbsp;Pick&nbsp;Plates" escape="false"
					  title="The number of cherry pick plates" />
					<t:outputText id="assayPlatesCount"
						value="#{cherryPickRequestViewer.assayPlatesDataModel.rowCount}"
						styleClass="dataText" />



				</t:panelGrid>
			</t:panelGrid>
		</t:collapsiblePanel>

		<t:collapsiblePanel id="screenerCherryPicksPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['screenerCherryPicks']}"
			title="Screener Cherry Picks" var="isCollapsed" titleVar="title">
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

			<t:outputText value="Cherry picks have not yet been specified."
				styleClass="label"
				rendered="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks && !cherryPickRequestViewer.editable}"
			/>

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
						value="#{cherryPickRequestViewer.screenerCherryPicksDataModel}"
						styleClass="standardTable" columnClasses="column"
						rows="#{cherryPickRequestViewer.screenerCherryPicksPerPage.selection}"
						rowClasses="row1,row2" headerClass="tableHeader"
						sortColumn="#{cherryPickRequestViewer.screenerCherryPicksSortManager.sortColumnName}"
						sortAscending="#{cherryPickRequestViewer.screenerCherryPicksSortManager.sortAscending}">
						<t:columns
							value="#{cherryPickRequestViewer.screenerCherryPicksSortManager.columnModel}"
							var="column" styleClass="column">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{column.name}" arrow="false">
									<f:facet name="ascending">
										<t:graphicImage value="/images/ascending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<f:facet name="descending">
										<t:graphicImage value="/images/descending-arrow.gif"
											rendered="true" border="0" />
									</f:facet>
									<h:outputText value="#{column.name}" />
								</t:commandSortHeader>
							</f:facet>
							<t:outputText value="#{cherryPickRequestViewer.screenerCherryPicksCellValue}" />
						</t:columns>
					</t:dataTable>
				</t:buffer>

				<t:panelGrid columns="4">
					<t:dataScroller id="screenerCherryPicksDataScroller"
						for="screenerCherryPicksTable" binding="#{cherryPickRequestViewer.screenerCherryPicksTableDataScroller1}"
						firstRowIndexVar="fromRow"
						lastRowIndexVar="toRow" rowsCountVar="rowCount" paginator="true"
						paginatorMaxPages="10" fastStep="10"
						renderFacetsIfSinglePage="false" styleClass="scroller"
						pageCountVar="pages"
						paginatorActiveColumnClass="scroller_activePage">
						<f:facet name="first">
							<t:graphicImage url="/images/arrow-first.png" border="0"
								title="First page" />
						</f:facet>
						<f:facet name="last">
							<t:graphicImage url="/images/arrow-last.png" border="0"
								title="Last page" />
						</f:facet>
						<f:facet name="previous">
							<t:graphicImage url="/images/arrow-previous.png" border="0"
								title="Previous page" />
						</f:facet>
						<f:facet name="next">
							<t:graphicImage url="/images/arrow-next.png" border="0"
								title="Next page" />
						</f:facet>
						<f:facet name="fastforward">
							<t:graphicImage url="/images/arrow-fastforward.png" border="0"
								title="Forward 10 pages" />
						</f:facet>
						<f:facet name="fastrewind">
							<t:graphicImage url="/images/arrow-fastrewind.png" border="0"
								title="Back 10 pages" />
						</f:facet>
					</t:dataScroller>
					<t:dataScroller id="screenerCherryPicksDataScroller2"
						for="screenerCherryPicksTable" binding="#{cherryPickRequestViewer.screenerCherryPicksTableDataScroller2}"
						firstRowIndexVar="fromRow"
						lastRowIndexVar="toRow" rowsCountVar="rowCount"
						pageCountVar="pages">
						<t:outputText value="#{fromRow}..#{toRow < 0 ? rowCount : toRow} of #{rowCount}"
							styleClass="label" />
					</t:dataScroller>

					<t:selectOneMenu id="screenerCherryPicksPerPage"
						value="#{cherryPickRequestViewer.screenerCherryPicksPerPage.value}"
						onchange="document.getElementById('updateScreenerCherryPicksPerPage').click();"
						styleClass="data"
						title="Number of screener cherry picks to display per page" >
						<f:selectItems
							value="#{cherryPickRequestViewer.screenerCherryPicksPerPage.selectItems}" />
					</t:selectOneMenu>
					<t:outputText value=" per page" styleClass="label" />
					<t:commandButton id="updateScreenerCherryPicksPerPage"
						forceId="true" value="update screener cherry picks per page"
						style="display: none" />

				</t:panelGrid>

				<t:div>
					<t:outputText value="#{screenerCherryPicksTableRenderBuffer}" escape="false" />
				</t:div>

			</t:panelGrid>

		</t:collapsiblePanel>

		<t:collapsiblePanel id="labCherryPicksPanel"
			value="#{cherryPickRequestViewer.isPanelCollapsedMap['labCherryPicks']}"
			title="Lab Cherry Picks" var="isCollapsed" titleVar="title">
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
					<t:commandButton id="viewCherryPickRequestWellVolumesForUnfulfilled"
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
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated}"
						action="#{cherryPickRequestViewer.createNewCherryPickRequestForUnfulfilledCherryPicks}"
						styleClass="command"
						title="Create a new cherry pick request consisting of unfulfilled cherry picks" />
					<t:outputLabel for="showFailedLabCherryPicks" value="Show failed:"
						styleClass="label"
						title="Show or hide failed lab cherry picks" />
					<t:selectBooleanCheckbox id="showFailedLabCherryPicks"
						value="#{cherryPickRequestViewer.showFailedLabCherryPicks}"
						valueChangeListener="#{cherryPickRequestViewer.toggleShowFailedLabCherryPicks}"
						onchange="javascript:document.getElementById('toggleShowFailedLabCherryPicksCommand').click()"
						immediate="true"
						styleClass="command"
						title="Show or hide failed lab cherry picks" />
					<t:commandButton id="toggleShowFailedLabCherryPicksCommand"
						immediate="true"
						forceId="true" style="display:none" />

				</t:panelGroup>

				<%-- Render table into a buffer, allowing dataScrollers to be positioned above table. See http://wiki.apache.org/myfaces/Buffer. --%>
				<t:buffer into="#{labCherryPicksTableRenderBuffer}">
					<t:dataTable id="labCherryPicksTable" var="row"
						value="#{cherryPickRequestViewer.labCherryPicksDataModel}"
						styleClass="standardTable" columnClasses="column"
						rows="#{cherryPickRequestViewer.labCherryPicksPerPage.selection}"
						rowClasses="row1,row2" headerClass="tableHeader"
						sortColumn="#{cherryPickRequestViewer.labCherryPicksSortManager.sortColumnName}"
						sortAscending="#{cherryPickRequestViewer.labCherryPicksSortManager.sortAscending}"
						rendered="#{!empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}">
						<t:columns
							value="#{cherryPickRequestViewer.labCherryPicksSortManager.columnModel}"
							var="column" styleClass="column">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{column.name}" arrow="false">
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
							<t:outputText value="#{cherryPickRequestViewer.labCherryPicksCellValue}" />
						</t:columns>
					</t:dataTable>
				</t:buffer>

				<t:panelGrid columns="4">
					<t:dataScroller id="labCherryPicksDataScroller"
						for="labCherryPicksTable" firstRowIndexVar="fromRow"
						lastRowIndexVar="toRow" rowsCountVar="rowCount" paginator="true"
						paginatorMaxPages="10" fastStep="10" pageCountVar="pages"
						renderFacetsIfSinglePage="false" styleClass="scroller"
						paginatorActiveColumnClass="scroller_activePage">
						<f:facet name="first">
							<t:graphicImage url="/images/arrow-first.png" border="0"
								title="First page" />
						</f:facet>
						<f:facet name="last">
							<t:graphicImage url="/images/arrow-last.png" border="0"
								title="Last page" />
						</f:facet>
						<f:facet name="previous">
							<t:graphicImage url="/images/arrow-previous.png" border="0"
								title="Previous page" />
						</f:facet>
						<f:facet name="next">
							<t:graphicImage url="/images/arrow-next.png" border="0"
								title="Next page" />
						</f:facet>
						<f:facet name="fastforward">
							<t:graphicImage url="/images/arrow-fastforward.png" border="0"
								title="Forward 10 pages" />
						</f:facet>
						<f:facet name="fastrewind">
							<t:graphicImage url="/images/arrow-fastrewind.png" border="0"
								title="Back 10 pages" />
						</f:facet>
					</t:dataScroller>
					<t:dataScroller id="labCherryPicksDataScroller2"
						for="labCherryPicksTable" firstRowIndexVar="fromRow"
						lastRowIndexVar="toRow" rowsCountVar="rowCount"
						pageCountVar="pages">
						<t:outputText value="#{fromRow}..#{toRow < 0 ? rowCount : toRow} of #{rowCount}"
							styleClass="label" />
					</t:dataScroller>

					<t:panelGroup
						rendered="#{!empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}">
						<t:selectOneMenu id="labCherryPicksPerPage"
							value="#{cherryPickRequestViewer.labCherryPicksPerPage.value}"
							onchange="document.getElementById('updateLabCherryPicksPerPage').click();"
							styleClass="data"
							title="Number of lab cherry picks to display per page" >
							<f:selectItems
								value="#{cherryPickRequestViewer.labCherryPicksPerPage.selectItems}" />
						</t:selectOneMenu>
						<t:outputText value=" per page" styleClass="label" />
						<t:commandButton id="updateLabCherryPicksPerPage" forceId="true"
							value="update lab cherry picks per page" style="display: none" />
					</t:panelGroup>

				</t:panelGrid>

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
						<h:graphicImage
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
						value="Show all failed plates:" styleClass="label"
						title="Show or hide the failed assay plates" />
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
							styleClass="label" title="The date the #{cherryPickRequestViewer.liquidTerm} transfer took place" />
						<t:inputDate id="dateOfLiquidTransfer"
							value="#{cherryPickRequestViewer.dateOfLiquidTransfer}"
							popupCalendar="true"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText" />
						<t:outputLabel for="liquidTransferComments" value="Comments:" styleClass="label"
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
