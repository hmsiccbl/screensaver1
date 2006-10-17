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
- make editable collaborators list follow the "add"/"delete" design we have for other sets
- hyperlink lab head and lead screener (in read-only mode)
- consider forcing a resort after a sort column value is edited
- fix/test import errors cause screenResultImporter to open, showing errors table
--%>

<f:subview id="screenViewer">

	<t:aliasBean alias="#{navigator}"
		value="#{searchResultsRegistry.searchResults}">
		<%@ include file="../searchResultsNavPanel.jspf"%>
	</t:aliasBean>

	<h:form id="screenForm">

		<t:panelGrid id="labelsAndDataColumns" columns="2" rowClasses="row1,row2">

			<t:outputLabel for="screenId" value="Screen ID"
				visibleOnUserRole="developer" styleClass="inputLabel" />
			<t:outputText id="screenId" value="#{screenViewer.screen.screenId}"
				styleClass="dataText" visibleOnUserRole="developer" />

			<t:outputLabel for="screenNumber" value="Screen Number"
				styleClass="inputLabel" />
			<t:inputText id="screenNumber"
				value="#{screenViewer.screen.screenNumber}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="title" value="Title" styleClass="inputLabel" />
			<t:inputText id="title" value="#{screenViewer.screen.title}"
				displayValueOnly="#{screenViewer.readOnly}" size="80"
				styleClass="input" displayValueOnlyStyleClass="dataText"/>

			<t:outputLabel for="dateCreatedEditable" value="Created"
				styleClass="inputLabel" />
			<t:inputDate id="dateCreatedEditable"
				value="#{screenViewer.screen.dateCreated}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateCreated"
				value="#{screenViewer.screen.dateCreated}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="dateOfApplicationEditable" value="Application Date"
				styleClass="inputLabel" />
			<t:inputDate id="dateOfApplicationEditable"
				value="#{screenViewer.screen.dateOfApplication}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateOfApplication"
				value="#{screenViewer.screen.dateOfApplication}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="dateDataMeetingScheduledEditable" value="Data Meeting Scheduled"
				styleClass="inputLabel" />
			<t:inputDate id="dateDataMeetingScheduledEditable"
				value="#{screenViewer.screen.dataMeetingScheduled}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateDataMeetingScheduled"
				value="#{screenViewer.screen.dataMeetingScheduled}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="dateDataMeetingCompletedEditable" value="Data Meeting Completed"
				styleClass="inputLabel" />
			<t:inputDate id="dateDataMeetingCompletedEditable"
				value="#{screenViewer.screen.dataMeetingComplete}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateDataMeetingCompleted"
				value="#{screenViewer.screen.dataMeetingComplete}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="screenType" value="Screen Type"
				styleClass="inputLabel" />
			<t:selectOneMenu id="screenType"
				value="#{screenViewer.screen.screenType}"
				converter="ScreenTypeConverter"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="input">
				<f:selectItems value="#{screenViewer.screenTypeSelectItems}" />
			</t:selectOneMenu>

			<%-- "lab name" == last name of "lead head", but former is required for UI, latter is for internal design --%>
			<t:outputLabel for="labName" value="Lab Name" styleClass="inputLabel" />
			<%-- TODO: Would like to use immediate="true", but seems to be causing problems 
			     with JSF components' attempts to save/restore local state. 
			     See https://wiki.med.harvard.edu/ICCBL/JsfImmedateAttributeAndHibernate --%>
			<t:selectOneMenu id="labName" value="#{screenViewer.screen.labHead}"
				converter="ScreeningRoomUserConverter"
				onchange="javascript:submit()" immediate="false"
				valueChangeListener="#{screenViewer.update}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText">
				<f:selectItems value="#{screenViewer.labNameSelectItems}" />
			</t:selectOneMenu>

			<t:outputLabel for="leadScreener" value="Lead Screener"
				styleClass="inputLabel" />
			<t:selectOneMenu id="leadScreener"
				value="#{screenViewer.screen.leadScreener}"
				converter="ScreeningRoomUserConverter"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText">
				<f:selectItems value="#{screenViewer.leadScreenerSelectItems}" />
			</t:selectOneMenu>

			<t:outputLabel for="collaborators"
				value="Collaborators" styleClass="inputLabel" />
			<t:panelGrid columns="2" styleClass="nonSpacingPanel">
				<t:selectManyListbox id="collaboratorsEditable"
					value="#{screenViewer.screen.collaboratorsList}"
					converter="ScreeningRoomUserConverter"
					rendered="#{!screenViewer.readOnly}" size="5" styleClass="input">
					<f:selectItems value="#{screenViewer.collaboratorSelectItems}" />
				</t:selectManyListbox>

				<t:dataTable var="collaborator"
					value="#{screenViewer.screen.collaborators}">
					<h:column>
						<t:commandLink action="#{screenViewer.viewCollaborator}"
							value="#{collaborator.lastName}, #{collaborator.firstName}"
							styleClass="dataText"/>
					</h:column>
				</t:dataTable>
			</t:panelGrid>

			<t:outputLabel for="publishableProtocol" value="Publishable Protocol"
				styleClass="inputLabel" />
			<t:inputTextarea id="publishableProtocol" rows="3" cols="80"
				value="#{screenViewer.screen.publishableProtocol}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="summary" value="Summary"
				styleClass="inputLabel" />
			<t:inputTextarea id="summary" rows="3" cols="80"
				value="#{screenViewer.screen.summary}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="comments" value="Comments"
				styleClass="inputLabel" />
			<t:inputTextarea id="comments" rows="3" cols="80"
				value="#{screenViewer.screen.comments}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="statusItems" value="Status Items"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.statusItems}" />
				<t:dataTable id="statusItems" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="statusItem"
					value="#{screenViewer.statusItemsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.statusItems}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Date" />
						</f:facet>
						<t:inputDate id="statusDateEditable"
							value="#{statusItem.statusDate}" popupCalendar="true"
							rendered="#{!screenViewer.readOnly}" styleClass="input" />
						<t:outputText id="statusDate" value="#{statusItem.statusDate}"
							rendered="#{screenViewer.readOnly}" styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Status" />
						</f:facet>
						<h:outputText value="#{statusItem.statusValue}" styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deleteStatusItem}" styleClass="command"
							rendered="#{!screenViewer.readOnly}" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:selectOneMenu value="#{screenViewer.newStatusValue}"
						rendered="#{!screenViewer.readOnly}" required="false"
						styleClass="input" converter="StatusValueConverter">
						<f:selectItems value="#{screenViewer.newStatusValueSelectItems}" />
					</t:selectOneMenu>
					<t:commandButton value="Add Status Item"
						action="#{screenViewer.addStatusItem}" immediate="false"
						disabled="#{empty screenViewer.newStatusValueSelectItems}"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="publications" value="Publications"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.publications}" />
				<t:dataTable id="publications" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="publication"
					value="#{screenViewer.publicationsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.publications}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Year" />
						</f:facet>
						<t:inputText value="#{publication.yearPublished}"
							displayValueOnly="#{screenViewer.readOnly}" maxlength="4"
							styleClass="input" displayValueOnlyStyleClass="dataText" />
						<%-- TODO: f:validator validatorId="YearValidator" />
						</t:inputText --%>

					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Pubmed ID" />
						</f:facet>
						<t:inputText value="#{publication.pubmedId}"
							rendered="#{!screenViewer.readOnly}" styleClass="dataText" />
						<h:outputLink
							value="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi"
							rendered="#{screenViewer.readOnly}">
							<f:param name="cmd" value="Search" />
							<f:param name="db" value="PubMed" />
							<f:param name="term" value="#{publication.pubmedId}[PMID]" />
							<t:outputText value="#{publication.pubmedId}" />
						</h:outputLink>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Title" />
						</f:facet>
						<t:inputText value="#{publication.title}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Authors" />
						</f:facet>
						<t:inputText value="#{publication.authors}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deletePublication}" styleClass="command"
							rendered="#{!screenViewer.readOnly}" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:commandButton value="Add Publication"
						action="#{screenViewer.addPublication}" immediate="false"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="lettersOfSupport" value="Letters of Support"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.lettersOfSupport}" />
				<t:dataTable id="lettersOfSupport" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="letterOfSupport"
					value="#{screenViewer.lettersOfSupportDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.lettersOfSupport}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Written" />
						</f:facet>
						<t:inputDate id="statusDateEditable"
							value="#{letterOfSupport.dateWritten}" popupCalendar="true"
							rendered="#{!screenViewer.readOnly}" styleClass="input" />
						<t:outputText id="statusDate" value="#{letterOfSupport.dateWritten}"
							rendered="#{screenViewer.readOnly}" styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Written By" />
						</f:facet>
						<t:inputText value="#{letterOfSupport.writtenBy}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deleteLetterOfSupport}"
							styleClass="command" rendered="#{!screenViewer.readOnly}" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:commandButton value="Add Letter of Support"
						action="#{screenViewer.addLetterOfSupport}" immediate="false"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="attachedFiles" value="Attached Files"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.attachedFiles}" />
				<t:dataTable id="attachedFiles" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="attachedFile"
					value="#{screenViewer.attachedFilesDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.attachedFiles}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="File Name" />
						</f:facet>
						<t:inputText value="#{attachedFile.filename}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Contents" />
						</f:facet>
						<t:inputText value="#{attachedFile.fileContents}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deleteAttachedFile}" styleClass="command"
							rendered="#{!screenViewer.readOnly}"/>
						<t:commandButton value="View..."
							action="#{screenViewer.viewAttachedFile}" styleClass="command"
							rendered="#{!screenViewer.readOnly}"/>
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:commandButton value="Add Attached File"
						action="#{screenViewer.addAttachedFile}" immediate="false"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="fundingSupports" value="Funding Supports"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.fundingSupports}" />
				<t:dataTable id="fundingSupports" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="fundingSupport"
					value="#{screenViewer.fundingSupportsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.fundingSupports}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Funding Support" />
						</f:facet>
						<h:outputText value="#{fundingSupport.value}"
							styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deleteFundingSupport}"
							styleClass="command" rendered="#{!screenViewer.readOnly}" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:selectOneMenu value="#{screenViewer.newFundingSupport}" required="false"
						rendered="#{!screenViewer.readOnly}" 
						styleClass="input"
						converter="FundingSupportConverter">
						<f:selectItems value="#{screenViewer.newFundingSupportSelectItems}" />
					</t:selectOneMenu>
					<t:commandButton value="Add Funding Support"
						action="#{screenViewer.addFundingSupport}" 
						disabled="#{empty screenViewer.newFundingSupportSelectItems}"
						immediate="false"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="assayReadoutTypes" value="Assay Readout Types"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.assayReadoutTypes}" />
				<t:dataTable id="assayReadoutTypes" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column"
					var="assayReadoutType"
					value="#{screenViewer.assayReadoutTypesDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.assayReadoutTypes}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Assay Readout Type" />
						</f:facet>
						<h:outputText value="#{assayReadoutType.value}"
							styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deleteAssayReadoutType}"
							styleClass="command" rendered="#{!screenViewer.readOnly}" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:selectOneMenu value="#{screenViewer.newAssayReadoutType}" required="false"
						rendered="#{!screenViewer.readOnly}" 
						styleClass="input"
						converter="AssayReadoutTypeConverter">
						<f:selectItems value="#{screenViewer.newAssayReadoutTypeSelectItems}" />
					</t:selectOneMenu>
					<t:commandButton value="Add Assay Readout Type"
						action="#{screenViewer.addAssayReadoutType}" immediate="false"
						disabled="#{empty screenViewer.newAssayReadoutTypeSelectItems}"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="AbaseTestsets" value="Abase Testsets"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.abaseTestsets}" />
				<t:dataTable id="AbaseTestsets" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="abaseTestset"
					value="#{screenViewer.abaseTestsetsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.abaseTestsets}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Date" />
						</f:facet>
						<t:inputDate id="abaseTestsetDateEditable"
							value="#{abaseTestset.testsetDate}" popupCalendar="true"
							rendered="#{!screenViewer.readOnly}" styleClass="input" />
						<t:outputText id="abaseTestsetDate" value="#{abaseTestset.testsetDate}"
							rendered="#{screenViewer.readOnly}" styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Name" />
						</f:facet>
						<t:inputText value="#{abaseTestset.testsetName}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Comments" />
						</f:facet>
						<t:inputText value="#{abaseTestset.comments}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton value="Delete"
							action="#{screenViewer.deleteAbaseTestset}" styleClass="command"
							rendered="#{!screenViewer.readOnly}" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:commandButton value="Add Abase Testset"
						action="#{screenViewer.addAbaseTestset}" immediate="false"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel for="visitItems" value="Visits"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.visits}" />
				<t:dataTable id="visitItems" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="visitItem"
					value="#{screenViewer.visitsDataModel}" preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.visits}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Visit Date" />
						</f:facet>
						<t:outputText id="visitDate" value="#{visitItem.visitDate}"
							styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Created" />
						</f:facet>
						<t:outputText id="createdDate" value="#{visitItem.dateCreated}"
							styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Type" />
						</f:facet>
						<t:outputText id="createdDate" value="#{visitItem.visitType}"
							styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Performed By" />
						</f:facet>
						<t:outputText id="visitItemFullName"
							value="#{visitItem.performedBy.fullName}"
							styleClass="dataText" />
						</t:selectOneListbox>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Action" />
						</f:facet>
						<t:commandButton id="viewVisit" action="#{screenViewer.viewVisit}"
							value="#{screenViewer.readOnly ? \"View...\" : \"View/Edit...\"}"
							styleClass="command" />
						<t:commandButton id="copyVisit" action="#{screenViewer.copyVisit}"
							rendered="#{!screenViewer.readOnly}" value="#{\"Copy...\"}"
							styleClass="command" alt="Copy to another screen" />
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:commandButton value="Add Cherry Pick Visit..."
						action="#{screenViewer.addCherryPickVisitItem}" immediate="false"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
					<t:commandButton value="Add Non-Cherry Pick Visit..."
						action="#{screenViewer.addNonCherryPickVisitItem}"
						immediate="false" styleClass="command"
						rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel value="Screen Results"
				styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:outputLabel value="<none>" styleClass="inputLabel"
					rendered="#{empty screenViewer.screen.screenResult}" />
				<t:outputLabel value="Date created: " styleClass="inputLabel"
					rendered="#{!empty screenViewer.screen.screenResult}" />
				<t:outputText
					value="#{screenViewer.screen.screenResult.dateCreated}"
					styleClass="data"
					rendered="#{!empty screenViewer.screen.screenResult}" />
				<t:commandButton
					value="#{screenViewer.readOnly ? \"View...\" : \"View/Edit/Load...\"}"
					action="#{screenViewer.viewScreenResult}" styleClass="command"
					rendered="#{!empty screenViewer.screen.screenResult || !screenViewer.readOnly}" />
			</t:panelGrid>
			
			<t:outputLabel value="Billing Information"
				styleClass="inputLabel" rendered="#{!screenViewer.readOnly}" />
			<t:commandButton id="viewBillingInformation" value="View/Edit..."
				action="#{screenViewer.viewBillingInformation}" styleClass="command"
				rendered="#{!screenViewer.readOnly}"
				enabledOnUserRole="billingAdmin" />

		</t:panelGrid>
		<t:panelGroup id="commandPanel">
			<t:commandButton id="save" value="Save" action="#{screenViewer.save}"
				styleClass="command" rendered="#{!screenViewer.readOnly}" />
		</t:panelGroup>
	</h:form>
</f:subview>


