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
 - paged, sortable cherry pick table
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

		<t:panelGrid id="cherryPickRequestInfoTable" columns="2"
			styleClass="standardTable" rowClasses="row1,row2"
			columnClasses="keyColumn,column">

			<t:panelGroup id="commandPanel"
				rendered="#{cherryPickRequestViewer.editable}"
				styleClass="commandPanel">
				<t:commandButton id="editCommand" value="Edit"
					action="#{cherryPickRequestViewer.setEditMode}"
					styleClass="command"
					rendered="#{!cherryPickRequestViewer.editMode && !cherryPickRequestViewer.cherryPickRequest.allocated}" />
				<t:commandButton id="saveCommand" value="Save"
					action="#{cherryPickRequestViewer.save}" styleClass="command"
					rendered="#{cherryPickRequestViewer.editMode}" />
				<h:commandButton id="cancelEditCommand" value="Cancel"
					rendered="#{cherryPickRequestViewer.editMode}"
					action="#{cherryPickRequestViewer.cancelEdit}" immediate="true"
					styleClass="command" />
			</t:panelGroup>
			<t:htmlTag value="br" />

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
				displayValueOnly="#{!cherryPickRequestViewer.editMode}" size="5"
				styleClass="inputText" displayValueOnlyStyleClass="dataText" />

			<t:outputText value="Approved&nbsp;Volume&nbsp;(&#181;L)"
				escape="false" />
			<t:inputText id="approvedVolume"
				value="#{cherryPickRequestViewer.cherryPickRequest.microliterTransferVolumePerWellApproved}"
				displayValueOnly="#{!cherryPickRequestViewer.editMode}" size="5"
				styleClass="inputText" displayValueOnlyStyleClass="dataText" />

			<t:outputText value="Assay&nbsp;Plate&nbsp;Type" escape="false" />
			<t:outputText 
				value="#{cherryPickRequestViewer.cherryPickRequest.assayPlateType}"
				styleClass="dataText"/>

			<t:outputText
				value="Randomize&nbsp;assay&nbsp;plate well&nbsp;layout"
				escape="false" />
			<t:selectBooleanCheckbox
				value="#{cherryPickRequestViewer.cherryPickRequest.randomizedAssayPlateLayout}"
				displayValueOnly="#{!cherryPickRequestViewer.editMode}"
				styleClass="command" displayValueOnlyStyleClass="dataText" />

			<t:outputText
				value="Empty&nbsp;columns&nbsp;on&nbsp;assay&nbsp;plate"
				escape="false" />
			<t:outputText
				value="#{cherryPickRequestViewer.emptyColumnsOnAssayPlateAsString}"
				rendered="#{!cherryPickRequestViewer.editMode}" styleClass="dataText"/>
			<t:selectManyListbox
				value="#{cherryPickRequestViewer.emptyColumnsOnAssayPlate.value}"
				size="24" rendered="#{cherryPickRequestViewer.editMode}"
				styleClass="input">
				<f:selectItems
					value="#{cherryPickRequestViewer.emptyColumnsOnAssayPlate.selectItems}" />
			</t:selectManyListbox>

		</t:panelGrid>

		<t:div styleClass="sectionHeader">
			<t:outputText value="Cherry Picks" styleClass="sectionHeader" />
		</t:div>

		<t:panelStack id="cherryPicksPanelStack"
			selectedPanel="#{empty cherryPickRequestViewer.cherryPickRequest.cherryPicks ? (cherryPickRequestViewer.editable ? \"addCherryPicks\" : \"noCherryPicks\") : \"viewCherryPicks\"}">

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
				<t:commandButton id="addCherryPicksCommand" value="Add Cherry Picks"
					action="#{cherryPickRequestViewer.addCherryPicks}"
					styleClass="command" />
			</t:panelGrid>

			<t:panelGrid id="viewCherryPicks" columns="1">

				<t:panelGroup id="cherryPicksCommandPanel"
					rendered="#{cherryPickRequestViewer.editable}">
					<t:commandButton id="deleteCherryPicks" value="Delete All"
						action="#{cherryPickRequestViewer.deleteAllCherryPicks}"
						disabled="#{cherryPickRequestViewer.cherryPickRequest.allocated}"
						styleClass="command" />
					<t:commandButton id="allocateCherryPicks" value="Reserve Liquid"
						action="#{cherryPickRequestViewer.allocateCherryPicks}"
						disabled="#{empty cherryPickRequestViewer.cherryPickRequest.cherryPicks || cherryPickRequestViewer.cherryPickRequest.allocated}"
						styleClass="command" />
					<t:commandButton id="deallocateCherryPicks"
						value="Cancel Reservation"
						action="#{cherryPickRequestViewer.deallocateCherryPicks}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command" />
					<t:commandButton id="plateMapCherryPicks" value="Map to Assay Plates"
						action="#{cherryPickRequestViewer.plateMapCherryPicks}"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated || cherryPickRequestViewer.cherryPickRequest.mapped}"
						styleClass="command" />
					<h:commandButton id="createCherryPickRequestForUnfulfillable" value="New Cherry Pick Request for Unfulfilled"
						disabled="#{!cherryPickRequestViewer.cherryPickRequest.allocated}"
						action="#{cherryPickRequestViewer.createCherryPickRequestForUnfulfilledCherryPicks}"
						styleClass="command" />
				</t:panelGroup>

				<t:dataTable id="cherryPicksTable" var="cherryPickRow"
					value="#{cherryPickRequestViewer.cherryPicksDataModel}"
					styleClass="standardTable" columnClasses="column"
					rowClasses="row1,row2" headerClass="tableHeader">
					<t:columns
						value="#{cherryPickRequestViewer.cherryPicksColumnModel}"
						var="columnName" styleClass="column">
						<f:facet name="header">
							<t:outputText value="#{columnName}" />
						</f:facet>
						<t:outputText value="#{cherryPickRow[columnName]}" />
					</t:columns>
				</t:dataTable>

			</t:panelGrid>

		</t:panelStack>


		<t:div styleClass="sectionHeader">
			<t:outputText value="Assay Plates" styleClass="sectionHeader" />
		</t:div>

		<t:outputText value="<none>" styleClass="label"
			rendered="#{!cherryPickRequestViewer.cherryPickRequest.mapped}" />

		<t:panelGroup id="selectedAssayPlatesCommandPanel"
			rendered="#{cherryPickRequestViewer.editable && cherryPickRequestViewer.cherryPickRequest.mapped}">
			<t:div>
				<t:outputText styleClass="label" value="For selected assay plates:"/>
			</t:div>
			<t:commandButton id="downloadPlateMappingFiles"
				value="Download Files"
				action="#{cherryPickRequestViewer.downloadPlateMappingFilesForSelectedAssayPlates}"
				disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
				styleClass="command" />
			<t:commandButton id="recordLiquidTransfer"
				value="Record Liquid Transfer"
				action="#{cherryPickRequestViewer.recordLiquidTransferForSelectedAssayPlates}"
				disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
				styleClass="command" />
			<h:commandButton id="duplicateCherryPickRequestForAssayPlates" value="Duplicate"
				disabled="#{!cherryPickRequestViewer.cherryPickRequest.mapped}"
				action="#{cherryPickRequestViewer.createDuplicateCherryPickRequestForSelectedAssayPlates}"
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
					<t:selectBooleanCheckbox id="selectAll" value=""
						onclick="document.getElementById('selectAllAssayPlatesButton').click()" />
				</f:facet>
				<t:selectBooleanCheckbox value="#{assayPlateRow.selected}" />
			</t:column>
		</t:dataTable>

		<t:commandButton id="selectAllAssayPlatesButton" forceId="true"
			action="#{cherryPickRequestViewer.selectAllAssayPlates}"
			rendered="#{cherryPickRequestViewer.cherryPickRequest.mapped}"
			style="display:none" />

		<t:div styleClass="sectionHeader">
			<t:outputText value="Liquid Transfers" styleClass="sectionHeader" />
		</t:div>

		<t:dataTable id="liquidTransferTable" var="liquidTransferRow"
			value="#{cherryPickRequestViewer.liquidTransferDataModel}"
			styleClass="standardTable" columnClasses="column"
			rowClasses="row1,row2" headerClass="tableHeader"
			rendered="#{!empty cherryPickRequestViewer.cherryPickRequest.cherryPickLiquidTransfers}">
			<t:columns
				value="#{cherryPickRequestViewer.liquidTransferColumnModel}"
				var="columnName" styleClass="column">
				<f:facet name="header">
					<t:outputText value="#{columnName}" />
				</f:facet>
				<t:outputText value="#{liquidTransferRow[columnName]}" />
			</t:columns>
		</t:dataTable>
		<t:outputText value="<none>" styleClass="label"
			rendered="#{empty cherryPickRequestViewer.cherryPickRequest.cherryPickLiquidTransfers}" />

	</h:form>

</f:subview>
