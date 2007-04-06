
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
			value="#{cherryPickRequestViewer.collapsiblePanelsState['screenSummary']}"
			title="Screen Summary" var="state" titleVar="title">
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
		</t:collapsiblePanel>

		<t:collapsiblePanel id="cherryPickRequestPanel"
			value="#{cherryPickRequestViewer.collapsiblePanelsState['cherryPickRequest']}"
			title="Cherry Pick Request" var="state" titleVar="title">
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

			<t:panelGrid columns="1">
				<t:panelGroup id="cherryPickRequstCommandPanel"
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
						popupCalendar="true"
						rendered="#{cherryPickRequestViewer.editMode}"
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

					<t:outputText value="Screener&nbsp;cherry&nbsp;picks"
						escape="false" />
					<t:outputText id="screenCherryPickCount"
						value="#{cherryPickRequestViewer.screenerCherryPickCount}"
						styleClass="dataText" />

					<t:outputText value="Lab&nbsp;cherry&nbsp;picks" escape="false" />
					<t:outputText id="labCherryPickCount"
						value="#{cherryPickRequestViewer.labCherryPickCount}"
						styleClass="dataText" />

					<t:outputText value="Cherry&nbsp;Pick&nbsp;Plates" escape="false" />
					<t:outputText id="assayPlatesCount"
						value="#{cherryPickRequestViewer.assayPlatesDataModel.rowCount}"
						styleClass="dataText" />



				</t:panelGrid>
			</t:panelGrid>
		</t:collapsiblePanel>

		<t:collapsiblePanel id="screenerCherryPicksPanel"
			value="#{cherryPickRequestViewer.collapsiblePanelsState['screenerCherryPicks']}"
			title="Screener Cherry Picks" var="state" titleVar="title">
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

			<t:outputText value="Cherry picks have not yet been specified."
				styleClass="label"
				rendered="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks && !cherryPickRequestViewer.editable}" />

			<t:panelGrid id="addCherryPicks" columns="1"
				rendered="#{empty cherryPickRequestViewer.cherryPickRequest.screenerCherryPicks && cherryPickRequestViewer.editable}">
				<t:outputLabel for="cherryPicksInput"
					value="Specify cherry picks as plate/well pairs:"
					styleClass="label" />
				<t:inputTextarea id="cherryPicksInput" rows="20" cols="30"
					value="#{cherryPickRequestViewer.cherryPicksInput}"
					styleClass="inputText" />
				<t:commandButton id="addPoolCherryPicksCommand"
					value="Add Cherry Picks (Pool Wells)"
					action="#{cherryPickRequestViewer.addPoolCherryPicks}"
					rendered="#{cherryPickRequestViewer.rnaiScreen}"
					styleClass="command" />
				<t:commandButton id="addCherryPicksCommand"
					value="Add Cherry Picks"
					action="#{cherryPickRequestViewer.addCherryPicks}"
					styleClass="command" />
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
						styleClass="command" />
				</t:panelGroup>

				<t:panelGrid columns="2">
					<t:dataScroller id="screenerCherryPicksDataScroller"
						for="screenerCherryPicksTable" firstRowIndexVar="fromRow"
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
						for="screenerCherryPicksTable" firstRowIndexVar="fromRow"
						lastRowIndexVar="toRow" rowsCountVar="rowCount"
						pageCountVar="pages">
						<t:outputText value="#{fromRow}..#{toRow} of #{rowCount}"
							styleClass="label" rendered="#{pages > 1}" />
					</t:dataScroller>
				</t:panelGrid>

				<t:dataTable id="screenerCherryPicksTable" var="cherryPickRow"
					value="#{cherryPickRequestViewer.screenerCherryPicksDataModel}"
					styleClass="standardTable" columnClasses="column" rows="20"
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

			</t:panelGrid>

		</t:collapsiblePanel>

		<t:collapsiblePanel id="labCherryPicksPanel"
			value="#{cherryPickRequestViewer.collapsiblePanelsState['labCherryPicks']}"
			title="Lab Cherry Picks" var="state" titleVar="title">
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

			<t:panelGrid columns="1">

				<t:outputText value="Cherry picks have not yet been specified."
					styleClass="label"
					rendered="#{empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}" />

				<t:panelGroup id="labCherryPicksCommandPanel"
					styleClass="commandPanel"
					rendered="#{cherryPickRequestViewer.editable && !empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}">
					<t:commandButton id="allocateCherryPicks"
						value="Reserve #{cherryPickRequestViewer.liquidTerm}"
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
					<t:commandButton id="createCherryPickRequestForUnfulfilled"
						value="New Cherry Pick Request for Unfulfilled"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated}"
						action="#{cherryPickRequestViewer.createNewCherryPickRequestForUnfulfilledCherryPicks}"
						styleClass="command" />
					<t:outputLabel for="showFailedLabCherryPicks" value="Show failed:"
						styleClass="label" />
					<t:selectBooleanCheckbox id="showFailedLabCherryPicks"
						value="#{cherryPickRequestViewer.showFailedLabCherryPicks}"
						onchange="javascript:document.getElementById('updateShowFailedCommand').click()"
						styleClass="command" />
				</t:panelGroup>

				<t:panelGrid columns="2">
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
						<t:outputText value="#{fromRow}..#{toRow} of #{rowCount}"
							styleClass="label" rendered="#{pages > 1}" />
					</t:dataScroller>
				</t:panelGrid>

				<t:dataTable id="labCherryPicksTable" var="cherryPickRow"
					value="#{cherryPickRequestViewer.labCherryPicksDataModel}"
					styleClass="standardTable" columnClasses="column" rows="20"
					rowClasses="row1,row2" headerClass="tableHeader"
					sortColumn="#{cherryPickRequestViewer.labCherryPicksSortManager.currentSortColumnName}"
					sortAscending="#{cherryPickRequestViewer.labCherryPicksSortManager.sortAscending}"
					rendered="#{!empty cherryPickRequestViewer.cherryPickRequest.labCherryPicks}">
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
								<t:outputText value="#{columnName}" />
							</t:commandSortHeader>
						</f:facet>
						<t:outputText value="#{cherryPickRow[columnName]}" />
					</t:columns>
				</t:dataTable>

			</t:panelGrid>

		</t:collapsiblePanel>

		<t:collapsiblePanel id="cherryPickPlatesPanel"
			value="#{cherryPickRequestViewer.collapsiblePanelsState['cherryPickPlates']}"
			title="Cherry Pick Plates" var="state" titleVar="title">
			<f:facet name="header">
				<t:div styleClass="sectionHeader">
					<t:headerLink immediate="true" styleClass="sectionHeader">
						<h:graphicImage
							value="#{state ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<t:outputText value="#{title}" styleClass="sectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:panelGrid columns="1">
				<t:outputText value="<none>" styleClass="label"
					rendered="#{!cherryPickRequestViewer.cherryPickRequest.mapped}" />

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

				<t:panelGrid id="selectedAssayPlatesCommandPanel" columns="1"
					styleClass="commandPanel"
					rendered="#{cherryPickRequestViewer.editable && cherryPickRequestViewer.cherryPickRequest.mapped}">

					<t:panelGroup>
						<t:outputLabel for="showFailedAssayPlates"
							value="Show all failed plates:" styleClass="label" />
						<t:selectBooleanCheckbox id="showFailedAssayPlates"
							value="#{cherryPickRequestViewer.showFailedAssayPlates}"
							onchange="javascript:document.getElementById('updateShowFailedCommand').click()"
							styleClass="command" />
					</t:panelGroup>

					<t:commandButton id="downloadPlateMappingFiles"
						value="Download Files for Selected Plates"
						action="#{cherryPickRequestViewer.downloadPlateMappingFilesForSelectedAssayPlates}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command" />

					<t:panelGrid columns="2">
						<t:outputLabel for="liquidTransferPerformedBy"
							value="Performed by:" styleClass="label" />
						<t:selectOneMenu id="liquidTransferPerformedBy"
							value="#{cherryPickRequestViewer.liquidTransferPerformedBy.value}"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText">
							<f:selectItems
								value="#{cherryPickRequestViewer.liquidTransferPerformedBy.selectItems}" />
						</t:selectOneMenu>
						<t:outputLabel for="dateOfLiquidTransfer" value="Date:"
							styleClass="label" />
						<t:inputDate id="dateOfLiquidTransfer"
							value="#{cherryPickRequestViewer.dateOfLiquidTransfer}"
							popupCalendar="true"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText" />
						<t:outputLabel for="comments" value="Comments:" styleClass="label" />
						<t:inputText id="comments"
							value="#{cherryPickRequestViewer.liquidTransferComments}"
							rendered="#{cherryPickRequestViewer.editable}"
							styleClass="inputText" />
					</t:panelGrid>

					<t:panelGroup>
						<t:commandButton id="recordLiquidTransfer"
							value="Record Selected Plates as Created"
							action="#{cherryPickRequestViewer.recordLiquidTransferForSelectedAssayPlates}"
							disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
							styleClass="command" />
						<t:commandButton id="recordFailureOfAssayPlates"
							value="Record Selected Plates as Failed"
							disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
							action="#{cherryPickRequestViewer.recordFailureOfAssayPlates}"
							styleClass="command" />
					</t:panelGroup>
				</t:panelGrid>

				<t:commandButton id="selectAllAssayPlatesButton" forceId="true"
					action="#{cherryPickRequestViewer.selectAllAssayPlates}"
					rendered="#{cherryPickRequestViewer.cherryPickRequest.mapped}"
					style="display:none" />
				<t:commandButton id="updateShowFailedCommand" forceId="true"
					action="#{cherryPickRequestViewer.updateShowFailed}"
					style="display:none" />

			</t:panelGrid>
		</t:collapsiblePanel>

	</h:form>

</f:subview>
